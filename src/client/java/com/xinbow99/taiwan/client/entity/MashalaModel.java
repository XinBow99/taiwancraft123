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
 * 馬莎拉蹄的模型。幾何由 {@code tools/gen-model.mjs} 從 {@code tools/models.js} 產生。
 *
 * <h2>名字</h2>
 * <p>同樣是同音替字，而且換完之後自己組成另一個意思：「馬…蹄」。跑車的性能單位
 * 本來就叫馬力，這個雙關是刻意的。
 *
 * <h2>跟藍爆的差別是姿態，不是尺寸</h2>
 * <p>藍爆是楔形（車頭貼地、一路往後升），這台是**三廂**——引擎蓋、車廂、行李廂
 * 三段各自水平。前者是賽道的形狀，後者是高速公路的形狀。
 *
 * <p>車頂那一段一定要**平**。第一版是圓弧的，整台讀起來像一隻甲蟲；
 * 把中間六片橫斷面壓成同一個高度之後才像一台跑房車。
 */
public class MashalaModel extends EntityModel<VehicleRenderState> {

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

    public MashalaModel(ModelPart root) {
        // 理由同機車：預設的 cutout-no-cull 會讓車殼內外側一起畫、互相穿插，
        // 看起來像整台車是半透明的
        super(root, RenderTypes::entitySolid);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body0",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0f, 0.0f, -0.8f, 18.0f, 4.0f, 1.6f),
                PartPose.offset(0.0f, 19.2f, -32.0f));
        root.addOrReplaceChild("body1",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.0f, 0.0f, -0.8f, 22.0f, 4.3f, 1.6f),
                PartPose.offset(0.0f, 18.9f, -30.5f));
        root.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.0f, 0.0f, -0.8f, 26.0f, 4.5f, 1.6f),
                PartPose.offset(0.0f, 18.7f, -29.0f));
        root.addOrReplaceChild("body3",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0f, 0.0f, -0.8f, 28.0f, 4.5f, 1.6f),
                PartPose.offset(0.0f, 18.5f, -27.5f));
        root.addOrReplaceChild("body4",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 4.5f, 1.6f),
                PartPose.offset(0.0f, 18.3f, -26.0f));
        root.addOrReplaceChild("body5",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 4.5f, 1.6f),
                PartPose.offset(0.0f, 18.1f, -24.5f));
        root.addOrReplaceChild("body6",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 4.7f, 1.6f),
                PartPose.offset(0.0f, 17.9f, -23.0f));
        root.addOrReplaceChild("body7",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 4.9f, 1.6f),
                PartPose.offset(0.0f, 17.7f, -21.5f));
        root.addOrReplaceChild("body8",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 5.2f, 1.6f),
                PartPose.offset(0.0f, 17.4f, -20.0f));
        root.addOrReplaceChild("body9",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 5.5f, 1.6f),
                PartPose.offset(0.0f, 17.1f, -18.5f));
        root.addOrReplaceChild("body10",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 5.6f, 1.6f),
                PartPose.offset(0.0f, 17.0f, -17.0f));
        root.addOrReplaceChild("body11",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 6.2f, 1.6f),
                PartPose.offset(0.0f, 16.4f, -15.5f));
        root.addOrReplaceChild("body12",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 7.6f, 1.6f),
                PartPose.offset(0.0f, 15.0f, -14.0f));
        root.addOrReplaceChild("body13",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 9.6f, 1.6f),
                PartPose.offset(0.0f, 13.0f, -12.5f));
        root.addOrReplaceChild("body14",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 11.6f, 1.6f),
                PartPose.offset(0.0f, 11.0f, -11.0f));
        root.addOrReplaceChild("body15",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 13.4f, 1.6f),
                PartPose.offset(0.0f, 9.2f, -9.5f));
        root.addOrReplaceChild("body16",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 14.6f, 1.6f),
                PartPose.offset(0.0f, 8.0f, -8.0f));
        root.addOrReplaceChild("body17",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 15.0f, 1.6f),
                PartPose.offset(0.0f, 7.6f, -6.5f));
        root.addOrReplaceChild("body18",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 15.1f, 1.6f),
                PartPose.offset(0.0f, 7.5f, -5.0f));
        root.addOrReplaceChild("body19",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 15.1f, 1.6f),
                PartPose.offset(0.0f, 7.5f, -3.5f));
        root.addOrReplaceChild("body20",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 15.0f, 1.6f),
                PartPose.offset(0.0f, 7.6f, -2.0f));
        root.addOrReplaceChild("body21",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 14.7f, 1.6f),
                PartPose.offset(0.0f, 7.9f, -0.5f));
        root.addOrReplaceChild("body22",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 14.0f, 1.6f),
                PartPose.offset(0.0f, 8.6f, 1.0f));
        root.addOrReplaceChild("body23",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 13.0f, 1.6f),
                PartPose.offset(0.0f, 9.6f, 2.5f));
        root.addOrReplaceChild("body24",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 11.8f, 1.6f),
                PartPose.offset(0.0f, 10.8f, 4.0f));
        root.addOrReplaceChild("body25",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 10.6f, 1.6f),
                PartPose.offset(0.0f, 12.0f, 5.5f));
        root.addOrReplaceChild("body26",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 9.6f, 1.6f),
                PartPose.offset(0.0f, 13.0f, 7.0f));
        root.addOrReplaceChild("body27",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 8.8f, 1.6f),
                PartPose.offset(0.0f, 13.8f, 8.5f));
        root.addOrReplaceChild("body28",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 8.2f, 1.6f),
                PartPose.offset(0.0f, 14.4f, 10.0f));
        root.addOrReplaceChild("body29",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 7.8f, 1.6f),
                PartPose.offset(0.0f, 14.8f, 11.5f));
        root.addOrReplaceChild("body30",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 7.6f, 1.6f),
                PartPose.offset(0.0f, 15.0f, 13.0f));
        root.addOrReplaceChild("body31",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 7.5f, 1.6f),
                PartPose.offset(0.0f, 15.1f, 14.5f));
        root.addOrReplaceChild("body32",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 7.4f, 1.6f),
                PartPose.offset(0.0f, 15.2f, 16.0f));
        root.addOrReplaceChild("body33",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 7.4f, 1.6f),
                PartPose.offset(0.0f, 15.2f, 17.5f));
        root.addOrReplaceChild("body34",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5f, 0.0f, -0.8f, 31.0f, 7.4f, 1.6f),
                PartPose.offset(0.0f, 15.2f, 19.0f));
        root.addOrReplaceChild("body35",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0f, 0.0f, -0.8f, 30.0f, 7.5f, 1.6f),
                PartPose.offset(0.0f, 15.3f, 20.5f));
        root.addOrReplaceChild("body36",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0f, 0.0f, -0.8f, 28.0f, 7.5f, 1.6f),
                PartPose.offset(0.0f, 15.5f, 22.0f));
        root.addOrReplaceChild("body37",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.5f, 0.0f, -0.8f, 25.0f, 7.2f, 1.6f),
                PartPose.offset(0.0f, 16.0f, 23.5f));
        root.addOrReplaceChild("body38",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.5f, 0.0f, -0.8f, 21.0f, 6.4f, 1.6f),
                PartPose.offset(0.0f, 16.8f, 25.0f));
        root.addOrReplaceChild("body39",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-8.5f, 0.0f, -0.8f, 17.0f, 5.6f, 1.6f),
                PartPose.offset(0.0f, 17.6f, 26.2f));
        root.addOrReplaceChild("glass_f",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-13.5f, -4.6f, -0.7f, 27.0f, 9.2f, 1.4f),
                PartPose.offsetAndRotation(0.0f, 11.5f, -11.0f, 41f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("glass_left",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-0.6f, -2.25f, -4.5f, 1.2f, 4.5f, 9.0f),
                PartPose.offset(15.0f, 9.75f, -3.5f));
        root.addOrReplaceChild("glass_right",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-0.6f, -2.25f, -4.5f, 1.2f, 4.5f, 9.0f),
                PartPose.offset(-15.0f, 9.75f, -3.5f));
        root.addOrReplaceChild("glass_b",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.5f, -4.55f, -0.7f, 25.0f, 9.1f, 1.4f),
                PartPose.offsetAndRotation(0.0f, 10.45f, 3.25f, -56f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("seam_left",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.4f, -0.5f, -9.0f, 0.8f, 1.0f, 18.0f),
                PartPose.offset(15.4f, 17.6f, -1.0f));
        root.addOrReplaceChild("seam_right",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.4f, -0.5f, -9.0f, 0.8f, 1.0f, 18.0f),
                PartPose.offset(-15.4f, 17.6f, -1.0f));
        root.addOrReplaceChild("vent_l0",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5f, -1.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(15.3f, 17.2f, -16.4f));
        root.addOrReplaceChild("vent_r0",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5f, -1.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(-15.3f, 17.2f, -16.4f));
        root.addOrReplaceChild("vent_l1",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5f, -1.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(15.3f, 17.2f, -14.8f));
        root.addOrReplaceChild("vent_r1",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5f, -1.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(-15.3f, 17.2f, -14.8f));
        root.addOrReplaceChild("vent_l2",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5f, -1.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(15.3f, 17.2f, -13.2f));
        root.addOrReplaceChild("vent_r2",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5f, -1.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(-15.3f, 17.2f, -13.2f));
        root.addOrReplaceChild("grille",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-7.0f, -1.6f, -0.8f, 14.0f, 3.2f, 1.6f),
                PartPose.offset(0.0f, 21.0f, -32.9f));
        root.addOrReplaceChild("slat0",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.25f, -1.4f, -0.4f, 0.5f, 2.8f, 0.8f),
                PartPose.offset(-5.2f, 21.0f, -33.3f));
        root.addOrReplaceChild("slat1",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.25f, -1.4f, -0.4f, 0.5f, 2.8f, 0.8f),
                PartPose.offset(-2.6f, 21.0f, -33.3f));
        root.addOrReplaceChild("slat2",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.25f, -1.4f, -0.4f, 0.5f, 2.8f, 0.8f),
                PartPose.offset(0.0f, 21.0f, -33.3f));
        root.addOrReplaceChild("slat3",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.25f, -1.4f, -0.4f, 0.5f, 2.8f, 0.8f),
                PartPose.offset(2.6f, 21.0f, -33.3f));
        root.addOrReplaceChild("slat4",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.25f, -1.4f, -0.4f, 0.5f, 2.8f, 0.8f),
                PartPose.offset(5.2f, 21.0f, -33.3f));
        root.addOrReplaceChild("shoe_left",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.45f, -0.8f, -0.25f, 0.9f, 1.6f, 0.5f),
                PartPose.offset(-1.3f, 20.5f, -33.6f));
        root.addOrReplaceChild("shoe_right",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.45f, -0.8f, -0.25f, 0.9f, 1.6f, 0.5f),
                PartPose.offset(1.3f, 20.5f, -33.6f));
        root.addOrReplaceChild("shoe_b",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.75f, -0.4f, -0.25f, 3.5f, 0.8f, 0.5f),
                PartPose.offset(0.0f, 21.7f, -33.6f));
        root.addOrReplaceChild("lamp_left",
                CubeListBuilder.create().texOffs(LAMP, LAMP_V)
                        .addBox(-2.0f, -0.7f, -0.6f, 4.0f, 1.4f, 1.2f),
                PartPose.offsetAndRotation(8.0f, 19.9f, -31.6f, 0f, 0f, -8f * Mth.DEG_TO_RAD));
        root.addOrReplaceChild("lamp_right",
                CubeListBuilder.create().texOffs(LAMP, LAMP_V)
                        .addBox(-2.0f, -0.7f, -0.6f, 4.0f, 1.4f, 1.2f),
                PartPose.offsetAndRotation(-8.0f, 19.9f, -31.6f, 0f, 0f, 8f * Mth.DEG_TO_RAD));
        root.addOrReplaceChild("duct_left",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.0f, -0.8f, -0.6f, 4.0f, 1.6f, 1.2f),
                PartPose.offset(8.0f, 22.2f, -31.6f));
        root.addOrReplaceChild("duct_right",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.0f, -0.8f, -0.6f, 4.0f, 1.6f, 1.2f),
                PartPose.offset(-8.0f, 22.2f, -31.6f));
        root.addOrReplaceChild("tail_left",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-5.2f, -1.1f, -0.5f, 5.2f, 2.2f, 1.0f),
                PartPose.offset(8.6f, 18.4f, 26.6f));
        root.addOrReplaceChild("tail_right",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(0.0f, -1.1f, -0.5f, 5.2f, 2.2f, 1.0f),
                PartPose.offset(-8.6f, 18.4f, 26.6f));
        root.addOrReplaceChild("pipe_left",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-2.6f, -0.9f, -0.9f, 2.6f, 1.8f, 1.8f),
                PartPose.offset(7.4f, 22.4f, 26.8f));
        root.addOrReplaceChild("pipe_right",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(0.0f, -0.9f, -0.9f, 2.6f, 1.8f, 1.8f),
                PartPose.offset(-7.4f, 22.4f, 26.8f));
        root.addOrReplaceChild("lip",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.0f, -0.6f, -1.4f, 26.0f, 1.2f, 2.8f),
                PartPose.offsetAndRotation(0.0f, 15.0f, 24.4f, -12f * Mth.DEG_TO_RAD, 0f, 0f));
        PartDefinition wheelFl = root.addOrReplaceChild("wheel_fl",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.5f, -4.4f, -4.4f, 5.0f, 8.8f, 8.8f),
                PartPose.offset(14.0f, 18.8f, -22.0f));
        PartDefinition wheelFr = root.addOrReplaceChild("wheel_fr",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.5f, -4.4f, -4.4f, 5.0f, 8.8f, 8.8f),
                PartPose.offset(-14.0f, 18.8f, -22.0f));
        PartDefinition wheelRl = root.addOrReplaceChild("wheel_rl",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.5f, -4.4f, -4.4f, 5.0f, 8.8f, 8.8f),
                PartPose.offset(14.0f, 18.8f, 20.0f));
        PartDefinition wheelRr = root.addOrReplaceChild("wheel_rr",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.5f, -4.4f, -4.4f, 5.0f, 8.8f, 8.8f),
                PartPose.offset(-14.0f, 18.8f, 20.0f));
        wheelFl.addOrReplaceChild("rim_fl",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.7f, -2.5f, -2.5f, 5.4f, 5.0f, 5.0f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("hub_fl",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.8f, -1.1f, -1.1f, 5.6f, 2.2f, 2.2f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("cal_fl",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-2.85f, -1.7f, -0.5f, 0.9f, 3.4f, 1.0f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("rim_fr",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.7f, -2.5f, -2.5f, 5.4f, 5.0f, 5.0f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("hub_fr",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.8f, -1.1f, -1.1f, 5.6f, 2.2f, 2.2f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("cal_fr",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(1.95f, -1.7f, -0.5f, 0.9f, 3.4f, 1.0f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("rim_rl",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.7f, -2.5f, -2.5f, 5.4f, 5.0f, 5.0f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("hub_rl",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.8f, -1.1f, -1.1f, 5.6f, 2.2f, 2.2f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("cal_rl",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-2.85f, -1.7f, -0.5f, 0.9f, 3.4f, 1.0f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("rim_rr",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.7f, -2.5f, -2.5f, 5.4f, 5.0f, 5.0f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("hub_rr",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.8f, -1.1f, -1.1f, 5.6f, 2.2f, 2.2f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("cal_rr",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(1.95f, -1.7f, -0.5f, 0.9f, 3.4f, 1.0f),
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
