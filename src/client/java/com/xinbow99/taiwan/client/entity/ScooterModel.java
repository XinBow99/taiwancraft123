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
 * 速克達的模型。數字跟 {@code tools/models.js} 的 {@code scooter} 一字不差。
 *
 * <h2>側面剪影決定它是不是速克達</h2>
 * <p>而剪影的關鍵是**前護板與座墊之間那個缺口**：一片直立的護板、一塊很低的平踏板、
 * 然後車尾才升起來。第一版的座墊往前多伸了兩格，缺口被填掉，整台就讀成一塊黑磚。
 * 那是在 {@code node tools/render.mjs scooter} 的算圖上看出來的，不是在遊戲裡。
 *
 * <h2>貼圖用「色票」而不是逐件拼 UV</h2>
 * <p>這台車有二十幾個零件，逐件排 UV 要一張 128×128 排得滿滿的，而且改一個尺寸就要重排。
 * 現在的貼圖是八塊 64×32 的純色色票，每個零件指到自己顏色的那一塊——**盒子只要落在
 * 同一塊純色區域裡，取樣到的就是那個顏色**，不管它多大。
 *
 * <p>代價是暫時畫不出細節（車殼上的線條、輪胎的花紋）。等真的要畫貼圖時再逐件排 UV，
 * 那時候尺寸已經定案了，排一次就不用再動。
 */
public class ScooterModel extends EntityModel<VehicleRenderState> {

    // 八塊色票的左上角。與 texture 產生器裡的配置一一對應
    private static final int BODY = 0,    BODY_V = 0;
    private static final int DARK = 64,   DARK_V = 0;
    private static final int SEAT = 0,    SEAT_V = 32;
    private static final int TIRE = 64,   TIRE_V = 32;
    private static final int CHROME = 0,  CHROME_V = 64;
    private static final int LAMP = 64,   LAMP_V = 64;
    private static final int AMBER = 0,   AMBER_V = 96;
    private static final int BRAKE = 64,  BRAKE_V = 96;

    private final ModelPart steer;
    private final ModelPart wheelFront;
    private final ModelPart wheelRear;

    public ScooterModel(ModelPart root) {
        // **明確指定不透明且會剔除背面的算繪型別。**
        // EntityModel 預設是 cutout-no-cull：不剔除背面的話，車殼內側的面會跟外側一起畫，
        // 兩者深度幾乎重疊就開始互相穿插——看起來就像整台車是半透明的。
        // 機車沒有任何一片需要鏤空或透明，用 entitySolid 最單純也最穩。
        super(root, RenderTypes::entitySolid);
        this.steer = root.getChild("steer");
        this.wheelFront = this.steer.getChild("wheel_front");
        this.wheelRear = root.getChild("wheel_rear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 踏板：低、平。可以站兩隻腳、可以放一袋菜——這是速克達跟打檔車最大的差別
        root.addOrReplaceChild("deck",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-4.5f, 0.0f, -4.5f, 9, 1, 9),
                PartPose.offset(0.0f, 20.0f, 1.0f));

        // 腿部護板，往後傾 12 度
        PartDefinition shield = root.addOrReplaceChild("shield",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.0f, -11.0f, -1.5f, 8, 11, 3),
                PartPose.offsetAndRotation(0.0f, 20.5f, -4.5f, 12f * Mth.DEG_TO_RAD, 0f, 0f));
        shield.addOrReplaceChild("shield_top",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.0f, -12.5f, -1.2f, 6, 2, 2),
                PartPose.ZERO);

        // 圓頭燈。速克達的臉就是這一顆
        PartDefinition lamp = root.addOrReplaceChild("lamp_case",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-3.0f, -3.0f, -1.5f, 6, 6, 3),
                PartPose.offsetAndRotation(0.0f, 11.5f, -7.0f, 12f * Mth.DEG_TO_RAD, 0f, 0f));
        lamp.addOrReplaceChild("lens",
                CubeListBuilder.create().texOffs(LAMP, LAMP_V)
                        .addBox(-2.0f, -2.0f, -1.4f, 4, 4, 1),
                PartPose.offset(0.0f, 0.0f, -1.5f));

        root.addOrReplaceChild("blink_left",
                CubeListBuilder.create().texOffs(AMBER, AMBER_V)
                        .addBox(-0.5f, -1.0f, -1.0f, 1, 2, 2),
                PartPose.offsetAndRotation(4.2f, 15.5f, -5.5f, 12f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("blink_right",
                CubeListBuilder.create().texOffs(AMBER, AMBER_V)
                        .addBox(-0.5f, -1.0f, -1.0f, 1, 2, 2),
                PartPose.offsetAndRotation(-4.2f, 15.5f, -5.5f, 12f * Mth.DEG_TO_RAD, 0f, 0f));

        // 座墊往後退到缺口之後才開始
        root.addOrReplaceChild("seat",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.0f, -3.0f, -5.0f, 8, 3, 10),
                PartPose.offsetAndRotation(0.0f, 14.0f, 5.0f, -3f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("tail_body",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.5f, 0.0f, -5.0f, 9, 6, 10),
                PartPose.offset(0.0f, 14.0f, 5.0f));

        // 靠背兼貨架。台灣的車幾乎每台都有
        PartDefinition rack = root.addOrReplaceChild("rack",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.5f, -1.0f, 0.0f, 7, 1, 3),
                PartPose.offsetAndRotation(0.0f, 13.0f, 10.0f, -8f * Mth.DEG_TO_RAD, 0f, 0f));
        rack.addOrReplaceChild("rack_bar",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.0f, -0.8f, 2.6f, 6, 1, 1),
                PartPose.ZERO);

        root.addOrReplaceChild("exhaust",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(0.0f, -1.0f, 0.0f, 2, 2, 7),
                PartPose.offset(3.2f, 20.0f, 3.0f));

        // ---- 會跟著龍頭轉的一整組 ----
        PartDefinition steer = root.addOrReplaceChild("steer",
                CubeListBuilder.create(), PartPose.offset(0.0f, 10.0f, -7.5f));

        PartDefinition bar = steer.addOrReplaceChild("bar",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-6.5f, -1.0f, -1.0f, 13, 2, 2),
                PartPose.offset(0.0f, -1.5f, 0.0f));
        bar.addOrReplaceChild("grip_left",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(4.7f, -1.2f, -1.2f, 2, 2, 2), PartPose.ZERO);
        bar.addOrReplaceChild("grip_right",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-6.7f, -1.2f, -1.2f, 2, 2, 2), PartPose.ZERO);

        addMirror(steer, "left", 5.0f, -12f);
        addMirror(steer, "right", -5.0f, 12f);

        steer.addOrReplaceChild("fork_left",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.7f, 0.0f, -0.7f, 1, 10, 1),
                PartPose.offsetAndRotation(2.2f, 0.0f, 0.0f, 12f * Mth.DEG_TO_RAD, 0f, 0f));
        steer.addOrReplaceChild("fork_right",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.7f, 0.0f, -0.7f, 1, 10, 1),
                PartPose.offsetAndRotation(-2.2f, 0.0f, 0.0f, 12f * Mth.DEG_TO_RAD, 0f, 0f));
        steer.addOrReplaceChild("fender",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-2.5f, -1.5f, -4.0f, 5, 1, 8),
                PartPose.offset(0.0f, 6.0f, -1.5f));

        PartDefinition front = steer.addOrReplaceChild("wheel_front",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.5f, -3.5f, -3.5f, 3, 7, 7),
                PartPose.offset(0.0f, 10.5f, -1.5f));
        front.addOrReplaceChild("hub_front",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.7f, -1.8f, -1.8f, 3, 4, 4), PartPose.ZERO);
        front.addOrReplaceChild("brake_front",
                CubeListBuilder.create().texOffs(BRAKE, BRAKE_V)
                        .addBox(1.5f, -0.9f, -0.9f, 1, 2, 2), PartPose.ZERO);

        PartDefinition rear = root.addOrReplaceChild("wheel_rear",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.75f, -3.5f, -3.5f, 4, 7, 7),
                PartPose.offset(0.0f, 20.5f, 8.0f));
        rear.addOrReplaceChild("hub_rear",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-2.0f, -1.8f, -1.8f, 4, 4, 4), PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void addMirror(PartDefinition steer, String side, float x, float tilt) {
        PartDefinition stem = steer.addOrReplaceChild("stem_" + side,
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.4f, -4.0f, -0.4f, 1, 4, 1),
                PartPose.offset(x, -1.5f, 0.0f));
        stem.addOrReplaceChild("mirror_" + side,
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-1.5f, -2.0f, -0.5f, 3, 2, 1),
                PartPose.offsetAndRotation(Math.signum(x) * 0.4f, -4.0f, 0.0f,
                        0f, 0f, tilt * Mth.DEG_TO_RAD));
    }

    @Override
    public void setupAnim(VehicleRenderState state) {
        super.setupAnim(state);
        // 龍頭跟著轉向。夾在 ±45 度，跟實體的滿舵一致——夾在 ±30 的話，低速打死方向時
        // 車已經在繞小圈了，畫面上的龍頭卻停在 30 度不動，看起來像是車自己在轉
        this.steer.yRot = Mth.clamp(state.steer, -45f, 45f) * Mth.DEG_TO_RAD;
        // 輪子跟著速度滾。不轉的輪子會讓整台車看起來是被拖著走的
        this.wheelFront.xRot = state.wheelSpin;
        this.wheelRear.xRot = state.wheelSpin;
    }
}
