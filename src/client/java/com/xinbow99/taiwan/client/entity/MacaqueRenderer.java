package com.xinbow99.taiwan.client.entity;

import com.xinbow99.taiwan.Taiwan;
import com.xinbow99.taiwan.entity.Macaque;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/**
 * 獼猴的算繪器。
 *
 * <p>{@code 0.4f} 是陰影半徑，跟碰撞箱寬（0.7）的一半差不多——陰影比實體大會讓牠看起來浮空。
 */
public class MacaqueRenderer extends MobRenderer<Macaque, MacaqueRenderState, MacaqueModel> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Taiwan.id("macaque"), "main");

    private static final Identifier TEXTURE = Taiwan.id("textures/entity/macaque.png");

    public MacaqueRenderer(EntityRendererProvider.Context context) {
        super(context, new MacaqueModel(context.bakeLayer(LAYER)), 0.4f);
    }

    @Override
    public MacaqueRenderState createRenderState() {
        return new MacaqueRenderState();
    }

    @Override
    public void extractRenderState(Macaque entity, MacaqueRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.carrying = entity.hasLoot();
    }

    @Override
    public Identifier getTextureLocation(MacaqueRenderState state) {
        return TEXTURE;
    }
}
