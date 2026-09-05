// 從 models.js 算出座位的附著點，不要用手算。
//
// 這組數字很容易錯，而且錯了只有進遊戲才看得出來——手算要同時處理三件事：
// 模型單位換算（÷16）、地面錨定的縮放、以及**模型 +Z 是車尾、實體 −Z 才是車尾**
// 這個反向。算繪器是 translate(0,1.5,0) → Ry(180−yaw) → Rz(180)，套進去之後
//
//     世界座標 = ( mx/16 , 1.5 − my/16 , −mz/16 )
//
// 所以往車尾的 +mz 會變成實體的 −z。號誌搞反，人就會坐到踏板前面去。
//
//   node tools/seat-point.mjs cygnus seat
//
import { readFileSync } from 'node:fs';

const [which, prefix = 'seat'] = process.argv.slice(2);
const src = readFileSync(new URL('./models.js', import.meta.url), 'utf8');
const { MODELS } = new Function(`${src}\nreturn { MODELS };`)();
const model = MODELS[which];
if (!model) { console.error(`models.js 裡沒有 ${which}`); process.exit(1); }

const seats = model.parts.filter(([n, parent]) => !parent && n.startsWith(prefix));
if (!seats.length) { console.error(`找不到名字以 ${prefix} 開頭的頂層零件`); process.exit(1); }

let zLo = Infinity, zHi = -Infinity, yTop = Infinity;
for (const [, , origin, size, pose] of seats) {
  const z0 = pose[2] + origin[2], z1 = z0 + size[2];
  zLo = Math.min(zLo, z0); zHi = Math.max(zHi, z1);
  yTop = Math.min(yTop, pose[1] + origin[1]);   // y 越小越高
}

const blocks = (v) => Math.round((v) * 1000) / 1000;
const seatY = blocks((24 - yTop) / 16);
console.log(`${model.title} 的「${prefix}」`);
console.log(`  模型 z ${zLo.toFixed(2)} ~ ${zHi.toFixed(2)}（+z 是車尾），上緣 y ${yTop.toFixed(2)}`);
console.log(`  座面高度 ${seatY} 格\n`);

// 騎士坐在前段的三分之一處、後座在後段的三分之一處——不是各自的正中央：
// 人是往前坐的，坐墊的後緣是靠背不是屁股的位置
const rider = zLo + (zHi - zLo) * 0.34;
const pillion = zLo + (zHi - zLo) * 0.78;
console.log(`  ScooterVariant 的參數（riderY, riderZ, pillionY, pillionZ）：`);
console.log(`    ${seatY}f, ${blocks(-rider / 16)}f, ${blocks(seatY + 0.05)}f, ${blocks(-pillion / 16)}f`);
