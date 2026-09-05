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
 * 藍爆堅尼的模型。幾何由 {@code tools/gen-model.mjs} 從 {@code tools/models.js} 產生。
 *
 * <h2>名字</h2>
 * <p>照這個專案招牌店名的同一套規矩——**同音替字**（見 {@code ShopName}）：保留語感節奏、
 * 換掉字，讀者認得出是哪一類，但不是任何一個真實商標。「爆」換掉「寶」是有理由的：
 * 超跑的識別本來就是聲音跟排場。
 *
 * <h2>三個識別點</h2>
 * <ol>
 *   <li><b>楔形</b>——車頭幾乎貼地、一路往後升到車頂。這是超跑跟一般房車最根本的差別。
 *   <li><b>極低的車頂</b>（1.04 格），比機車還矮。
 *   <li><b>尾翼</b>——最省事也最有效的識別。沒有它，側面就只是一台低矮的車。
 * </ol>
 *
 * <p>輪圈佔輪子的比例是調過的：佔六成時整顆讀成一個箱子，四成才像輪子，
 * 而且中間要再放一顆暗色的轂。這是在算圖上看出來的，不是在遊戲裡。
 */
public class LanbaoModel extends EntityModel<VehicleRenderState> {

    // 八塊色票的左上角。兩款跑車共用這組槽位名稱，差別只在貼圖裡填什麼顏色。
    // 順序必須跟 tools/swatch-texture.mjs 的調色盤一致。
    private static final int PAINT = 0,   PAINT_V = 0;
    private static final int DARK = 64,   DARK_V = 0;
    private static final int GLASS = 0,   GLASS_V = 32;
    private static final int LAMP = 64,   LAMP_V = 32;
    private static final int RED = 0,     RED_V = 64;
    private static final int TIRE = 64,   TIRE_V = 64;
    private static final int RIM = 0,     RIM_V = 96;
    private static final int CHROME = 64, CHROME_V = 96;

    public LanbaoModel(ModelPart root) {
        // 理由同機車：預設的 cutout-no-cull 會讓車殼內外側一起畫、互相穿插，
        // 看起來像整台車是半透明的
        super(root, RenderTypes::entitySolid);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body0",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-8.0f, 0.0f, -0.8f, 16.0f, 1.8f, 1.6f),
                PartPose.offset(0.0f, 21.4f, -30.0f));
        root.addOrReplaceChild("body1",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.0f, 0.0f, -0.8f, 20.0f, 2.6f, 1.6f),
                PartPose.offset(0.0f, 20.6f, -28.5f));
        root.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.0f, 0.0f, -0.8f, 24.0f, 3.2f, 1.6f),
                PartPose.offset(0.0f, 20.0f, -27.0f));
        root.addOrReplaceChild("body3",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.5f, 0.0f, -0.8f, 27.0f, 3.5f, 1.6f),
                PartPose.offset(0.0f, 19.5f, -25.5f));
        root.addOrReplaceChild("body4",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.5f, 0.0f, -0.8f, 29.0f, 3.8f, 1.6f),
                PartPose.offset(0.0f, 19.0f, -24.0f));
        root.addOrReplaceChild("body5",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 4.0f, 1.6f),
                PartPose.offset(0.0f, 18.6f, -22.5f));
        root.addOrReplaceChild("body6",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 4.4f, 1.6f),
                PartPose.offset(0.0f, 18.2f, -21.0f));
        root.addOrReplaceChild("body7",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 4.8f, 1.6f),
                PartPose.offset(0.0f, 17.8f, -19.5f));
        root.addOrReplaceChild("body8",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 5.4f, 1.6f),
                PartPose.offset(0.0f, 17.2f, -18.0f));
        root.addOrReplaceChild("body9",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 6.0f, 1.6f),
                PartPose.offset(0.0f, 16.6f, -16.5f));
        root.addOrReplaceChild("body10",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 6.6f, 1.6f),
                PartPose.offset(0.0f, 16.0f, -15.0f));
        root.addOrReplaceChild("body11",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 7.4f, 1.6f),
                PartPose.offset(0.0f, 15.2f, -13.5f));
        root.addOrReplaceChild("body12",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 8.4f, 1.6f),
                PartPose.offset(0.0f, 14.2f, -12.0f));
        root.addOrReplaceChild("body13",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 9.8f, 1.6f),
                PartPose.offset(0.0f, 12.8f, -10.5f));
        root.addOrReplaceChild("body14",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 11.4f, 1.6f),
                PartPose.offset(0.0f, 11.2f, -9.0f));
        root.addOrReplaceChild("body15",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.5f, 0.0f, -0.8f, 29.0f, 13.0f, 1.6f),
                PartPose.offset(0.0f, 9.6f, -7.5f));
        root.addOrReplaceChild("body16",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0f, 0.0f, -0.8f, 28.0f, 14.0f, 1.6f),
                PartPose.offset(0.0f, 8.6f, -6.0f));
        root.addOrReplaceChild("body17",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0f, 0.0f, -0.8f, 28.0f, 14.6f, 1.6f),
                PartPose.offset(0.0f, 8.0f, -4.5f));
        root.addOrReplaceChild("body18",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0f, 0.0f, -0.8f, 28.0f, 15.0f, 1.6f),
                PartPose.offset(0.0f, 7.6f, -3.0f));
        root.addOrReplaceChild("body19",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0f, 0.0f, -0.8f, 28.0f, 15.2f, 1.6f),
                PartPose.offset(0.0f, 7.4f, -1.5f));
        root.addOrReplaceChild("body20",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0f, 0.0f, -0.8f, 28.0f, 15.2f, 1.6f),
                PartPose.offset(0.0f, 7.4f, 0.0f));
        root.addOrReplaceChild("body21",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0f, 0.0f, -0.8f, 28.0f, 15.0f, 1.6f),
                PartPose.offset(0.0f, 7.6f, 1.5f));
        root.addOrReplaceChild("body22",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0f, 0.0f, -0.8f, 28.0f, 14.4f, 1.6f),
                PartPose.offset(0.0f, 8.2f, 3.0f));
        root.addOrReplaceChild("body23",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.5f, 0.0f, -0.8f, 29.0f, 13.4f, 1.6f),
                PartPose.offset(0.0f, 9.2f, 4.5f));
        root.addOrReplaceChild("body24",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 12.0f, 1.6f),
                PartPose.offset(0.0f, 10.6f, 6.0f));
        root.addOrReplaceChild("body25",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 10.4f, 1.6f),
                PartPose.offset(0.0f, 12.2f, 7.5f));
        root.addOrReplaceChild("body26",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 9.0f, 1.6f),
                PartPose.offset(0.0f, 13.6f, 9.0f));
        root.addOrReplaceChild("body27",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 8.0f, 1.6f),
                PartPose.offset(0.0f, 14.6f, 10.5f));
        root.addOrReplaceChild("body28",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 7.4f, 1.6f),
                PartPose.offset(0.0f, 15.2f, 12.0f));
        root.addOrReplaceChild("body29",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 7.2f, 1.6f),
                PartPose.offset(0.0f, 15.4f, 13.5f));
        root.addOrReplaceChild("body30",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 7.2f, 1.6f),
                PartPose.offset(0.0f, 15.4f, 15.0f));
        root.addOrReplaceChild("body31",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 7.2f, 1.6f),
                PartPose.offset(0.0f, 15.4f, 16.5f));
        root.addOrReplaceChild("body32",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 7.2f, 1.6f),
                PartPose.offset(0.0f, 15.4f, 18.0f));
        root.addOrReplaceChild("body33",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.5f, 0.0f, -0.8f, 29.0f, 7.2f, 1.6f),
                PartPose.offset(0.0f, 15.6f, 19.5f));
        root.addOrReplaceChild("body34",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.5f, 0.0f, -0.8f, 27.0f, 7.0f, 1.6f),
                PartPose.offset(0.0f, 16.0f, 21.0f));
        root.addOrReplaceChild("body35",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.0f, 0.0f, -0.8f, 24.0f, 6.6f, 1.6f),
                PartPose.offset(0.0f, 16.6f, 22.5f));
        root.addOrReplaceChild("body36",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-10.0f, 0.0f, -0.8f, 20.0f, 5.8f, 1.6f),
                PartPose.offset(0.0f, 17.4f, 24.0f));
        root.addOrReplaceChild("glass_f",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-13.0f, 0.0f, -1.2f, 24.0f, 7.0f, 2.4f),
                PartPose.offsetAndRotation(0.0f, 8.4f, -8.6f, 34f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("glass_left",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-0.6f, 0.0f, -7.0f, 1.2f, 5.0f, 14.0f),
                PartPose.offset(13.6f, 8.2f, -1.0f));
        root.addOrReplaceChild("glass_right",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-0.6f, 0.0f, -7.0f, 1.2f, 5.0f, 14.0f),
                PartPose.offset(-13.6f, 8.2f, -1.0f));
        root.addOrReplaceChild("glass_b",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-11.0f, 0.0f, -1.2f, 22.0f, 5.6f, 2.4f),
                PartPose.offsetAndRotation(0.0f, 8.2f, 6.2f, -40f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("lamp_left",
                CubeListBuilder.create().texOffs(LAMP, LAMP_V)
                        .addBox(-4.4f, -1.4f, -0.6f, 4.4f, 2.4f, 1.2f),
                PartPose.offset(8.4f, 20.2f, -29.6f));
        root.addOrReplaceChild("lamp_right",
                CubeListBuilder.create().texOffs(LAMP, LAMP_V)
                        .addBox(0.0f, -1.4f, -0.6f, 4.4f, 2.4f, 1.2f),
                PartPose.offset(-8.4f, 20.2f, -29.6f));
        root.addOrReplaceChild("tail_left",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-4.6f, -1.0f, -0.5f, 4.6f, 2.0f, 1.0f),
                PartPose.offset(8.0f, 18.6f, 24.4f));
        root.addOrReplaceChild("tail_right",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(0.0f, -1.0f, -0.5f, 4.6f, 2.0f, 1.0f),
                PartPose.offset(-8.0f, 18.6f, 24.4f));
        root.addOrReplaceChild("vent_left",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-6.0f, -1.6f, -0.6f, 6.0f, 3.2f, 1.2f),
                PartPose.offset(7.6f, 22.0f, -29.4f));
        root.addOrReplaceChild("vent_right",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(0.0f, -1.6f, -0.6f, 6.0f, 3.2f, 1.2f),
                PartPose.offset(-7.6f, 22.0f, -29.4f));
        root.addOrReplaceChild("scoop_left",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.8f, -2.0f, -4.0f, 1.6f, 4.0f, 8.0f),
                PartPose.offset(14.4f, 13.6f, 6.0f));
        root.addOrReplaceChild("scoop_right",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.8f, -2.0f, -4.0f, 1.6f, 4.0f, 8.0f),
                PartPose.offset(-14.4f, 13.6f, 6.0f));
        root.addOrReplaceChild("wing",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-14.0f, -0.9f, -2.6f, 28.0f, 1.8f, 5.2f),
                PartPose.offsetAndRotation(0.0f, 12.0f, 24.6f, -8f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("wing_left",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.9f, 0.0f, -1.6f, 1.8f, 5.0f, 3.2f),
                PartPose.offset(12.6f, 12.6f, 24.2f));
        root.addOrReplaceChild("wing_right",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.9f, 0.0f, -1.6f, 1.8f, 5.0f, 3.2f),
                PartPose.offset(-12.6f, 12.6f, 24.2f));
        root.addOrReplaceChild("diffuser",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-13.0f, -1.4f, -1.6f, 26.0f, 2.8f, 3.2f),
                PartPose.offset(0.0f, 22.4f, 25.4f));
        PartDefinition wheelFl = root.addOrReplaceChild("wheel_fl",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.4f, -4.2f, -4.2f, 4.8f, 8.4f, 8.4f),
                PartPose.offset(13.4f, 19.6f, -21.0f));
        PartDefinition wheelFr = root.addOrReplaceChild("wheel_fr",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.4f, -4.2f, -4.2f, 4.8f, 8.4f, 8.4f),
                PartPose.offset(-13.4f, 19.6f, -21.0f));
        PartDefinition wheelRl = root.addOrReplaceChild("wheel_rl",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.6f, -4.6f, -4.6f, 5.2f, 9.2f, 9.2f),
                PartPose.offset(13.6f, 19.2f, 19.0f));
        PartDefinition wheelRr = root.addOrReplaceChild("wheel_rr",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.6f, -4.6f, -4.6f, 5.2f, 9.2f, 9.2f),
                PartPose.offset(-13.6f, 19.2f, 19.0f));
        wheelFl.addOrReplaceChild("rim_fl",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.6f, -2.4f, -2.4f, 5.2f, 4.8f, 4.8f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("hub_fl",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.7f, -1.1f, -1.1f, 5.4f, 2.2f, 2.2f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("rim_fr",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.6f, -2.4f, -2.4f, 5.2f, 4.8f, 4.8f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("hub_fr",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.7f, -1.1f, -1.1f, 5.4f, 2.2f, 2.2f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("rim_rl",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.8f, -2.6f, -2.6f, 5.6f, 5.2f, 5.2f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("hub_rl",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.9f, -1.2f, -1.2f, 5.8f, 2.4f, 2.4f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("rim_rr",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.8f, -2.6f, -2.6f, 5.6f, 5.2f, 5.2f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("hub_rr",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.9f, -1.2f, -1.2f, 5.8f, 2.4f, 2.4f),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(VehicleRenderState state) {
        super.setupAnim(state);
        // 汽車沒有會動的零件：輪子不轉（四顆輪子各自轉起來要四個 pivot，
        // 而在這個尺寸下沒人看得出來），龍頭也不轉——方向盤在車裡面。
    }
}
