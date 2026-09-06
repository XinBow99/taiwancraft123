/**
 * 網格車用的色票貼圖：8 條 16px 寬的直帶，每條由上而下有明暗漸層。
 *
 * ## 為什麼要漸層而不是純色
 *
 * Minecraft 的實體算繪只有「六個面各自一個固定亮度」，沒有 AO、沒有陰影。整台車
 * 鋪同一個純色的話，所有轉折都糊在一起——那是體素版最大的問題。放樣網格的法線
 * 本來就會變化，再讓 UV 的 v 對到**頂點高度**，車身就多一層上亮下暗的漸層，
 * 車肩、裙邊、輪拱的分界不必靠幾何也讀得出來。
 *
 * 用法：node tools/swatch-gradient.mjs <out.png>
 */
import {writeFileSync} from 'node:fs';
import {deflateSync} from 'node:zlib';

// 順序必須跟 tools/lambo.mjs 的 BAND 一致
const BANDS = [
  ['body',   [255, 214,  60], [198, 134,   8]],
  ['black',  [ 58,  61,  66], [ 17,  18,  21]],
  ['glass',  [ 74,  86, 102], [ 21,  26,  36]],
  ['white',  [255, 255, 255], [196, 204, 214]],
  ['red',    [240,  58,  52], [136,  20,  18]],
  ['tyre',   [ 58,  58,  62], [ 17,  17,  19]],
  ['dark',   [ 44,  46,  52], [ 12,  13,  16]],
  ['chrome', [176, 182, 190], [ 84,  89,  96]],
];

const W = 128, H = 128, BW = 16;
const px = Buffer.alloc(W * H * 4);

BANDS.forEach(([, top, bot], i) => {
  for (let y = 0; y < H; y++) {
    const t = y / (H - 1);
    // 平方根讓亮的那一半佔多一點——線性漸層在遊戲裡看起來會偏暗
    const k = Math.sqrt(t);
    const c = [0, 1, 2].map(j => Math.round(top[j] + (bot[j] - top[j]) * k));
    for (let x = i * BW; x < (i + 1) * BW; x++) {
      const o = (y * W + x) * 4;
      px[o] = c[0]; px[o + 1] = c[1]; px[o + 2] = c[2]; px[o + 3] = 255;
    }
  }
});

const raw = Buffer.alloc(H * (W * 4 + 1));
for (let y = 0; y < H; y++) px.copy(raw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4);

const T = [];
for (let n = 0; n < 256; n++) { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1; T[n] = c >>> 0; }
const chunk = (tag, data) => {
  const b = Buffer.alloc(8 + data.length + 4);
  b.writeUInt32BE(data.length, 0); b.write(tag, 4); data.copy(b, 8);
  let c = 0xFFFFFFFF;
  for (let i = 4; i < 8 + data.length; i++) c = T[(c ^ b[i]) & 255] ^ (c >>> 8);
  b.writeUInt32BE((c ^ 0xFFFFFFFF) >>> 0, 8 + data.length);
  return b;
};
const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
writeFileSync(process.argv[2], Buffer.concat([
  Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
  chunk('IHDR', ihdr), chunk('IDAT', deflateSync(raw)), chunk('IEND', Buffer.alloc(0)),
]));
console.log('wrote', process.argv[2], BANDS.map(b => b[0]).join(' '));
