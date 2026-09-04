// 把 models.js 裡的模型算成一張 PNG 拼圖。
//
// 為什麼不用瀏覽器：算圖是驗證流程的一環，而驗證不該依賴「螢幕上剛好開著一個視窗」。
// 這支腳本沒有任何相依套件（PNG 編碼用 node 內建的 zlib），可以在 CI 裡跑。
//
//   node tools/render.mjs scooter out.png
//
import { readFileSync, writeFileSync } from 'node:fs';
import { deflateSync } from 'node:zlib';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
const src = readFileSync(join(here, 'models.js'), 'utf8');
const { MODELS } = new Function(`${src}\nreturn { MODELS };`)();

const which = process.argv[2] || 'macaque';
const outPath = process.argv[3] || join(here, `${which}.png`);
const model = MODELS[which];
if (!model) {
  console.error(`沒有這個模型：${which}。可用：${Object.keys(MODELS).join(', ')}`);
  process.exit(1);
}

// ---------------------------------------------------------------- 幾何（與檢視器同一套）
const D2R = Math.PI / 180;
const IDENT = [1,0,0, 0,1,0, 0,0,1, 0,0,0];

function mul(a, b) {
  const o = new Array(12);
  for (let r = 0; r < 3; r++) for (let c = 0; c < 3; c++)
    o[r*3+c] = a[r*3]*b[c] + a[r*3+1]*b[3+c] + a[r*3+2]*b[6+c];
  for (let c = 0; c < 3; c++)
    o[9+c] = a[9]*b[c] + a[10]*b[3+c] + a[11]*b[6+c] + b[9+c];
  return o;
}
function trs(off, rot) {
  const [rx, ry, rz] = rot.map(d => d * D2R);
  const cx=Math.cos(rx), sx=Math.sin(rx), cy=Math.cos(ry), sy=Math.sin(ry),
        cz=Math.cos(rz), sz=Math.sin(rz);
  return mul(mul(mul(
    [cz,-sz,0, sz,cz,0, 0,0,1, 0,0,0],
    [cy,0,sy, 0,1,0, -sy,0,cy, 0,0,0]),
    [1,0,0, 0,cx,-sx, 0,sx,cx, 0,0,0]),
    [1,0,0, 0,1,0, 0,0,1, off[0],off[1],off[2]]);
}
const apply = (m, p) => [
  m[0]*p[0]+m[3]*p[1]+m[6]*p[2]+m[9],
  m[1]*p[0]+m[4]*p[1]+m[7]*p[2]+m[10],
  m[2]*p[0]+m[5]*p[1]+m[8]*p[2]+m[11],
];

const world = {};
function worldOf(part) {
  const [name, parent, , , off, rot] = part;
  if (world[name]) return world[name];
  const base = parent ? worldOf(model.parts.find(p => p[0] === parent)) : IDENT;
  return world[name] = mul(trs(off, rot), base);
}

const FACES = [
  [[0,1,3,2], [0,0,-1]], [[5,4,6,7], [0,0,1]],
  [[4,0,2,6], [-1,0,0]], [[1,5,7,3], [1,0,0]],
  [[4,5,1,0], [0,-1,0]], [[2,3,7,6], [0,1,0]],
];

function quads() {
  const out = [];
  for (const part of model.parts) {
    const [, , org, size, , , color] = part;
    const m = worldOf(part);
    const rot = [...m]; rot[9] = rot[10] = rot[11] = 0;
    const c = [];
    for (let i = 0; i < 8; i++) {
      c.push(apply(m, [
        org[0] + ((i & 1) ? size[0] : 0),
        org[1] + ((i & 4) ? size[1] : 0),
        org[2] + ((i & 2) ? size[2] : 0),
      ]));
    }
    for (const [idx, n] of FACES) out.push({ pts: idx.map(i => c[i]), n: apply(rot, n), color });
  }
  return out;
}

// ---------------------------------------------------------------- 光柵化
const CELL = 340, COLS = 4, ROWS = 3;
const W = CELL * COLS, H = CELL * ROWS;
const buf = new Uint8Array(W * H * 3);
const BG = [26, 29, 35], GRID = [42, 47, 55];
for (let i = 0; i < W * H; i++) { buf[i*3] = BG[0]; buf[i*3+1] = BG[1]; buf[i*3+2] = BG[2]; }

function px(x, y, rgb) {
  if (x < 0 || y < 0 || x >= W || y >= H) return;
  const i = (y * W + x) * 3;
  buf[i] = rgb[0]; buf[i+1] = rgb[1]; buf[i+2] = rgb[2];
}

/** 掃描線填一個凸多邊形。 */
function fillPoly(pts, rgb) {
  let minY = Infinity, maxY = -Infinity;
  for (const p of pts) { minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]); }
  const y0 = Math.max(0, Math.ceil(minY)), y1 = Math.min(H - 1, Math.floor(maxY));
  for (let y = y0; y <= y1; y++) {
    const xs = [];
    for (let i = 0; i < pts.length; i++) {
      const a = pts[i], b = pts[(i + 1) % pts.length];
      if ((a[1] <= y && b[1] > y) || (b[1] <= y && a[1] > y)) {
        xs.push(a[0] + (y - a[1]) / (b[1] - a[1]) * (b[0] - a[0]));
      }
    }
    if (xs.length < 2) continue;
    xs.sort((p, q) => p - q);
    for (let k = 0; k + 1 < xs.length; k += 2) {
      for (let x = Math.ceil(xs[k]); x <= Math.floor(xs[k+1]); x++) px(x, y, rgb);
    }
  }
}

const hex = h => [parseInt(h.slice(1,3),16), parseInt(h.slice(3,5),16), parseInt(h.slice(5,7),16)];
const shade = (rgb, k) => rgb.map(c => Math.min(255, Math.round(c * k)));

const VIEWS = [
  ['front 0',   0, 12], ['fr 45',  45, 12], ['right 90', 90, 12], ['br 135', 135, 12],
  ['back 180',180, 12], ['bl 225',225, 12], ['left 270',270, 12], ['fl 315', 315, 12],
  ['side flat', 90, 0], ['front flat', 0, 0], ['top', 82, 0], ['3/4 low', 40, -25],
];

VIEWS.forEach(([, yaw, pitch], idx) => {
  const ox = (idx % COLS) * CELL, oy = Math.floor(idx / COLS) * CELL;
  const label = VIEWS[idx][0];
  const isTop = label === 'top';
  const cy = Math.cos(yaw*D2R), sy = Math.sin(yaw*D2R);
  const cp = Math.cos((isTop ? 82 : pitch)*D2R), sp = Math.sin((isTop ? 82 : pitch)*D2R);
  const P = model.pivot, s = model.scale;

  const view = p => {
    const x = p[0]-P[0], y = p[1]-P[1], z = p[2]-P[2];
    const x2 = x*cy + z*sy, z2 = -x*sy + z*cy;
    return [x2, y*cp - z2*sp, y*sp + z2*cp];
  };

  // 地面線：判斷「有沒有浮空／有沒有陷進地裡」全靠它
  const gy = oy + CELL/2 + (24 - P[1]) * s;
  for (let x = ox + 8; x < ox + CELL - 8; x++) px(x, Math.round(gy), GRID);

  const list = quads().map(q => {
    const v = q.pts.map(view);
    return { v, depth: v.reduce((t,p)=>t+p[2],0)/4, n: view([q.n[0]+P[0], q.n[1]+P[1], q.n[2]+P[2]]), color: q.color };
  }).sort((a, b) => b.depth - a.depth);

  for (const q of list) {
    const lit = 0.72 + 0.45 * Math.max(0, -q.n[1] / (Math.hypot(...q.n) || 1));
    const rgb = shade(hex(q.color), lit);
    fillPoly(q.v.map(p => [ox + CELL/2 + p[0]*s, oy + CELL/2 + p[1]*s]), rgb);
  }
});

// ---------------------------------------------------------------- PNG 編碼
const CRC = (() => {
  const t = new Int32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c;
  }
  return t;
})();
function crc32(b) {
  let c = -1;
  for (const x of b) c = CRC[(c ^ x) & 0xFF] ^ (c >>> 8);
  return (c ^ -1) >>> 0;
}
function chunk(type, data) {
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(body));
  return Buffer.concat([len, body, crc]);
}
const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4);
ihdr[8] = 8; ihdr[9] = 2; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;

const raw = Buffer.alloc(H * (W * 3 + 1));
for (let y = 0; y < H; y++) {
  raw[y * (W * 3 + 1)] = 0;
  Buffer.from(buf.buffer, y * W * 3, W * 3).copy(raw, y * (W * 3 + 1) + 1);
}
writeFileSync(outPath, Buffer.concat([
  Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]),
  chunk('IHDR', ihdr),
  chunk('IDAT', deflateSync(raw, { level: 9 })),
  chunk('IEND', Buffer.alloc(0)),
]));

// ---------------------------------------------------------------- 尺寸摘要
let lo = [ Infinity, Infinity, Infinity], hi = [-Infinity,-Infinity,-Infinity];
for (const part of model.parts) {
  const [, , org, size] = part, m = worldOf(part);
  for (let i = 0; i < 8; i++) {
    const p = apply(m, [org[0]+((i&1)?size[0]:0), org[1]+((i&4)?size[1]:0), org[2]+((i&2)?size[2]:0)]);
    for (let k = 0; k < 3; k++) { lo[k] = Math.min(lo[k], p[k]); hi[k] = Math.max(hi[k], p[k]); }
  }
}
const f = v => v.toFixed(1);
console.log(`${model.title}  →  ${outPath}`);
console.log(`  高 ${f(hi[1]-lo[1])}px = ${((hi[1]-lo[1])/16).toFixed(2)} 格` +
            `  寬 ${f(hi[0]-lo[0])}px = ${((hi[0]-lo[0])/16).toFixed(2)} 格` +
            `  長 ${f(hi[2]-lo[2])}px = ${((hi[2]-lo[2])/16).toFixed(2)} 格`);
console.log(`  最低點 y=${f(hi[1])}（地面是 24）  最高點 y=${f(lo[1])}`);
