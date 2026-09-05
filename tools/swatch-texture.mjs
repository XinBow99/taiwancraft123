// 產生機車的「色票」貼圖：128×128，切成八塊 64×32 的純色。
//
// 為什麼是色票而不是逐件排 UV：這台車二十幾個零件，逐件排要一張排得滿滿的圖，
// 而且改一個尺寸就要重排。色票的規則是「盒子只要落在同一塊純色區域裡，取樣到的就是
// 那個顏色」，跟盒子多大無關——所以模型可以一直改，貼圖不用動。
//
// 槽位的順序必須跟 ScooterModel / CygnusModel 裡的 texOffs 常數一致：
//
//   (0,0)  BODY    (64,0)  DARK
//   (0,32) SEAT    (64,32) TIRE
//   (0,64) CHROME  (64,64) LAMP
//   (0,96) AMBER   (64,96) BRAKE
//
//   node tools/swatch-texture.mjs cygnus src/main/resources/assets/taiwan/textures/entity/cygnus.png
//
import { writeFileSync } from 'node:fs';
import { deflateSync } from 'node:zlib';

// 通用款用八格（64×32）；勁戰的紅黑配色需要的顏色比八個多，改用十六格（32×32）。
// 兩者都是 128×128，差別只在切法——所以模型端各自帶自己的 texOffs 常數就好。
const LAYOUTS = {
  classic: { cols: 2, sw: 64, sh: 32, size: 128 },
  cygnus:  { cols: 8, sw: 32, sh: 32, size: 256 },
  lanbao:  { cols: 2, sw: 64, sh: 32, size: 128 },
  mashala: { cols: 2, sw: 64, sh: 32, size: 128 },
  // 8+9 的六型共用一個十六格版型，差別只在每一格填什麼顏色
  eightnine_temple: { cols: 4, sw: 32, sh: 32, size: 128 },
  eightnine_rider: { cols: 4, sw: 32, sh: 32, size: 128 },
  eightnine_street: { cols: 4, sw: 32, sh: 32, size: 128 },
  eightnine_boss: { cols: 4, sw: 32, sh: 32, size: 128 },
  eightnine_white: { cols: 4, sw: 32, sh: 32, size: 128 },
  eightnine_night_market: { cols: 4, sw: 32, sh: 32, size: 128 },
};

const PALETTES = {
  // 兩款跑車。槽位順序：PAINT DARK GLASS LAMP ｜ RED TIRE RIM CHROME
  lanbao:  ['#f2c318', '#161719', '#232a31', '#f2f5f7', '#d2342c', '#141416', '#5f656b', '#b0b6bc'],
  mashala: ['#1f4fc8', '#15171a', '#28303a', '#f2f5f7', '#c93028', '#141416', '#6e747a', '#cfd4d8'],
  // BODY DARK SEAT TIRE / CHROME LAMP AMBER BRAKE
  classic: ['#2f3237', '#212429', '#191b1e', '#121417', '#b9c0c6', '#e9e4d8', '#e08a2c', '#b4302c'],
  // 勁戰四代（紅黑）。順序＝CygnusModel 裡三十二個槽位常數的順序。
  // 每個顏色都有明暗階：體素風的立體感是靠同一個顏色的三階明暗做出來的，
  // 只有一階的話所有面都一樣亮，整台看起來是平的
  cygnus: [
    '#d81f26', '#9a1219', '#f0353c', '#33363b', '#22252a', '#4d5157', '#6b7075', '#8b9197',
    '#1a1c1f', '#141417', '#6a2f8a', '#431a5c', '#9aa0a6', '#b8bec4', '#e8ecef', '#f0821e',
    '#c0392b', '#2a2d31', '#3f434a', '#575c63', '#a03038', '#7a0e14', '#ffffff', '#c8ccd0',
    '#101215', '#5a5f66', '#7d838a', '#262a2f', '#b0343c', '#ff9a3c', '#8f4bb0', '#2f3338',
  ],
};

// 8+9 六型。槽位順序：
//   SKIN SKIN_DK HAIR HAIR_LT ｜ EYE DARK RED GOLD
//   SHIRT SHIRT_DK PANTS PANTS_DK ｜ SHOE SHOE_DK WHITE89 ACCENT
//
// 六型的差別全部在這張表裡，模型只有一個。廟會那型的 SHIRT 直接填膚色——他是赤膊的，
// 上衣那幾個盒子就變成身體本身
const EIGHTNINE = {
  temple: ['#e0ac86','#c8916b','#1c1a19','#332f2c','#20140e','#1e2024','#b3271f','#d8ae3a',
           '#e0ac86','#c8916b','#f2f0ec','#d8d4cc','#f2f2f4','#c2c2c6','#f2f2f4','#8a1f18'],
  rider:  ['#e0ac86','#c8916b','#1c1a19','#332f2c','#20140e','#15171a','#b3271f','#8a8f96',
           '#232629','#16181b','#2b2f34','#1c1f23','#e8e8ea','#c2c2c6','#f2f2f4','#5a6068'],
  street: ['#e0ac86','#c8916b','#1c1a19','#332f2c','#20140e','#1e2024','#b3271f','#d8ae3a',
           '#26282c','#191b1f','#5a6d8c','#43536c','#e8e8ea','#c2c2c6','#f2f2f4','#3d4a5e'],
  boss:   ['#e0ac86','#c8916b','#1c1a19','#332f2c','#20140e','#15171a','#8a1f18','#e8c352',
           '#1a1c20','#101216','#1a1c20','#101216','#f2f2f4','#c2c2c6','#f2f2f4','#c9a33a'],
  white:  ['#e0ac86','#c8916b','#1c1a19','#332f2c','#20140e','#2a2c30','#b3271f','#d8ae3a',
           '#f4f4f2','#dedcd6','#f4f4f2','#dedcd6','#f2f2f4','#c2c2c6','#ffffff','#b8b2a4'],
  night_market: ['#e0ac86','#c8916b','#1c1a19','#332f2c','#20140e','#141619','#b3271f','#9aa0a6',
           '#1f2226','#131518','#24272b','#171a1d','#e8e8ea','#c2c2c6','#f2f2f4','#4a5058'],
};
for (const [name, palette] of Object.entries(EIGHTNINE)) PALETTES['eightnine_' + name] = palette;

const name = process.argv[2] || 'cygnus';
const out = process.argv[3];
const palette = PALETTES[name];
if (!palette || !out) {
  console.error(`用法：node tools/swatch-texture.mjs <${Object.keys(PALETTES).join('|')}> <輸出路徑>`);
  process.exit(1);
}

const { size = 128 } = LAYOUTS[name];
const W = size, H = size;
const { cols, sw: SW, sh: SH } = LAYOUTS[name];
const px = Buffer.alloc(W * H * 4);
palette.forEach((hex, i) => {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  const x0 = (i % cols) * SW, y0 = Math.floor(i / cols) * SH;
  for (let y = y0; y < y0 + SH; y++) {
    for (let x = x0; x < x0 + SW; x++) {
      const o = (y * W + x) * 4;
      px[o] = r; px[o + 1] = g; px[o + 2] = b; px[o + 3] = 255;
    }
  }
});

// PNG：每一列前面要加一個 filter byte（0 = None）
const raw = Buffer.alloc(H * (W * 4 + 1));
for (let y = 0; y < H; y++) {
  raw[y * (W * 4 + 1)] = 0;
  px.copy(raw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4);
}

const crcTable = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
const crc = (buf) => {
  let c = 0xffffffff;
  for (const b of buf) c = crcTable[(c ^ b) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
};
const chunk = (type, data) => {
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const c = Buffer.alloc(4); c.writeUInt32BE(crc(body));
  return Buffer.concat([len, body, c]);
};
const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4);
ihdr[8] = 8; ihdr[9] = 6; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;

writeFileSync(out, Buffer.concat([
  Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
  chunk('IHDR', ihdr),
  chunk('IDAT', deflateSync(raw, { level: 9 })),
  chunk('IEND', Buffer.alloc(0)),
]));
console.log(`${name} → ${out}  (${palette.join(' ')})`);
