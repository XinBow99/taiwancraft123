package com.xinbow99.taiwan.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionf;

/**
 * 把搶到的東西畫在獼猴懷裡。
 *
 * <p>在這之前，{@code carrying} 只改姿勢、贓物本身是看不見的——玩家看到的是一隻猴子抱著空氣
 * 跑掉。**東西畫出來，那一下「欸？我的麵包」才成立。**
 *
 * <h2>跟著骨架走，不要抄座標</h2>
 * <p>位置是用 {@link ModelPart#translateAndRotate} 從軀幹一路走到前肢算出來的，不是寫死的
 * 常數。抄座標的話，{@code setupAnim} 一改抱東西的角度，贓物就會飄在半空——而那種錯位
 * 沒有任何編譯錯誤，只能靠眼睛發現。
 *
 * <h2>只跟手的位置，不跟手的角度</h2>
 * <p>走到掌心之後把前肢的旋轉**轉回來**（{@code invert()}）。不轉回來的話贓物會跟著手臂的
 * 擺動一起翻跟斗；轉回來之後它維持正面朝前，看起來才像被抱著而不是被甩著。
 */
public class MacaqueLootLayer extends RenderLayer<MacaqueRenderState, MacaqueModel> {

    /**
     * 贓物縮到 0.65。
     *
     * <p>整隻猴子已經被 {@code MacaqueRenderer.SCALE} 縮到 0.75 了，物品在那之上還要再小一點：
     * 原尺寸的方塊抱在懷裡會比牠的頭還大，讀起來像牠扛著一箱貨。
     */
    private static final float ITEM_SCALE = 0.65f;

    /** 掌心在前肢局部座標的 y。手掌那塊方塊是 y=7~9，取中間。 */
    private static final float HAND_Y = 8.0f / 16.0f;

    public MacaqueLootLayer(RenderLayerParent<MacaqueRenderState, MacaqueModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       MacaqueRenderState state, float yRot, float xRot) {
        // 空手就整個跳過。用 heldItem 而不是 state.carrying：能不能畫得出來由物品本身決定
        if (state.heldItem.isEmpty()) return;

        MacaqueModel model = this.getParentModel();
        ModelPart arm = model.carryingArm();

        poseStack.pushPose();

        model.bodyPart().translateAndRotate(poseStack);
        arm.translateAndRotate(poseStack);
        // 沿著手臂往下移到掌心。這一步必須在「轉回角度」之前——位移是在手臂的座標系裡量的
        poseStack.translate(0.0f, HAND_Y, 0.0f);

        // translateAndRotate 用的是 rotationZYX，所以反轉整個四元數就好，
        // 不用自己去湊三個軸的順序
        poseStack.mulPose(new Quaternionf().rotationZYX(arm.zRot, arm.yRot, arm.xRot).invert());

        // 稍微往身體內側靠，不然物品的一半會穿出前肢外側
        poseStack.translate(0.0f, 0.0f, -0.08f);
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        // GROUND 情境的物品是平躺的（法線朝上），轉 90 度立起來，正面才朝著玩家
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));

        state.heldItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);

        poseStack.popPose();
    }
}
