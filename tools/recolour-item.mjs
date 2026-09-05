// 把機車的物品圖示換色成另一個車款。
//
// 可行是因為那張 16×16 只用了色票裡的五個顏色（BODY / BODY_DK / TIRE / CHROME / LAMP），
// 所以換色是查表，不是重畫。槽位定義見 swatch-texture.mjs——那兩份調色盤必須一致。
//
//   node tools/recolour-item.mjs classic cygnus \
//     src/main/resources/assets/taiwan/textures/item/scooter.png \
//     src/main/resources/assets/taiwan/textures/item/cygnus.png
//
import { readFileSync, writeFileSync } from 'node:fs';
import { deflateSync, inflateSync } from 'node:zlib';

// 兩套調色盤的槽位數不一樣（通用款八格、勁戰十六格），所以不能用索引對，要用**角色**對。
// 用索引的話車殼會對到暗紅、暗部會對到車殼灰，整張圖的層次會反過來。
const MAP = {
  'classic->cygnus': {
    '#2f3237': '#d81f26',   // 車殼 → 紅
    '#212429': '#33363b',   // 暗部 → 車殼灰
    '#191b1e': '#1a1c1f',   // 座墊
    '#121417': '#141417',   // 輪胎
    '#b9c0c6': '#9aa0a6',   // 鍍鉻
    '#e9e4d8': '#d7dbe0',   // 燈
    '#e08a2c': '#f0821e',   // 方向燈
    '#b4302c': '#c0392b',   // 煞車燈
  },
};

const [from, to, inPath, outPath] = process.argv.slice(2);
const map = new Map(Object.entries(MAP[`${from}->${to}`] || {}));
if (!map.size || !inPath || !outPath) {
  console.error('用法：node tools/recolour-item.mjs <來源款> <目標款> <輸入png> <輸出png>');
  console.error('可用的對應：' + Object.keys(MAP).join(', '));
  process.exit(1);
}

// ---- PNG 解碼（只處理 8-bit RGBA，那是這個專案唯一會出現的格式）----
const png = readFileSync(inPath);
const W = png.readUInt32BE(16), H = png.readUInt32BE(20);
if (png[24] !== 8 || png[25] !== 6) throw new Error('只支援 8-bit RGBA');
let off = 8, idat = [];
while (off < png.length) {
  const len = png.readUInt32BE(off);
  if (png.toString('ascii', off + 4, off + 8) === 'IDAT') idat.push(png.subarray(off + 8, off + 8 + len));
  off += 12 + len;
}
const raw = inflateSync(Buffer.concat(idat));
const px = Buffer.alloc(W * H * 4);
for (let y = 0; y < H; y++) {
  const filter = raw[y * (W * 4 + 1)];
  for (let x = 0; x < W * 4; x++) {
    const cur = raw[y * (W * 4 + 1) + 1 + x];
    const a = x >= 4 ? px[y * W * 4 + x - 4] : 0;
    const b = y > 0 ? px[(y - 1) * W * 4 + x] : 0;
    const c = (x >= 4 && y > 0) ? px[(y - 1) * W * 4 + x - 4] : 0;
    let v;
    switch (filter) {
      case 0: v = cur; break;
      case 1: v = cur + a; break;
      case 2: v = cur + b; break;
      case 3: v = cur + ((a + b) >> 1); break;
      case 4: {
        const p = a + b - c, pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
        v = cur + (pa <= pb && pa <= pc ? a : pb <= pc ? b : c);
        break;
      }
      default: throw new Error('未知的 filter ' + filter);
    }
    px[y * W * 4 + x] = v & 255;
  }
}

// ---- 換色 ----
let hit = 0, miss = new Set();
for (let i = 0; i < W * H; i++) {
  if (!px[i * 4 + 3]) continue;
  const key = '#' + [0, 1, 2].map(j => px[i * 4 + j].toString(16).padStart(2, '0')).join('');
  const next = map.get(key);
  if (!next) { miss.add(key); continue; }
  px[i * 4] = parseInt(next.slice(1, 3), 16);
  px[i * 4 + 1] = parseInt(next.slice(3, 5), 16);
  px[i * 4 + 2] = parseInt(next.slice(5, 7), 16);
  hit++;
}

// ---- PNG 編碼 ----
const outRaw = Buffer.alloc(H * (W * 4 + 1));
for (let y = 0; y < H; y++) {
  outRaw[y * (W * 4 + 1)] = 0;
  px.copy(outRaw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4);
}
const table = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
const crc = (buf) => {
  let c = 0xffffffff;
  for (const b of buf) c = table[(c ^ b) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
};
const chunk = (type, data) => {
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const cc = Buffer.alloc(4); cc.writeUInt32BE(crc(body));
  return Buffer.concat([len, body, cc]);
};
const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4);
ihdr[8] = 8; ihdr[9] = 6;
writeFileSync(outPath, Buffer.concat([
  Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
  chunk('IHDR', ihdr),
  chunk('IDAT', deflateSync(outRaw, { level: 9 })),
  chunk('IEND', Buffer.alloc(0)),
]));
console.log(`${inPath} → ${outPath}：換掉 ${hit} 個像素`);
if (miss.size) console.log(`  不在調色盤裡、原樣保留：${[...miss].join(' ')}`);
