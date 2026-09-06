/**
 * 程式產生的低多邊形超跑，輸出 Blockbench 的 `free` 格式 mesh 專案。
 *
 * ## 為什麼是放樣（loft）而不是體素
 *
 * 體素做斜面永遠是階梯，而且每一階要六個面。放樣是沿著車長排一串**封閉斷面**，
 * 相鄰兩個斷面之間連成四邊形——同一個斜面只要一個面，曲率靠斷面參數帶出來。
 * 左右對稱是生成方式保證的：右半算完直接鏡射，不是「盡量對稱」。
 * 結構照 white-low-poly-semi-truck.bbmodel：一個車殼 mesh ＋ 四個獨立輪子 mesh，
 * 每個輪子各自包一層群組當轉軸。
 *
 * ## 四組手調的東西
 *
 * 1. `KEY`：沿車長的關鍵影格，給半寬、車底、車頂、車頂半寬四條曲線。
 * 2. `STATIONS`：斷面放在哪些 z。車頭與擋風玻璃排密一點，那裡曲率最大。
 * 3. `halfSection`：斷面本身的 9 個控制點，決定車肩、腰線、裙邊的比例。
 * 4. `DECALS`：貼在車殼表面上的燈與進氣口，座標用「(z, 環上參數)」而不是 xyz。
 *
 * ## 輪拱
 *
 * 放樣做輪拱的辦法是讓輪子那幾站的**外側底面抬高**，斷面局部變成 ∩ 形：中線的
 * 車底不動，外側的地板升到輪子頂上，兩者之間那段邊就是輪拱的內壁。抬升沿 z 用
 * 三角形漸變，拱口才會慢慢張開而不是一刀切。
 *
 * 用法：node tools/lambo.mjs <out.bbmodel>
 */
import {writeFileSync} from 'node:fs';

const TEX = 128;

// 材質帶。貼圖是 8 條 16px 寬的直帶，帶內由上而下有明暗漸層：
// u 取帶中央（固定），v 由頂點高度決定。這樣車身會有一層上亮下暗的漸層，
// 而不是每個面一塊死板的純色——上一版體素車最大的問題就是整台同一個值。
const BAND = {body: 0, black: 1, glass: 2, white: 3, red: 4, tyre: 5, dark: 6, chrome: 7};
const uOf = b => BAND[b] * 16 + 8;

// ---- 縱向曲線（z 為車長，負是車頭）----------------------------------------
//  z 從 -42（車頭）到 +42（車尾），全長 84 單位 = 5.25 格。
//
//  比例照 Aventador：軸距佔車長 0.56、車高佔車長 0.24、前懸比後懸短。
//  **車頭是寬而扁的，不是尖的**——參考圖的正面幾乎跟車身同寬，收的是高度不是寬度。
//  上一版把 z=-42 的半寬收到 6.0，正面看變成船首，這裡改成 14.5。
const KEY = [
  //  z      hw     yb     yt     rw
  [-42.0,  14.5,   1.2,   6.4,   9.0],   // 前保險桿端面
  [-39.0,  16.6,   1.1,   7.6,  11.0],
  [-36.0,  17.8,   1.2,   8.5,  12.5],   // 頭燈
  [-32.0,  18.4,   1.4,   9.0,  13.5],
  [-27.0,  18.7,   1.5,   9.3,  14.0],   // 前軸
  [-21.0,  17.8,   1.5,   9.6,  14.0],   // 引擎蓋最低處
  [-16.0,  17.4,   1.5,  11.6,  13.0],   // 擋風玻璃根部
  [-10.0,  17.2,   1.5,  15.8,  10.0],   // 擋風玻璃
  [ -4.0,  17.2,   1.5,  19.3,   6.8],   // 車頂前緣
  [  2.0,  17.4,   1.5,  19.4,   7.0],   // 車頂
  [  8.0,  18.0,   1.5,  17.5,   8.8],   // 後擋風／引擎室蓋
  [ 14.0,  19.4,   1.6,  15.0,  12.5],
  [ 21.0,  19.7,   1.7,  13.6,  14.5],   // 後軸，車尾最寬
  [ 27.0,  19.4,   1.8,  13.1,  14.5],
  [ 33.0,  18.6,   2.2,  12.7,  14.0],
  [ 38.0,  17.0,   3.0,  12.3,  12.5],
  [ 42.0,  14.0,   3.6,  11.4,  10.0],   // 尾端面
];

const lerp = (a, b, t) => a + (b - a) * t;

function curve(z) {
  if (z <= KEY[0][0]) return KEY[0].slice(1);
  for (let i = 1; i < KEY.length; i++) {
    if (z <= KEY[i][0]) {
      const t = (z - KEY[i - 1][0]) / (KEY[i][0] - KEY[i - 1][0]);
      return [1, 2, 3, 4].map(k => lerp(KEY[i - 1][k], KEY[i][k], t));
    }
  }
  return KEY[KEY.length - 1].slice(1);
}

const STATIONS = [-42, -40.5, -38.5, -36.5, -34.5, -32, -29.5, -27, -24.5, -22, -19,
                  -16, -13, -10, -7, -4, -1, 2, 5, 8, 11, 14, 17, 20, 23, 26, 29,
                  32, 35, 37.5, 40, 42];

const WHEEL_Z = [-26, 21];        // 前後輪心
const R = 6.0;                    // 輪半徑
const ARCH_TOP = R * 2 + 0.7;     // 拱頂內緣，要蓋過輪子頂端（輪心 R ＋ 半徑 R）
const FENDER = 14.4;              // 拱口處外側的車頂高度（葉子板隆起）
const ARCH_HALF = 10.5;           // 拱口沿 z 的半長

/** 這一站的輪拱抬升程度，0..1。 */
function archAmount(z) {
  let a = 0;
  for (const wz of WHEEL_Z) a = Math.max(a, 1 - Math.abs(z - wz) / ARCH_HALF);
  return Math.max(0, a);
}

/**
 * 右半斷面的 9 個控制點，由車底中線往上繞到車頂中線。
 *
 * <p>外側那幾點的地板是 {@code ybOut}，跟中線的 {@code yb} 分開——輪拱就是把
 * {@code ybOut} 拉到輪子頂上，中線不動。
 *
 * <p>索引意義（別的地方用 u 參數定位貼片時要對得上）：
 * 0 底面中線、2 裙邊下緣、3 下側面、4 腰線（最寬）、5 車肩、6 車窗根部、
 * 7 車頂邊、8 車頂中線。
 */
function halfSection(hw, yb, yt, rw, arch) {
  const ybOut = lerp(yb, ARCH_TOP, arch);
  // **外側的頂也要跟著抬**。只抬地板的話，前輪那一站的車頂（9.3）比拱頂（12.7）
  // 還低，斷面會內外翻轉、整個車殼扭掉。真車也是這樣：葉子板本來就高過引擎蓋，
  // 中間再凹下去——所以中線用 yt，外側用 ytOut。
  const ytOut = lerp(yt, Math.max(yt, FENDER), arch);
  const h = ytOut - ybOut;
  // 輪拱處把外側點再往外推。半寬沿車長幾乎是定值的話，車側就是一塊平板；
  // 真車是門檻內收、前後葉子板鼓出來，這條 6.5% 的外擴就是那個腰身。
  const hwO = hw * (1 + 0.065 * arch);
  return [
    [0.00 * hw, yb],
    [0.58 * hw, lerp(yb, ybOut, 0.5)],
    [0.93 * hwO, ybOut],
    [1.00 * hwO, ybOut + 0.22 * h],
    [0.99 * hwO, ybOut + 0.48 * h],     // 腰線折角，車身側面最寬的那條稜
    [0.93 * hwO, ybOut + 0.70 * h],
    [0.78 * hw,  ybOut + 0.87 * h],
    [Math.min(rw, 0.62 * hw), Math.max(yt * 0.97, ybOut + 0.97 * h)],
    [0.00 * hw, yt],
  ];
}

const P = 9;                      // 右半控制點數
const N = P * 2 - 2;              // 封閉斷面點數（兩個中線點不重複）

/** 封閉斷面：右半 9 點 ＋ 左半 7 點。 */
function section(z) {
  const [hw, yb, yt, rw] = curve(z);
  const right = halfSection(hw, yb, yt, rw, archAmount(z));
  const loop = right.map(([x, y]) => [x, y, z]);
  for (let i = right.length - 2; i >= 1; i--) loop.push([-right[i][0], right[i][1], z]);
  return loop;
}

/**
 * 車殼表面上的一點，用「z ＋ 環上參數 u」定位，u 可以是小數。
 *
 * <p>燈與進氣口要**貼在曲面上**。用固定 xyz 擺的話，車頭一收窄燈就飄在外面或陷進
 * 車殼裡；改成沿斷面參數取點，再往外推一點點，就永遠貼著車身走。
 */
function surf(z, u, side, lift = 0.25) {
  const [hw, yb, yt, rw] = curve(z);
  const half = halfSection(hw, yb, yt, rw, archAmount(z));
  const i = Math.max(0, Math.min(P - 2, Math.floor(u)));
  const t = u - i;
  const x = lerp(half[i][0], half[i + 1][0], t);
  const y = lerp(half[i][1], half[i + 1][1], t);
  // 往外推的方向：從斷面中心指向這一點。中心取中線上下的中點，夠用了。
  const cy = (yb + yt) / 2;
  const dx = x, dy = y - cy;
  const len = Math.hypot(dx, dy) || 1;
  return [side * (x + dx / len * lift), y + dy / len * lift, z];
}

// ---- Blockbench 資料結構 ---------------------------------------------------
// 固定種子：同一份參數要產生同一個檔案，不然每次重建的 diff 都是全紅。
let seed = 0x2545F491;
const rnd = () => (seed = (seed * 1664525 + 1013904223) >>> 0) / 4294967296;
const hex = n => Array.from({length: n}, () => '0123456789abcdef'[(rnd() * 16) | 0]).join('');
const uuid = () => `${hex(8)}-${hex(4)}-${hex(4)}-${hex(4)}-${hex(12)}`;
const key = () => hex(8);

function mesh(name, origin) {
  return {
    name, color: 0, origin, rotation: [0, 0, 0], shading: 'smooth', export: true,
    visibility: true, locked: false, render_order: 'default', scope: 0,
    allow_mirror_modeling: false, type: 'mesh', uuid: uuid(), vertices: {}, faces: {},
  };
}

/** 頂點存的是「相對元件 origin」的座標，這是 Blockbench mesh 的規矩。 */
const addV = (m, p) => {
  const k = key();
  m.vertices[k] = [p[0] - m.origin[0], p[1] - m.origin[1], p[2] - m.origin[2]];
  return k;
};

const addF = (m, vs, band, vv) => {
  const uv = {};
  vs.forEach((k, i) => { uv[k] = [uOf(band), vv[i] * (TEX - 1)]; });
  m.faces[key()] = {uv, texture: 0, vertices: vs};
};

const YMAX = 21;
// 只用漸層的上半段。整段 0..1 都用的話，車身多數面落在 y≈8～13、取到帶子中間，
// 黃色會被壓成橘褐色——第一版三個視角看起來都像生鏽，就是這裡。
const vFor = y => 0.07 + 0.46 * Math.min(1, Math.max(0, 1 - y / YMAX));

/** 一片四邊形，UV 的 v 直接由每個角的高度決定。 */
const quad = (m, corners, band) =>
  addF(m, corners.map(p => addV(m, p)), band, corners.map(p => vFor(p[1])));

/** 一顆長方體，六個面同一個材質帶。用來做尾翼、下擾流、後視鏡這些附加件。 */
function box(m, [x0, y0, z0], [x1, y1, z1], band) {
  const c = [[x0, y0, z0], [x1, y0, z0], [x1, y1, z0], [x0, y1, z0],
             [x0, y0, z1], [x1, y0, z1], [x1, y1, z1], [x0, y1, z1]];
  const k = c.map(p => addV(m, p));
  const face = idx => addF(m, idx.map(i => k[i]), band, idx.map(i => vFor(c[i][1])));
  face([3, 2, 1, 0]);   // 前（-z）
  face([4, 5, 6, 7]);   // 後（+z）
  face([7, 6, 2, 3]);   // 上
  face([0, 1, 5, 4]);   // 下
  face([4, 7, 3, 0]);   // 左（-x）
  face([1, 2, 6, 5]);   // 右（+x）
}

// ---- 車殼 ------------------------------------------------------------------
const body = mesh('shell', [0, 0, 0]);
const pts = STATIONS.map(section);
const rings = pts.map(ring => ring.map(p => addV(body, p)));

/**
 * 這個位置屬於哪個材質。
 *
 * <p>索引 0..8 是右半由下而上，9..15 是左半由上而下，所以先鏡射回右半再判斷——
 * 直接用原始索引的話左半會上錯色，而且只有從左邊看才發現。
 */
function bandAt(idx, z, y) {
  const up = idx <= P - 1 ? idx : N - idx;
  // **下緣的黑要看高度，不能只看索引。**輪拱那幾站的 up<=2 已經被抬到 y≈12，
  // 整段用索引判的話，正面看等於在車頭糊一大塊黑，裙邊反而不見了。
  if (up <= 2 && (y < 4.6 || archAmount(z) > 0.4)) return 'black';  // 車底、裙邊、拱內壁
  if (up >= 6 && z > -17 && z < 12) return 'glass';                 // 擋風、車頂、後擋風
  return 'body';
}

for (let s = 0; s < rings.length - 1; s++) {
  for (let i = 0; i < N; i++) {
    const j = (i + 1) % N;
    const ys = [pts[s][i][1], pts[s][j][1], pts[s + 1][j][1], pts[s + 1][i][1]];
    const zm = (STATIONS[s] + STATIONS[s + 1]) / 2;
    const ym = ys.reduce((a, b) => a + b, 0) / 4;
    addF(body, [rings[s][i], rings[s][j], rings[s + 1][j], rings[s + 1][i]],
         bandAt(i, zm, ym), ys.map(vFor));
  }
}

// 前後封口：往斷面中心打扇形。尾端要反向，不然法線朝內。
for (const [ring, p, flip] of [[rings[0], pts[0], false],
                               [rings[rings.length - 1], pts[pts.length - 1], true]]) {
  const cy = p.reduce((a, q) => a + q[1], 0) / N;
  const c = addV(body, [0, cy, p[0][2]]);
  for (let i = 0; i < N; i++) {
    const j = (i + 1) % N;
    const tri = flip ? [c, ring[j], ring[i]] : [c, ring[i], ring[j]];
    addF(body, tri, 'black', [vFor(cy), vFor(p[i][1]), vFor(p[j][1])]);
  }
}

// ---- 貼在車殼上的燈與進氣口 ------------------------------------------------
// 每片用四個 (z, u) 角定義，左右各做一份。u 的意義見 halfSection 的註解。
const DECALS = [
  // 頭燈：Y 字形日行燈的兩筆 ＋ 中間的暗色燈殼
  ['white', [[-41.4, 5.0], [-36.8, 5.4], [-36.8, 4.6], [-41.4, 4.1]]],  // 上緣那一撇
  ['white', [[-38.6, 4.7], [-36.2, 3.6], [-37.0, 2.9], [-39.4, 4.0]]],  // 往下勾的那一撇
  ['dark',  [[-36.6, 5.3], [-32.6, 5.6], [-32.6, 4.5], [-36.6, 4.3]]],  // 燈殼
  // 前保險桿兩側的進氣口
  ['black', [[-40.2, 3.6], [-35.4, 3.8], [-35.4, 2.9], [-40.2, 2.8]]],
  // 側窗。原本靠 bandAt 把 up==5 整條塗成玻璃，那條帶子沿 z 被切頭切尾，
  // 側面看是一道鋸齒；改成貼片才有乾淨的邊。
  ['glass', [[-13.5, 5.9], [3.5, 5.9], [2.5, 4.9], [-11.0, 5.0]]],
  // 車門後的側進氣口：Aventador 最好認的特徵，缺了整台就不像
  ['black', [[3.5, 4.8], [12.5, 4.6], [13.0, 3.5], [4.5, 3.6]]],
  // 引擎蓋兩側的洩壓孔
  ['black', [[-24.0, 5.6], [-18.0, 5.6], [-18.0, 5.0], [-24.0, 5.0]]],
  // 尾燈
  ['red',   [[38.6, 4.9], [41.6, 4.7], [41.6, 3.7], [38.6, 3.8]]],
  // 後保險桿的散熱網
  ['black', [[36.0, 3.2], [41.4, 3.1], [41.4, 2.4], [36.0, 2.5]]],
];

for (const side of [1, -1]) {
  for (const [band, corners] of DECALS) {
    const cs = corners.map(([z, u]) => surf(z, u, side));
    // 左半要反向繞，不然法線朝內、從外面看是空的
    quad(body, side > 0 ? cs : cs.slice().reverse(), band);
  }
}

// ---- 附加件：尾翼、下擾流、後視鏡 ------------------------------------------
box(body, [-16.5, 17.2, 36.5], [16.5, 18.4, 41.5], 'black');            // 尾翼翼板
box(body, [-16.5, 15.2, 39.8], [-14.5, 18.4, 41.8], 'black');           // 左翼端板
box(body, [ 14.5, 15.2, 39.8], [ 16.5, 18.4, 41.8], 'black');           // 右翼端板
for (const sx of [-1, 1]) {
  box(body, [sx * 10.5 - 1.1, 12.4, 37.0], [sx * 10.5 + 1.1, 17.2, 39.4], 'black');   // 翼柱
  box(body, [sx * 18.2 - 1.4, 12.6, -13.2], [sx * 18.2 + 1.4, 14.0, -10.6], 'black'); // 後視鏡
}
box(body, [-17.5, 0.35, -43.2], [17.5, 1.35, -37.0], 'black');          // 前下擾流唇
box(body, [-15.5, 1.4, 38.0], [15.5, 3.2, 42.6], 'black');              // 後下分流器

// ---- 輪子：16 邊形柱，輪圈輻條靠交錯上色做出來 -----------------------------
const WHEEL = [['wheel_fl', -18.4, WHEEL_Z[0]], ['wheel_fr', 18.4, WHEEL_Z[0]],
               ['wheel_rl', -18.4, WHEEL_Z[1]], ['wheel_rr', 18.4, WHEEL_Z[1]]];
const TW = 3.6, SIDES = 16, RIM = 0.66;

const wheels = WHEEL.map(([name, x, z]) => {
  const m = mesh(name, [x, R, z]);
  const out = x > 0 ? 1 : -1;
  const ring = (dx, r) => Array.from({length: SIDES}, (_, i) => {
    const a = i / SIDES * Math.PI * 2;
    return addV(m, [x + dx, R + Math.sin(a) * r, z + Math.cos(a) * r]);
  });
  const inner = ring(-out * TW / 2, R), outer = ring(out * TW / 2, R);
  const rimO = ring(out * TW / 2 * 0.92, R * RIM), rimI = ring(-out * TW / 2 * 0.92, R * RIM);
  for (let i = 0; i < SIDES; i++) {
    const j = (i + 1) % SIDES;
    addF(m, [inner[i], inner[j], outer[j], outer[i]], 'tyre', [0.5, 0.5, 0.5, 0.5]);   // 胎面
    addF(m, [outer[i], outer[j], rimO[j], rimO[i]], 'tyre', [0.72, 0.72, 0.8, 0.8]);   // 外側胎壁
    addF(m, [rimI[i], rimI[j], inner[j], inner[i]], 'tyre', [0.8, 0.8, 0.72, 0.72]);   // 內側胎壁
  }
  const hubO = addV(m, [x + out * TW / 2 * 0.92, R, z]);
  const hubI = addV(m, [x - out * TW / 2 * 0.92, R, z]);
  for (let i = 0; i < SIDES; i++) {
    const j = (i + 1) % SIDES;
    // 交錯 chrome／black：16 邊形就變成 8 根輻條，不用真的做出輻條幾何
    addF(m, [hubO, rimO[j], rimO[i]], i % 2 ? 'chrome' : 'black', [0.25, 0.45, 0.45]);
    addF(m, [hubI, rimI[i], rimI[j]], 'black', [0.85, 0.85, 0.85]);
  }
  return m;
});

// ---- 組成專案 --------------------------------------------------------------
const group = (name, origin, kids) => ({
  name, uuid: uuid(), export: true, locked: false, scope: 0, selected: false,
  _static: {properties: {}, temp_data: {}}, origin, rotation: [0, 0, 0], color: 0,
  children: [], reset: false, shade: true, mirror_uv: false, visibility: true,
  autouv: 0, isOpen: false, primary_selected: false, _kids: kids,
});

const bodyG = group('body', [0, 0, 0], [body.uuid]);
const wheelGs = wheels.map((m, i) => group(WHEEL[i][0], m.origin, [m.uuid]));
const carG = group('car', [0, 0, 0], null);
const all = [carG, bodyG, ...wheelGs];
const node = g => ({uuid: g.uuid, isOpen: false, children: g._kids ?? []});

writeFileSync(process.argv[2], JSON.stringify({
  meta: {format_version: '5.0', model_format: 'free', box_uv: false},
  name: 'lambo_mesh', model_identifier: '', visible_box: [1, 1, 0],
  variable_placeholders: '', variable_placeholder_buttons: [], timeline_setups: [],
  unhandled_root_fields: {}, resolution: {width: TEX, height: TEX},
  elements: [body, ...wheels],
  groups: all.map(({_kids, ...g}) => g),
  outliner: [{...node(carG), children: [bodyG, ...wheelGs].map(node)}],
  textures: [{
    name: 'lambo_mesh.png', folder: 'entity', namespace: 'taiwan', id: '0', group: '',
    scope: 0, width: TEX, height: TEX, uv_width: TEX, uv_height: TEX, particle: false,
    use_as_default: false, layers_enabled: false, sync_to_project: '', file_format: 'png',
    render_mode: 'default', render_sides: 'auto', wrap_mode: 'clamp', pbr_channel: 'color',
    fps: 0, frame_time: 1, frame_order_type: 'loop', frame_order: '', frame_interpolate: false,
    visible: true, internal: true, saved: false, uuid: uuid(),
    relative_path: '../src/main/resources/assets/taiwan/textures/entity/lambo_mesh.png',
  }],
  animations: [],
}));

const parts = [body, ...wheels];
console.log('元件', parts.length,
            '頂點', parts.reduce((a, m) => a + Object.keys(m.vertices).length, 0),
            '面', parts.reduce((a, m) => a + Object.keys(m.faces).length, 0));
