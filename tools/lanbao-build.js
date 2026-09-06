/**
 * 體素藍寶堅尼 — 在 Blockbench 內用 risky_eval 執行（不是 node 模組）。
 *
 * 不是「疊方塊」，是先把車當成一個曲面雕出來再取樣成方塊：
 *   1. 站點表 ST 沿車長給五條線 —— 中線頂（引擎蓋→風擋→車頂→尾廂，一條連續的
 *      水滴輪廓）、葉子板頂、外緣頂、車底、以及斷面的寬度曲線。
 *   2. topAt(|x|) 讓葉子板高過引擎蓋，中央再加一點拱度（crown），頂面就不會是平牆。
 *   3. hwAt(y) 下段 sin^1.5（側裙內收）、上段四分之一橢圓（車頂收窄），
 *      肩線最寬 —— 這是車身的張力線。
 *   4. 輪拱＝橢圓弧的減法，拱頂只比胎頂高 0.2，車體是「包」在輪子上而不是罩著。
 *   5. 燈組不是外掛方塊，是把最外兩格的材質換掉，Y 字燈嵌在鈑金裡。
 *   6. 逐格算材質 → x 切成同材質的 run → 沿 z 合併 → 一個 run 一個長方體。
 *
 * 比例鎖在 長:寬:高 = 10 : 4.46 : 2.0（92 x 41 x 18.4），車艙佔車長 28%。
 * 注意：重跑這支腳本會清掉 outliner，動畫（idle / launch）是存在 .bbmodel 裡的，
 * 重建之後群組 uuid 會換一組，動畫要靠群組**名稱**重新連回去。要改幾何又想留動畫的話，
 * 先另存一份 bbmodel，重建後再把 animations 區塊貼回去。
 *
 * 貼圖走 fromDataURL（create_texture 的尺寸參數無效），box UV 自己設 uv_offset
 * 指到 16px 高的純色帶，所以每個 cube 的 net 高度（sz+sy）必須 <= 16。
 */
(function () {
  const ZSTEP = 2, Z0 = -46, Z1 = 46, NZ = (Z1 - Z0) / ZSTEP;
  const YMAX = 20, MAXRUN = 14;

  // z, ycore, yfend, yside, ybot, hwmax, hwb, hwt, ys, xc, xf
  const ST = [
    [-46,  6.6,  7.0,  5.8, 1.4, 16.0, 12.6, 13.2, 4.2,  5.0, 12.5],
    [-42,  9.0,  9.8,  8.0, 1.4, 18.6, 14.2, 14.6, 5.0,  5.5, 13.6],
    [-38, 10.8, 11.8,  9.8, 1.4, 19.9, 15.0, 15.0, 5.6,  6.0, 14.5],
    [-34, 12.0, 13.2, 11.2, 1.5, 20.3, 15.3, 14.8, 6.2,  6.5, 14.6],
    [-30, 12.8, 14.2, 12.2, 1.5, 20.4, 15.4, 14.4, 6.8,  7.0, 14.8],
    [-26, 13.0, 14.6, 12.8, 1.5, 20.4, 15.5, 14.0, 7.2,  7.0, 15.0],
    [-22, 13.0, 14.4, 12.8, 1.6, 20.0, 15.2, 13.2, 7.4,  7.0, 14.8],
    [-18, 13.4, 14.0, 12.8, 1.6, 19.6, 15.0, 12.2, 7.6,  6.5, 14.8],
    [-14, 14.8, 14.8, 13.4, 1.6, 19.3, 14.8, 10.8, 7.8,  6.0, 15.0],
    [-10, 16.2, 16.2, 13.8, 1.7, 19.1, 14.7,  9.2, 8.0,  6.0, 15.0],
    [ -6, 17.6, 17.6, 14.4, 1.7, 18.8, 14.5,  8.0, 8.0,  6.0, 15.0],
    [ -2, 18.4, 18.4, 14.8, 1.7, 18.6, 14.3,  7.4, 8.2,  6.0, 15.0],
    [  2, 18.4, 18.4, 14.9, 1.8, 18.8, 14.4,  7.4, 8.4,  6.0, 15.0],
    [  6, 18.0, 18.2, 15.0, 1.8, 19.4, 14.8,  8.0, 8.6,  6.5, 15.2],
    [ 10, 16.8, 17.4, 15.0, 1.9, 20.1, 15.3,  9.4, 8.8,  6.5, 15.4],
    [ 14, 15.2, 16.4, 14.8, 1.9, 20.5, 15.6, 11.2, 9.0,  7.0, 15.4],
    [ 18, 14.0, 15.4, 14.4, 2.0, 20.5, 15.6, 12.6, 9.0,  7.0, 15.4],
    [ 22, 13.2, 14.6, 14.0, 2.0, 20.4, 15.4, 13.4, 9.0,  7.0, 15.4],
    [ 26, 12.8, 14.0, 13.6, 2.0, 20.2, 15.0, 13.6, 9.0,  7.5, 15.4],
    [ 30, 12.6, 13.4, 13.2, 2.1, 19.8, 14.4, 13.4, 9.0,  8.0, 15.4],
    [ 34, 12.5, 13.0, 12.9, 2.2, 19.2, 13.6, 13.0, 9.0,  8.0, 15.2],
    [ 38, 12.4, 12.7, 12.6, 2.5, 18.4, 12.4, 12.6, 9.0,  8.5, 15.0],
    [ 42, 12.2, 12.4, 12.3, 3.0, 17.6, 11.4, 12.2, 9.0,  8.5, 15.0],
    [ 46, 12.0, 12.2, 12.0, 3.6, 16.4,  9.8, 11.8, 9.0,  8.5, 15.0]
  ];

  const lerp = (a, b, t) => a + (b - a) * t;

  function station(z) {
    if (z <= ST[0][0]) return ST[0];
    if (z >= ST[ST.length - 1][0]) return ST[ST.length - 1];
    for (let i = 0; i < ST.length - 1; i++) {
      if (z >= ST[i][0] && z <= ST[i + 1][0]) {
        const t = (z - ST[i][0]) / (ST[i + 1][0] - ST[i][0]);
        const r = [];
        for (let k = 0; k < ST[i].length; k++) r.push(lerp(ST[i][k], ST[i + 1][k], t));
        return r;
      }
    }
    return ST[0];
  }

  // 頂面高度：中央一段平的蓋面 → 用 smoothstep 一路順到葉子板峰 → 往外緣落下。
  // 這裡刻意「不」在中央做下凹，Aventador EVO 的引擎蓋是一道連續的弧，
  // 中央凹一格在體素化之後會直接變成一條溝。
  function topAt(s, ax) {
    const hwm = s[5], xc = s[9], xf = s[10];
    if (ax <= xc) return s[1];
    if (ax <= xf) {
      const t = (ax - xc) / Math.max(0.001, xf - xc);
      return lerp(s[1], s[2], t * t * (3 - 2 * t));
    }
    const t = Math.min(1, (ax - xf) / Math.max(0.001, hwm - xf));
    return lerp(s[2], s[3], 1 - Math.cos(t * Math.PI / 2));
  }

  // 斷面半寬：下段 sin^1.5 讓側裙內收，肩線最寬，上段橢圓收窄成窄車艙
  function hwAt(s, y) {
    const ybot = s[4], hwm = s[5], hwb = s[6], hwt = s[7], ys = s[8];
    const ytS = Math.max(s[1], s[2]);
    if (y < ybot) return 0;
    if (y <= ys) {
      const t = Math.min(1, (y - ybot) / Math.max(0.001, ys - ybot));
      return hwb + (hwm - hwb) * Math.pow(Math.sin(t * Math.PI / 2), 1.5);
    }
    const t = Math.min(1, (y - ys) / Math.max(0.001, ytS - ys));
    return hwt + (hwm - 0.7 - hwt) * Math.sqrt(Math.max(0, 1 - t * t));
  }

  const FA = -26, RA = 25;
  const ARCH_R = 7.0;
  const AXLE = [{ z: FA, L: ARCH_R, H: ARCH_R, yc: 6.6 }, { z: RA, L: ARCH_R, H: ARCH_R, yc: 6.6 }];
  const ARCH_IN = 13.6;
  function inArch(z, y, ax) {
    if (ax < ARCH_IN) return false;
    for (const a of AXLE) {
      const d = (z - a.z) / a.L;
      if (Math.abs(d) < 1 && y <= a.yc + a.H * Math.sqrt(1 - d * d)) return true;
    }
    return false;
  }

  function solid(s, z, y, ax) {
    if (y < s[4]) return false;
    if (ax > hwAt(s, y)) return false;
    if (y > topAt(s, ax)) return false;
    if (inArch(z, y, ax)) return false;
    return true;
  }

  // 腰線（玻璃下緣）
  const BL = [[-19, 11.6], [-14, 12.4], [-8, 13.0], [-2, 13.2], [4, 13.6], [9, 14.2]];
  function beltAt(z) {
    if (z <= BL[0][0]) return BL[0][1];
    if (z >= BL[BL.length - 1][0]) return BL[BL.length - 1][1];
    for (let i = 0; i < BL.length - 1; i++)
      if (z >= BL[i][0] && z <= BL[i + 1][0])
        return lerp(BL[i][1], BL[i + 1][1], (z - BL[i][0]) / (BL[i + 1][0] - BL[i][0]));
    return BL[0][1];
  }

  const tone = y => (y >= 15 ? 'body1' : y >= 11 ? 'body2' : y >= 6 ? 'body3' : 'body4');

  // 前後表面的 z（依 |x|,y 記憶化）。燈組靠換材質嵌進鈑金，不是外掛小方塊。
  const FZ = {}, BZ = {};
  function frontZ(ax, y) {
    const k = ax + '_' + y;
    if (k in FZ) return FZ[k];
    let r = null;
    for (let z = Z0 - 1; z < 0; z += 0.5) if (solid(station(z), z, y, ax)) { r = z; break; }
    FZ[k] = r; return r;
  }
  function backZ(ax, y) {
    const k = ax + '_' + y;
    if (k in BZ) return BZ[k];
    let r = null;
    for (let z = Z1 + 1; z > 0; z -= 0.5) if (solid(station(z), z, y, ax)) { r = z; break; }
    BZ[k] = r; return r;
  }
  function sideX(z, y) {
    const s = station(z);
    for (let ax = 21; ax > 0; ax -= 0.5) if (solid(s, z, y, ax)) return ax;
    return null;
  }
  const inBox = (ax, y, a0, a1, y0, y1) => ax >= a0 && ax <= a1 && y >= y0 && y <= y1;
  const drlY = (ax, y) =>
    inBox(ax, y, 8.6, 13.0, 8.4, 9.8) ||
    inBox(ax, y, 13.0, 16.0, 7.0, 8.5) ||
    inBox(ax, y, 8.6, 10.0, 6.2, 8.4);
  const tailY = (ax, y) =>
    inBox(ax, y, 7.4, 12.4, 9.4, 10.8) ||
    inBox(ax, y, 12.4, 15.4, 8.0, 9.5) ||
    inBox(ax, y, 7.4, 8.8, 7.2, 9.4);

  function mat(s, z, y, ax) {
    const hw = hwAt(s, y);
    if (y <= 3.0) return 'carbon';                                          // 下擾流／側裙
    if (z < -30) {
      const fz = frontZ(ax, y);
      if (fz !== null && z <= fz + 2.0) {
        if (drlY(ax, y)) return 'white';
        if (inBox(ax, y, 8.0, 15.8, 6.0, 10.0)) return 'shadow';
      }
    }
    if (z > 32) {
      const bz = backZ(ax, y);
      if (bz !== null && z >= bz - 2.0) {
        if (tailY(ax, y)) return 'red';
        if (inBox(ax, y, 6.8, 16.0, 6.4, 11.4)) return 'shadow';
      }
    }
    if (z <= -41 && y <= 5.4 && ax <= 6.5) return 'black';                  // 中央下格柵
    if (z <= -39 && y <= 6.2 && ax >= 12.5 && ax <= 17.0) return 'black';   // 兩側進氣
    if (z >= 3 && z <= 17.5 && ax > hw - 4.2) {                             // 側進氣：往後張開的斜口
      const itop = Math.min(12.2, 8.4 + 0.30 * (z - 3));
      if (y >= 4.4 && y <= itop) return (y >= 8.0 && y < 9.0) ? tone(y) : 'black';
    }
    if (z >= 41 && y >= 3.0 && y <= 6.6 && ax <= 9.5) return 'black';       // 尾板下段
    if (z >= -19 && z <= 9) {
      const bl = beltAt(z);
      const roof = (z > -7 && z < 4.5) ? 1.9 : 0;
      if (y >= bl && y <= s[1] - roof) {
        const pillar = (z < -14.5 || z > 4.5) && ax > hw - 2.2;             // 只有 A/C 柱收邊
        return pillar ? tone(y) : 'glass';
      }
    }
    return tone(y);
  }

  // ---- 取樣 → x 方向的 run → 沿 z 合併 ------------------------------------
  const boxes = [];
  const open = new Map();
  for (let i = 0; i < NZ; i++) {
    const za = Z0 + i * ZSTEP, zb = za + ZSTEP, zc = za + ZSTEP / 2;
    const s = station(zc);
    const runs = [];
    for (let y = 0; y < YMAX; y++) {
      const yc = y + 0.5;
      let cur = null;
      for (let ix = -21; ix < 21; ix++) {
        const ax = Math.abs(ix + 0.5);
        const m = solid(s, zc, yc, ax) ? mat(s, zc, yc, ax) : null;
        if (cur && m !== null && cur.m === m) cur.x1 = ix + 1;
        else { if (cur) runs.push(cur); cur = m === null ? null : { y: y, x0: ix, x1: ix + 1, m: m }; }
      }
      if (cur) runs.push(cur);
    }
    const seen = new Set();
    for (const r of runs) {
      const key = r.y + '|' + r.x0 + '|' + r.x1 + '|' + r.m;
      seen.add(key);
      const o = open.get(key);
      if (o && o.z1 === za && (o.z1 - o.z0) < MAXRUN) { o.z1 = zb; continue; }
      if (o) boxes.push(o);
      open.set(key, { y: r.y, x0: r.x0, x1: r.x1, m: r.m, z0: za, z1: zb });
    }
    for (const k of Array.from(open.keys()))
      if (!seen.has(k)) { boxes.push(open.get(k)); open.delete(k); }
  }
  for (const o of open.values()) boxes.push(o);

  // ---- 貼圖：256x256，16 條 16px 高的純色帶 -------------------------------
  const BANDS = ['body1', 'body2', 'body3', 'body4', 'glass', 'black', 'white', 'red',
                 'amber', 'silver', 'tan', 'carbon', 'tyre', 'rimdark', 'rimlite', 'shadow'];
  const COLOR = {
    body1: '#ffd642', body2: '#f7bd1e', body3: '#e2a610', body4: '#c1860a',
    glass: '#111520', black: '#15171b', white: '#f3f5f1', red: '#d8202a',
    amber: '#ff9a16', silver: '#a7aeb4', tan: '#c79a58', carbon: '#24272c',
    tyre:  '#191b1e', rimdark: '#2b2f35', rimlite: '#7d858c', shadow: '#0d0f12'
  };
  const BAND = {};
  BANDS.forEach((nm, i) => { BAND[nm] = i * 16; });

  const cv = document.createElement('canvas');
  cv.width = 256; cv.height = 256;
  const cx = cv.getContext('2d');
  BANDS.forEach((nm, i) => { cx.fillStyle = COLOR[nm]; cx.fillRect(0, i * 16, 256, 16); });

  // ---- 建模 ---------------------------------------------------------------
  Undo.initEdit({ outliner: true, elements: [], textures: Texture.all.slice() });

  Project.texture_width = 256;
  Project.texture_height = 256;
  const tex = Texture.all[0];
  tex.fromDataURL(cv.toDataURL());
  tex.uv_width = 256; tex.uv_height = 256;

  // Group 的建構參數不見得會留住 name/origin，明確補一次再 init（輪子群組的
  // origin 是轉軸，之後做輪胎旋轉動畫要用，掉了就轉不起來）
  function mkGroup(name, origin, parent) {
    const g = new Group({ name: name, origin: origin });
    g.name = name; g.origin = origin.slice();
    if (parent) g.addTo(parent);
    g.init();
    g.name = name; g.origin = origin.slice();
    return g;
  }
  // chassis 夾在 root 與車殼之間，樞紐放在後軸：起步抬頭是繞後軸轉，
  // 輪子掛在 root 底下所以不會跟著抬起來。origin 是樞紐不是位移，旋轉為 0 時不動幾何。
  const gRoot = mkGroup('lanbao', [0, 0, 0], null);
  const gChassis = mkGroup('chassis', [0, 6.6, RA], gRoot);
  const gBody = mkGroup('body', [0, 0, 0], gChassis);
  const gDet  = mkGroup('details', [0, 0, 0], gChassis);
  const wheelG = {};
  [['wheel_fl', -1, FA], ['wheel_fr', 1, FA], ['wheel_rl', -1, RA], ['wheel_rr', 1, RA]]
    .forEach(w => { wheelG[w[0]] = mkGroup(w[0], [w[1] * 16, 6.6, w[2]], gRoot); });

  let n = 0; const bad = [];
  function add(name, x0, x1, y0, y1, z0, z1, m, grp, origin) {
    const sx = x1 - x0, sy = y1 - y0, sz = z1 - z0;
    if (sz + sy > 16 || 2 * (sx + sz) > 256) bad.push(name + ' ' + sx + 'x' + sy + 'x' + sz);
    const c = new Cube({
      name: name, from: [x0, y0, z0], to: [x1, y1, z1],
      origin: origin || [0, 0, 0], autouv: 0, box_uv: true, uv_offset: [0, BAND[m]]
    }).addTo(grp).init();
    if (c.faces) Object.keys(c.faces).forEach(f => { c.faces[f].texture = tex.uuid; });
    n++; return c;
  }

  for (const b of boxes) add('s' + n, b.x0, b.x1, b.y, b.y + 1, b.z0, b.z1, b.m, gBody);

  // ---- 輪：同心圓盤，胎 → 輪圈外唇 → 輪輻面 → 中心蓋 ----------------------
  const R = 6.6, CY = 6.6, XIN = 14.0, XOUT = 19.6;
  function wheel(gname, side, za) {
    const g = wheelG[gname], org = [side * 16, CY, za];
    function disc(tag, rad, nSeg, xa, xb, m) {
      const step = 2 * rad / nSeg;
      for (let k = 0; k < nSeg; k++) {
        const ya = CY - rad + k * step, yb = ya + step;
        const dy = Math.abs((ya + yb) / 2 - CY);
        const h = Math.sqrt(Math.max(0, rad * rad - dy * dy));
        if (h < 0.4) continue;
        const hz = Math.round(h * 4) / 4;
        const a = side < 0 ? -xb : xa, b = side < 0 ? -xa : xb;
        add(gname + tag + k, a, b, ya, yb, za - hz, za + hz, m, g, org);
      }
    }
    disc('_t', R, 18, XIN, XOUT, 'tyre');
    disc('_l', 5.0, 12, XOUT, XOUT + 0.4, 'rimlite');
    disc('_s', 3.8, 8, XOUT + 0.4, XOUT + 0.7, 'rimdark');
    const a = side < 0 ? -(XOUT + 0.9) : XOUT + 0.7, b = side < 0 ? -(XOUT + 0.7) : XOUT + 0.9;
    add(gname + '_hub', a, b, CY - 1.2, CY + 1.2, za - 1.2, za + 1.2, 'silver', g, org);
  }
  wheel('wheel_fl', -1, FA); wheel('wheel_fr', 1, FA);
  wheel('wheel_rl', -1, RA); wheel('wheel_rr', 1, RA);

  // ---- 細節 ---------------------------------------------------------------
  // 後視鏡：貼在 A 柱外緣，位置由曲面掃出來
  [-1, 1].forEach(side => {
    const ax = sideX(-13, 12.5) || 18;
    const s0 = side < 0 ? -(ax + 1.6) : ax - 0.4, s1 = side < 0 ? -(ax - 0.4) : ax + 1.6;
    add('mstalk' + side, s0, s1, 12.4, 13.2, -15.0, -13.0, 'black', gDet);
    const m0 = side < 0 ? -(ax + 4.8) : ax + 1.2, m1 = side < 0 ? -(ax + 1.2) : ax + 4.8;
    add('mirror' + side, m0, m1, 12.6, 14.2, -16.0, -13.4, 'black', gDet);
  });

  // 尾翼：壓低的鵝頸柱 + 薄翼面 + 端板
  [-1, 1].forEach(side => {
    const p0 = side < 0 ? -12.5 : 9.5, p1 = side < 0 ? -9.5 : 12.5;
    add('wing_pyl' + side, p0, p1, 12.3, 15.7, 34.5, 37.0, 'carbon', gDet);
    const e0 = side < 0 ? -18.6 : 16.8, e1 = side < 0 ? -16.8 : 18.6;
    add('wing_ep' + side, e0, e1, 14.9, 17.3, 34.0, 40.6, 'carbon', gDet);
  });
  add('wing_blade', -16.8, 16.8, 15.7, 16.7, 34.0, 40.0, 'carbon', gDet);
  add('wing_lip', -16.8, 16.8, 16.7, 17.3, 38.4, 40.6, 'carbon', gDet);

  // 前下擾流唇 + 導流鰭
  add('splitter', -18.0, 18.0, 1.0, 1.8, -47.4, -42.0, 'carbon', gDet);
  [-1, 1].forEach(side => {
    const x0 = side < 0 ? -17.0 : 12.0, x1 = side < 0 ? -12.0 : 17.0;
    add('canard' + side, x0, x1, 1.8, 2.9, -47.0, -43.0, 'carbon', gDet);
  });

  // 後分流器
  for (let k = -2; k <= 2; k++)
    add('fin' + (k + 2), k * 4.4 - 0.6, k * 4.4 + 0.6, 1.4, 4.4, 41.0, 47.2, 'carbon', gDet);
  add('difpan', -13.0, 13.0, 1.0, 1.6, 40.5, 47.2, 'carbon', gDet);
  [-1, 1].forEach(side => {
    const x0 = side < 0 ? -8.0 : 4.2, x1 = side < 0 ? -4.2 : 8.0;
    add('exh' + side, x0, x1, 4.6, 6.8, 45.6, 47.4, 'shadow', gDet);
  });

  // 引擎蓋洩壓孔 + 引擎室百葉，高度跟著蓋面走
  [-1, 1].forEach(side => {
    for (let k = 0; k < 3; k++) {
      const zc = -34 + k * 4.5, s = station(zc + 1.2);
      const x0 = side < 0 ? -12.5 : 8.5, x1 = side < 0 ? -8.5 : 12.5;
      add('vent' + side + k, x0, x1, s[1] - 0.6, s[1] + 0.05, zc, zc + 2.4, 'shadow', gDet);
    }
    for (let k = 0; k < 3; k++) {
      const zc = 11.5 + k * 2.8, s = station(zc + 1.0);
      const dx0 = side < 0 ? -9.5 : 2.4, dx1 = side < 0 ? -2.4 : 9.5;
      add('deck' + side + k, dx0, dx1, s[1] - 0.6, s[1] + 0.05, zc, zc + 1.8, 'shadow', gDet);
    }
  });

  Canvas.updateAll();
  Undo.finishEdit('build lanbao');

  const mn = [1e9, 1e9, 1e9], mx = [-1e9, -1e9, -1e9];
  for (const c of Cube.all) for (let i = 0; i < 3; i++) {
    mn[i] = Math.min(mn[i], c.from[i]); mx[i] = Math.max(mx[i], c.to[i]);
  }
  const L = mx[2] - mn[2], W = mx[0] - mn[0], H = mx[1] - mn[1];
  // Codecs.project 走的是 compile 的 undo 變體，群組只留 uuid，name/origin 會掉。
  // 自己把 live 的群組資料補回 outliner 再落檔（輪子群組的 origin 是轉軸，不能掉）。
  const SAVE_PATH = 'C:/Users/asdew/OneDrive/桌面/projects/taiwan-template-26.2/models/lanbao.bbmodel';
  const out = Codecs.project.compile({ raw: true });
  const byUuid = {};
  Group.all.forEach(g => { byUuid[g.uuid] = g; });
  (function fixNames(nodes) {
    for (const nd of nodes || []) {
      if (nd && typeof nd === 'object') {
        const g = byUuid[nd.uuid];
        if (g) {
          nd.name = g.name;
          nd.origin = g.origin.slice();
          nd.rotation = (g.rotation || [0, 0, 0]).slice();
          nd.export = true; nd.visibility = true;
        }
        fixNames(nd.children);
      }
    }
  })(out.outliner);
  require('fs').writeFileSync(SAVE_PATH, JSON.stringify(out));

  const gnames = Group.all.map(g => g.name + '@' + g.origin.join(','));
  return JSON.stringify({
    saved: SAVE_PATH,
    groups: gnames,
    cubes: Cube.all.length, shell: boxes.length,
    size: [+W.toFixed(1), +H.toFixed(1), +L.toFixed(1)],
    ratio: [10, +(W / L * 10).toFixed(2), +(H / L * 10).toFixed(2)],
    badUV: bad.slice(0, 8), badCount: bad.length
  });
})()
