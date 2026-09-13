/**
 * 把 models/bargarjung.bbmodel 烘成 Java：零件樹 ＋ 四段動作。
 *
 * ## 座標系換算
 * Blockbench 的 modded_entity 專案是**Y 朝上、地面在 0**；ModelPart 是**Y 朝下、地面在 24**。
 * 兩者之間差一個鏡射 M = diag(1, −1, 1)，所以：
 *
 * - 位置：mcY = 24 − bbY（樞紐取絕對值後再減去父骨骼的樞紐，變成相對位移）
 * - 方塊角落：y = 父樞紐的 bbY − 方塊的 to[1]（先翻面，min 角落換成原本的 max 角落）
 * - 旋轉：鏡射會把手性翻過來，M·Rz(c)Ry(b)Rx(a)·M = Rz(−c)Ry(b)Rx(−a)。
 *   也就是 **x 與 z 變號、y 不變**。合成順序 ZYX 兩邊一致（Blockbench 的
 *   THREE.Euler 用 'ZYX'，ModelPart 用 rotationZYX），所以只要逐軸變號就好。
 * - 位移動畫：{@code KeyframeAnimations.posVec} 自己會把 y 變號，所以照抄 Blockbench 的值。
 *
 * 用法：node tools/bake-bargarjung.mjs
 */
import { readFileSync, writeFileSync, copyFileSync } from 'node:fs';

const SRC_MODEL = 'models/bargarjung.bbmodel';
const SRC_PNG = 'models/bargarjung.png';
const OUT_PNG = 'src/main/resources/assets/taiwan/textures/entity/bargarjung.png';
const OUT_DIR = 'src/client/java/com/xinbow99/taiwan/client/entity';
const PACKAGE = 'com.xinbow99.taiwan.client.entity';

const src = JSON.parse(readFileSync(SRC_MODEL, 'utf8'));
const TEX_W = src.resolution.width;
const TEX_H = src.resolution.height;

const groups = Object.fromEntries((src.groups || []).map(g => [g.uuid, g]));
const elements = Object.fromEntries((src.elements || []).map(e => [e.uuid, e]));

/** 讓 0.30000000000000004 這種東西不要進到產生出來的原始碼裡。 */
const f = v => {
  const n = Math.round(Number(v) * 1e5) / 1e5;
  return `${Number.isInteger(n) ? n.toFixed(1) : String(n)}F`;
};

// ---- 零件樹 ---------------------------------------------------------------

const lines = [];
const varOf = name => name.replace(/[^A-Za-z0-9_]/g, '_');

function emitGroup(node, parentVar, parentOrigin) {
  const g = groups[node.uuid];
  const cubes = (node.children || []).filter(c => typeof c === 'string').map(c => elements[c]);
  const name = g.name;
  const v = varOf(name);

  const builder = cubes.length === 0
    ? ['CubeListBuilder.create()']
    : ['CubeListBuilder.create()'].concat(cubes.map(el => {
        const [ox, oy, oz] = g.origin;
        const x = el.from[0] - ox;
        // 翻面之後方塊的「最小角」是原本的「最大角」
        const y = oy - el.to[1];
        const z = el.from[2] - oz;
        const w = el.to[0] - el.from[0];
        const h = el.to[1] - el.from[1];
        const d = el.to[2] - el.from[2];
        const grow = el.inflate ? `, new CubeDeformation(${f(el.inflate)})` : '';
        return `        .texOffs(${el.uv_offset[0]}, ${el.uv_offset[1]})`
             + `.addBox(${f(x)}, ${f(y)}, ${f(z)}, ${f(w)}, ${f(h)}, ${f(d)}${grow})`;
      }));

  // 樞紐是相對於父骨骼的。Y 已經翻面，所以是「父的 bbY 減自己的 bbY」
  const pose = `PartPose.offset(${f(g.origin[0] - parentOrigin[0])}, `
             + `${f(parentOrigin[1] - g.origin[1])}, ${f(g.origin[2] - parentOrigin[2])})`;

  lines.push(`        PartDefinition ${v} = ${parentVar}.addOrReplaceChild("${name}",`);
  lines.push(`                ${builder.join('\n')},`);
  lines.push(`                ${pose});`);
  lines.push('');

  for (const child of node.children || []) {
    if (typeof child !== 'string') emitGroup(child, v, g.origin);
  }
}

// 根骨骼的父樞紐是 ModelPart 的原點，也就是 bb 的 y=24（實體眼前那一格的高度慣例）
for (const node of src.outliner) emitGroup(node, 'root', [0, 24, 0]);

const geometry = `package ${PACKAGE};

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 巴嘎囧的零件樹。
 *
 * <p><b>這個檔案是產生出來的，不要手改。</b>改 {@code models/bargarjung.bbmodel}
 * 之後跑 {@code node tools/bake-bargarjung.mjs} 重出一份。
 */
public final class BargarjungGeometry {

    private BargarjungGeometry() {
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

${lines.join('\n')}        return LayerDefinition.create(mesh, ${TEX_W}, ${TEX_H});
    }
}
`;

writeFileSync(`${OUT_DIR}/BargarjungGeometry.java`, geometry);

// ---- 動作 -----------------------------------------------------------------

const INTERP = { linear: 'LINEAR', catmullrom: 'CATMULLROM' };
const num = v => {
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
};

/** 一個通道在 Java 端長什麼樣。旋轉要逐軸變號（見檔頭），位移交給 posVec 自己翻。 */
const CHANNELS = {
  rotation: { target: 'ROTATION', fn: 'degreeVec', map: p => [-num(p.x), num(p.y), -num(p.z)] },
  position: { target: 'POSITION', fn: 'posVec', map: p => [num(p.x), num(p.y), num(p.z)] },
};

const animLines = [];
const names = [];

for (const animation of src.animations || []) {
  const shortName = animation.name.replace(/^animation\./, '');
  const constant = shortName.toUpperCase();
  names.push({ constant, shortName });

  const body = [];
  for (const animator of Object.values(animation.animators || {})) {
    const byChannel = new Map();
    for (const frame of animator.keyframes || []) {
      if (!CHANNELS[frame.channel]) continue;   // scale 用不到：菸靠 visible 開關
      if (!byChannel.has(frame.channel)) byChannel.set(frame.channel, []);
      byChannel.get(frame.channel).push(frame);
    }
    for (const [channel, frames] of byChannel) {
      frames.sort((a, b) => a.time - b.time);
      const spec = CHANNELS[channel];
      body.push(`                .addAnimation("${animator.name}", new AnimationChannel(AnimationChannel.Targets.${spec.target},`);
      const keys = frames.map(frame => {
        const [x, y, z] = spec.map(frame.data_points[0] || {});
        const interp = INTERP[frame.interpolation] || 'LINEAR';
        return `                        new Keyframe(${f(frame.time)}, KeyframeAnimations.${spec.fn}(${f(x)}, ${f(y)}, ${f(z)}),`
             + `\n                                AnimationChannel.Interpolations.${interp})`;
      });
      body.push(keys.join(',\n') + '))');
    }
  }

  animLines.push(`    /** {@code ${animation.name}}，${animation.length} 秒${animation.loop === 'loop' ? '、循環' : ''}。 */`);
  animLines.push(`    public static final AnimationDefinition ${constant} =`);
  animLines.push(`            AnimationDefinition.Builder.withLength(${f(animation.length)})`);
  if (animation.loop === 'loop') animLines.push('                    .looping()');
  animLines.push(body.join('\n'));
  animLines.push('                .build();');
  animLines.push('');
}

const anims = `package ${PACKAGE};

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * 巴嘎囧的四段動作：站、走、打招呼、抽菸。
 *
 * <p><b>這個檔案是產生出來的，不要手改。</b>在 Blockbench 裡調完
 * {@code models/bargarjung.bbmodel} 的時間軸之後跑 {@code node tools/bake-bargarjung.mjs}。
 *
 * <p>旋轉的 x、z 在烘的時候變過號了——Blockbench 的 Y 朝上，ModelPart 的 Y 朝下，
 * 兩個座標系差一個鏡射，鏡射會把繞 X 與繞 Z 的轉向翻過來。繞 Y 的不用動。
 */
public final class BargarjungAnimations {

    private BargarjungAnimations() {
    }

${animLines.join('\n')}}
`;

writeFileSync(`${OUT_DIR}/BargarjungAnimations.java`, anims);
copyFileSync(SRC_PNG, OUT_PNG);

console.log(`零件樹 → ${OUT_DIR}/BargarjungGeometry.java`);
console.log(`動作   → ${OUT_DIR}/BargarjungAnimations.java（${names.map(n => n.shortName).join('、')}）`);
console.log(`貼圖   → ${OUT_PNG}`);
