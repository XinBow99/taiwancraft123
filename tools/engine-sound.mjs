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
 *
 * ---------------------------------------------------------------------------
 * 合成方式：**脈衝 ＋ 共振腔**，不是把一堆正弦波加起來
 * ---------------------------------------------------------------------------
 * 上一版是加法合成（把 180 個諧波依一條音色曲線疊起來）。頻譜量起來對得很準，
 * 但聽起來像電子音——因為那本質上是一台風琴：一堆相位固定的正弦波持續發聲，
 * 沒有「被敲一下然後餘韻慢慢散掉」這件事，而汽缸的渾厚感**就是那個餘韻**。
 *
 * 現在的作法跟真的引擎同構：
 *
 *   1. 每次點火放一個**不對稱的壓力脈衝**（極快上升、指數衰減）；
 *   2. 這個脈衝去敲五個**共振器**（汽缸／排氣管／消音器的共鳴頻率）；
 *   3. 共振器自己震盪、自己衰減——低頻那顆的餘韻比點火週期還長，
 *      所以每一發疊在上一發的尾巴上，那個「疊出來的厚度」就是渾厚感的來源。
 *
 * 頻譜的目標沒有變（見 RESONATORS 上面那段量測結果），變的是「怎麼把能量放進去」：
 * 同樣的八度頻帶分布，用持續的正弦波做出來是風琴，用脈衝敲共振腔做出來是引擎。
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

/**
 * 共振腔。`[頻率, Q, 音量]`
 *
 * 頻率與音量是照參考錄音（勁戰四代＋正蠍排氣管前段）的八度頻帶量出來的
 * （tools/analyse-engine.mjs）：
 *
 *   40-80 Hz −3 dB ｜ 80-160 Hz 0 dB ｜ 160-320 Hz 0 dB ｜ 320-640 Hz −11 dB
 *   640-1280 −8 dB ｜ 1280-2560 −9 dB ｜ 2560-5120 −16 dB ｜ 5120-10240 −14 dB
 *
 * 也就是能量幾乎全在 80~320 Hz，320 Hz 之後一道十幾 dB 的斷崖，中高頻只剩排氣管的沙啞。
 *
 * **Q 值決定的是「渾厚」還是「刺」**：Q 越高餘韻越長。最低那顆 Q=11，餘韻約 39 ms，
 * 比怠速的點火週期（20 ms）還長——所以每一發都疊在上一發的尾巴上，那個重疊就是
 * 汽缸的厚度。Q 調到 3 以下的話餘韻在下一次點火前就散光了，聽起來會變回「噠噠噠」。
 */
const RESONATORS = [
	[92, 11, 0.30],     // 汽缸與排氣管前段的低頻共鳴——「渾厚」就是這一顆
	[155, 9, 1.00],     // 主體（80~160 那一格）
	[250, 7, 3.20],     // 主體（160~320 那一格）
	[430, 4.5, 0.90],   // 斷崖
	[1250, 3.2, 2.40],  // 中頻的沙啞。這些增益是量出來的最佳組合，單獨拉一顆反而會變差——
	//                     normalize 與 tanh 是非線性的，某一段變大會把整體壓下去
	// 2.5 kHz 以上不放共振腔：脈衝的頻譜在那裡已經比低頻低五十幾 dB，
	// 再高的增益也榨不出東西來。那一段改用跟點火同步的亮噪音（見 engineLoop），
	// 那也才是物理上對的——高頻是排氣的紊流，不是腔體的共鳴
];

/**
 * 一次燃燒的壓力脈衝（`t` 是點火之後過了幾秒）。
 *
 * **不能用 delta。**delta 的頻譜是平的，會把每個共振器敲得一樣響，高頻過滿，
 * 聽起來是「喀」不是「砰」。真的排氣脈衝是很快衝起來、再指數衰減的不對稱波形，
 * 那個形狀本身就是一個低通——它決定了哪些共振被敲得比較用力。
 *
 * 0.4 ms 上升、3.5 ms 衰減，是四行程單缸排氣脈衝的量級。
 */
const pulseShape = (t) =>
	// 燃燒：慢的那一段，撐起低頻的厚度
	(1 - Math.exp(-t / 0.0004)) * Math.exp(-t / 0.0035)
	// 機械敲擊（汽門、活塞）：快得多的一小下。**沒有它高頻是空的**——
	// 燃燒那段 3.5 ms 的衰減本身是很強的低通，6 kHz 已經衰減四十幾 dB，
	// 量出來 5~10 kHz 比參考低 15 dB。這一項的衰減快十倍，剛好補那一段
	// 反壓的回彈。**脈衝必須是雙極性的**：全正的脈衝有很大的直流與次低頻含量，
	// 50 Hz 的基頻會壓過所有共振腔（量出來 40~80 Hz 變成整段最強，而參考是 80~320 最強）。
	// 這一項把積分拉回接近 0，低頻才輪得到共振腔去決定
	- 0.30 * (1 - Math.exp(-t / 0.0020)) * Math.exp(-t / 0.0100)
	// 機械敲擊（汽門、活塞）：快得多的一小下。**沒有它高頻是空的**——
	// 燃燒那段的衰減本身是很強的低通，6 kHz 已經衰減四十幾 dB
	+ 0.70 * (1 - Math.exp(-t / 0.00005)) * Math.exp(-t / 0.00016);

/**
 * 點火脈衝列。`freqAt(i)` 是第 i 個取樣的點火頻率（Hz）。
 *
 * 勁戰是四行程單缸，每兩轉點一次火，所以 50 Hz ≒ 6000 rpm。
 *
 * 每一發的力道與時機都有一點差異（`kickAt`）：真的引擎不會兩次爆炸一模一樣，
 * 完全一致聽起來像節拍器。
 */
function fireTrain(n, freqAt, kickAt) {
	const out = new Float64Array(n);
	let ph = 0;
	let cycle = 0;
	for (let i = 0; i < n; i++) {
		const f0 = freqAt(i);
		ph += f0 / SR;
		if (ph >= 1) { ph -= 1; cycle++; }
		out[i] = kickAt(cycle) * pulseShape(ph / f0);
	}
	return out;
}

/**
 * 二階共振器（bandpass）。這是「被敲一下然後餘韻慢慢散掉」的那個東西。
 *
 * <p>**跑好幾圈是為了無縫循環**：共振器在循環結尾還在震，如果起點的狀態是 0，
 * 接縫就會有一聲「啪」。輸入本身是週期性的（整數次點火剛好填滿一個循環），
 * 所以多跑幾圈之後濾波器會收斂到週期解，第二圈之後的輸出頭尾自然接得起來。
 * 高 Q 的那顆收斂比較慢，所以跑四圈。
 */
function resonate(input, freq, q, laps = 4) {
	const n = input.length;
	const w = (2 * Math.PI * freq) / SR;
	const r = Math.exp(-w / (2 * q));
	const a1 = 2 * r * Math.cos(w);
	const a2 = -r * r;
	const out = new Float64Array(n);
	let y1 = 0;
	let y2 = 0;
	for (let lap = 0; lap < laps; lap++) {
		for (let i = 0; i < n; i++) {
			const y = input[i] + a1 * y1 + a2 * y2;
			y2 = y1;
			y1 = y;
			out[i] = y;
		}
	}
	// 共振器在共振點的增益跟 Q 成正比，不除掉的話低頻那顆會蓋掉其他全部
	const gain = (1 - r) * Math.sqrt(1 + r * r - 2 * r * Math.cos(2 * w));
	for (let i = 0; i < n; i++) out[i] *= gain;
	return out;
}

/** 把脈衝列丟進所有共振腔，加起來就是引擎的本體。 */
function combustion(n, freqAt, kickAt) {
	const pulses = fireTrain(n, freqAt, kickAt);
	const out = new Float64Array(n);
	for (const [freq, q, gain] of RESONATORS) {
		const band = resonate(pulses, freq, q);
		for (let i = 0; i < n; i++) out[i] += gain * band[i];
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
const LOOP = SR;

function engineLoop() {
	// 每一發的力道差異。±20%——真的引擎的循環變動大約就是這個量級，
	// 調到 0 聽起來像節拍器，調到 ±50% 會變成快抛錨的樣子
	const kick = Array.from({ length: F0 + 2 }, () => 0.8 + rnd() * 0.4);
	const core = combustion(LOOP, () => F0, (c) => kick[c % F0]);
	const noise = circularNoise(LOOP, 1800);
	// 排氣的紊流噪音。共振腔做不出 2.5 kHz 以上的量，這條才是那一段的來源
	const bright = circularNoise(LOOP, 9000, 2);
	const period = SR / F0;

	const out = new Float64Array(LOOP);
	for (let i = 0; i < LOOP; i++) {
		const t = (i % period) / SR;               // 這次點火之後過了多久
		const burst = Math.exp(-t * 26);            // 進氣／機械噪音跟著點火脈動
		// 傳動與進氣的嘯聲。頻率取整數 Hz、循環長度剛好 1 秒，所以接縫仍然是無縫的
		const whine = 0.04 * Math.sin(2 * Math.PI * 325 * (i / SR))
			+ 0.022 * Math.sin(2 * Math.PI * 650 * (i / SR) + 1.0);
		out[i] = core[i] + 0.16 * noise[i] * (0.25 + 1.5 * burst)
			+ 0.085 * bright[i] * (0.2 + 2.2 * burst)
			+ whine * (0.7 + 0.5 * burst);
	}
	// 軟削峰：把尖端壓圓，聽起來才不是刺的
	for (let i = 0; i < LOOP; i++) out[i] = Math.tanh(out[i] * 1.15);
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

	// 點火之後的轉速：先衝到 96 Hz（約 11500 rpm 的點火頻率，實際是被起動的那一下），
	// 再掉回怠速的 45 Hz
	const freqAt = (i) => {
		if (i < crank) return 14 + (i / crank) * 10;   // 起動馬達帶著引擎慢慢轉
		const t = (i - crank) / (n - crank);
		return t < 0.28
			? 24 + (96 - 24) * (t / 0.28)
			: 96 - (96 - 45) * ((t - 0.28) / 0.72) ** 0.6;
	};
	// 起動馬達在帶的時候還沒點火，力道很小；點著之後才是完整的爆震
	const kicks = Array.from({ length: 400 }, () => 0.75 + rnd() * 0.5);
	const engine = combustion(n, freqAt, (c) => kicks[c % kicks.length]);
	const noise = circularNoise(n, 2200);

	for (let i = 0; i < n; i++) {
		const t = i / SR;
		if (i < crank) {
			// 起動馬達：金屬的高頻嘯聲，音高隨著轉起來而上升
			const w = 780 + (i / crank) * 420;
			const whine = 0.5 * Math.sin(2 * Math.PI * w * t) + 0.25 * Math.sin(2 * Math.PI * w * 1.5 * t);
			const ramp = Math.min(1, i / (SR * 0.05));
			out[i] = ramp * (0.28 * whine + 0.18 * noise[i]) + 0.30 * engine[i] * ramp;
		} else {
			const t2 = (i - crank) / SR;
			const swell = Math.min(1, t2 / 0.04);
			out[i] = swell * (engine[i] + 0.22 * noise[i] * (0.4 + 0.8 * Math.exp(-t2 * 3)));
		}
	}
	// 尾巴淡出，好接上循環的引擎聲
	const fade = Math.round(SR * 0.12);
	for (let i = 0; i < fade; i++) out[n - fade + i] *= 1 - i / fade;
	for (let i = 0; i < n; i++) out[i] = Math.tanh(out[i] * 1.15);
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
