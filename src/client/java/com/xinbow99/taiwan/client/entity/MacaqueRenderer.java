package com.xinbow99.taiwan.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xinbow99.taiwan.Taiwan;
import com.xinbow99.taiwan.entity.Macaque;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 獼猴的算繪器。
 *
 * <p>{@code 0.3f} 是陰影半徑，跟縮放後的實際寬度差不多——陰影比實體大會讓牠看起來浮空。
 */
public class MacaqueRenderer extends MobRenderer<Macaque, MacaqueRenderState, MacaqueModel> {

    /**
     * 模型縮放。
     *
     * <p>{@link MacaqueModel} 是照 {@code models/monkey.bbmodel} 一比一搬過來的，站起來
     * 有 19 格高＝1.19 個方塊，但碰撞箱是 {@code sized(0.7f, 0.9f)}＝14.4 格。差這麼多的話
     * 頭會整顆穿出碰撞箱：玩家瞄頭打不到、牠也會把臉埋進上方的方塊裡。
     *
     * <p>選擇縮模型而不是放大碰撞箱，是因為碰撞箱牽動尋路、擠壓與生成判定——那是行為，
     * 不該為了外觀改。{@code 14.25 / 19 ≈ 0.75}，縮完剛好塞在 0.9 底下。
     *
     * <p>縮放是繞著實體原點（腳底）做的，所以腳還是踩在地上，不用另外補位移。
     */
    private static final float SCALE = 0.75f;

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Taiwan.id("macaque"), "main");

    private static final Identifier TEXTURE = Taiwan.id("textures/entity/macaque.png");

    public MacaqueRenderer(EntityRendererProvider.Context context) {
        super(context, new MacaqueModel(context.bakeLayer(LAYER)), 0.3f);
        this.addLayer(new MacaqueLootLayer(this));
    }

    @Override
    protected void scale(MacaqueRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public MacaqueRenderState createRenderState() {
        return new MacaqueRenderState();
    }

    @Override
    public void extractRenderState(Macaque entity, MacaqueRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.carrying = entity.hasLoot();
        state.sitAmount = entity.sitAmount(partialTick);
        // getAttackAnim 是 0→1 的線性斜坡，sin(x·π) 把它折成 0→1→0：
        // 手舉起來再放下。直接拿斜坡用的話，揮完會從最高點瞬間彈回原位
        state.swipeAmount = Mth.sin(entity.getAttackAnim(partialTick) * Mth.PI);
        // 把主手物品烘成 ItemStackRenderState 給 MacaqueLootLayer 用。
        // 原版的靜態方法讀的就是 getMainHandItem()，用 ItemDisplayContext.GROUND
        HoldingEntityRenderState.extractHoldingEntityRenderState(entity, state, this.itemModelResolver);
    }

    @Override
    public Identifier getTextureLocation(MacaqueRenderState state) {
        return TEXTURE;
    }
}
