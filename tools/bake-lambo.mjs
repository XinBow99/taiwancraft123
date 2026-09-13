/**
 * 把 models/new_lambo.bbmodel 烘焙成 MeshGeometry 的資源檔（幾何 ＋ 動作）。
 *
 * ## 為什麼需要這一步
 * new_lambo 有 1,653 顆方塊，其中 1,590 顆具有三軸旋轉角。
 * 原版 ModelPart 不支援每方塊單獨旋轉，且超過 1,500 個 ModelPart 會超出 JVM
 * 64KB 方法字節碼上限並嚴重降低遊戲幀率。
 * 透過烘焙為頂點幾何，在 26.2 的 submitCustomGeometry 下可在一次 Draw Call 內完成全車算繪。
 *
 * ## 骨骼是照動作挑的，不是照 outliner 照抄
 * 只有**時間軸上動得到的群組**才會變成骨骼（加上根）。其餘群組（chassis、front_fascia、
 * cockpit、rear_engine、aero）的方塊會被併進最近的那一根動得到的祖先。
 * 這件事很重要：每一根骨骼在算繪時都是一次矩陣複製與一段迴圈，把不會動的東西分成
 * 五根骨骼只是白付五次成本。
 *
 * 骨骼是**有階層的**：steering_left 轉的時候要連它底下的 wheel_left_front 一起轉，
 * suspension_body 上下震的時候剪刀門要跟著車身走。攤平成一層做不到這件事。
 *
 * ## 座標系
 * Blockbench 是 Y 朝上、地面在 0；ModelPart 是 Y 朝下、地面在 24。兩者差一個鏡射
 * M = diag(1, −1, 1)，所以旋轉的 x 與 z 要變號、y 不變（推導見 tools/bake-bargarjung.mjs
 * 的檔頭），位移的 y 要變號。輸出的長度單位是**格**（已經除以 16），
 * 因為 PoseStack 吃的是格，不是模型單位。
 *
 * 用法：node tools/bake-lambo.mjs
 */
import { readFileSync, writeFileSync, copyFileSync } from 'node:fs';

const SRC_MODEL = 'models/new_lambo.bbmodel';
const SRC_PNG = 'models/new_lambo.png';
const OUT_JSON = 'src/main/resources/assets/taiwan/models/entity/lanbao.json';
const OUT_PNG = 'src/main/resources/assets/taiwan/textures/entity/lanbao.png';

const src = JSON.parse(readFileSync(SRC_MODEL, 'utf8'));
const TEX = src.resolution.width || 2048;

// 縮放比例：真車 Aventador 長約 4.78m（遊戲中約 4.8 格）。
// BB 中總長為 178.4 單位，178.4 * 0.4305 = 76.8 單位 = 4.8 格。
const SCALE = 0.4305;
const rad = d => d * Math.PI / 180;
const round = (v, n = 5) => +v.toFixed(n);

function local(origin, rotation, pivotOnly) {
  const [rx, ry, rz] = (rotation || [0, 0, 0]).map(rad);
  const cx = Math.cos(rx), sx = Math.sin(rx), cy = Math.cos(ry), sy = Math.sin(ry),
        cz = Math.cos(rz), sz = Math.sin(rz);
  const m = [
    cy*cz,            cy*sz,           -sy,
    sx*sy*cz - cx*sz, sx*sy*sz + cx*cz, sx*cy,
    cx*sy*cz + sx*sz, cx*sy*sz - sx*cz, cx*cy,
  ];
  const o = origin || [0, 0, 0];
  if (!pivotOnly) return {m, o};
  return {m, o: [
    o[0] - (m[0]*o[0] + m[3]*o[1] + m[6]*o[2]),
    o[1] - (m[1]*o[0] + m[4]*o[1] + m[7]*o[2]),
    o[2] - (m[2]*o[0] + m[5]*o[1] + m[8]*o[2]),
  ]};
}

const apply = (t, p) => [
  t.m[0]*p[0] + t.m[3]*p[1] + t.m[6]*p[2] + t.o[0],
  t.m[1]*p[0] + t.m[4]*p[1] + t.m[7]*p[2] + t.o[1],
  t.m[2]*p[0] + t.m[5]*p[1] + t.m[8]*p[2] + t.o[2],
];

const chain = (parent, t) => {
  const m = new Array(9);
  for (let c = 0; c < 3; c++) for (let r = 0; r < 3; r++)
    m[c*3+r] = parent.m[r] * t.m[c*3] + parent.m[3+r] * t.m[c*3+1] + parent.m[6+r] * t.m[c*3+2];
  return {m, o: apply(parent, t.o)};
};

const groups = Object.fromEntries((src.groups || []).map(g => [g.uuid, g]));
const elements = Object.fromEntries((src.elements || []).map(e => [e.uuid, e]));

// ---- 哪些群組要變成骨骼 ---------------------------------------------------

const animations = (src.animations || []).map(a => ({
  name: a.name.replace(/^animation\.new_lambo\./, ''),
  length: a.length,
  loop: a.loop === 'loop',
  animators: Object.values(a.animators || {}).filter(an => (an.keyframes || []).length),
}));

const animated = new Set();
for (const a of animations) for (const an of a.animators) animated.add(an.name);

// ---- 走 outliner，把方塊分進骨骼 ------------------------------------------

const bones = new Map();
/** 骨骼在網格空間（模型單位、Y 朝下、地面在 24）的樞紐。 */
const meshPivot = origin => [origin[0] * SCALE, 24 - origin[1] * SCALE, origin[2] * SCALE];

function boneOf(name, parent, origin) {
  if (!bones.has(name)) {
    bones.set(name, {name, parent, pivot: meshPivot(origin), quads: [], normals: []});
  }
  return bones.get(name);
}

function emitCube(el, xf, bone) {
  const t = chain(xf, local(el.origin, el.rotation, true));
  const [x0, y0, z0] = el.from;
  const [x1, y1, z1] = el.to;

  const faceDefs = {
    north: {
      pts: [[x1, y1, z0], [x0, y1, z0], [x0, y0, z0], [x1, y0, z0]],
      uv: (u0, v0, u1, v1) => [[u0, v0], [u1, v0], [u1, v1], [u0, v1]]
    },
    south: {
      pts: [[x0, y1, z1], [x1, y1, z1], [x1, y0, z1], [x0, y0, z1]],
      uv: (u0, v0, u1, v1) => [[u0, v0], [u1, v0], [u1, v1], [u0, v1]]
    },
    east: {
      pts: [[x1, y1, z1], [x1, y1, z0], [x1, y0, z0], [x1, y0, z1]],
      uv: (u0, v0, u1, v1) => [[u0, v0], [u1, v0], [u1, v1], [u0, v1]]
    },
    west: {
      pts: [[x0, y1, z0], [x0, y1, z1], [x0, y0, z1], [x0, y0, z0]],
      uv: (u0, v0, u1, v1) => [[u0, v0], [u1, v0], [u1, v1], [u0, v1]]
    },
    up: {
      pts: [[x0, y1, z0], [x1, y1, z0], [x1, y1, z1], [x0, y1, z1]],
      uv: (u0, v0, u1, v1) => [[u1, v1], [u0, v1], [u0, v0], [u1, v0]]
    },
    down: {
      pts: [[x0, y0, z1], [x1, y0, z1], [x1, y0, z0], [x0, y0, z0]],
      uv: (u0, v0, u1, v1) => [[u1, v0], [u0, v0], [u0, v1], [u1, v1]]
    }
  };

  for (const [dir, def] of Object.entries(faceDefs)) {
    const f = el.faces?.[dir];
    if (!f || f.texture === null || f.texture === false) continue;
    const [u0, v0, u1, v1] = f.uv;
    const uvs = def.uv(u0, v0, u1, v1);

    const pts = def.pts.map((pt, i) => {
      const p = apply(t, pt);
      // 座標系換算：Minecraft 中 Y 朝下，地面在 24
      return [p[0] * SCALE, 24 - p[1] * SCALE, p[2] * SCALE, uvs[i][0] / TEX, uvs[i][1] / TEX];
    });

    pts.reverse(); // 繞向反轉（Y 翻轉）
    const [a, b2, c] = pts;
    const u = [b2[0]-a[0], b2[1]-a[1], b2[2]-a[2]];
    const v = [c[0]-a[0], c[1]-a[1], c[2]-a[2]];
    let n = [u[1]*v[2]-u[2]*v[1], u[2]*v[0]-u[0]*v[2], u[0]*v[1]-u[1]*v[0]];
    const len = Math.hypot(...n) || 1;
    n = n.map(x => x / len);

    // 頂點存成**相對於自己骨骼的樞紐**：算繪時先移到樞紐再轉，轉的才是自轉不是公轉
    const pivot = bone.pivot;
    for (const p of pts) {
      bone.quads.push(
        round((p[0] - pivot[0]) / 16),
        round((p[1] - pivot[1]) / 16),
        round((p[2] - pivot[2]) / 16),
        round(p[3]),
        round(p[4])
      );
    }
    bone.normals.push(round(n[0], 4), round(n[1], 4), round(n[2], 4));
  }
}

function walk(node, xf, bone) {
  if (typeof node === 'string') {
    const el = elements[node];
    if (el) emitCube(el, xf, bone);
    return;
  }
  const g = groups[node.uuid] || node;
  const here = chain(xf, local(g.origin, g.rotation, true));

  // 動得到的群組自成骨骼，其餘併進最近的那一根動得到的祖先
  const b = animated.has(g.name) ? boneOf(g.name, bone.name, g.origin) : bone;

  for (const c of node.children || []) walk(c, here, b);
}

const ID = {m: [1,0,0, 0,1,0, 0,0,1], o: [0, 0, 0]};
const rootGroup = groups[src.outliner[0].uuid];
const root = boneOf(rootGroup.name, null, rootGroup.origin);
for (const n of src.outliner) walk(n, ID, root);

// ---- 動作 -----------------------------------------------------------------

// 插入順序就是父在子之前：outliner 是深度優先走下來的，父一定先被建出來
const ORDER = [...bones.keys()];

const bakedAnimations = {};
for (const a of animations) {
  const out = {length: a.length, loop: a.loop, bones: {}};
  for (const an of a.animators) {
    if (!bones.has(an.name)) throw new Error(`動作 ${a.name} 動到不存在的骨骼 ${an.name}`);
    const channels = {};
    for (const frame of an.keyframes) {
      const p = frame.data_points[0] || {};
      const n = key => {
        const value = Number(p[key]);
        return Number.isFinite(value) ? value : 0;
      };
      let values;
      if (frame.channel === 'rotation') {
        // 鏡射：繞 X 與繞 Z 變號，繞 Y 不變。存弧度，算繪時不必再換
        values = [rad(-n('x')), rad(n('y')), rad(-n('z'))];
      } else if (frame.channel === 'position') {
        // 位移：y 變號，而且要跟著車一起縮小、換成格
        values = [n('x') * SCALE / 16, -n('y') * SCALE / 16, n('z') * SCALE / 16];
      } else {
        continue;   // scale 沒用到
      }
      const list = channels[frame.channel] || (channels[frame.channel] = []);
      list.push([frame.time, ...values.map(v => round(v, 6))]);
    }
    for (const list of Object.values(channels)) list.sort((x, y) => x[0] - y[0]);
    if (Object.keys(channels).length) out.bones[an.name] = channels;
  }
  bakedAnimations[a.name] = out;
}

// ---- 輸出 -----------------------------------------------------------------

const out = {
  bones: ORDER.map(name => bones.get(name)).map(b => {
    const parent = b.parent ? bones.get(b.parent) : null;
    // 樞紐存成**相對於父骨骼**的位移，算繪時一層一層疊上去
    const pivot = parent
      ? [b.pivot[0] - parent.pivot[0], b.pivot[1] - parent.pivot[1], b.pivot[2] - parent.pivot[2]]
      : b.pivot;
    return {
      name: b.name,
      parent: b.parent,
      pivot: pivot.map(v => round(v / 16)),
      quads: b.quads,
      normals: b.normals,
    };
  }),
  animations: bakedAnimations,
};

writeFileSync(OUT_JSON, JSON.stringify(out));
console.log(`成功輸出網格資源至 ${OUT_JSON}`);
for (const b of out.bones) {
  console.log(`  骨骼: ${b.name}（父: ${b.parent || '—'}，樞紐: [${b.pivot.map(x => x.toFixed(4))}]，面數: ${b.quads.length / 20}）`);
}
for (const [name, a] of Object.entries(out.animations)) {
  console.log(`  動作: ${name}（${a.length} 秒${a.loop ? '、循環' : ''}，骨骼: ${Object.keys(a.bones).join('、')}）`);
}

copyFileSync(SRC_PNG, OUT_PNG);
console.log(`成功同步貼圖至 ${OUT_PNG}`);
