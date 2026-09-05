package com.xinbow99.taiwan.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xinbow99.taiwan.Taiwan;
import com.xinbow99.taiwan.entity.Scooter;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Axis;

/**
 * 速克達的算繪器。
 *
 * <p>模型的正面是 -Z，而實體的 yaw 0 面向 +Z，所以要轉 180 度——這跟原版的船同一個約定。
 * 少了這一下，車會倒著跑。
 */
public class ScooterRenderer extends EntityRenderer<Scooter, ScooterRenderState> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Taiwan.id("scooter"), "main");

    private static final Identifier TEXTURE = Taiwan.id("textures/entity/scooter.png");

    private final ScooterModel model;

    public ScooterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ScooterModel(context.bakeLayer(LAYER));
        this.shadowRadius = 0.6f;
    }

    @Override
    public ScooterRenderState createRenderState() {
        return new ScooterRenderState();
    }

    @Override
    public void extractRenderState(Scooter entity, ScooterRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        Vec3 motion = entity.getDeltaMovement();
        float horizontal = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        state.wheelSpin = (entity.tickCount + partialTick) * horizontal * 1.6f;
        state.steer = entity.steerAngle();
        // 壓車角度是實體自己算的（把手角度 × 速度），不是這裡從把手角度推的：
        // 高速時把手只打得動 8 度，用「龍頭的一半」去傾，全速過彎會只傾 4 度像在滑冰
        state.lean = entity.leanAngle();
        state.yRot = entity.getYRot();
    }

    @Override
    public void submit(ScooterRenderState state, PoseStack pose,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        // 模型是以 y=24 為地面畫的，往上抬 1.5 格才會站在實體的腳下
        pose.translate(0.0f, 1.5f, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - state.yRot));
        if (state.lean != 0f) pose.mulPose(Axis.ZP.rotationDegrees(state.lean));

        this.model.setupAnim(state);
        // 最後那個 int 是**外框顏色**，不是模型顏色。
        //
        // 這裡本來寫死 -1（＝0xFFFFFFFF，不透明白色），於是每一台機車都被畫上一圈白色描邊，
        // 而描邊是那種會穿過牆壁畫在最上層的東西——整座城的機車在山的另一頭都看得到。
        // 會踩到是因為這個多載的參數表是 (貼圖, 亮度, overlay, 外框, 剝落貼圖)，
        // 中間沒有「顏色」那一格；-1 在別的算繪 API 裡通常代表「不染色」，抄過來就中了。
        // 用 state.outlineColor：平常是 0（不畫），實體真的在發光時才是隊伍顏色
        collector.submitModel(this.model, state, pose, TEXTURE, state.lightCoords,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                state.outlineColor, null);
        pose.popPose();

        super.submit(state, pose, collector, camera);
    }
}
