/**
 * 產生速克達的引擎聲。
 *
 * 為什麼是合成的而不是找現成的錄音：素材站上的檔案有授權與出處問題，塞進 repo 之後
 * 沒人知道它能不能散布。這支程式是可重跑的來源，改參數就能再生一次。
 *
 * 兩個檔案：
 *   scooter_engine.ogg — 無縫循環的引擎聲，遊戲裡用 pitch 表示轉速
 *   scooter_start.ogg  — 發動（起動馬達 → 點火 → 回到怠速）
 *
 * 用法：node tools/engine-sound.mjs   （需要 ffmpeg 在 PATH）
 */
import { writeFileSync, mkdirSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const SR = 44100;
const OUT = 'src/main/resources/assets/taiwan/sounds';

// 固定亂數：每次跑出來的檔案要一樣，不然 git 每次都看到「檔案變了」
let seed = 20260905;
const rnd = () => (seed = (seed * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff;

/** 共振峰。排氣管的聲音就是幾個共振峰疊出來的，少了它只剩刺耳的鋸齒波。 */
const res = (f, center, bw) => 1 / (1 + ((f - center) / bw) ** 2);

/** 每個諧波的音量：共振峰 × 高頻滾降。 */
function timbre(f) {
	const shape = 1.0 * res(f, 150, 110) + 0.85 * res(f, 480, 330) + 0.4 * res(f, 1500, 1100);
	return shape / (1 + (f / 3000) ** 2);
}

const HARMONICS = 48;
// 相位不完全對齊（±0.5 rad）：全部對齊會變成尖銳的「喀」，全部隨機則糊成一團嗡嗡聲
const phase = Array.from({ length: HARMONICS + 1 }, () => (rnd() - 0.5) * 1.0);

/**
 * 脈衝列。`freqAt(i)` 是第 i 個取樣的點火頻率（Hz）——二行程引擎每轉點一次火，
 * 50 Hz 約等於 3000 rpm。
 */
function buzz(n, freqAt) {
	const out = new Float64Array(n);
	let ph = 0;
	for (let i = 0; i < n; i++) {
		const f0 = freqAt(i);
		ph += f0 / SR;
		let v = 0;
		for (let h = 1; h <= HARMONICS; h++) {
			const f = h * f0;
			if (f > 11000) break;
			v += (timbre(f) / h ** 0.85) * Math.sin(2 * Math.PI * h * ph + phase[h]);
		}
		out[i] = v;
	}
	return out;
}

/**
 * 循環式低通。頭尾要接得起來，一般的濾波器會在接縫留下一聲「啪」——所以每一級都先
 * 空跑一圈讓狀態收斂，第二圈的結果才留下來。
 *
 * 三級串接（-18 dB/oct）。只用一級的話高頻壓不下去，引擎聲上面會浮一層白噪音的「嘶——」，
 * 那個比引擎本身還吵。
 */
function circularNoise(n, cutoff, stages = 3) {
	let buf = new Float64Array(n);
	for (let i = 0; i < n; i++) buf[i] = rnd() * 2 - 1;
	const a = Math.exp((-2 * Math.PI * cutoff) / SR);
	for (let s = 0; s < stages; s++) {
		const out = new Float64Array(n);
		let y = 0;
		for (let lap = 0; lap < 2; lap++) {
			for (let i = 0; i < n; i++) {
				y = buf[i] * (1 - a) + y * a;
				out[i] = y;
			}
		}
		buf = out;
	}
	// 每一級都會掉音量，補回來
	let max = 0;
	for (const v of buf) max = Math.max(max, Math.abs(v));
	for (let i = 0; i < n; i++) buf[i] /= max;
	return buf;
}

function normalize(buf, peak = 0.92) {
	let max = 0;
	for (const v of buf) max = Math.max(max, Math.abs(v));
	const g = peak / max;
	for (let i = 0; i < buf.length; i++) buf[i] *= g;
	return buf;
}

function wav(samples) {
	const n = samples.length;
	const b = Buffer.alloc(44 + n * 2);
	b.write('RIFF', 0); b.writeUInt32LE(36 + n * 2, 4); b.write('WAVE', 8);
	b.write('fmt ', 12); b.writeUInt32LE(16, 16); b.writeUInt16LE(1, 20);
	b.writeUInt16LE(1, 22); b.writeUInt32LE(SR, 24); b.writeUInt32LE(SR * 2, 28);
	b.writeUInt16LE(2, 32); b.writeUInt16LE(16, 34);
	b.write('data', 36); b.writeUInt32LE(n * 2, 40);
	for (let i = 0; i < n; i++) {
		b.writeInt16LE(Math.max(-32767, Math.min(32767, Math.round(samples[i] * 32767))), 44 + i * 2);
	}
	return b;
}

// ------------------------------------------------------------------ 循環引擎聲
//
// 長度必須是整數個點火週期，頭尾才接得起來：50 Hz × 1.0 秒 = 50 次點火。
const F0 = 50;
const LOOP = SR; // 1.0 秒

function engineLoop() {
	const core = buzz(LOOP, () => F0);
	const noise = circularNoise(LOOP, 1500);
	const period = SR / F0;
	// 每個週期的力道略有差異。真的引擎不會兩次爆炸一模一樣，
	// 完全一致聽起來像電子音，而不像機械
	const kick = Array.from({ length: F0 }, () => 0.8 + rnd() * 0.4);

	const out = new Float64Array(LOOP);
	for (let i = 0; i < LOOP; i++) {
		// 換 kick 值的位置挑在兩次點火中間（最安靜的地方）：如果換在脈衝的起點，
		// 那個振幅落差本身就是一聲喀
		const c = Math.floor((((i + period / 2) % LOOP) / period));
		const t = (i % period) / SR;             // 這次點火之後過了多久
		const burst = Math.exp(-t * 26);          // 進氣／機械噪音跟著點火脈動
		out[i] = 0.80 * kick[c] * core[i] + 0.22 * noise[i] * (0.25 + 1.5 * burst);
	}
	// 軟削峰：把尖端壓圓，聽起來才不是刺的
	for (let i = 0; i < LOOP; i++) out[i] = Math.tanh(out[i] * 1.35);
	// 留 3 dB 的空間：Vorbis 編碼會讓峰值稍微超過原始波形，壓到 0 dBFS 會削出雜音
	return normalize(out, 0.72);
}

// ------------------------------------------------------------------ 發動
//
// 起動馬達的「唧——」→ 點火 →「轟」地衝上去 → 回到怠速。
function startSound() {
	const n = Math.round(SR * 1.45);
	const crank = Math.round(SR * 0.42);   // 起動馬達轉的時間
	const out = new Float64Array(n);

	// 點火之後的轉速：先衝到 96 Hz（約 5800 rpm），再掉回怠速的 45 Hz
	const freqAt = (i) => {
		if (i < crank) return 14 + (i / crank) * 10;   // 起動馬達帶著引擎慢慢轉
		const t = (i - crank) / (n - crank);
		return t < 0.28
			? 24 + (96 - 24) * (t / 0.28)
			: 96 - (96 - 45) * ((t - 0.28) / 0.72) ** 0.6;
	};
	const engine = buzz(n, freqAt);
	const noise = circularNoise(n, 2200);

	for (let i = 0; i < n; i++) {
		const t = i / SR;
		if (i < crank) {
			// 起動馬達：金屬的高頻嘯聲，音高隨著轉起來而上升
			const w = 780 + (i / crank) * 420;
			const whine = 0.5 * Math.sin(2 * Math.PI * w * t) + 0.25 * Math.sin(2 * Math.PI * w * 1.5 * t);
			const ramp = Math.min(1, i / (SR * 0.05));
			out[i] = ramp * (0.28 * whine + 0.18 * noise[i]) + 0.35 * engine[i] * ramp;
		} else {
			const t2 = (i - crank) / SR;
			const swell = Math.min(1, t2 / 0.04);
			out[i] = swell * (0.95 * engine[i] + 0.3 * noise[i] * (0.4 + 0.8 * Math.exp(-t2 * 3)));
		}
	}
	// 尾巴淡出，好接上循環的引擎聲
	const fade = Math.round(SR * 0.12);
	for (let i = 0; i < fade; i++) out[n - fade + i] *= 1 - i / fade;
	for (let i = 0; i < n; i++) out[i] = Math.tanh(out[i] * 1.3);
	return normalize(out, 0.7);
}

function emit(name, samples) {
	const tmp = join(tmpdir(), `${name}.wav`);
	writeFileSync(tmp, wav(samples));
	mkdirSync(OUT, { recursive: true });
	execFileSync('ffmpeg', ['-y', '-loglevel', 'error', '-i', tmp,
		'-c:a', 'libvorbis', '-q:a', '3', '-ac', '1', `${OUT}/${name}.ogg`]);
	console.log(`${OUT}/${name}.ogg  (${(samples.length / SR).toFixed(2)}s)`);
}

emit('scooter_engine', engineLoop());
emit('scooter_start', startSound());
