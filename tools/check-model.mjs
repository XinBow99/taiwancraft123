// 對照 models.js 與 Java 的 createBodyLayer()。
//
// models.js 開頭寫著「每一筆都必須跟對應的 Java 一字不差，否則這個檢視器就沒有意義」——
// 這支腳本讓那句話變成可以驗證的，而不是靠人盯。
//
// 比的是「盒子」與「擺放」兩組數字的**多重集合**，不比名字也不比順序：
// 兩邊的命名慣例本來就不同（models.js 用 blink_l，Java 用 blink_left），
// 而順序只影響可讀性，不影響畫出來的東西。真正會出錯的是抄數字。
//
//   node tools/check-model.mjs cygnus src/client/java/.../CygnusModel.java
//
import { readFileSync } from 'node:fs';

const [which, javaPath] = process.argv.slice(2);
if (!which || !javaPath) {
  console.error('用法：node tools/check-model.mjs <模型名> <Java 檔路徑>');
  process.exit(1);
}

const src = readFileSync(new URL('./models.js', import.meta.url), 'utf8');
const { MODELS } = new Function(`${src}\nreturn { MODELS };`)();
const model = MODELS[which];
if (!model) { console.error(`models.js 裡沒有 ${which}`); process.exit(1); }

const n = (v) => Math.round(v * 10000) / 10000;
const key = (arr) => arr.map(n).join(',');

// ---- models.js 這一側 ----
const jsBoxes = [], jsPoses = [];
for (const [, , origin, size, pose, rot] of model.parts) {
  if (size[0] || size[1] || size[2]) jsBoxes.push(key([...origin, ...size]));
  jsPoses.push(key([...pose, ...(rot || [0, 0, 0])]));
}

// ---- Java 這一側 ----
const java = readFileSync(javaPath, 'utf8');
const num = String.raw`(-?[\d.]+)f?`;
const javaBoxes = [...java.matchAll(
  new RegExp(String.raw`\.addBox\(\s*${num},\s*${num},\s*${num},\s*${num},\s*${num},\s*${num}\s*\)`, 'g'),
)].map((m) => key(m.slice(1, 7).map(Number)));

// 旋轉的每一個分量都可能是「純 0f」或「N f * Mth.DEG_TO_RAD」，而且 DEG_TO_RAD 可能出現在
// 三個槽位的任何一個（車身繞 X 傾、後照鏡繞 Z 傾）。只認第一個槽位的話，後照鏡會被誤判成不符
const angle = String.raw`(-?[\d.]+)f?(?:\s*\*\s*Mth\.DEG_TO_RAD)?`;
const javaPoses = [];
for (const m of java.matchAll(
  new RegExp(String.raw`PartPose\.offsetAndRotation\(\s*${num},\s*${num},\s*${num},\s*` +
    String.raw`${angle},\s*${angle},\s*${angle}\s*\)`, 'g'),
)) {
  javaPoses.push(key(m.slice(1, 7).map(Number)));
}
for (const m of java.matchAll(new RegExp(String.raw`PartPose\.offset\(\s*${num},\s*${num},\s*${num}\s*\)`, 'g'))) {
  javaPoses.push(key([...m.slice(1, 4).map(Number), 0, 0, 0]));
}
for (const _ of java.matchAll(/PartPose\.ZERO/g)) javaPoses.push(key([0, 0, 0, 0, 0, 0]));

// ---- 比對 ----
function diff(label, a, b) {
  const count = (xs) => xs.reduce((m, x) => m.set(x, (m.get(x) || 0) + 1), new Map());
  const ca = count(a), cb = count(b);
  const onlyA = [], onlyB = [];
  for (const [k, v] of ca) if ((cb.get(k) || 0) < v) onlyA.push(`${k} ×${v - (cb.get(k) || 0)}`);
  for (const [k, v] of cb) if ((ca.get(k) || 0) < v) onlyB.push(`${k} ×${v - (ca.get(k) || 0)}`);
  console.log(`${label}：models.js ${a.length} 筆，Java ${b.length} 筆`);
  if (!onlyA.length && !onlyB.length) { console.log('  完全相符'); return true; }
  onlyA.forEach((x) => console.log(`  只在 models.js：${x}`));
  onlyB.forEach((x) => console.log(`  只在 Java    ：${x}`));
  return false;
}

const ok = [
  diff('盒子 (原點+尺寸)', jsBoxes, javaBoxes),
  diff('擺放 (位移+旋轉)', jsPoses, javaPoses),
].every(Boolean);
console.log(ok ? '\n一致。' : '\n不一致——算圖看到的不是遊戲裡會畫的東西。');
process.exit(ok ? 0 : 1);
