// 從 models.js 產生 Java 的 createBodyLayer() 內容。
//
// 為什麼要有這支：這個模型改一次外型就要把四十幾個零件手抄進 Java 一次，而手抄會出錯
// （第一次移植就把小數尺寸四捨五入掉了，check-model.mjs 抓到二十幾筆）。產生出來的東西
// 定義上就跟預覽一致，那個「一字不差」的不變式不再靠人維持。
//
// 顏色→貼圖槽位是查表的：預覽用色碼，Java 用 texOffs，兩邊是同一組八個槽位。
//
//   node tools/gen-model.mjs cygnus
//
import { readFileSync } from 'node:fs';

const which = process.argv[2] || 'cygnus';
const src = readFileSync(new URL('./models.js', import.meta.url), 'utf8');
const { MODELS } = new Function(`${src}\nreturn { MODELS };`)();
const model = MODELS[which];
if (!model) { console.error(`models.js 裡沒有 ${which}`); process.exit(1); }

// 色碼 → 槽位常數名。兩套調色盤（通用款 C、勁戰 W）的同一個槽位對到同一個常數
// 色碼 → 槽位常數名。兩套調色盤各自獨立（通用款八格、勁戰十六格），
// 所以色碼不能重複——重複的話這張表分不出該對到哪個常數
const SLOT = {
  // 通用速克達（八格）
  '#2f3237': 'BODY', '#212429': 'DARK', '#191b1e': 'SEAT', '#121417': 'TIRE',
  '#b9c0c6': 'CHROME', '#e9e4d8': 'LAMP', '#e08a2c': 'AMBER', '#b4302c': 'BRAKE',
  // 勁戰四代（三十二格，紅黑）
  '#d81f26': 'RED', '#9a1219': 'RED_DK', '#f0353c': 'RED_LT', '#33363b': 'BODY',
  '#22252a': 'BODY_DK', '#4d5157': 'BODY_LT', '#6b7075': 'GREY', '#8b9197': 'GREY_LT',
  '#1a1c1f': 'SEAT', '#141417': 'TIRE', '#6a2f8a': 'RIM', '#431a5c': 'RIM_DK',
  '#9aa0a6': 'CHROME', '#b8bec4': 'SILVER', '#e8ecef': 'WHITE', '#f0821e': 'AMBER',
  '#c0392b': 'BRAKE', '#2a2d31': 'SHADOW', '#3f434a': 'SLATE', '#575c63': 'STEEL',
  '#a03038': 'RED_MID', '#7a0e14': 'RED_DEEP', '#ffffff': 'PURE', '#c8ccd0': 'ASH',
  '#101215': 'INK', '#5a5f66': 'GUN', '#7d838a': 'PEWTER', '#262a2f': 'COAL',
  '#b0343c': 'ROSE', '#ff9a3c': 'AMBER_LT', '#8f4bb0': 'ORCHID', '#2f3338': 'GRAPHITE',
};

// models.js 的簡寫 → Java 的完整零件名（給 setupAnim 抓得到）
const RENAME = {
  wheel_f: 'wheel_front', wheel_r: 'wheel_rear',
  hub_f: 'hub_front', hub_r: 'hub_rear', brake_f: 'brake_front',
};
const suffix = (n) => n.replace(/_l$/, '_left').replace(/_r$/, '_right');
const javaName = (n) => RENAME[n] ?? suffix(n);

// 收到小數第四位：折線是算出來的，不收的話會冒出 11.000000000000002f 這種東西
const f = (v) => {
  const r = Math.round(v * 10000) / 10000;
  return Number.isInteger(r) ? `${r}.0f` : `${r}f`;
};
const varName = (n) => javaName(n).replace(/_([a-z])/g, (_, c) => c.toUpperCase());

// 哪些零件有小孩 → 需要接成變數
const hasChild = new Set(model.parts.map(([, parent]) => parent).filter(Boolean));

const out = [];
out.push('        MeshDefinition mesh = new MeshDefinition();');
out.push('        PartDefinition root = mesh.getRoot();');
out.push('');

for (const [name, parent, origin, size, pose, rot, colour] of model.parts) {
  const jn = javaName(name);
  const parentVar = parent ? varName(parent) : 'root';
  const decl = hasChild.has(name) ? `PartDefinition ${varName(name)} = ` : '';
  const slot = SLOT[colour];
  if (!slot) throw new Error(`${name} 的顏色 ${colour} 不在槽位表裡`);

  const cubes = (size[0] || size[1] || size[2])
    ? `CubeListBuilder.create().texOffs(${slot}, ${slot}_V)\n`
      + `                        .addBox(${[...origin, ...size].map(f).join(', ')})`
    : 'CubeListBuilder.create()';

  const [rx, ry, rz] = rot || [0, 0, 0];
  const posed = (rx || ry || rz)
    ? `PartPose.offsetAndRotation(${pose.map(f).join(', ')}, `
      + [rx, ry, rz].map((d) => (d ? `${d}f * Mth.DEG_TO_RAD` : '0f')).join(', ') + ')'
    : (pose[0] || pose[1] || pose[2])
      ? `PartPose.offset(${pose.map(f).join(', ')})`
      : 'PartPose.ZERO';

  out.push(`        ${decl}${parentVar}.addOrReplaceChild("${jn}",`);
  out.push(`                ${cubes},`);
  out.push(`                ${posed});`);
}
out.push('');
const tex = which === 'cygnus' ? 256 : 128;
out.push(`        return LayerDefinition.create(mesh, ${tex}, ${tex});`);
console.log(out.join('\n'));
