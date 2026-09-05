package com.xinbow99.taiwan.client.entity;

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
 * 8+9 的模型。幾何由 {@code tools/gen-model.mjs} 從 {@code tools/models.js} 產生。
 *
 * <h2>站姿是三七步</h2>
 * <p>這是整個模型最重要的一件事。三七步是重心壓在一隻腳上、另一隻腳斜開、
 * 骨盆歪一邊、肩膀跟著垮下來的站法——立正站著的 NPC 是村民，不是 8+9。
 *
 * <p>做法是六個關節同時偏一點點，而不是把某一根手臂轉很大：
 * 承重腳打直、另一腳往外斜開並微彎、身體往承重側傾、頭往反方向補回來（人會下意識
 * 讓視線保持水平）、雙手垂在身側但手肘外開。每一項都只有幾度，合起來才是那個姿勢；
 * 單獨放大任何一項都會變成殘廢的走路。
 *
 * <h2>走路是八字外開＋左右晃</h2>
 * <p>原版的走路動畫是前後擺腿。8+9 的走法多兩件事：**腳尖外八**（腿在擺動的同時
 * 帶一點外旋）與**上半身左右晃**（每一步把肩膀壓到承重腳那一側）。
 * 手臂擺幅比原版小、但手肘更開——手不是甩的，是撐開的。
 *
 * <h2>成團時更張揚</h2>
 * <p>{@code inCrowd} 會把幅度整個放大：人多的時候站得更歪、晃得更明顯。這是這個
 * 族群的核心機制（見 {@code EightNine}）在畫面上的體現，不只是台詞變大聲。
 */
public class EightNineModel extends EntityModel<EightNineRenderState> {

    // 十六塊色票的左上角（32×32 一格，4×4 排在 128×128）。
    // 六型共用這個版型，換的只是貼圖裡每一格填什麼顏色。
    // 順序必須跟 tools/swatch-texture.mjs 的 EIGHTNINE 調色盤一致。
    private static final int SKIN = 0,      SKIN_V = 0;
    private static final int SKIN_DK = 32,  SKIN_DK_V = 0;
    private static final int HAIR = 64,     HAIR_V = 0;
    private static final int HAIR_LT = 96,  HAIR_LT_V = 0;
    private static final int EYE = 0,       EYE_V = 32;
    private static final int DARK = 32,     DARK_V = 32;
    private static final int RED = 64,      RED_V = 32;
    private static final int GOLD = 96,     GOLD_V = 32;
    private static final int SHIRT = 0,     SHIRT_V = 64;
    private static final int SHIRT_DK = 32, SHIRT_DK_V = 64;
    private static final int PANTS = 64,    PANTS_V = 64;
    private static final int PANTS_DK = 96, PANTS_DK_V = 64;
    private static final int SHOE = 0,      SHOE_V = 96;
    private static final int SHOE_DK = 32,  SHOE_DK_V = 96;
    private static final int WHITE89 = 64,  WHITE89_V = 96;
    private static final int ACCENT = 96,   ACCENT_V = 96;

    /** 三七步：重心那一側的骨盆偏移（度）。 */
    private static final float STANCE = 5.0f;
    /** 走路時上半身左右晃的幅度（度）。 */
    private static final float SWAY = 6.5f;
    /** 腳尖外八的角度（度）。 */
    private static final float TOE_OUT = 11.0f;

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart armLeft;
    private final ModelPart armRight;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart shades;
    private final ModelPart band;
    private final ModelPart bandTail;
    private final ModelPart chain;
    private final ModelPart bag;
    private final ModelPart strap;

    public EightNineModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.armLeft = root.getChild("arm_left");
        this.armRight = root.getChild("arm_right");
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
        this.shades = this.head.getChild("shades");
        this.band = this.head.getChild("band");
        this.bandTail = this.head.getChild("band_tail");
        this.chain = this.body.getChild("chain");
        this.bag = this.body.getChild("bag");
        this.strap = this.body.getChild("strap");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(SKIN, SKIN_V)
                        .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                PartPose.ZERO);
        head.addOrReplaceChild("hair",
                CubeListBuilder.create().texOffs(HAIR, HAIR_V)
                        .addBox(-4.45f, -8.45f, -4.45f, 8.9f, 5.4f, 8.9f),
                PartPose.ZERO);
        head.addOrReplaceChild("fade",
                CubeListBuilder.create().texOffs(HAIR_LT, HAIR_LT_V)
                        .addBox(-4.3f, -3.1f, -4.3f, 8.6f, 1.6f, 8.6f),
                PartPose.ZERO);
        head.addOrReplaceChild("shades",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-4.55f, -5.3f, -4.95f, 9.1f, 2.0f, 0.7f),
                PartPose.ZERO);
        head.addOrReplaceChild("band",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-4.65f, -6.7f, -4.65f, 9.3f, 1.8f, 9.3f),
                PartPose.ZERO);
        head.addOrReplaceChild("band_tail",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.8f, -6.2f, 4.6f, 1.6f, 6.5f, 0.8f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 6f * Mth.DEG_TO_RAD, 0f, 0f));
        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(EYE, EYE_V)
                        .addBox(1.2f, -4.6f, -4.06f, 1.4f, 1.2f, 0.1f),
                PartPose.ZERO);
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(EYE, EYE_V)
                        .addBox(-2.6f, -4.6f, -4.06f, 1.4f, 1.2f, 0.1f),
                PartPose.ZERO);
        head.addOrReplaceChild("brow_left",
                CubeListBuilder.create().texOffs(HAIR, HAIR_V)
                        .addBox(1.0f, -5.6f, -4.06f, 1.8f, 0.5f, 0.1f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0f, 0f, -8f * Mth.DEG_TO_RAD));
        head.addOrReplaceChild("brow_right",
                CubeListBuilder.create().texOffs(HAIR, HAIR_V)
                        .addBox(-2.8f, -5.6f, -4.06f, 1.8f, 0.5f, 0.1f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0f, 0f, 8f * Mth.DEG_TO_RAD));
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(SHIRT, SHIRT_V)
                        .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
                PartPose.ZERO);
        body.addOrReplaceChild("chain",
                CubeListBuilder.create().texOffs(GOLD, GOLD_V)
                        .addBox(-2.6f, 1.4f, -2.35f, 5.2f, 1.0f, 0.7f),
                PartPose.ZERO);
        body.addOrReplaceChild("strap",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.4f, 0.6f, -2.3f, 1.2f, 7.0f, 0.6f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0f, 0f, -22f * Mth.DEG_TO_RAD));
        body.addOrReplaceChild("bag",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-4.9f, 6.4f, -2.6f, 3.4f, 2.8f, 1.4f),
                PartPose.ZERO);
        PartDefinition armLeft = root.addOrReplaceChild("arm_left",
                CubeListBuilder.create().texOffs(SKIN, SKIN_V)
                        .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition armRight = root.addOrReplaceChild("arm_right",
                CubeListBuilder.create().texOffs(SKIN, SKIN_V)
                        .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-5.0f, 2.0f, 0.0f));
        armLeft.addOrReplaceChild("sleeve_left",
                CubeListBuilder.create().texOffs(SHIRT, SHIRT_V)
                        .addBox(-1.3f, -2.3f, -2.3f, 4.6f, 5.0f, 4.6f),
                PartPose.ZERO);
        armRight.addOrReplaceChild("sleeve_right",
                CubeListBuilder.create().texOffs(SHIRT, SHIRT_V)
                        .addBox(-3.3f, -2.3f, -2.3f, 4.6f, 5.0f, 4.6f),
                PartPose.ZERO);
        PartDefinition legLeft = root.addOrReplaceChild("leg_left",
                CubeListBuilder.create().texOffs(PANTS, PANTS_V)
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(1.9f, 12.0f, 0.0f));
        PartDefinition legRight = root.addOrReplaceChild("leg_right",
                CubeListBuilder.create().texOffs(PANTS, PANTS_V)
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-1.9f, 12.0f, 0.0f));
        legLeft.addOrReplaceChild("cuff_left",
                CubeListBuilder.create().texOffs(PANTS, PANTS_V)
                        .addBox(-2.6f, 5.5f, -2.6f, 5.2f, 6.8f, 5.2f),
                PartPose.ZERO);
        legRight.addOrReplaceChild("cuff_right",
                CubeListBuilder.create().texOffs(PANTS, PANTS_V)
                        .addBox(-2.6f, 5.5f, -2.6f, 5.2f, 6.8f, 5.2f),
                PartPose.ZERO);
        legLeft.addOrReplaceChild("shoe_left",
                CubeListBuilder.create().texOffs(SHOE, SHOE_V)
                        .addBox(-2.8f, 10.4f, -3.2f, 5.6f, 2.4f, 6.4f),
                PartPose.ZERO);
        legRight.addOrReplaceChild("shoe_right",
                CubeListBuilder.create().texOffs(SHOE, SHOE_V)
                        .addBox(-2.8f, 10.4f, -3.2f, 5.6f, 2.4f, 6.4f),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(EightNineRenderState state) {
        super.setupAnim(state);

        // 配件的開關。六型共用一個模型，靠這裡決定誰有什麼
        this.shades.visible = state.variant.hasShades();
        this.band.visible = state.variant.hasHeadband();
        this.bandTail.visible = state.variant.hasHeadband();
        this.chain.visible = state.variant.hasChain();
        this.bag.visible = state.variant.hasBag();
        this.strap.visible = state.variant.hasBag();

        // 頭跟著視線。state.yRot 已經是**相對於身體**的頭部偏航（bodyRot 才是身體本身），
        // 這一段跟原版一樣，沒有 8+9 的成分
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

        // 成團時所有幅度放大。人多的時候站得更歪、晃得更明顯
        float attitude = state.inCrowd ? 1.45f : 1.0f;
        float walk = Mth.clamp(state.walkAnimationSpeed, 0f, 1f);
        float phase = state.walkAnimationPos;

        // ---- 走路：前後擺腿（原版）＋ 外八 ＋ 上身左右晃 ----------------------
        float swing = Mth.cos(phase * 0.6662f) * 1.1f * walk;
        this.legLeft.xRot = swing;
        this.legRight.xRot = -swing;
        // 外八：擺動的同時帶外旋，而且**站著時也保留**——外八不是走路才有的
        this.legLeft.yRot = TOE_OUT * Mth.DEG_TO_RAD;
        this.legRight.yRot = -TOE_OUT * Mth.DEG_TO_RAD;

        // 手臂擺幅比原版小（0.6 倍），但手肘外開。手不是甩的，是撐開的
        this.armLeft.xRot = -swing * 0.6f;
        this.armRight.xRot = swing * 0.6f;
        this.armLeft.zRot = (0.14f + 0.05f * Mth.cos(phase * 0.6662f) * walk) * attitude;
        this.armRight.zRot = -(0.14f + 0.05f * Mth.cos(phase * 0.6662f) * walk) * attitude;

        // 上半身左右晃：一步一次，跟腿同相位但頻率減半
        float sway = Mth.sin(phase * 0.3331f) * walk;
        this.body.zRot = sway * SWAY * Mth.DEG_TO_RAD * attitude;
        // 頭往反方向補回來，人會下意識讓視線保持水平
        this.head.zRot = -this.body.zRot * 0.5f;

        // ---- 站著：三七步 ---------------------------------------------------
        // walk 趨近 0 時才生效，跟走路動畫之間是連續的，不會在起步那一刻跳一下
        float idle = 1f - walk;
        if (idle > 0.01f) {
            float lean = STANCE * idle * attitude * Mth.DEG_TO_RAD;
            // 重心壓在右腳：右腿打直，左腿往外斜開並微彎
            this.legRight.xRot += 0.0f;
            this.legRight.zRot = -lean * 0.4f;
            this.legLeft.xRot += 0.12f * idle;
            this.legLeft.zRot = lean * 1.6f;
            this.legLeft.yRot += (TOE_OUT * 0.6f) * idle * Mth.DEG_TO_RAD;
            // 身體往承重側傾，頭反向補
            this.body.zRot += lean * 0.9f;
            this.head.zRot -= lean * 0.7f;
            // 手肘再開一點，重心那側的手垂得比較低
            this.armRight.zRot -= lean * 0.5f;
            this.armLeft.zRot += lean * 0.9f;
        }
    }
}
