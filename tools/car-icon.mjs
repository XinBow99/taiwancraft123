// 兩款跑車的 16×16 物品圖示。
//
// 機車的圖示是換色來的（recolour-item.mjs），汽車不行——側面輪廓完全不一樣。
// 所以這裡直接用像素圖排版：一台車在 16×16 只有大概 14×11 可用，形狀認得出來
// 靠的是三件事：頂線（楔形／斜背）、兩顆輪子的間距、車頭的燈。細節放不下也不用放。
//
//   node tools/car-icon.mjs
//
import { readFileSync, writeFileSync } from 'node:fs';
import { deflateSync } from 'node:zlib';

// 楔形超跑：車頭貼地、頂線一路往後升，右上角那三格是尾翼
const LANBAO = [
  '................',
  '................',
  '................',
  '...........PPP..',
  '.....GGGGGGPPP..',
  '...GGGGGGGGPPPP.',
  '..PPPPPPPPPPPPPP',
  '.LPPPPPPPPPPPPPP',
  '.LPPPPPPPPPPPPPR',
  '..DDDDDDDDDDDDD.',
  '..TTTT....TTTT..',
  '.TTMMTT..TTMMTT.',
  '.TTMMTT..TTMMTT.',
  '..TTTT....TTTT..',
  '................',
  '................',
];

// 斜背雙門：車頂之後一路滑下去，沒有直角的行李廂
const MASHALA = [
  '................',
  '................',
  '................',
  '.....PPPPP......',
  '....PGGGGPP.....',
  '..PPGGGGGGPPP...',
  '.PPPPPPPPPPPPPP.',
  'LPPPPPPPPPPPPPPR',
  'LPPPPPPPPPPPPPPR',
  '.DDDDDDDDDDDDDD.',
  '..TTTT....TTTT..',
  '.TTMMTT..TTMMTT.',
  '.TTMMTT..TTMMTT.',
  '..TTTT....TTTT..',
  '................',
  '................',
];

// 調色盤直接從 swatch-texture.mjs 讀出來。色碼要是在四個地方各寫一份（models.js、
// gen-model.mjs、swatch-texture.mjs、這裡），改一次顏色就會有一個地方忘記跟上——
// 實際上已經發生過：模型換成黃色之後，物品圖示還是舊的黃綠色。
// 槽位順序：PAINT DARK GLASS LAMP RED TIRE RIM CHROME
const PAL = Object.fromEntries(
  readFileSync(new URL('./swatch-texture.mjs', import.meta.url), 'utf8')
    .split(/\r?\n/)
    .map((line) => line.match(/^\s*(lanbao|mashala):\s*\[(.+)\],\s*$/))
    .filter(Boolean)
    .map((m) => [m[1], m[2].split(',').map((h) => h.trim().replace(/'/g, ''))]),
);
if (!PAL.lanbao || !PAL.mashala) throw new Error('讀不到 swatch-texture.mjs 的調色盤');

const CARS = {
  lanbao: { art: LANBAO, p: PAL.lanbao },
  mashala: { art: MASHALA, p: PAL.mashala },
};
const SLOT = { P: 0, D: 1, G: 2, L: 3, R: 4, T: 5, M: 6, C: 7 };

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
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const cc = Buffer.alloc(4);
  cc.writeUInt32BE(crc(body));
  return Buffer.concat([len, body, cc]);
};

for (const [name, { art, p }] of Object.entries(CARS)) {
  const W = 16;
  const H = 16;
  const raw = Buffer.alloc(H * (W * 4 + 1));
  for (let y = 0; y < H; y++) {
    if (art[y].length !== W) throw new Error(`${name} 第 ${y} 列長度是 ${art[y].length}`);
    raw[y * (W * 4 + 1)] = 0;
    for (let x = 0; x < W; x++) {
      const ch = art[y][x];
      if (ch === '.') continue;
      const o = y * (W * 4 + 1) + 1 + x * 4;
      const hex = p[SLOT[ch]];
      raw[o] = parseInt(hex.slice(1, 3), 16);
      raw[o + 1] = parseInt(hex.slice(3, 5), 16);
      raw[o + 2] = parseInt(hex.slice(5, 7), 16);
      raw[o + 3] = 255;
    }
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(W, 0);
  ihdr.writeUInt32BE(H, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  const out = `src/main/resources/assets/taiwan/textures/item/${name}.png`;
  writeFileSync(out, Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(raw, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ]));
  console.log(`${out}  (${p[0]})`);
}
