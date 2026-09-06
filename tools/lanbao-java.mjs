/**
 * 把 models/lanbao.bbmodel 轉成遊戲端的 LanbaoModel.java 與 lanbao.png。
 *
 * ## 座標怎麼換
 *
 * Blockbench 這邊是 y 朝上、地面在 y=0；原版實體模型是 **y 朝下、地面在 y=24**
 * （算繪器再 `translate(0,1.5,0)` 把它擺回實體腳下）。所以
 *
 *     mcX = bbX * S            mcZ = bbZ * S
 *     mcY(方塊的 from) = 24 - bbTo.y * S      高度 = bbSize.y * S
 *
 * `S = 0.81` 是把 94.8 單位的車長收成 76.8（＝4.8 格＝真車 4.8 公尺）。
 * 這個係數不是湊出來的：換算完軸距 41.3 單位、車寬 34.5、車高 14.6，跟舊模型
 * （42.5 / 34.8 / 15.6）幾乎重疊，所以 VehicleModel.LANBAO 的 2.2 × 1.3 碰撞箱
 * 與兩個座位偏移都不用動。
 *
 * ## 為什麼不是一顆方塊一個 ModelPart
 *
 * 舊檔案 599 顆方塊就是 599 個 `addOrReplaceChild`。零件樹只有在需要**各自旋轉**時
 * 才有意義——這台車會動的只有底盤與四顆輪子，所以只開五個零件，其餘方塊用
 * `CubeListBuilder` 串在一起。少 594 個 ModelPart 等於每一幀少走 594 次矩陣。
 *
 * 代價是 `createBodyLayer` 會塞爆一個方法（JVM 單一方法上限 64KB 位元組碼），
 * 所以每個零件的方塊再切成幾個 static helper。
 *
 * ## 貼圖
 *
 * bbmodel 把貼圖存成 data URL，直接解出來寫成 PNG。UV 是 box UV，每顆方塊的
 * `uv_offset` 指到 16px 高的純色帶，所以 `texOffs` 原樣抄過去就好。
 *
 * 用法：node tools/lanbao-java.mjs
 */
import { readFileSync, writeFileSync } from 'node:fs';

// 幾何烘進 Java 用的縮放。**不要為了放大車而調這個**：box UV 的展開圖高度是「深+高」，
// 最大那顆輪胎片已經 14.2，再乘上去就會超出 16px 的色帶、取樣到隔壁顏色。
const BAKE = 0.81;
// 畫面上要多大。1.60 → 車高 28.8 單位 = 1.8 格，跟玩家一樣高。
const TARGET = 1.60;
// 放大改用 ModelPart 的縮放，包在一個樞紐在地面（y=24）的 scale 零件上，
// 車才不會浮起來或陷進地板，而且貼圖與動畫都不用重做。
const VIEW = TARGET / BAKE;
const GROUND = 24;
const CHUNK = 120;                 // 每個 helper 方法放幾顆方塊

const SRC = 'models/lanbao.bbmodel';
const OUT_JAVA = 'src/client/java/com/xinbow99/taiwan/client/entity/LanbaoModel.java';
const OUT_PNG = 'src/main/resources/assets/taiwan/textures/entity/lanbao.png';

const bb = JSON.parse(readFileSync(SRC, 'utf8'));

// ---- 貼圖 ------------------------------------------------------------------
const tex = bb.textures[0];
const b64 = (tex.source || '').replace(/^data:image\/png;base64,/, '');
if (!b64) throw new Error('貼圖不是 data URL，無法取出');
writeFileSync(OUT_PNG, Buffer.from(b64, 'base64'));

// ---- 分組 --------------------------------------------------------------------
//
// 刻意**不讀 bbmodel 的 outliner**。Blockbench 的專案 codec 走的是 compile 的 undo 變體，
// 群組節點只留 {uuid, isOpen, children}——name 與 origin 會掉。也就是說只要在 Blockbench
// 裡按一次存檔，這裡就找不到「chassis」這個群組了（實際踩過）。
//
// 改成用方塊名稱分組、樞紐寫死成常數。這幾個數字跟 tools/lanbao-build.js 裡的
// FA / RA / CY 是同一組，改那邊就要改這邊。
const FA = -26, RA = 25, CY = 6.6, TRACK = 16;
const PIVOT = {
  chassis:  [0, CY, RA],          // 起步抬頭繞後軸轉
  wheel_fl: [-TRACK, CY, FA],
  wheel_fr: [TRACK, CY, FA],
  wheel_rl: [-TRACK, CY, RA],
  wheel_rr: [TRACK, CY, RA],
};

const PARTS = Object.keys(PIVOT).map(name => ({ name, origin: PIVOT[name], cubes: [] }));
const byName = new Map(PARTS.map(p => [p.name, p]));
for (const e of bb.elements) {
  const wheel = PARTS.find(p => p.name.startsWith('wheel_') && e.name.startsWith(p.name));
  (wheel || byName.get('chassis')).cubes.push(e);
}

const total = PARTS.reduce((n, p) => n + p.cubes.length, 0);
if (total !== bb.elements.length) {
  throw new Error(`方塊漏了：分到 ${total}，檔案裡有 ${bb.elements.length}`);
}
for (const p of PARTS) if (!p.cubes.length) throw new Error(`零件 ${p.name} 一顆方塊都沒有`);

const f = v => (Math.round(v * 1e4) / 1e4).toFixed(4) + 'f';

// 方塊座標換算成「相對於所屬零件樞紐」的原版座標
function boxOf(e, pivot) {
  const px = pivot[0] * BAKE, py = GROUND - pivot[1] * BAKE, pz = pivot[2] * BAKE;
  const x = e.from[0] * BAKE - px;
  const y = (GROUND - e.to[1] * BAKE) - py;
  const z = e.from[2] * BAKE - pz;
  const w = (e.to[0] - e.from[0]) * BAKE;
  const h = (e.to[1] - e.from[1]) * BAKE;
  const d = (e.to[2] - e.from[2]) * BAKE;
  const [u, v] = e.uv_offset || [0, 0];
  // box UV 的展開圖是 2*(寬+深) x (深+高)，超出 16px 高的色帶就會吃到別的顏色
  if (d + h > 16) throw new Error(`${e.name} 的 UV 展開圖太高：${(d + h).toFixed(2)}`);
  return `                .texOffs(${u}, ${v}).addBox(${f(x)}, ${f(y)}, ${f(z)}, ${f(w)}, ${f(h)}, ${f(d)})`;
}

const helpers = [];
const partBuild = [];
for (const p of PARTS) {
  const camel = p.name.replace(/_(.)/g, (_, c) => c.toUpperCase());
  const chunks = [];
  for (let i = 0; i < p.cubes.length; i += CHUNK) chunks.push(p.cubes.slice(i, i + CHUNK));
  chunks.forEach((chunk, k) => {
    helpers.push(
      `    private static CubeListBuilder ${camel}${k}(CubeListBuilder b) {\n` +
      `        return b\n${chunk.map(e => boxOf(e, p.origin)).join('\n')};\n    }`);
  });
  const call = chunks.map((_, k) => `${camel}${k}(`).join('') +
    'CubeListBuilder.create()' + ')'.repeat(chunks.length);
  // 座標相對於 scale 零件（它的樞紐就在地面 y=GROUND），所以 y 要再扣掉 GROUND
  const px = p.origin[0] * BAKE, py = GROUND - p.origin[1] * BAKE, pz = p.origin[2] * BAKE;
  partBuild.push(
    `        scale.addOrReplaceChild("${p.name}",\n` +
    `                ${call},\n` +
    `                PartPose.offset(${f(px)}, ${f(py - GROUND)}, ${f(pz)}));`);
}

const java = `package com.xinbow99.taiwan.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * 藍爆堅尼的模型。**這個檔案是產生出來的，不要手改**——改 {@code tools/lanbao-build.js}
 * 的造型參數，在 Blockbench 重建，再跑 {@code node tools/lanbao-java.mjs}。
 *
 * <h2>名字</h2>
 * <p>照這個專案招牌店名的同一套規矩——**同音替字**（見 {@code ShopName}）：保留語感節奏、
 * 換掉字，讀者認得出是哪一類，但不是任何一個真實商標。
 *
 * <h2>幾何從哪來</h2>
 * <p>不是體素化真車，也不是照剪影猜的，是**先雕一個曲面再取樣**：沿車長排一張站點表，
 * 每一站給「中線頂 / 葉子板頂 / 外緣頂 / 車底 / 斷面寬度曲線」五條線，組成一個隱函數
 * {@code solid(x,y,z)}；逐格取樣後沿 x 切成同材質的 run，再沿 z 合併成長方體。
 *
 * <p>幾個刻意的決定：
 * <ul>
 *   <li>頂面是**單一 smoothstep**，中央不下凹——中央凹一格在體素化後會變成一條溝，
 *       而 Aventador EVO 的引擎蓋是一道連續的弧；
 *   <li>斷面下段用 sin^1.5，側裙才會內收而不是踩著一塊踏板；肩線最寬並往上內收 0.7，
 *       量化之後沿著側面留下一道折線，那是車身的張力線；
 *   <li>車頭寬度從前輪拱一路撐到頭燈，只在最前 8 格收——太早收會變成獨木舟；
 *   <li>輪拱是**正圓**（半徑 7.0 對胎半徑 6.6），輪盤每一圈的弦長用該圈中心算。
 *       用外緣算的話上下兩端收太快，輪子會變成蛋形；
 *   <li>Y 字頭尾燈不是外掛方塊，是把最外兩格的材質換掉，燈嵌在鈑金裡。
 * </ul>
 *
 * <h2>比例與大小</h2>
 * <p>長:寬:高 = 10 : 4.49 : 1.90，車艙佔車長 28%。Blockbench 那邊是 94.8 x 42.6 x 18 單位。
 *
 * <p>大小分兩段做，這是有理由的。座標**烘進 Java 時只乘 ${BAKE}**：box UV 的展開圖高度是
 * 「深 + 高」，最大那顆輪胎片已經 14.2，乘上去就會超出貼圖裡 16px 高的純色帶、
 * 取樣到隔壁的顏色。真正的放大交給 {@link #VIEW_SCALE} 這個 {@code ModelPart} 縮放，
 * 掛在一個樞紐**就在地面**（模型空間 y=${GROUND}）的 {@code scale} 零件上——樞紐不在地面的話，
 * 放大會讓車浮起來或陷進地板。
 *
 * <p>兩段相乘 = ${TARGET}，畫面上是 151.7 x 68.2 x 28.8 單位，也就是 9.5 x 4.3 x 1.8 格：
 * 車頂跟玩家一樣高。
 *
 * <h2>為什麼只有五個零件</h2>
 * <p>${bb.elements.length} 顆方塊只開五個 {@code ModelPart}。零件樹只有在需要各自旋轉時才有意義，
 * 這台車會動的就是底盤與四顆輪子；其餘方塊串在同一個 {@code CubeListBuilder} 上。
 * 舊版是一顆方塊一個零件，等於每一幀多走五百多次矩陣。
 */
public class LanbaoModel extends EntityModel<VehicleRenderState> {

    /** 前輪的最大轉角（度）。比機車小很多——汽車的方向盤打得再滿，輪子也只轉這麼多。 */
    private static final float STEER_MAX = 30.0f;

    /** 起步時車頭最多抬幾度。 */
    private static final float SQUAT_MAX = 3.2f;

    /** 畫面上的放大倍率。見類別說明——放大不能烘進座標，會撐爆 box UV 的色帶。 */
    private static final float VIEW_SCALE = ${VIEW.toFixed(4)}f;

    private final ModelPart scale;
    private final ModelPart chassis;
    private final ModelPart wheelFl;
    private final ModelPart wheelFr;
    private final ModelPart wheelRl;
    private final ModelPart wheelRr;

    /** 底盤的靜止位置。怠速微震是疊在這上面的，不能每一幀從當下的值再加。 */
    private final float chassisY;

    public LanbaoModel(ModelPart root) {
        // 理由同機車：預設的 cutout-no-cull 會讓車殼內外側一起畫、互相穿插，
        // 看起來像整台車是半透明的
        super(root, RenderTypes::entitySolid);
        this.scale = root.getChild("scale");
        this.chassis = this.scale.getChild("chassis");
        this.wheelFl = this.scale.getChild("wheel_fl");
        this.wheelFr = this.scale.getChild("wheel_fr");
        this.wheelRl = this.scale.getChild("wheel_rl");
        this.wheelRr = this.scale.getChild("wheel_rr");
        this.chassisY = this.chassis.y;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 放大用的空零件。樞紐擺在地面，縮放才不會把車抬離地面
        PartDefinition scale = root.addOrReplaceChild("scale",
                CubeListBuilder.create(), PartPose.offset(0.0000f, ${GROUND}.0000f, 0.0000f));

${partBuild.join('\n\n')}

        return LayerDefinition.create(mesh, ${bb.resolution.width}, ${bb.resolution.height});
    }

${helpers.join('\n\n')}

    @Override
    public void setupAnim(VehicleRenderState state) {
        super.setupAnim(state);

        // 每一幀重設：super.setupAnim() 會把零件還原成 initialPose，縮放只在建構子裡設會被吃掉
        this.scale.xScale = VIEW_SCALE;
        this.scale.yScale = VIEW_SCALE;
        this.scale.zScale = VIEW_SCALE;

        this.wheelFl.xRot = state.wheelSpin;
        this.wheelFr.xRot = state.wheelSpin;
        this.wheelRl.xRot = state.wheelSpin;
        this.wheelRr.xRot = state.wheelSpin;

        // 只有前輪轉向。xRot 跟 yRot 同時給一個零件是安全的：ModelPart 的旋轉是
        // rotationZYX，**X 先套、Y 後套**，正好就是「輪子先自轉、再整顆偏過去」的順序。
        // 反過來的話輪子會繞著一根歪掉的軸滾。
        float steer = Mth.clamp(state.steer, -STEER_MAX, STEER_MAX) * Mth.DEG_TO_RAD;
        this.wheelFl.yRot = steer;
        this.wheelFr.yRot = steer;

        // 怠速微震。只有簧上質量在抖——輪子不是 chassis 的子零件，所以四顆輪胎
        // 會乖乖踩在地上，不會跟著車身一起跳。
        //
        // 兩個頻率相加而不是單一正弦：單一正弦看起來像在呼吸，V12 怠速是不規則的。
        // 速度一起來就收掉（車在跑的時候，路面震動比引擎震動大得多，那是另一回事）。
        float idle = 1.0f - Mth.clamp(state.speedKmh / 6.0f, 0.0f, 1.0f);
        float t = state.ageInTicks;
        float shake = Mth.sin(t * 2.7f) * 0.6f + Mth.sin(t * 4.3f) * 0.4f;
        this.chassis.y = this.chassisY + idle * 0.16f * shake;
        this.chassis.zRot = idle * 0.0016f * Mth.cos(t * 3.1f);

        // 起步抬頭。樞紐在**後軸**（見 tools/lanbao-build.js 的 chassis 群組），
        // 所以這一轉是車尾下沉、車頭翹起來，不是整台繞著中心翻。
        //
        // 負號：這個模型空間的 +Y 是朝下的（地面在 y=24），繞 +X 轉會把 -Z 的車頭
        // 往「y 變大」＝往下帶。要抬頭就得給負的。
        float squat = Mth.clamp(state.accel, -1.0f, 1.0f) * SQUAT_MAX;
        this.chassis.xRot = -squat * Mth.DEG_TO_RAD + idle * 0.0012f * Mth.sin(t * 2.7f);
    }
}
`;

writeFileSync(OUT_JAVA, java);
console.log(`寫出 ${OUT_JAVA}`);
console.log(`  方塊 ${bb.elements.length}、零件 ${PARTS.length}、helper ${helpers.length}`);
console.log(`  各零件方塊數：${PARTS.map(p => p.name + '=' + p.cubes.length).join(', ')}`);
console.log(`寫出 ${OUT_PNG}（${bb.resolution.width}x${bb.resolution.height}）`);
