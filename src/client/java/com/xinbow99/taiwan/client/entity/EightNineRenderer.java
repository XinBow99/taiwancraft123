package com.xinbow99.taiwan.client.entity;

import com.xinbow99.taiwan.Taiwan;
import com.xinbow99.taiwan.entity.EightNine;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/**
 * 8+9 的算繪器。
 *
 * <p>六型共用一個模型、一個算繪器與**一張貼圖**。以前每一型各有一張色票貼圖，
 * 那是舊模型（體素堆的方塊人）的版型；巴嘎囧這一版是照真人比例做的，
 * 衣服的圖樣直接畫在貼圖上，換色會把刺青與破洞一起換掉。型的差別現在只在台詞。
 *
 * <p>{@code 0.35f} 是陰影半徑，約等於碰撞箱寬（0.6）的一半——陰影比實體大會讓人看起來浮空。
 */
public class EightNineRenderer extends MobRenderer<EightNine, EightNineRenderState, BargarjungModel> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Taiwan.id("bargarjung"), "main");

    private static final Identifier TEXTURE = Taiwan.id("textures/entity/bargarjung.png");

    public EightNineRenderer(EntityRendererProvider.Context context) {
        super(context, new BargarjungModel(context.bakeLayer(LAYER)), 0.35f);
    }

    @Override
    public EightNineRenderState createRenderState() {
        return new EightNineRenderState();
    }

    @Override
    public void extractRenderState(EightNine entity, EightNineRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.variant = entity.variant();
        state.inCrowd = entity.inCrowd();
        state.smoking = entity.isSmoking();
        // copyFrom 而不是持有參考，理由見 EightNineRenderState
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.greetAnimationState.copyFrom(entity.greetAnimationState);
        state.smokeAnimationState.copyFrom(entity.smokeAnimationState);
    }

    @Override
    public Identifier getTextureLocation(EightNineRenderState state) {
        return TEXTURE;
    }
}
