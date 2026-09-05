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
 * <p>六型共用一個模型與一個算繪器，差別只在貼圖（{@code EightNineVariant.texture()}）
 * 與配件的開關（在模型的 {@code setupAnim} 裡）。開六個算繪器等於把同一份幾何烘六次。
 *
 * <p>{@code 0.35f} 是陰影半徑，約等於碰撞箱寬（0.6）的一半——陰影比實體大會讓人看起來浮空。
 */
public class EightNineRenderer extends MobRenderer<EightNine, EightNineRenderState, EightNineModel> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Taiwan.id("eightnine"), "main");

    public EightNineRenderer(EntityRendererProvider.Context context) {
        super(context, new EightNineModel(context.bakeLayer(LAYER)), 0.35f);
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
    }

    @Override
    public Identifier getTextureLocation(EightNineRenderState state) {
        return state.variant.texture();
    }
}
