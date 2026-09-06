/**
 * Blockbench 的 mesh 模型 → 執行期讀得動的幾何資源。
 *
 * 為什麼需要這一步：原版的 ModelPart 只畫得了長方體，低多邊形的車殼進不去。
 * 26.2 的 SubmitNodeCollector 有 submitCustomGeometry，可以自己送頂點，所以
 * 把 bbmodel 先烘成「骨骼 → 四邊形陣列」，執行期只負責送出去。
 *
 * 用法：node tools/bbmesh.mjs <in.bbmodel> <out.json>
 */
import {readFileSync, writeFileSync} from 'node:fs';

const src = JSON.parse(readFileSync(process.argv[2], 'utf8'));
const TEX = src.textures[0].uv_width;          // UV 是貼圖像素空間，不是 project resolution

const rad = d => d * Math.PI / 180;
/**
 * 一層的變換。
 *
 * <p>**群組跟元件的 origin 意思不一樣**，這是這支程式踩過的坑：
 * 元件的頂點是「相對自己的 origin」存的，所以 origin 是位移，p' = R·v + P；
 * 群組的 origin 是**樞紐**，底下的東西已經在模型座標了，所以 p' = R·(p − P) + P，
 * 位移那一項是 P − R·P。把群組也當位移的話，巢狀群組會把 origin 一路相加——
 * 那台卡車的車寬會從 26 變成 44，而且旋轉為零時完全看不出來哪裡錯。
 */
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
/** 兩層變換合成。子層的座標是「相對自己的樞紐」，所以先轉再加自己的樞紐，最後套父層。 */
const chain = (parent, t) => {
  const m = new Array(9);
  for (let c = 0; c < 3; c++) for (let r = 0; r < 3; r++)
    m[c*3+r] = parent.m[r] * t.m[c*3] + parent.m[3+r] * t.m[c*3+1] + parent.m[6+r] * t.m[c*3+2];
  return {m, o: apply(parent, t.o)};
};

const groups = Object.fromEntries((src.groups || []).map(g => [g.uuid, g]));
const elements = Object.fromEntries(src.elements.map(e => [e.uuid, e]));
const bones = new Map();
const boneOf = (name, pivot) => {
  if (!bones.has(name)) bones.set(name, {name, pivot, quads: [], normals: []});
  return bones.get(name);
};

function walk(node, xf, bone) {
  if (typeof node === 'string') { emit(elements[node], xf, bone); return; }
  const g = groups[node.uuid] || node;
  const here = chain(xf, local(g.origin, g.rotation, true));
  // 輪子要能自己轉，所以自成一根骨骼，樞紐就是這個群組的世界位置
  const b = /^wheel_/.test(g.name || '') ? boneOf(g.name, apply(xf, g.origin)) : bone;
  for (const c of node.children || []) walk(c, here, b);
}

function emit(el, xf, bone) {
  if (!el || el.type !== 'mesh') return;
  const t = chain(xf, local(el.origin, el.rotation));
  for (const f of Object.values(el.faces || {})) {
    const keys = f.vertices;
    if (keys.length < 3) continue;
    // 三角形補成退化四邊形：算繪型別吃的是 QUADS，一次四個頂點
    const order = keys.length === 3 ? [0, 1, 2, 2] : [0, 1, 2, 3];
    const pts = order.map(i => {
      const p = apply(t, el.vertices[keys[i]]);
      // 換到 ModelPart 的慣例：Y 朝下、地面在 24。這跟另外兩台車共用同一組
      // 算繪器的翻正，不能只有這台是 Y 朝上
      return [p[0], 24 - p[1], p[2], f.uv[keys[i]][0] / TEX, f.uv[keys[i]][1] / TEX];
    });
    // 只翻 Y 等於換手性，繞向會反過來、面被剔除掉，所以要倒著寫
    pts.reverse();
    const [a, b2, c] = pts;
    const u = [b2[0]-a[0], b2[1]-a[1], b2[2]-a[2]];
    const v = [c[0]-a[0], c[1]-a[1], c[2]-a[2]];
    let n = [u[1]*v[2]-u[2]*v[1], u[2]*v[0]-u[0]*v[2], u[0]*v[1]-u[1]*v[0]];
    const len = Math.hypot(...n) || 1;
    n = n.map(x => x / len);
    const pivot = bone.pivot;
    for (const p of pts) bone.quads.push(
      +(p[0] - pivot[0]).toFixed(4),
      +(p[1] - (24 - pivot[1])).toFixed(4),
      +(p[2] - pivot[2]).toFixed(4),
      +p[3].toFixed(5), +p[4].toFixed(5));
    bone.normals.push(+n[0].toFixed(4), +n[1].toFixed(4), +n[2].toFixed(4));
  }
}

boneOf('body', [0, 0, 0]);
const ID = {m: [1,0,0, 0,1,0, 0,0,1], o: [0, 0, 0]};
for (const n of src.outliner) walk(n, ID, bones.get('body'));

const out = {
  bones: [...bones.values()]
    .filter(b => b.quads.length)
    .map(b => ({name: b.name, pivot: [b.pivot[0], 24 - b.pivot[1], b.pivot[2]],
                quads: b.quads, normals: b.normals})),
};
writeFileSync(process.argv[3], JSON.stringify(out));
const q = out.bones.reduce((a, b) => a + b.quads.length / 20, 0);
console.log(out.bones.map(b => `${b.name}:${b.quads.length/20}`).join(' '), '四邊形合計', q);
