package com.xinbow99.taiwan.client.entity;

import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * 8+9（巴嘎囧）的模型。
 *
 * <h2>幾何與動作都是烘出來的</h2>
 * <p>零件樹在 {@link BargarjungGeometry}、四段動作在 {@link BargarjungAnimations}，
 * 兩份都由 {@code tools/bake-bargarjung.mjs} 從 {@code models/bargarjung.bbmodel} 產生。
 * 這個檔案只負責**什麼時候播哪一段**，不放幾何——幾何改了要重烘，手改會被蓋掉。
 *
 * <h2>四段動作的分工</h2>
 * <ul>
 *   <li>{@code walk}：靠 {@code walkAnimationPos} 驅動，不是靠時間。站著不動的時候
 *       權重自然是 0，所以它跟 {@code idle} 可以同時掛著、不用互斥。
 *   <li>{@code idle}：站著時的呼吸與東張西望。
 *   <li>{@code greet}：遇到人時的點頭招呼，一次性。
 *   <li>{@code smoke}：抽菸，循環。**只有這一段菸才看得見**——菸不是永遠掛在手上的。
 * </ul>
 *
 * <h2>疊加而不是取代</h2>
 * <p>{@code KeyframeAnimation.apply} 用的是 {@code offsetRotation}／{@code offsetPos}：
 * 每一段都是疊在靜止姿勢上的**偏移**。所以頭部朝向要在播動作**之前**設好，
 * 招呼的點頭才會疊在玩家的視線方向上，而不是把視線蓋掉。
 */
public class BargarjungModel extends EntityModel<EightNineRenderState> {

    /**
     * 走路動畫的時間縮放。{@code applyWalk} 把時間算成 {@code pos × 50 × 這個值}（毫秒），
     * 而動作長 1.2 秒，所以一個完整步伐是 10 個 {@code walkAnimationPos} 單位——
     * 跟原版人形的 9.4 差不多，換句話說腳步跟位移是對得上的。
     */
    private static final float WALK_TIME = 2.4f;

    /**
     * 走路權重的斜率。{@code speed × 這個值} 夾到 1，所以時速到三成就已經是滿幅。
     * 慢慢晃跟快步走的差別交給動作本身，不要靠縮幅度做——縮幅度看起來像沒力氣。
     */
    private static final float WALK_GAIN = 2.5f;

    /** 成團時走路的幅度倍率。人多的時候走得更大搖大擺，這是這個族群的核心機制。 */
    private static final float CROWD_SWAGGER = 1.35f;

    private final ModelPart head;
    private final ModelPart cigarette;

    private final KeyframeAnimation idle;
    private final KeyframeAnimation walk;
    private final KeyframeAnimation greet;
    private final KeyframeAnimation smoke;

    public BargarjungModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        ModelPart body = root.getChild("bargarjung");
        this.head = body.getChild("head");
        this.cigarette = body.getChild("right_arm").getChild("right_forearm").getChild("cigarette");
        this.idle = BargarjungAnimations.IDLE.bake(root);
        this.walk = BargarjungAnimations.WALK.bake(root);
        this.greet = BargarjungAnimations.GREET.bake(root);
        this.smoke = BargarjungAnimations.SMOKE.bake(root);
    }

    public static net.minecraft.client.model.geom.builders.LayerDefinition createBodyLayer() {
        return BargarjungGeometry.createBodyLayer();
    }

    @Override
    public void setupAnim(EightNineRenderState state) {
        super.setupAnim(state);

        // 沒在抽的時候手上不該有一根菸。這是用 visible 而不是動畫裡的 scale 通道做的：
        // 動畫沒在播的時候 resetPose 會把 scale 還原成 1，菸就會憑空出現
        this.cigarette.visible = state.smoking;

        // 頭部朝向要在動作之前——動作是疊加的偏移，招呼的點頭要疊在視線上
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

        float swagger = state.inCrowd ? CROWD_SWAGGER : 1.0f;
        this.walk.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed,
                WALK_TIME, WALK_GAIN * swagger);
        this.idle.apply(state.idleAnimationState, state.ageInTicks);
        this.smoke.apply(state.smokeAnimationState, state.ageInTicks);
        // 招呼放最後：它動到的是頭與右手，要蓋在 idle 與 smoke 的同一批骨骼上面
        this.greet.apply(state.greetAnimationState, state.ageInTicks);
    }
}
