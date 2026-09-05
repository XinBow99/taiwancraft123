/**
 * 分析一段引擎錄音，印出調 engine-sound.mjs 需要的兩組數字。
 *
 * 為什麼要有這支：合成的音色是靠 timbre() 那條曲線決定的，而那條曲線只能對著
 * 「參考錄音的頻譜長什麼樣」來調。用耳朵比對是調不準的——八度頻帶差 6 dB，
 * 聽起來就是「悶」或「刺」，但說不出差在哪、也不知道該往哪邊動。
 *
 *   node tools/analyse-engine.mjs <錄音檔> [起秒 迄秒]
 *
 * 需要 ffmpeg 在 PATH（用它把任何格式轉成單聲道 f32 raw）。
 */
import { execFileSync } from 'node:child_process';
import { readFileSync, unlinkSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const [file, fromArg, toArg] = process.argv.slice(2);
if (!file) {
  console.error('用法：node tools/analyse-engine.mjs <錄音檔> [起秒 迄秒]');
  process.exit(1);
}

const SR = 44100;
const raw = join(tmpdir(), `engine-${process.pid}.f32`);
execFileSync('ffmpeg', ['-v', 'error', '-y', '-i', file,
  '-ac', '1', '-ar', String(SR), '-f', 'f32le', raw]);
const buf = readFileSync(raw);
unlinkSync(raw);
const x = new Float32Array(buf.buffer, buf.byteOffset, buf.length / 4);
const dur = x.length / SR;

const from = fromArg ? Number(fromArg) : 0;
const to = toArg ? Number(toArg) : dur;
console.log(`${file}`);
console.log(`  長度 ${dur.toFixed(2)} 秒，分析 ${from.toFixed(2)}~${to.toFixed(2)} 秒\n`);

// ---------------------------------------------------------------- 基頻軌跡
//
// 用自相關而不是 FFT 找基頻：引擎聲的基頻（點火頻率）常常比它的諧波弱很多，
// 在頻譜上取最大值會抓到二次或三次諧波，估出來的轉速就會是兩三倍。
function fundamental(start, len, loHz = 20, hiHz = 260) {
  const n = len;
  const lag0 = Math.floor(SR / hiHz);
  const lag1 = Math.min(Math.floor(SR / loHz), Math.floor(n / 2));
  let mean = 0;
  for (let i = 0; i < n; i++) mean += x[start + i];
  mean /= n;

  let best = 0, bestLag = 0;
  for (let lag = lag0; lag <= lag1; lag++) {
    let num = 0, d1 = 0, d2 = 0;
    for (let i = 0; i + lag < n; i++) {
      const a = x[start + i] - mean, b = x[start + i + lag] - mean;
      num += a * b; d1 += a * a; d2 += b * b;
    }
    const r = num / (Math.sqrt(d1 * d2) + 1e-12);
    if (r > best) { best = r; bestLag = lag; }
  }
  return { hz: bestLag ? SR / bestLag : 0, conf: best };
}

console.log('  時間    基頻 Hz   信心   四行程單缸換算轉速');
const win = Math.floor(SR * 0.25);
const track = [];
for (let t = from; t + 0.25 <= to; t += 0.25) {
  const { hz, conf } = fundamental(Math.floor(t * SR), win);
  track.push({ t, hz, conf });
  // 四行程單缸：每兩轉點火一次 → rpm = hz * 120
  const bar = '#'.repeat(Math.round(hz / 4));
  console.log(`  ${t.toFixed(2).padStart(5)}  ${hz.toFixed(1).padStart(7)}  ${conf.toFixed(2)}  ${String(Math.round(hz * 120)).padStart(5)}  ${bar}`);
}
const good = track.filter((p) => p.conf > 0.3);
if (good.length) {
  const lo = Math.min(...good.map((p) => p.hz));
  const hi = Math.max(...good.map((p) => p.hz));
  console.log(`\n  基頻範圍 ${lo.toFixed(1)}~${hi.toFixed(1)} Hz（轉速約 ${Math.round(lo * 120)}~${Math.round(hi * 120)} rpm，比值 ${(hi / lo).toFixed(2)}×）`);
}

// ---------------------------------------------------------------- 頻譜形狀
//
// 八度頻帶的相對能量。這是 timbre() 要對齊的東西：合成出來的聲音只要每個頻帶
// 都落在參考的 3 dB 以內，聽起來就是同一類引擎。
function bands(start, len) {
  // 夾在檔案長度內：循環音檔只有 1 秒，從 0.75 秒取 0.4 秒的窗會讀到緩衝區外面，
  // 整張頻譜會變成 NaN
  const avail = x.length - start;
  const n = 1 << Math.floor(Math.log2(Math.max(1024, Math.min(len, avail))));
  const re = new Float64Array(n), im = new Float64Array(n);
  for (let i = 0; i < n; i++) {
    // Hann 窗：不加窗的話兩端的突變會在頻譜上糊成一片
    re[i] = x[start + i] * (0.5 - 0.5 * Math.cos((2 * Math.PI * i) / (n - 1)));
  }
  // 就地 FFT（Cooley–Tukey）
  for (let i = 1, j = 0; i < n; i++) {
    let bit = n >> 1;
    for (; j & bit; bit >>= 1) j ^= bit;
    j ^= bit;
    if (i < j) { [re[i], re[j]] = [re[j], re[i]]; [im[i], im[j]] = [im[j], im[i]]; }
  }
  for (let len2 = 2; len2 <= n; len2 <<= 1) {
    const ang = (-2 * Math.PI) / len2;
    for (let i = 0; i < n; i += len2) {
      for (let k = 0; k < len2 / 2; k++) {
        const wr = Math.cos(ang * k), wi = Math.sin(ang * k);
        const ur = re[i + k], ui = im[i + k];
        const vr = re[i + k + len2 / 2] * wr - im[i + k + len2 / 2] * wi;
        const vi = re[i + k + len2 / 2] * wi + im[i + k + len2 / 2] * wr;
        re[i + k] = ur + vr; im[i + k] = ui + vi;
        re[i + k + len2 / 2] = ur - vr; im[i + k + len2 / 2] = ui - vi;
      }
    }
  }
  const edges = [40, 80, 160, 320, 640, 1280, 2560, 5120, 10240, 20000];
  const out = [];
  for (let b = 0; b < edges.length - 1; b++) {
    let e = 0;
    const i0 = Math.round((edges[b] * n) / SR), i1 = Math.round((edges[b + 1] * n) / SR);
    for (let i = i0; i < i1 && i < n / 2; i++) e += re[i] * re[i] + im[i] * im[i];
    out.push({ lo: edges[b], hi: edges[b + 1], e });
  }
  const peak = Math.max(...out.map((o) => o.e));
  return out.map((o) => ({ ...o, db: 10 * Math.log10(o.e / peak + 1e-12) }));
}

for (const [label, t] of [['最安靜處（怠速）', good.length ? good.reduce((a, b) => (a.hz < b.hz ? a : b)).t : from],
                          ['最高轉處（催油門）', good.length ? good.reduce((a, b) => (a.hz > b.hz ? a : b)).t : from]]) {
  console.log(`\n  ${label} @ ${t.toFixed(2)}s 的八度頻帶：`);
  for (const b of bands(Math.floor(t * SR), Math.floor(SR * 0.4))) {
    console.log(`    ${String(b.lo).padStart(5)}-${String(b.hi).padEnd(6)}Hz  ${b.db.toFixed(1).padStart(6)} dB  ${'#'.repeat(Math.max(0, Math.round(40 + b.db)))}`);
  }
}
