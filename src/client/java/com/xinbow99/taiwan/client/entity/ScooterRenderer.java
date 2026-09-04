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
        // 轉彎壓車：龍頭轉多少就傾一半。機車過彎是靠傾的，不傾會像在滑冰
        state.lean = -entity.steerAngle() * 0.5f;
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
        collector.submitModel(this.model, state, pose, TEXTURE, state.lightCoords,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, -1, null);
        pose.popPose();

        super.submit(state, pose, collector, camera);
    }
}
