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
                        .addBox(-7.3461f, 0.0f, -1.0704f, 14.6922f, 0.68f, 2.1408f),
                PartPose.offset(0.0f, 17.472f, -42.816f));
        root.addOrReplaceChild("body1",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0305f, 0.0f, -1.0704f, 18.0609f, 0.68f, 2.1408f),
                PartPose.offset(0.0f, 18.152f, -42.816f));
        root.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.7628f, 0.0f, -1.0704f, 19.5256f, 0.68f, 2.1408f),
                PartPose.offset(0.0f, 18.832f, -42.816f));
        root.addOrReplaceChild("body3",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.9437f, 0.0f, -1.0704f, 19.8875f, 0.68f, 2.1408f),
                PartPose.offset(0.0f, 19.512f, -42.816f));
        root.addOrReplaceChild("body4",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.861f, 0.0f, -1.0704f, 19.7219f, 0.68f, 2.1408f),
                PartPose.offset(0.0f, 20.192f, -42.816f));
        root.addOrReplaceChild("body5",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.6488f, 0.0f, -1.0704f, 19.2977f, 0.68f, 2.1408f),
                PartPose.offset(0.0f, 20.872f, -42.816f));
        root.addOrReplaceChild("body6",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.3074f, 0.0f, -1.0704f, 18.6148f, 0.68f, 2.1408f),
                PartPose.offset(0.0f, 21.552f, -42.816f));
        root.addOrReplaceChild("body7",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-8.8366f, 0.0f, -1.0704f, 17.6732f, 0.68f, 2.1408f),
                PartPose.offset(0.0f, 22.232f, -42.816f));
        root.addOrReplaceChild("body8",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-8.9785f, 0.0f, -1.0704f, 17.9571f, 0.731f, 2.1408f),
                PartPose.offset(0.0f, 17.064f, -40.809f));
        root.addOrReplaceChild("body9",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.0372f, 0.0f, -1.0704f, 22.0745f, 0.731f, 2.1408f),
                PartPose.offset(0.0f, 17.795f, -40.809f));
        root.addOrReplaceChild("body10",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.9323f, 0.0f, -1.0704f, 23.8646f, 0.731f, 2.1408f),
                PartPose.offset(0.0f, 18.526f, -40.809f));
        root.addOrReplaceChild("body11",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.1535f, 0.0f, -1.0704f, 24.3069f, 0.731f, 2.1408f),
                PartPose.offset(0.0f, 19.257f, -40.809f));
        root.addOrReplaceChild("body12",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.0523f, 0.0f, -1.0704f, 24.1046f, 0.731f, 2.1408f),
                PartPose.offset(0.0f, 19.988f, -40.809f));
        root.addOrReplaceChild("body13",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.793f, 0.0f, -1.0704f, 23.5861f, 0.731f, 2.1408f),
                PartPose.offset(0.0f, 20.719f, -40.809f));
        root.addOrReplaceChild("body14",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.3757f, 0.0f, -1.0704f, 22.7514f, 0.731f, 2.1408f),
                PartPose.offset(0.0f, 21.45f, -40.809f));
        root.addOrReplaceChild("body15",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.8003f, 0.0f, -1.0704f, 21.6006f, 0.731f, 2.1408f),
                PartPose.offset(0.0f, 22.181f, -40.809f));
        root.addOrReplaceChild("body16",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.611f, 0.0f, -1.0704f, 21.222f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 16.792f, -38.802f));
        root.addOrReplaceChild("body17",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.044f, 0.0f, -1.0704f, 26.088f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 17.557f, -38.802f));
        root.addOrReplaceChild("body18",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.1018f, 0.0f, -1.0704f, 28.2036f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 18.322f, -38.802f));
        root.addOrReplaceChild("body19",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.3632f, 0.0f, -1.0704f, 28.7263f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 19.087f, -38.802f));
        root.addOrReplaceChild("body20",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.2436f, 0.0f, -1.0704f, 28.4872f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 19.852f, -38.802f));
        root.addOrReplaceChild("body21",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.9372f, 0.0f, -1.0704f, 27.8744f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 20.617f, -38.802f));
        root.addOrReplaceChild("body22",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.444f, 0.0f, -1.0704f, 26.888f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 21.382f, -38.802f));
        root.addOrReplaceChild("body23",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.764f, 0.0f, -1.0704f, 25.5279f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 22.147f, -38.802f));
        root.addOrReplaceChild("body24",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4272f, 0.0f, -1.0704f, 22.8545f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 16.52f, -36.795f));
        root.addOrReplaceChild("body25",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0474f, 0.0f, -1.0704f, 28.0948f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 17.285f, -36.795f));
        root.addOrReplaceChild("body26",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.1866f, 0.0f, -1.0704f, 30.3732f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 18.05f, -36.795f));
        root.addOrReplaceChild("body27",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.468f, 0.0f, -1.0704f, 30.9361f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 18.815f, -36.795f));
        root.addOrReplaceChild("body28",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.3393f, 0.0f, -1.0704f, 30.6785f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 19.58f, -36.795f));
        root.addOrReplaceChild("body29",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0093f, 0.0f, -1.0704f, 30.0186f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 20.345f, -36.795f));
        root.addOrReplaceChild("body30",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.4782f, 0.0f, -1.0704f, 28.9563f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 21.11f, -36.795f));
        root.addOrReplaceChild("body31",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.7458f, 0.0f, -1.0704f, 27.4916f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 21.875f, -36.795f));
        root.addOrReplaceChild("body32",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 16.248f, -34.788f));
        root.addOrReplaceChild("body33",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 17.013f, -34.788f));
        root.addOrReplaceChild("body34",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 17.778f, -34.788f));
        root.addOrReplaceChild("body35",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 18.543f, -34.788f));
        root.addOrReplaceChild("body36",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 19.308f, -34.788f));
        root.addOrReplaceChild("body37",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 20.073f, -34.788f));
        root.addOrReplaceChild("body38",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 20.838f, -34.788f));
        root.addOrReplaceChild("body39",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 21.603f, -34.788f));
        root.addOrReplaceChild("body40",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 15.976f, -32.781f));
        root.addOrReplaceChild("body41",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 16.741f, -32.781f));
        root.addOrReplaceChild("body42",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 17.506f, -32.781f));
        root.addOrReplaceChild("body43",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 18.271f, -32.781f));
        root.addOrReplaceChild("body44",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 19.036f, -32.781f));
        root.addOrReplaceChild("body45",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 19.801f, -32.781f));
        root.addOrReplaceChild("body46",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 20.566f, -32.781f));
        root.addOrReplaceChild("body47",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 0.765f, 2.1408f),
                PartPose.offset(0.0f, 21.331f, -32.781f));
        root.addOrReplaceChild("body48",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 0.799f, 2.1408f),
                PartPose.offset(0.0f, 15.704f, -30.774f));
        root.addOrReplaceChild("body49",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 0.799f, 2.1408f),
                PartPose.offset(0.0f, 16.503f, -30.774f));
        root.addOrReplaceChild("body50",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 0.799f, 2.1408f),
                PartPose.offset(0.0f, 17.302f, -30.774f));
        root.addOrReplaceChild("body51",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 0.799f, 2.1408f),
                PartPose.offset(0.0f, 18.101f, -30.774f));
        root.addOrReplaceChild("body52",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 0.799f, 2.1408f),
                PartPose.offset(0.0f, 18.9f, -30.774f));
        root.addOrReplaceChild("body53",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 0.799f, 2.1408f),
                PartPose.offset(0.0f, 19.699f, -30.774f));
        root.addOrReplaceChild("body54",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 0.799f, 2.1408f),
                PartPose.offset(0.0f, 20.498f, -30.774f));
        root.addOrReplaceChild("body55",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 0.799f, 2.1408f),
                PartPose.offset(0.0f, 21.297f, -30.774f));
        root.addOrReplaceChild("body56",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 0.833f, 2.1408f),
                PartPose.offset(0.0f, 15.432f, -28.767f));
        root.addOrReplaceChild("body57",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 0.833f, 2.1408f),
                PartPose.offset(0.0f, 16.265f, -28.767f));
        root.addOrReplaceChild("body58",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 0.833f, 2.1408f),
                PartPose.offset(0.0f, 17.098f, -28.767f));
        root.addOrReplaceChild("body59",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 0.833f, 2.1408f),
                PartPose.offset(0.0f, 17.931f, -28.767f));
        root.addOrReplaceChild("body60",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 0.833f, 2.1408f),
                PartPose.offset(0.0f, 18.764f, -28.767f));
        root.addOrReplaceChild("body61",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 0.833f, 2.1408f),
                PartPose.offset(0.0f, 19.597f, -28.767f));
        root.addOrReplaceChild("body62",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 0.833f, 2.1408f),
                PartPose.offset(0.0f, 20.43f, -28.767f));
        root.addOrReplaceChild("body63",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 0.833f, 2.1408f),
                PartPose.offset(0.0f, 21.263f, -28.767f));
        root.addOrReplaceChild("body64",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 0.884f, 2.1408f),
                PartPose.offset(0.0f, 15.024f, -26.76f));
        root.addOrReplaceChild("body65",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 0.884f, 2.1408f),
                PartPose.offset(0.0f, 15.908f, -26.76f));
        root.addOrReplaceChild("body66",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 0.884f, 2.1408f),
                PartPose.offset(0.0f, 16.792f, -26.76f));
        root.addOrReplaceChild("body67",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 0.884f, 2.1408f),
                PartPose.offset(0.0f, 17.676f, -26.76f));
        root.addOrReplaceChild("body68",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 0.884f, 2.1408f),
                PartPose.offset(0.0f, 18.56f, -26.76f));
        root.addOrReplaceChild("body69",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 0.884f, 2.1408f),
                PartPose.offset(0.0f, 19.444f, -26.76f));
        root.addOrReplaceChild("body70",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 0.884f, 2.1408f),
                PartPose.offset(0.0f, 20.328f, -26.76f));
        root.addOrReplaceChild("body71",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 0.884f, 2.1408f),
                PartPose.offset(0.0f, 21.212f, -26.76f));
        root.addOrReplaceChild("body72",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 0.935f, 2.1408f),
                PartPose.offset(0.0f, 14.616f, -24.753f));
        root.addOrReplaceChild("body73",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 0.935f, 2.1408f),
                PartPose.offset(0.0f, 15.551f, -24.753f));
        root.addOrReplaceChild("body74",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 0.935f, 2.1408f),
                PartPose.offset(0.0f, 16.486f, -24.753f));
        root.addOrReplaceChild("body75",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 0.935f, 2.1408f),
                PartPose.offset(0.0f, 17.421f, -24.753f));
        root.addOrReplaceChild("body76",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 0.935f, 2.1408f),
                PartPose.offset(0.0f, 18.356f, -24.753f));
        root.addOrReplaceChild("body77",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 0.935f, 2.1408f),
                PartPose.offset(0.0f, 19.291f, -24.753f));
        root.addOrReplaceChild("body78",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 0.935f, 2.1408f),
                PartPose.offset(0.0f, 20.226f, -24.753f));
        root.addOrReplaceChild("body79",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 0.935f, 2.1408f),
                PartPose.offset(0.0f, 21.161f, -24.753f));
        root.addOrReplaceChild("body80",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 14.48f, -22.746f));
        root.addOrReplaceChild("body81",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 15.432f, -22.746f));
        root.addOrReplaceChild("body82",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 16.384f, -22.746f));
        root.addOrReplaceChild("body83",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 17.336f, -22.746f));
        root.addOrReplaceChild("body84",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 18.288f, -22.746f));
        root.addOrReplaceChild("body85",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 19.24f, -22.746f));
        root.addOrReplaceChild("body86",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 20.192f, -22.746f));
        root.addOrReplaceChild("body87",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 21.144f, -22.746f));
        root.addOrReplaceChild("body88",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.054f, 2.1408f),
                PartPose.offset(0.0f, 13.664f, -20.739f));
        root.addOrReplaceChild("body89",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.054f, 2.1408f),
                PartPose.offset(0.0f, 14.718f, -20.739f));
        root.addOrReplaceChild("body90",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.054f, 2.1408f),
                PartPose.offset(0.0f, 15.772f, -20.739f));
        root.addOrReplaceChild("body91",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.054f, 2.1408f),
                PartPose.offset(0.0f, 16.826f, -20.739f));
        root.addOrReplaceChild("body92",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.054f, 2.1408f),
                PartPose.offset(0.0f, 17.88f, -20.739f));
        root.addOrReplaceChild("body93",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.054f, 2.1408f),
                PartPose.offset(0.0f, 18.934f, -20.739f));
        root.addOrReplaceChild("body94",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.054f, 2.1408f),
                PartPose.offset(0.0f, 19.988f, -20.739f));
        root.addOrReplaceChild("body95",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.054f, 2.1408f),
                PartPose.offset(0.0f, 21.042f, -20.739f));
        root.addOrReplaceChild("body96",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 11.76f, -18.732f));
        root.addOrReplaceChild("body97",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 13.052f, -18.732f));
        root.addOrReplaceChild("body98",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 14.344f, -18.732f));
        root.addOrReplaceChild("body99",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 15.636f, -18.732f));
        root.addOrReplaceChild("body100",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 16.928f, -18.732f));
        root.addOrReplaceChild("body101",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 18.22f, -18.732f));
        root.addOrReplaceChild("body102",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 19.512f, -18.732f));
        root.addOrReplaceChild("body103",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 20.804f, -18.732f));
        root.addOrReplaceChild("body104",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 9.04f, -16.725f));
        root.addOrReplaceChild("body105",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 10.672f, -16.725f));
        root.addOrReplaceChild("body106",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 12.304f, -16.725f));
        root.addOrReplaceChild("body107",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 13.936f, -16.725f));
        root.addOrReplaceChild("body108",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 15.568f, -16.725f));
        root.addOrReplaceChild("body109",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 17.2f, -16.725f));
        root.addOrReplaceChild("body110",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 18.832f, -16.725f));
        root.addOrReplaceChild("body111",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 20.464f, -16.725f));
        root.addOrReplaceChild("body112",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 1.972f, 2.1408f),
                PartPose.offset(0.0f, 6.32f, -14.718f));
        root.addOrReplaceChild("body113",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 1.972f, 2.1408f),
                PartPose.offset(0.0f, 8.292f, -14.718f));
        root.addOrReplaceChild("body114",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 1.972f, 2.1408f),
                PartPose.offset(0.0f, 10.264f, -14.718f));
        root.addOrReplaceChild("body115",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 1.972f, 2.1408f),
                PartPose.offset(0.0f, 12.236f, -14.718f));
        root.addOrReplaceChild("body116",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 1.972f, 2.1408f),
                PartPose.offset(0.0f, 14.208f, -14.718f));
        root.addOrReplaceChild("body117",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 1.972f, 2.1408f),
                PartPose.offset(0.0f, 16.18f, -14.718f));
        root.addOrReplaceChild("body118",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 1.972f, 2.1408f),
                PartPose.offset(0.0f, 18.152f, -14.718f));
        root.addOrReplaceChild("body119",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 1.972f, 2.1408f),
                PartPose.offset(0.0f, 20.124f, -14.718f));
        root.addOrReplaceChild("body120",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 2.278f, 2.1408f),
                PartPose.offset(0.0f, 3.872f, -12.711f));
        root.addOrReplaceChild("body121",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 2.278f, 2.1408f),
                PartPose.offset(0.0f, 6.15f, -12.711f));
        root.addOrReplaceChild("body122",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 2.278f, 2.1408f),
                PartPose.offset(0.0f, 8.428f, -12.711f));
        root.addOrReplaceChild("body123",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 2.278f, 2.1408f),
                PartPose.offset(0.0f, 10.706f, -12.711f));
        root.addOrReplaceChild("body124",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 2.278f, 2.1408f),
                PartPose.offset(0.0f, 12.984f, -12.711f));
        root.addOrReplaceChild("body125",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 2.278f, 2.1408f),
                PartPose.offset(0.0f, 15.262f, -12.711f));
        root.addOrReplaceChild("body126",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 2.278f, 2.1408f),
                PartPose.offset(0.0f, 17.54f, -12.711f));
        root.addOrReplaceChild("body127",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 2.278f, 2.1408f),
                PartPose.offset(0.0f, 19.818f, -12.711f));
        root.addOrReplaceChild("body128",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 2.482f, 2.1408f),
                PartPose.offset(0.0f, 2.24f, -10.704f));
        root.addOrReplaceChild("body129",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 2.482f, 2.1408f),
                PartPose.offset(0.0f, 4.722f, -10.704f));
        root.addOrReplaceChild("body130",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 2.482f, 2.1408f),
                PartPose.offset(0.0f, 7.204f, -10.704f));
        root.addOrReplaceChild("body131",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 2.482f, 2.1408f),
                PartPose.offset(0.0f, 9.686f, -10.704f));
        root.addOrReplaceChild("body132",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 2.482f, 2.1408f),
                PartPose.offset(0.0f, 12.168f, -10.704f));
        root.addOrReplaceChild("body133",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 2.482f, 2.1408f),
                PartPose.offset(0.0f, 14.65f, -10.704f));
        root.addOrReplaceChild("body134",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 2.482f, 2.1408f),
                PartPose.offset(0.0f, 17.132f, -10.704f));
        root.addOrReplaceChild("body135",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 2.482f, 2.1408f),
                PartPose.offset(0.0f, 19.614f, -10.704f));
        root.addOrReplaceChild("body136",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 1.696f, -8.697f));
        root.addOrReplaceChild("body137",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 4.246f, -8.697f));
        root.addOrReplaceChild("body138",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 6.796f, -8.697f));
        root.addOrReplaceChild("body139",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 9.346f, -8.697f));
        root.addOrReplaceChild("body140",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 11.896f, -8.697f));
        root.addOrReplaceChild("body141",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 14.446f, -8.697f));
        root.addOrReplaceChild("body142",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 16.996f, -8.697f));
        root.addOrReplaceChild("body143",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 19.546f, -8.697f));
        root.addOrReplaceChild("body144",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 1.56f, -6.69f));
        root.addOrReplaceChild("body145",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 4.127f, -6.69f));
        root.addOrReplaceChild("body146",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 6.694f, -6.69f));
        root.addOrReplaceChild("body147",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 9.261f, -6.69f));
        root.addOrReplaceChild("body148",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 11.828f, -6.69f));
        root.addOrReplaceChild("body149",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 14.395f, -6.69f));
        root.addOrReplaceChild("body150",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 16.962f, -6.69f));
        root.addOrReplaceChild("body151",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 19.529f, -6.69f));
        root.addOrReplaceChild("body152",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 1.56f, -4.683f));
        root.addOrReplaceChild("body153",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 4.127f, -4.683f));
        root.addOrReplaceChild("body154",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 6.694f, -4.683f));
        root.addOrReplaceChild("body155",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 9.261f, -4.683f));
        root.addOrReplaceChild("body156",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 11.828f, -4.683f));
        root.addOrReplaceChild("body157",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 14.395f, -4.683f));
        root.addOrReplaceChild("body158",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 16.962f, -4.683f));
        root.addOrReplaceChild("body159",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 2.567f, 2.1408f),
                PartPose.offset(0.0f, 19.529f, -4.683f));
        root.addOrReplaceChild("body160",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 1.696f, -2.676f));
        root.addOrReplaceChild("body161",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 4.246f, -2.676f));
        root.addOrReplaceChild("body162",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 6.796f, -2.676f));
        root.addOrReplaceChild("body163",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 9.346f, -2.676f));
        root.addOrReplaceChild("body164",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 11.896f, -2.676f));
        root.addOrReplaceChild("body165",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 14.446f, -2.676f));
        root.addOrReplaceChild("body166",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 16.996f, -2.676f));
        root.addOrReplaceChild("body167",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 2.55f, 2.1408f),
                PartPose.offset(0.0f, 19.546f, -2.676f));
        root.addOrReplaceChild("body168",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 2.499f, 2.1408f),
                PartPose.offset(0.0f, 2.104f, -0.669f));
        root.addOrReplaceChild("body169",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 2.499f, 2.1408f),
                PartPose.offset(0.0f, 4.603f, -0.669f));
        root.addOrReplaceChild("body170",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 2.499f, 2.1408f),
                PartPose.offset(0.0f, 7.102f, -0.669f));
        root.addOrReplaceChild("body171",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 2.499f, 2.1408f),
                PartPose.offset(0.0f, 9.601f, -0.669f));
        root.addOrReplaceChild("body172",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 2.499f, 2.1408f),
                PartPose.offset(0.0f, 12.1f, -0.669f));
        root.addOrReplaceChild("body173",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 2.499f, 2.1408f),
                PartPose.offset(0.0f, 14.599f, -0.669f));
        root.addOrReplaceChild("body174",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 2.499f, 2.1408f),
                PartPose.offset(0.0f, 17.098f, -0.669f));
        root.addOrReplaceChild("body175",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 2.499f, 2.1408f),
                PartPose.offset(0.0f, 19.597f, -0.669f));
        root.addOrReplaceChild("body176",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 2.38f, 2.1408f),
                PartPose.offset(0.0f, 3.056f, 1.338f));
        root.addOrReplaceChild("body177",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 2.38f, 2.1408f),
                PartPose.offset(0.0f, 5.436f, 1.338f));
        root.addOrReplaceChild("body178",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 2.38f, 2.1408f),
                PartPose.offset(0.0f, 7.816f, 1.338f));
        root.addOrReplaceChild("body179",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 2.38f, 2.1408f),
                PartPose.offset(0.0f, 10.196f, 1.338f));
        root.addOrReplaceChild("body180",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 2.38f, 2.1408f),
                PartPose.offset(0.0f, 12.576f, 1.338f));
        root.addOrReplaceChild("body181",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 2.38f, 2.1408f),
                PartPose.offset(0.0f, 14.956f, 1.338f));
        root.addOrReplaceChild("body182",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 2.38f, 2.1408f),
                PartPose.offset(0.0f, 17.336f, 1.338f));
        root.addOrReplaceChild("body183",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 2.38f, 2.1408f),
                PartPose.offset(0.0f, 19.716f, 1.338f));
        root.addOrReplaceChild("body184",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 2.21f, 2.1408f),
                PartPose.offset(0.0f, 4.416f, 3.345f));
        root.addOrReplaceChild("body185",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 2.21f, 2.1408f),
                PartPose.offset(0.0f, 6.626f, 3.345f));
        root.addOrReplaceChild("body186",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 2.21f, 2.1408f),
                PartPose.offset(0.0f, 8.836f, 3.345f));
        root.addOrReplaceChild("body187",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 2.21f, 2.1408f),
                PartPose.offset(0.0f, 11.046f, 3.345f));
        root.addOrReplaceChild("body188",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 2.21f, 2.1408f),
                PartPose.offset(0.0f, 13.256f, 3.345f));
        root.addOrReplaceChild("body189",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 2.21f, 2.1408f),
                PartPose.offset(0.0f, 15.466f, 3.345f));
        root.addOrReplaceChild("body190",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 2.21f, 2.1408f),
                PartPose.offset(0.0f, 17.676f, 3.345f));
        root.addOrReplaceChild("body191",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 2.21f, 2.1408f),
                PartPose.offset(0.0f, 19.886f, 3.345f));
        root.addOrReplaceChild("body192",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 2.006f, 2.1408f),
                PartPose.offset(0.0f, 6.048f, 5.352f));
        root.addOrReplaceChild("body193",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 2.006f, 2.1408f),
                PartPose.offset(0.0f, 8.054f, 5.352f));
        root.addOrReplaceChild("body194",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 2.006f, 2.1408f),
                PartPose.offset(0.0f, 10.06f, 5.352f));
        root.addOrReplaceChild("body195",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 2.006f, 2.1408f),
                PartPose.offset(0.0f, 12.066f, 5.352f));
        root.addOrReplaceChild("body196",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 2.006f, 2.1408f),
                PartPose.offset(0.0f, 14.072f, 5.352f));
        root.addOrReplaceChild("body197",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 2.006f, 2.1408f),
                PartPose.offset(0.0f, 16.078f, 5.352f));
        root.addOrReplaceChild("body198",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 2.006f, 2.1408f),
                PartPose.offset(0.0f, 18.084f, 5.352f));
        root.addOrReplaceChild("body199",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 2.006f, 2.1408f),
                PartPose.offset(0.0f, 20.09f, 5.352f));
        root.addOrReplaceChild("body200",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.802f, 2.1408f),
                PartPose.offset(0.0f, 7.68f, 7.359f));
        root.addOrReplaceChild("body201",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.802f, 2.1408f),
                PartPose.offset(0.0f, 9.482f, 7.359f));
        root.addOrReplaceChild("body202",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.802f, 2.1408f),
                PartPose.offset(0.0f, 11.284f, 7.359f));
        root.addOrReplaceChild("body203",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.802f, 2.1408f),
                PartPose.offset(0.0f, 13.086f, 7.359f));
        root.addOrReplaceChild("body204",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.802f, 2.1408f),
                PartPose.offset(0.0f, 14.888f, 7.359f));
        root.addOrReplaceChild("body205",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.802f, 2.1408f),
                PartPose.offset(0.0f, 16.69f, 7.359f));
        root.addOrReplaceChild("body206",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.802f, 2.1408f),
                PartPose.offset(0.0f, 18.492f, 7.359f));
        root.addOrReplaceChild("body207",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.802f, 2.1408f),
                PartPose.offset(0.0f, 20.294f, 7.359f));
        root.addOrReplaceChild("body208",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 9.04f, 9.366f));
        root.addOrReplaceChild("body209",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 10.672f, 9.366f));
        root.addOrReplaceChild("body210",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 12.304f, 9.366f));
        root.addOrReplaceChild("body211",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 13.936f, 9.366f));
        root.addOrReplaceChild("body212",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 15.568f, 9.366f));
        root.addOrReplaceChild("body213",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 17.2f, 9.366f));
        root.addOrReplaceChild("body214",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 18.832f, 9.366f));
        root.addOrReplaceChild("body215",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.632f, 2.1408f),
                PartPose.offset(0.0f, 20.464f, 9.366f));
        root.addOrReplaceChild("body216",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.496f, 2.1408f),
                PartPose.offset(0.0f, 10.128f, 11.373f));
        root.addOrReplaceChild("body217",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.496f, 2.1408f),
                PartPose.offset(0.0f, 11.624f, 11.373f));
        root.addOrReplaceChild("body218",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.496f, 2.1408f),
                PartPose.offset(0.0f, 13.12f, 11.373f));
        root.addOrReplaceChild("body219",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.496f, 2.1408f),
                PartPose.offset(0.0f, 14.616f, 11.373f));
        root.addOrReplaceChild("body220",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.496f, 2.1408f),
                PartPose.offset(0.0f, 16.112f, 11.373f));
        root.addOrReplaceChild("body221",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.496f, 2.1408f),
                PartPose.offset(0.0f, 17.608f, 11.373f));
        root.addOrReplaceChild("body222",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.496f, 2.1408f),
                PartPose.offset(0.0f, 19.104f, 11.373f));
        root.addOrReplaceChild("body223",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.496f, 2.1408f),
                PartPose.offset(0.0f, 20.6f, 11.373f));
        root.addOrReplaceChild("body224",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.394f, 2.1408f),
                PartPose.offset(0.0f, 10.944f, 13.38f));
        root.addOrReplaceChild("body225",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.394f, 2.1408f),
                PartPose.offset(0.0f, 12.338f, 13.38f));
        root.addOrReplaceChild("body226",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.394f, 2.1408f),
                PartPose.offset(0.0f, 13.732f, 13.38f));
        root.addOrReplaceChild("body227",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.394f, 2.1408f),
                PartPose.offset(0.0f, 15.126f, 13.38f));
        root.addOrReplaceChild("body228",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.394f, 2.1408f),
                PartPose.offset(0.0f, 16.52f, 13.38f));
        root.addOrReplaceChild("body229",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.394f, 2.1408f),
                PartPose.offset(0.0f, 17.914f, 13.38f));
        root.addOrReplaceChild("body230",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.394f, 2.1408f),
                PartPose.offset(0.0f, 19.308f, 13.38f));
        root.addOrReplaceChild("body231",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.394f, 2.1408f),
                PartPose.offset(0.0f, 20.702f, 13.38f));
        root.addOrReplaceChild("body232",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.326f, 2.1408f),
                PartPose.offset(0.0f, 11.488f, 15.387f));
        root.addOrReplaceChild("body233",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.326f, 2.1408f),
                PartPose.offset(0.0f, 12.814f, 15.387f));
        root.addOrReplaceChild("body234",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.326f, 2.1408f),
                PartPose.offset(0.0f, 14.14f, 15.387f));
        root.addOrReplaceChild("body235",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.326f, 2.1408f),
                PartPose.offset(0.0f, 15.466f, 15.387f));
        root.addOrReplaceChild("body236",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.326f, 2.1408f),
                PartPose.offset(0.0f, 16.792f, 15.387f));
        root.addOrReplaceChild("body237",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.326f, 2.1408f),
                PartPose.offset(0.0f, 18.118f, 15.387f));
        root.addOrReplaceChild("body238",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.326f, 2.1408f),
                PartPose.offset(0.0f, 19.444f, 15.387f));
        root.addOrReplaceChild("body239",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.326f, 2.1408f),
                PartPose.offset(0.0f, 20.77f, 15.387f));
        root.addOrReplaceChild("body240",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 11.76f, 17.394f));
        root.addOrReplaceChild("body241",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 13.052f, 17.394f));
        root.addOrReplaceChild("body242",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 14.344f, 17.394f));
        root.addOrReplaceChild("body243",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 15.636f, 17.394f));
        root.addOrReplaceChild("body244",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 16.928f, 17.394f));
        root.addOrReplaceChild("body245",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 18.22f, 17.394f));
        root.addOrReplaceChild("body246",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 19.512f, 17.394f));
        root.addOrReplaceChild("body247",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.292f, 2.1408f),
                PartPose.offset(0.0f, 20.804f, 17.394f));
        root.addOrReplaceChild("body248",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 11.896f, 19.401f));
        root.addOrReplaceChild("body249",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 13.171f, 19.401f));
        root.addOrReplaceChild("body250",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 14.446f, 19.401f));
        root.addOrReplaceChild("body251",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 15.721f, 19.401f));
        root.addOrReplaceChild("body252",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 16.996f, 19.401f));
        root.addOrReplaceChild("body253",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 18.271f, 19.401f));
        root.addOrReplaceChild("body254",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 19.546f, 19.401f));
        root.addOrReplaceChild("body255",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 20.821f, 19.401f));
        root.addOrReplaceChild("body256",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 12.032f, 21.408f));
        root.addOrReplaceChild("body257",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 13.29f, 21.408f));
        root.addOrReplaceChild("body258",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 14.548f, 21.408f));
        root.addOrReplaceChild("body259",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 15.806f, 21.408f));
        root.addOrReplaceChild("body260",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 17.064f, 21.408f));
        root.addOrReplaceChild("body261",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 18.322f, 21.408f));
        root.addOrReplaceChild("body262",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 19.58f, 21.408f));
        root.addOrReplaceChild("body263",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 20.838f, 21.408f));
        root.addOrReplaceChild("body264",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 12.032f, 23.415f));
        root.addOrReplaceChild("body265",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 13.29f, 23.415f));
        root.addOrReplaceChild("body266",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 14.548f, 23.415f));
        root.addOrReplaceChild("body267",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 15.806f, 23.415f));
        root.addOrReplaceChild("body268",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 17.064f, 23.415f));
        root.addOrReplaceChild("body269",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 18.322f, 23.415f));
        root.addOrReplaceChild("body270",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 19.58f, 23.415f));
        root.addOrReplaceChild("body271",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 20.838f, 23.415f));
        root.addOrReplaceChild("body272",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6516f, 0.0f, -1.0704f, 25.3032f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 12.032f, 25.422f));
        root.addOrReplaceChild("body273",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5525f, 0.0f, -1.0704f, 31.1049f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 13.29f, 25.422f));
        root.addOrReplaceChild("body274",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.8137f, 0.0f, -1.0704f, 33.6274f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 14.548f, 25.422f));
        root.addOrReplaceChild("body275",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.1253f, 0.0f, -1.0704f, 34.2506f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 15.806f, 25.422f));
        root.addOrReplaceChild("body276",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.9828f, 0.0f, -1.0704f, 33.9655f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 17.064f, 25.422f));
        root.addOrReplaceChild("body277",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.6174f, 0.0f, -1.0704f, 33.2349f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 18.322f, 25.422f));
        root.addOrReplaceChild("body278",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0294f, 0.0f, -1.0704f, 32.0588f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 19.58f, 25.422f));
        root.addOrReplaceChild("body279",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.2186f, 0.0f, -1.0704f, 30.4372f, 1.258f, 2.1408f),
                PartPose.offset(0.0f, 20.838f, 25.422f));
        root.addOrReplaceChild("body280",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.2435f, 0.0f, -1.0704f, 24.487f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 12.168f, 27.429f));
        root.addOrReplaceChild("body281",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0508f, 0.0f, -1.0704f, 30.1016f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 13.443f, 27.429f));
        root.addOrReplaceChild("body282",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2713f, 0.0f, -1.0704f, 32.5427f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 14.718f, 27.429f));
        root.addOrReplaceChild("body283",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.5729f, 0.0f, -1.0704f, 33.1458f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 15.993f, 27.429f));
        root.addOrReplaceChild("body284",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.4349f, 0.0f, -1.0704f, 32.8699f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 17.268f, 27.429f));
        root.addOrReplaceChild("body285",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.0814f, 0.0f, -1.0704f, 32.1628f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 18.543f, 27.429f));
        root.addOrReplaceChild("body286",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.5123f, 0.0f, -1.0704f, 31.0246f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 19.818f, 27.429f));
        root.addOrReplaceChild("body287",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.7277f, 0.0f, -1.0704f, 29.4553f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 21.093f, 27.429f));
        root.addOrReplaceChild("body288",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4272f, 0.0f, -1.0704f, 22.8545f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 12.44f, 29.436f));
        root.addOrReplaceChild("body289",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.0474f, 0.0f, -1.0704f, 28.0948f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 13.715f, 29.436f));
        root.addOrReplaceChild("body290",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.1866f, 0.0f, -1.0704f, 30.3732f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 14.99f, 29.436f));
        root.addOrReplaceChild("body291",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.468f, 0.0f, -1.0704f, 30.9361f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 16.265f, 29.436f));
        root.addOrReplaceChild("body292",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.3393f, 0.0f, -1.0704f, 30.6785f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 17.54f, 29.436f));
        root.addOrReplaceChild("body293",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0093f, 0.0f, -1.0704f, 30.0186f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 18.815f, 29.436f));
        root.addOrReplaceChild("body294",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.4782f, 0.0f, -1.0704f, 28.9563f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 20.09f, 29.436f));
        root.addOrReplaceChild("body295",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.7458f, 0.0f, -1.0704f, 27.4916f, 1.275f, 2.1408f),
                PartPose.offset(0.0f, 21.365f, 29.436f));
        root.addOrReplaceChild("body296",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2029f, 0.0f, -1.0704f, 20.4058f, 1.224f, 2.1408f),
                PartPose.offset(0.0f, 13.12f, 31.443f));
        root.addOrReplaceChild("body297",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.5423f, 0.0f, -1.0704f, 25.0846f, 1.224f, 2.1408f),
                PartPose.offset(0.0f, 14.344f, 31.443f));
        root.addOrReplaceChild("body298",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.5594f, 0.0f, -1.0704f, 27.1189f, 1.224f, 2.1408f),
                PartPose.offset(0.0f, 15.568f, 31.443f));
        root.addOrReplaceChild("body299",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8107f, 0.0f, -1.0704f, 27.6215f, 1.224f, 2.1408f),
                PartPose.offset(0.0f, 16.792f, 31.443f));
        root.addOrReplaceChild("body300",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.6958f, 0.0f, -1.0704f, 27.3915f, 1.224f, 2.1408f),
                PartPose.offset(0.0f, 18.016f, 31.443f));
        root.addOrReplaceChild("body301",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.4012f, 0.0f, -1.0704f, 26.8023f, 1.224f, 2.1408f),
                PartPose.offset(0.0f, 19.24f, 31.443f));
        root.addOrReplaceChild("body302",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.9269f, 0.0f, -1.0704f, 25.8539f, 1.224f, 2.1408f),
                PartPose.offset(0.0f, 20.464f, 31.443f));
        root.addOrReplaceChild("body303",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.2731f, 0.0f, -1.0704f, 24.5461f, 1.224f, 2.1408f),
                PartPose.offset(0.0f, 21.688f, 31.443f));
        root.addOrReplaceChild("body304",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-8.5704f, 0.0f, -1.0704f, 17.1409f, 1.088f, 2.1408f),
                PartPose.offset(0.0f, 14.208f, 33.45f));
        root.addOrReplaceChild("body305",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.5355f, 0.0f, -1.0704f, 21.0711f, 1.088f, 2.1408f),
                PartPose.offset(0.0f, 15.296f, 33.45f));
        root.addOrReplaceChild("body306",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.3899f, 0.0f, -1.0704f, 22.7799f, 1.088f, 2.1408f),
                PartPose.offset(0.0f, 16.384f, 33.45f));
        root.addOrReplaceChild("body307",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.601f, 0.0f, -1.0704f, 23.202f, 1.088f, 2.1408f),
                PartPose.offset(0.0f, 17.472f, 33.45f));
        root.addOrReplaceChild("body308",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.5044f, 0.0f, -1.0704f, 23.0089f, 1.088f, 2.1408f),
                PartPose.offset(0.0f, 18.56f, 33.45f));
        root.addOrReplaceChild("body309",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.257f, 0.0f, -1.0704f, 22.514f, 1.088f, 2.1408f),
                PartPose.offset(0.0f, 19.648f, 33.45f));
        root.addOrReplaceChild("body310",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.8586f, 0.0f, -1.0704f, 21.7172f, 1.088f, 2.1408f),
                PartPose.offset(0.0f, 20.736f, 33.45f));
        root.addOrReplaceChild("body311",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.3094f, 0.0f, -1.0704f, 20.6187f, 1.088f, 2.1408f),
                PartPose.offset(0.0f, 21.824f, 33.45f));
        root.addOrReplaceChild("body312",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-6.938f, 0.0f, -1.0704f, 13.8759f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 15.296f, 35.0556f));
        root.addOrReplaceChild("body313",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-8.5288f, 0.0f, -1.0704f, 17.0575f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 16.248f, 35.0556f));
        root.addOrReplaceChild("body314",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-9.2204f, 0.0f, -1.0704f, 18.4408f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 17.2f, 35.0556f));
        root.addOrReplaceChild("body315",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-9.3913f, 0.0f, -1.0704f, 18.7826f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 18.152f, 35.0556f));
        root.addOrReplaceChild("body316",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-9.3131f, 0.0f, -1.0704f, 18.6263f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 19.104f, 35.0556f));
        root.addOrReplaceChild("body317",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-9.1128f, 0.0f, -1.0704f, 18.2256f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 20.056f, 35.0556f));
        root.addOrReplaceChild("body318",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-8.7903f, 0.0f, -1.0704f, 17.5806f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 21.008f, 35.0556f));
        root.addOrReplaceChild("body319",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-8.3457f, 0.0f, -1.0704f, 16.6914f, 0.952f, 2.1408f),
                PartPose.offset(0.0f, 21.96f, 35.0556f));
        root.addOrReplaceChild("seam_left",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.442f, -0.68f, -12.042f, 0.884f, 1.36f, 24.084f),
                PartPose.offset(17.017f, 15.296f, -1.338f));
        root.addOrReplaceChild("seam_right",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.442f, -0.68f, -12.042f, 0.884f, 1.36f, 24.084f),
                PartPose.offset(-17.017f, 15.296f, -1.338f));
        root.addOrReplaceChild("vent_l0",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5525f, -1.36f, -0.669f, 1.105f, 2.72f, 1.338f),
                PartPose.offset(16.9065f, 14.752f, -21.9432f));
        root.addOrReplaceChild("vent_r0",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5525f, -1.36f, -0.669f, 1.105f, 2.72f, 1.338f),
                PartPose.offset(-16.9065f, 14.752f, -21.9432f));
        root.addOrReplaceChild("vent_l1",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5525f, -1.36f, -0.669f, 1.105f, 2.72f, 1.338f),
                PartPose.offset(16.9065f, 14.752f, -19.8024f));
        root.addOrReplaceChild("vent_r1",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5525f, -1.36f, -0.669f, 1.105f, 2.72f, 1.338f),
                PartPose.offset(-16.9065f, 14.752f, -19.8024f));
        root.addOrReplaceChild("vent_l2",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5525f, -1.36f, -0.669f, 1.105f, 2.72f, 1.338f),
                PartPose.offset(16.9065f, 14.752f, -17.6616f));
        root.addOrReplaceChild("vent_r2",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.5525f, -1.36f, -0.669f, 1.105f, 2.72f, 1.338f),
                PartPose.offset(-16.9065f, 14.752f, -17.6616f));
        root.addOrReplaceChild("grille",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-7.735f, -2.176f, -1.0704f, 15.47f, 4.352f, 2.1408f),
                PartPose.offset(0.0f, 19.92f, -44.0202f));
        root.addOrReplaceChild("slat0",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.2762f, -1.904f, -0.5352f, 0.5525f, 3.808f, 1.0704f),
                PartPose.offset(-5.746f, 19.92f, -44.5554f));
        root.addOrReplaceChild("slat1",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.2762f, -1.904f, -0.5352f, 0.5525f, 3.808f, 1.0704f),
                PartPose.offset(-2.873f, 19.92f, -44.5554f));
        root.addOrReplaceChild("slat2",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.2762f, -1.904f, -0.5352f, 0.5525f, 3.808f, 1.0704f),
                PartPose.offset(0.0f, 19.92f, -44.5554f));
        root.addOrReplaceChild("slat3",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.2762f, -1.904f, -0.5352f, 0.5525f, 3.808f, 1.0704f),
                PartPose.offset(2.873f, 19.92f, -44.5554f));
        root.addOrReplaceChild("slat4",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.2762f, -1.904f, -0.5352f, 0.5525f, 3.808f, 1.0704f),
                PartPose.offset(5.746f, 19.92f, -44.5554f));
        root.addOrReplaceChild("shoe_left",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.4972f, -1.088f, -0.3345f, 0.9945f, 2.176f, 0.669f),
                PartPose.offset(-1.4365f, 19.24f, -44.9568f));
        root.addOrReplaceChild("shoe_right",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.4972f, -1.088f, -0.3345f, 0.9945f, 2.176f, 0.669f),
                PartPose.offset(1.4365f, 19.24f, -44.9568f));
        root.addOrReplaceChild("shoe_b",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.9337f, -0.544f, -0.3345f, 3.8675f, 1.088f, 0.669f),
                PartPose.offset(0.0f, 20.872f, -44.9568f));
        root.addOrReplaceChild("lamp_left",
                CubeListBuilder.create().texOffs(LAMP, LAMP_V)
                        .addBox(-2.21f, -0.952f, -0.8028f, 4.42f, 1.904f, 1.6056f),
                PartPose.offsetAndRotation(8.84f, 18.424f, -42.2808f, 0f, 0f, -8f * Mth.DEG_TO_RAD));
        root.addOrReplaceChild("lamp_right",
                CubeListBuilder.create().texOffs(LAMP, LAMP_V)
                        .addBox(-2.21f, -0.952f, -0.8028f, 4.42f, 1.904f, 1.6056f),
                PartPose.offsetAndRotation(-8.84f, 18.424f, -42.2808f, 0f, 0f, 8f * Mth.DEG_TO_RAD));
        root.addOrReplaceChild("duct_left",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.21f, -1.088f, -0.8028f, 4.42f, 2.176f, 1.6056f),
                PartPose.offset(8.84f, 21.552f, -42.2808f));
        root.addOrReplaceChild("duct_right",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-2.21f, -1.088f, -0.8028f, 4.42f, 2.176f, 1.6056f),
                PartPose.offset(-8.84f, 21.552f, -42.2808f));
        root.addOrReplaceChild("tail_left",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-5.746f, -1.496f, -0.669f, 5.746f, 2.992f, 1.338f),
                PartPose.offset(9.503f, 16.384f, 35.5908f));
        root.addOrReplaceChild("tail_right",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(0.0f, -1.496f, -0.669f, 5.746f, 2.992f, 1.338f),
                PartPose.offset(-9.503f, 16.384f, 35.5908f));
        root.addOrReplaceChild("pipe_left",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-2.873f, -1.224f, -1.2042f, 2.873f, 2.448f, 2.4084f),
                PartPose.offset(8.177f, 21.824f, 35.8584f));
        root.addOrReplaceChild("pipe_right",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(0.0f, -1.224f, -1.2042f, 2.873f, 2.448f, 2.4084f),
                PartPose.offset(-8.177f, 21.824f, 35.8584f));
        root.addOrReplaceChild("lip",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-14.365f, -0.816f, -1.8732f, 28.73f, 1.632f, 3.7464f),
                PartPose.offsetAndRotation(0.0f, 11.76f, 32.6472f, -12f * Mth.DEG_TO_RAD, 0f, 0f));
        PartDefinition wheelFl = root.addOrReplaceChild("wheel_fl",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1692f, -5.5777f, -5.5777f, 6.3383f, 11.1555f, 11.1555f),
                PartPose.offset(15.47f, 16.928f, -29.436f));
        PartDefinition wheelFr = root.addOrReplaceChild("wheel_fr",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1692f, -5.5777f, -5.5777f, 6.3383f, 11.1555f, 11.1555f),
                PartPose.offset(-15.47f, 16.928f, -29.436f));
        PartDefinition wheelRl = root.addOrReplaceChild("wheel_rl",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1692f, -5.5777f, -5.5777f, 6.3383f, 11.1555f, 11.1555f),
                PartPose.offset(15.47f, 16.928f, 26.76f));
        PartDefinition wheelRr = root.addOrReplaceChild("wheel_rr",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1692f, -5.5777f, -5.5777f, 6.3383f, 11.1555f, 11.1555f),
                PartPose.offset(-15.47f, 16.928f, 26.76f));
        wheelFl.addOrReplaceChild("rim_fl",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-3.4227f, -3.1692f, -3.1692f, 6.8454f, 6.3383f, 6.3383f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("hub_fl",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.5495f, -1.3944f, -1.3944f, 7.0989f, 2.7889f, 2.7889f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("cal_fl",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-3.6128f, -2.155f, -0.6338f, 1.1409f, 4.3101f, 1.2677f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("rim_fr",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-3.4227f, -3.1692f, -3.1692f, 6.8454f, 6.3383f, 6.3383f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("hub_fr",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.5495f, -1.3944f, -1.3944f, 7.0989f, 2.7889f, 2.7889f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("cal_fr",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(2.472f, -2.155f, -0.6338f, 1.1409f, 4.3101f, 1.2677f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("rim_rl",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-3.4227f, -3.1692f, -3.1692f, 6.8454f, 6.3383f, 6.3383f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("hub_rl",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.5495f, -1.3944f, -1.3944f, 7.0989f, 2.7889f, 2.7889f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("cal_rl",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-3.6128f, -2.155f, -0.6338f, 1.1409f, 4.3101f, 1.2677f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("rim_rr",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-3.4227f, -3.1692f, -3.1692f, 6.8454f, 6.3383f, 6.3383f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("hub_rr",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.5495f, -1.3944f, -1.3944f, 7.0989f, 2.7889f, 2.7889f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("cal_rr",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(2.472f, -2.155f, -0.6338f, 1.1409f, 4.3101f, 1.2677f),
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
