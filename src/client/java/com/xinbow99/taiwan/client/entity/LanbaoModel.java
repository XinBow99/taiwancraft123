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
 * 藍爆堅尼的模型。
 *
 * <h2>名字</h2>
 * <p>照這個專案招牌店名的同一套規矩——**同音替字**（見 {@code ShopName}）：保留語感節奏、
 * 換掉字，讀者認得出是哪一類，但不是任何一個真實商標。
 *
 * <h2>幾何從哪來</h2>
 * <p>不是手刻的，也不是照剪影猜的，是把一台真車的 GLB **體素化**出來的：
 * <ol>
 *   <li>走 glTF 的節點樹，一路把 4×4 矩陣乘下去，把所有三角形換算到世界座標；
 *   <li>用重心座標在三角形上取樣，打進一個 60×13×29 的體素網格（車長 60 格）；
 *   <li>沿**車寬**與**車高**把每一條體素柱從最外層填實——車殼內部看不到，填實之後
 *       才合得起來。輪拱不會被填掉，因為它是向下開口的，柱子的最低點就在拱緣；
 *   <li>greedy 合併成長方體，合併時**同色才合**，這樣每一顆方塊只有一個顏色。
 * </ol>
 *
 * <p>材質先降維成八個色票類別（車漆／暗色／玻璃／燈罩／紅／胎／輪圈／鍍鉻），再跑兩輪
 * 3×3×3 多數決去雜訊。不去雜訊的話光是材質邊界的鋸齒就會把方塊數從 528 撐到 1600 以上——
 * 顏色差一格看不出來，方塊數差三倍看得出來。
 *
 * <p>代價是尾燈那幾顆紅像素太少，撐不過多數決，所以尾燈是最後手工補回去的兩條。
 *
 * <h2>座標對應</h2>
 * <p>一格體素 = 1.2 個模型單位，所以車長 72 單位（4.5 格）、車高 15.6、車寬 34.8，
 * 對得上 {@code VehicleModel.LANBAO} 的 2.2 × 1.3 碰撞箱。GLB 的 +X 是車頭，模型的車頭是
 * −Z，所以那一軸要翻號；模型的 +Y 朝下、地面在 y=24。
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

    /** 前輪的最大轉角（度）。比機車小很多——汽車的方向盤打得再滿，輪子也只轉這麼多。 */
    private static final float STEER_MAX = 30.0f;

    private final ModelPart wheelFl;
    private final ModelPart wheelFr;
    private final ModelPart wheelRl;
    private final ModelPart wheelRr;

    public LanbaoModel(ModelPart root) {
        // 理由同機車：預設的 cutout-no-cull 會讓車殼內外側一起畫、互相穿插，
        // 看起來像整台車是半透明的
        super(root, RenderTypes::entitySolid);
        this.wheelFl = root.getChild("wheel_fl");
        this.wheelFr = root.getChild("wheel_fr");
        this.wheelRl = root.getChild("wheel_rl");
        this.wheelRr = root.getChild("wheel_rr");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("b0",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 12.0000f, 15.6000f, 1.2000f, 8.4000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b1",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 13.2000f, 16.8000f, 6.0000f, 7.2000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b2",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 15.6000f, 14.4000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b3",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 15.6000f, -7.2000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b4",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b5",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 15.6000f, 16.8000f, 2.4000f, 1.2000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b6",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 15.6000f, -3.6000f, 2.4000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b7",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 15.6000f, -24.0000f, 2.4000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b8",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 15.6000f, 15.6000f, 2.4000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b9",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 15.6000f, -6.0000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b10",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 15.6000f, -27.6000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b11",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 15.6000f, 6.0000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b12",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 15.6000f, -24.0000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b13",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 14.4000f, 7.2000f, 2.4000f, 1.2000f, 28.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b14",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 14.4000f, -22.8000f, 2.4000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b15",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 14.4000f, 6.0000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b16",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 14.4000f, -24.0000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b17",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 14.4000f, 7.2000f, 1.2000f, 1.2000f, 28.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b18",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 14.4000f, -22.8000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b19",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 13.2000f, 13.2000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b20",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 13.2000f, -9.6000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b21",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 13.2000f, 13.2000f, 2.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b22",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 13.2000f, -9.6000f, 2.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b23",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 12.0000f, 18.0000f, 1.2000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b24",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 12.0000f, -1.2000f, 1.2000f, 1.2000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b25",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 12.0000f, 27.6000f, 8.4000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b26",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 18.0000f, 12.0000f, 1.2000f, 2.4000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b27",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 18.0000f, -10.8000f, 1.2000f, 2.4000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b28",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 18.0000f, -34.8000f, 1.2000f, 2.4000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b29",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 19.2000f, 12.0000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b30",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 19.2000f, -12.0000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b31",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 19.2000f, -36.0000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b32",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 16.8000f, 15.6000f, 2.4000f, 3.6000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b33",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 16.8000f, 15.6000f, 1.2000f, 2.4000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b34",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 16.8000f, 13.2000f, 4.8000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b35",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 16.8000f, -8.4000f, 4.8000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b36",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 16.8000f, -31.2000f, 4.8000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b37",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 16.8000f, 13.2000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b38",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 16.8000f, -9.6000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b39",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 16.8000f, -32.4000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b40",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 16.8000f, 13.2000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b41",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 16.8000f, -8.4000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b42",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 16.8000f, -31.2000f, 2.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b43",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 15.6000f, 13.2000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b44",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 15.6000f, -8.4000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b45",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b46",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 15.6000f, 13.2000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b47",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 15.6000f, -8.4000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b48",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b49",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 14.4000f, 7.2000f, 1.2000f, 1.2000f, 27.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b50",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 14.4000f, -21.6000f, 1.2000f, 1.2000f, 28.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b51",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 14.4000f, 7.2000f, 1.2000f, 1.2000f, 27.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b52",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 14.4000f, -21.6000f, 1.2000f, 1.2000f, 28.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b53",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 14.4000f, 8.4000f, 1.2000f, 1.2000f, 26.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b54",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 14.4000f, -19.2000f, 1.2000f, 1.2000f, 27.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b55",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 13.2000f, 13.2000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b56",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 13.2000f, -9.6000f, 2.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b57",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 13.2000f, 13.2000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b58",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 13.2000f, -9.6000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b59",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 13.2000f, 13.2000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b60",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 13.2000f, -8.4000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b61",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 12.0000f, 16.8000f, 2.4000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b62",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 12.0000f, -1.2000f, 2.4000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b63",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 12.0000f, 16.8000f, 1.2000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b64",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 12.0000f, -1.2000f, 1.2000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b65",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 20.4000f, 10.8000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b66",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 20.4000f, -12.0000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b67",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 20.4000f, -36.0000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b68",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 20.4000f, 15.6000f, 7.2000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b69",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 19.2000f, 10.8000f, 2.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b70",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 19.2000f, -12.0000f, 2.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b71",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 19.2000f, -34.8000f, 2.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b72",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 18.0000f, 10.8000f, 1.2000f, 2.4000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b73",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 18.0000f, -12.0000f, 1.2000f, 2.4000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b74",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 18.0000f, -34.8000f, 1.2000f, 2.4000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b75",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 19.2000f, 10.8000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b76",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 19.2000f, -12.0000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b77",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 19.2000f, -34.8000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b78",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 18.0000f, 12.0000f, 3.6000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b79",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 18.0000f, -10.8000f, 3.6000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b80",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 18.0000f, -33.6000f, 3.6000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b81",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 18.0000f, 12.0000f, 3.6000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b82",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 18.0000f, -10.8000f, 3.6000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b83",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 18.0000f, -33.6000f, 3.6000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b84",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 16.8000f, 25.2000f, 1.2000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b85",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 16.8000f, 12.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b86",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 16.8000f, -9.6000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b87",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 16.8000f, -31.2000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b88",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 15.6000f, 13.2000f, 1.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b89",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 15.6000f, -8.4000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b90",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b91",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 15.6000f, 13.2000f, 1.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b92",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 15.6000f, -8.4000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b93",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b94",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 14.4000f, 3.6000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b95",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 31.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b96",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 14.4000f, 6.0000f, 1.2000f, 1.2000f, 27.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b97",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 14.4000f, -22.8000f, 1.2000f, 1.2000f, 28.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b98",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 14.4000f, 8.4000f, 1.2000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b99",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 14.4000f, -18.0000f, 1.2000f, 1.2000f, 26.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b100",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 13.2000f, 12.0000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b101",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 13.2000f, -9.6000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b102",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 13.2000f, 13.2000f, 1.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b103",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 13.2000f, -7.2000f, 1.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b104",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 12.0000f, 16.8000f, 2.4000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b105",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 12.0000f, -1.2000f, 2.4000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b106",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 12.0000f, 16.8000f, 2.4000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b107",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 12.0000f, -1.2000f, 2.4000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b108",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 10.8000f, 16.8000f, 2.4000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b109",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 10.8000f, 0.0000f, 2.4000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b110",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 10.8000f, 27.6000f, 8.4000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b111",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 19.2000f, 10.8000f, 1.2000f, 3.6000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b112",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 19.2000f, -10.8000f, 1.2000f, 3.6000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b113",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 19.2000f, -33.6000f, 1.2000f, 3.6000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b114",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 20.4000f, 10.8000f, 4.8000f, 2.4000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b115",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 20.4000f, -12.0000f, 4.8000f, 2.4000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b116",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 20.4000f, -34.8000f, 4.8000f, 2.4000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b117",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 21.6000f, 9.6000f, 8.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b118",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 21.6000f, -13.2000f, 8.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b119",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 21.6000f, -36.0000f, 8.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b120",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 20.4000f, 10.8000f, 3.6000f, 2.4000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b121",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 20.4000f, -12.0000f, 3.6000f, 2.4000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b122",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 20.4000f, -34.8000f, 3.6000f, 2.4000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b123",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 19.2000f, 10.8000f, 1.2000f, 3.6000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b124",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 19.2000f, -10.8000f, 1.2000f, 3.6000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b125",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 19.2000f, -33.6000f, 1.2000f, 3.6000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b126",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 19.2000f, 25.2000f, 1.2000f, 3.6000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b127",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 18.0000f, 25.2000f, 1.2000f, 2.4000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b128",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 16.8000f, 25.2000f, 1.2000f, 2.4000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b129",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 13.2000f, 25.2000f, 1.2000f, 6.0000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b130",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 16.8000f, 25.2000f, 1.2000f, 2.4000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b131",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 12.0000f, 25.2000f, 1.2000f, 4.8000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b132",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 15.6000f, 12.0000f, 1.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b133",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 15.6000f, -8.4000f, 1.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b134",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b135",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 14.4000f, 2.4000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b136",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b137",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 13.2000f, 13.2000f, 1.2000f, 1.2000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b138",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 13.2000f, -7.2000f, 1.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b139",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 12.0000f, 15.6000f, 1.2000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b140",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 12.0000f, -2.4000f, 1.2000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b141",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 10.8000f, 16.8000f, 2.4000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b142",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 10.8000f, 0.0000f, 2.4000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b143",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 10.8000f, 16.8000f, 1.2000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b144",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 10.8000f, 0.0000f, 1.2000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b145",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 19.2000f, 25.2000f, 1.2000f, 3.6000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b146",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 20.4000f, 25.2000f, 1.2000f, 2.4000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b147",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 19.2000f, 25.2000f, 1.2000f, 3.6000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b148",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 13.2000f, 25.2000f, 1.2000f, 7.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b149",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 19.2000f, 25.2000f, 2.4000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b150",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 16.8000f, 25.2000f, 1.2000f, 2.4000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b151",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 16.8000f, 25.2000f, 1.2000f, 2.4000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b152",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 12.0000f, 25.2000f, 1.2000f, 4.8000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b153",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 12.0000f, 25.2000f, 1.2000f, 4.8000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b154",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 12.0000f, 15.6000f, 1.2000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b155",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 12.0000f, -1.2000f, 1.2000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b156",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 10.8000f, 15.6000f, 1.2000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b157",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 10.8000f, 0.0000f, 1.2000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b158",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 22.8000f, 8.4000f, 8.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b159",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 22.8000f, -13.2000f, 8.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b160",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 22.8000f, -36.0000f, 8.4000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b161",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 20.4000f, 25.2000f, 1.2000f, 2.4000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b162",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 20.4000f, 25.2000f, 2.4000f, 2.4000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b163",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 19.2000f, 25.2000f, 1.2000f, 2.4000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b164",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 14.4000f, 25.2000f, 1.2000f, 6.0000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b165",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 13.2000f, 25.2000f, 1.2000f, 7.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b166",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 16.8000f, 25.2000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b167",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 12.0000f, 25.2000f, 1.2000f, 4.8000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b168",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 12.0000f, 25.2000f, 1.2000f, 4.8000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b169",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 12.0000f, 2.4000f, 1.2000f, 1.2000f, 27.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b170",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 10.8000f, 0.0000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b171",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 10.8000f, 0.0000f, 1.2000f, 1.2000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b172",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 22.8000f, 8.4000f, 4.8000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b173",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 22.8000f, -13.2000f, 4.8000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b174",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 22.8000f, -34.8000f, 4.8000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b175",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 22.8000f, 7.2000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b176",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 22.8000f, -14.4000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b177",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 22.8000f, -36.0000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b178",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 22.8000f, 8.4000f, 2.4000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b179",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 22.8000f, -13.2000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b180",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 22.8000f, -34.8000f, 2.4000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b181",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 20.4000f, 25.2000f, 1.2000f, 2.4000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b182",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 21.6000f, 25.2000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b183",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 20.4000f, 25.2000f, 1.2000f, 2.4000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b184",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 18.0000f, 25.2000f, 1.2000f, 3.6000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b185",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 16.8000f, 25.2000f, 1.2000f, 2.4000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b186",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 14.4000f, 25.2000f, 1.2000f, 2.4000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b187",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 13.2000f, 22.8000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b188",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 12.0000f, 9.6000f, 1.2000f, 1.2000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b189",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 10.8000f, 0.0000f, 1.2000f, 1.2000f, 28.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b190",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 10.8000f, 4.8000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b191",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 19.2000f, 25.2000f, 1.2000f, 3.6000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b192",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 21.6000f, 25.2000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b193",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 14.4000f, 25.2000f, 1.2000f, 2.4000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b194",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 13.2000f, 22.8000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b195",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 12.0000f, 12.0000f, 1.2000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b196",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.0000f, 10.8000f, 25.2000f, 1.2000f, 2.4000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b197",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-1.8000f, 12.0000f, 25.2000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b198",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-0.6000f, 10.8000f, 22.8000f, 6.0000f, 2.4000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b199",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 10.8000f, 13.2000f, 4.8000f, 1.2000f, 14.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b200",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-1.8000f, 10.8000f, 0.0000f, 1.2000f, 1.2000f, 27.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b201",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 10.8000f, 6.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b202",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 10.8000f, 9.6000f, 1.2000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b203",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 10.8000f, 10.8000f, 1.2000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b204",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 13.2000f, 22.8000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b205",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 12.0000f, 15.6000f, 1.2000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b206",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 10.8000f, 15.6000f, 1.2000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b207",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 19.2000f, -3.6000f, 1.2000f, 2.4000f, 28.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b208",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 19.2000f, -33.6000f, 1.2000f, 2.4000f, 30.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b209",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 12.0000f, 22.8000f, 1.2000f, 2.4000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b210",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 13.2000f, 22.8000f, 3.6000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b211",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 13.2000f, 22.8000f, 4.8000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b212",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 12.0000f, 18.0000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b213",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 12.0000f, 0.0000f, 2.4000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b214",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 9.6000f, 20.4000f, 1.2000f, 3.6000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b215",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-1.8000f, 12.0000f, 24.0000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b216",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 12.0000f, 2.4000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b217",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 12.0000f, 3.6000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b218",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 10.8000f, 16.8000f, 1.2000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b219",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 9.6000f, 0.0000f, 6.0000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b220",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-1.8000f, 9.6000f, 13.2000f, 9.6000f, 1.2000f, 12.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b221",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-1.8000f, 9.6000f, 0.0000f, 9.6000f, 1.2000f, 13.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b222",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-1.8000f, 12.0000f, 19.2000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b223",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 9.6000f, 19.2000f, 4.8000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b224",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 9.6000f, 0.0000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b225",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 9.6000f, 19.2000f, 4.8000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b226",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 19.2000f, -4.8000f, 1.2000f, 3.6000f, 27.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b227",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 19.2000f, -33.6000f, 1.2000f, 3.6000f, 28.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b228",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 21.6000f, -4.8000f, 1.2000f, 1.2000f, 27.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b229",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 21.6000f, -33.6000f, 1.2000f, 1.2000f, 28.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b230",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 13.2000f, 15.6000f, 1.2000f, 6.0000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b231",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 12.0000f, 6.0000f, 1.2000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b232",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-0.6000f, 12.0000f, 19.2000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b233",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(0.6000f, 12.0000f, -1.2000f, 4.8000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b234",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-0.6000f, 10.8000f, 0.0000f, 6.0000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b235",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 10.8000f, 20.4000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b236",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 9.6000f, 20.4000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b237",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 13.2000f, 6.0000f, 1.2000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b238",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 13.2000f, -9.6000f, 1.2000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b239",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 13.2000f, -3.6000f, 2.4000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b240",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 13.2000f, -6.0000f, 1.2000f, 1.2000f, 26.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b241",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.0000f, 12.0000f, 19.2000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b242",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 9.6000f, 0.0000f, 1.2000f, 2.4000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b243",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 22.8000f, -7.2000f, 2.4000f, 1.2000f, 26.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b244",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 22.8000f, -33.6000f, 2.4000f, 1.2000f, 26.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b245",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 22.8000f, -7.2000f, 1.2000f, 1.2000f, 26.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b246",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 22.8000f, -34.8000f, 1.2000f, 1.2000f, 27.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b247",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 13.2000f, 15.6000f, 3.6000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b248",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 13.2000f, 0.0000f, 1.2000f, 1.2000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b249",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 13.2000f, 3.6000f, 1.2000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b250",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 13.2000f, 12.0000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b251",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 12.0000f, -1.2000f, 3.6000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b252",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 14.4000f, 15.6000f, 6.0000f, 9.6000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b253",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 20.4000f, -1.2000f, 1.2000f, 3.6000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b254",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 20.4000f, -21.6000f, 1.2000f, 3.6000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b255",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 20.4000f, 1.2000f, 3.6000f, 3.6000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b256",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 20.4000f, -16.8000f, 3.6000f, 3.6000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b257",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 20.4000f, 10.8000f, 1.2000f, 3.6000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b258",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 14.4000f, 15.6000f, 6.0000f, 6.0000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b259",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 9.6000f, 3.6000f, 1.2000f, 1.2000f, 14.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b260",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.0000f, 19.2000f, 13.2000f, 4.8000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b261",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(1.8000f, 13.2000f, 15.6000f, 1.2000f, 7.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b262",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 13.2000f, 15.6000f, 4.8000f, 6.0000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b263",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 21.6000f, 0.0000f, 4.8000f, 2.4000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b264",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 21.6000f, -16.8000f, 4.8000f, 2.4000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b265",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 20.4000f, 14.4000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b266",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 20.4000f, -6.0000f, 4.8000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b267",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.0000f, 20.4000f, 14.4000f, 4.8000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b268",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(1.8000f, 20.4000f, -9.6000f, 2.4000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b269",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(1.8000f, 20.4000f, -36.0000f, 2.4000f, 1.2000f, 26.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b270",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-17.4000f, 18.0000f, 14.4000f, 6.0000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b271",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-4.2000f, 18.0000f, 14.4000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b272",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(1.8000f, 18.0000f, 13.2000f, 1.2000f, 2.4000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b273",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(3.0000f, 18.0000f, 14.4000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b274",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 18.0000f, -9.6000f, 1.2000f, 2.4000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b275",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 18.0000f, -34.8000f, 1.2000f, 2.4000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b276",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(10.2000f, 18.0000f, 14.4000f, 6.0000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b277",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-11.4000f, 18.0000f, 14.4000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b278",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-5.4000f, 18.0000f, 14.4000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b279",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-3.0000f, 18.0000f, 13.2000f, 4.8000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b280",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 15.6000f, 13.2000f, 7.2000f, 2.4000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b281",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 16.8000f, -8.4000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b282",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 16.8000f, -32.4000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b283",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 16.8000f, -6.0000f, 9.6000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b284",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 16.8000f, -28.8000f, 9.6000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b285",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 16.8000f, -7.2000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b286",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 16.8000f, -31.2000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b287",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 14.4000f, 0.0000f, 1.2000f, 3.6000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b288",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 14.4000f, -16.8000f, 1.2000f, 3.6000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b289",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 15.6000f, 0.0000f, 2.4000f, 2.4000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b290",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 15.6000f, -16.8000f, 2.4000f, 2.4000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b291",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 16.8000f, 7.2000f, 1.2000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b292",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 15.6000f, -6.0000f, 7.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b293",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 15.6000f, -27.6000f, 7.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b294",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 15.6000f, -6.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b295",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 15.6000f, -28.8000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b296",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 15.6000f, 8.4000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b297",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 14.4000f, 14.4000f, 7.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b298",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 14.4000f, -3.6000f, 7.2000f, 1.2000f, 19.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b299",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 14.4000f, -24.0000f, 7.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b300",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 14.4000f, -6.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b301",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b302",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 14.4000f, -8.4000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b303",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 14.4000f, -2.4000f, 1.2000f, 1.2000f, 18.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b304",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 14.4000f, 9.6000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b305",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 13.2000f, 7.2000f, 2.4000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b306",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 13.2000f, -4.8000f, 1.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b307",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b308",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 13.2000f, 3.6000f, 7.2000f, 1.2000f, 12.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b309",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 13.2000f, -9.6000f, 7.2000f, 1.2000f, 13.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b310",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 12.0000f, -1.2000f, 1.2000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b311",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 19.2000f, 13.2000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b312",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 20.4000f, -10.8000f, 4.8000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b313",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 20.4000f, -36.0000f, 4.8000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b314",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 19.2000f, 3.6000f, 4.8000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b315",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 19.2000f, -10.8000f, 1.2000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b316",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 19.2000f, -36.0000f, 1.2000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b317",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 19.2000f, -10.8000f, 1.2000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b318",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(3.0000f, 19.2000f, -36.0000f, 1.2000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b319",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 19.2000f, -8.4000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b320",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 19.2000f, -32.4000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b321",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 19.2000f, -1.2000f, 3.6000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b322",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 19.2000f, -16.8000f, 3.6000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b323",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 18.0000f, 7.2000f, 1.2000f, 2.4000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b324",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-17.4000f, 18.0000f, 13.2000f, 6.0000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b325",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 18.0000f, -8.4000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b326",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 18.0000f, -32.4000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b327",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 18.0000f, -9.6000f, 1.2000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b328",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 18.0000f, -34.8000f, 1.2000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b329",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-4.2000f, 18.0000f, 13.2000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b330",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(3.0000f, 18.0000f, 13.2000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b331",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(10.2000f, 18.0000f, 13.2000f, 4.8000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b332",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 14.4000f, 3.6000f, 6.0000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b333",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 9.6000f, 4.8000f, 1.2000f, 1.2000f, 9.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b334",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-17.4000f, 18.0000f, 12.0000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b335",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 19.2000f, -10.8000f, 6.0000f, 1.2000f, 24.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b336",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 19.2000f, -36.0000f, 6.0000f, 1.2000f, 25.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b337",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 15.6000f, 3.6000f, 4.8000f, 3.6000f, 9.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b338",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 18.0000f, -2.4000f, 8.4000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b339",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 18.0000f, -18.0000f, 8.4000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b340",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 18.0000f, -34.8000f, 8.4000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b341",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 18.0000f, -9.6000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b342",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 18.0000f, -32.4000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b343",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 18.0000f, -1.2000f, 3.6000f, 1.2000f, 14.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b344",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 18.0000f, -16.8000f, 3.6000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b345",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 16.8000f, -8.4000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b346",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 16.8000f, -31.2000f, 1.2000f, 1.2000f, 22.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b347",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 15.6000f, -8.4000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b348",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 21.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b349",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 10.8000f, 9.6000f, 3.6000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b350",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 8.4000f, -1.2000f, 13.2000f, 1.2000f, 13.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b351",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 21.6000f, 3.6000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b352",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 20.4000f, 7.2000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b353",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 8.4000f, -1.2000f, 1.2000f, 1.2000f, 12.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b354",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 10.8000f, 6.0000f, 2.4000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b355",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 8.4000f, -1.2000f, 1.2000f, 1.2000f, 9.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b356",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 13.2000f, 1.2000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b357",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 15.6000f, -16.8000f, 3.6000f, 4.8000f, 20.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b358",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 14.4000f, -6.0000f, 4.8000f, 1.2000f, 9.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b359",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 8.4000f, -1.2000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b360",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 10.8000f, 0.0000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b361",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-11.4000f, 12.0000f, -2.4000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b362",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-10.2000f, 10.8000f, -12.0000f, 19.2000f, 1.2000f, 12.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b363",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-9.0000f, 9.6000f, -13.2000f, 16.8000f, 1.2000f, 13.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b364",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(7.8000f, 9.6000f, -10.8000f, 1.2000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b365",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-10.2000f, 12.0000f, -8.4000f, 19.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b366",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-10.2000f, 12.0000f, -16.8000f, 19.2000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b367",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(9.0000f, 10.8000f, -8.4000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b368",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-9.0000f, 8.4000f, -4.8000f, 8.4000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b369",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-0.6000f, 8.4000f, -3.6000f, 8.4000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b370",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-11.4000f, 12.0000f, -9.6000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b371",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(9.0000f, 12.0000f, -13.2000f, 1.2000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b372",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 13.2000f, -9.6000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b373",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-0.6000f, 8.4000f, -10.8000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b374",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(0.6000f, 8.4000f, -4.8000f, 7.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b375",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-7.8000f, 8.4000f, -8.4000f, 6.0000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b376",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-1.8000f, 8.4000f, -9.6000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b377",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(0.6000f, 8.4000f, -9.6000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b378",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(1.8000f, 8.4000f, -10.8000f, 3.6000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b379",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(5.4000f, 8.4000f, -9.6000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b380",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(6.6000f, 8.4000f, -7.2000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b381",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 20.4000f, -16.8000f, 3.6000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b382",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 14.4000f, -12.0000f, 3.6000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b383",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 20.4000f, -9.6000f, 1.2000f, 2.4000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b384",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 16.8000f, -16.8000f, 1.2000f, 4.8000f, 9.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b385",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(7.8000f, 13.2000f, -20.4000f, 1.2000f, 1.2000f, 13.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b386",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(9.0000f, 13.2000f, -13.2000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b387",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 15.6000f, -10.8000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b388",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(6.6000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 16.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b389",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-6.6000f, 8.4000f, -10.8000f, 4.8000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b390",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 19.2000f, -10.8000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b391",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-10.2000f, 13.2000f, -14.4000f, 2.4000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b392",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-7.8000f, 13.2000f, -13.2000f, 14.4000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b393",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(10.2000f, 13.2000f, -12.0000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b394",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-1.8000f, 8.4000f, -10.8000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b395",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(0.6000f, 8.4000f, -10.8000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b396",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 16.8000f, -16.8000f, 1.2000f, 3.6000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b397",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 20.4000f, -16.8000f, 1.2000f, 2.4000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b398",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 14.4000f, -27.6000f, 2.4000f, 1.2000f, 15.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b399",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-9.0000f, 10.8000f, -16.8000f, 16.8000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b400",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(7.8000f, 10.8000f, -15.6000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b401",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 15.6000f, -16.8000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b402",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 14.4000f, -18.0000f, 2.4000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b403",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 14.4000f, -18.0000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b404",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 13.2000f, -18.0000f, 3.6000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b405",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-4.2000f, 13.2000f, -14.4000f, 10.8000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b406",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 12.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b407",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-7.8000f, 9.6000f, -14.4000f, 15.6000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b408",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 22.8000f, -16.8000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b409",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 15.6000f, -16.8000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b410",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 14.4000f, -18.0000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b411",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 14.4000f, -18.0000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b412",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 13.2000f, -25.2000f, 3.6000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b413",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b414",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 13.2000f, -16.8000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b415",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 13.2000f, -22.8000f, 8.4000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b416",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 13.2000f, -20.4000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b417",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 13.2000f, -18.0000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b418",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 13.2000f, -25.2000f, 3.6000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b419",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-6.6000f, 9.6000f, -15.6000f, 12.0000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b420",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 14.4000f, -18.0000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b421",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 9.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b422",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 15.6000f, -18.0000f, 1.2000f, 3.6000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b423",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 14.4000f, -20.4000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b424",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b425",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-9.0000f, 13.2000f, -24.0000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b426",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-9.0000f, 12.0000f, -22.8000f, 3.6000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b427",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-5.4000f, 12.0000f, -20.4000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b428",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-4.2000f, 10.8000f, -19.2000f, 9.6000f, 2.4000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b429",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(5.4000f, 12.0000f, -22.8000f, 2.4000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b430",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(7.8000f, 12.0000f, -19.2000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b431",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-7.8000f, 10.8000f, -18.0000f, 3.6000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b432",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(5.4000f, 10.8000f, -18.0000f, 2.4000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b433",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(7.8000f, 14.4000f, -21.6000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b434",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-7.8000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b435",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 13.2000f, -21.6000f, 2.4000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b436",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(5.4000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b437",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 13.2000f, -24.0000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b438",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 12.0000f, -22.8000f, 6.0000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b439",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(9.0000f, 12.0000f, -22.8000f, 4.8000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b440",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-6.6000f, 10.8000f, -19.2000f, 2.4000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b441",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(6.6000f, 14.4000f, -25.2000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b442",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 12.0000f, -21.6000f, 6.0000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b443",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(1.8000f, 12.0000f, -20.4000f, 3.6000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b444",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 12.0000f, -22.8000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b445",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 12.0000f, -22.8000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b446",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-0.6000f, 10.8000f, -20.4000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b447",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(4.2000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b448",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b449",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 12.0000f, -21.6000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b450",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 12.0000f, -21.6000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b451",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(1.8000f, 12.0000f, -21.6000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b452",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(3.0000f, 12.0000f, -22.8000f, 2.4000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b453",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-7.8000f, 14.4000f, -26.4000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b454",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(5.4000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b455",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(7.8000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b456",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-6.6000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b457",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 13.2000f, -22.8000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b458",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-5.4000f, 12.0000f, -22.8000f, 8.4000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b459",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 14.4000f, -26.4000f, 3.6000f, 9.6000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b460",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 22.8000f, -33.6000f, 1.2000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b461",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 22.8000f, -33.6000f, 2.4000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b462",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 20.4000f, -31.2000f, 1.2000f, 3.6000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b463",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 16.8000f, -28.8000f, 1.2000f, 7.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b464",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 14.4000f, -25.2000f, 1.2000f, 9.6000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b465",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 19.2000f, -32.4000f, 1.2000f, 3.6000f, 9.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b466",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(10.2000f, 20.4000f, -32.4000f, 2.4000f, 2.4000f, 9.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b467",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 19.2000f, -32.4000f, 1.2000f, 1.2000f, 9.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b468",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(12.6000f, 16.8000f, -30.0000f, 1.2000f, 3.6000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b469",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 18.0000f, -31.2000f, 1.2000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b470",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 16.8000f, -31.2000f, 1.2000f, 2.4000f, 8.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b471",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 16.8000f, -30.0000f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b472",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-12.6000f, 15.6000f, -28.8000f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b473",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 15.6000f, -28.8000f, 2.4000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b474",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 15.6000f, -27.6000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b475",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-9.0000f, 14.4000f, -24.0000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b476",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-6.6000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b477",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 14.4000f, -24.0000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b478",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(4.2000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b479",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(11.4000f, 14.4000f, -27.6000f, 2.4000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b480",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 14.4000f, -26.4000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b481",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-5.4000f, 13.2000f, -25.2000f, 9.6000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b482",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-6.6000f, 15.6000f, -25.2000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b483",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 15.6000f, -27.6000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b484",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(5.4000f, 15.6000f, -25.2000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b485",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b486",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-5.4000f, 14.4000f, -27.6000f, 2.4000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b487",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 14.4000f, -25.2000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b488",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-1.8000f, 14.4000f, -28.8000f, 3.6000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b489",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(1.8000f, 14.4000f, -27.6000f, 2.4000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b490",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-9.0000f, 13.2000f, -25.2000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b491",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 19.2000f, -27.6000f, 1.2000f, 3.6000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b492",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(15.0000f, 15.6000f, -26.4000f, 1.2000f, 3.6000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b493",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-6.6000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b494",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(5.4000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b495",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-3.0000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b496",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(6.6000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b497",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 19.2000f, -30.0000f, 2.4000f, 4.8000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b498",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 18.0000f, -28.8000f, 1.2000f, 4.8000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b499",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 15.6000f, -28.8000f, 2.4000f, 3.6000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b500",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-16.2000f, 15.6000f, -27.6000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b501",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 14.4000f, -27.6000f, 2.4000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b502",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 14.4000f, -27.6000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b503",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-5.4000f, 15.6000f, -30.0000f, 4.8000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b504",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-0.6000f, 15.6000f, -28.8000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b505",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(0.6000f, 15.6000f, -30.0000f, 3.6000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b506",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b507",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(13.8000f, 19.2000f, -30.0000f, 1.2000f, 3.6000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b508",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 16.8000f, -30.0000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b509",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-4.2000f, 16.8000f, -31.2000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b510",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-3.0000f, 16.8000f, -33.6000f, 7.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b511",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(4.2000f, 16.8000f, -32.4000f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b512",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-0.6000f, 15.6000f, -30.0000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b513",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-13.8000f, 19.2000f, -32.4000f, 1.2000f, 4.8000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b514",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-15.0000f, 20.4000f, -31.2000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b515",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-13.8000f, 16.8000f, -31.2000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b516",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-12.6000f, 16.8000f, -31.2000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b517",
                CubeListBuilder.create().texOffs(GLASS, GLASS_V)
                        .addBox(-3.0000f, 15.6000f, -31.2000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b518",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-1.8000f, 15.6000f, -31.2000f, 3.6000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b519",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(-12.6000f, 18.0000f, -32.4000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b520",
                CubeListBuilder.create().texOffs(DARK, DARK_V)
                        .addBox(11.4000f, 18.0000f, -32.4000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b521",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 16.8000f, -32.4000f, 2.4000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b522",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-4.2000f, 16.8000f, -33.6000f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b523",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-10.2000f, 22.8000f, -34.8000f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b524",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-7.8000f, 22.8000f, -36.0000f, 3.6000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b525",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-5.4000f, 20.4000f, -36.0000f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("b526",
                CubeListBuilder.create().texOffs(PAINT, PAINT_V)
                        .addBox(-1.8000f, 18.0000f, -36.0000f, 3.6000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        PartDefinition wheelFl = root.addOrReplaceChild("wheel_fl",
                CubeListBuilder.create(), PartPose.offset(-14.2600f, 18.8533f, -20.9033f));
        wheelFl.addOrReplaceChild("w527",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.9400f, 2.7467f, 2.9033f, 4.8000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w528",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.1400f, 1.5467f, -1.8967f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w529",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.9400f, 1.5467f, 4.1033f, 4.8000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w530",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.1400f, -0.8533f, 1.7033f, 2.4000f, 2.4000f, 3.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w531",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-0.7400f, -3.2533f, 4.1033f, 3.6000f, 4.8000f, 1.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w532",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, -3.2533f, 4.1033f, 2.4000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w533",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.9400f, 3.9467f, 1.7033f, 4.8000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w534",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, 2.7467f, 2.9033f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w535",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.9400f, 1.5467f, 0.5033f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w536",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-0.7400f, 1.5467f, -5.4967f, 3.6000f, 1.2000f, 9.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w537",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-0.7400f, 0.3467f, 1.7033f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w538",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(0.4600f, -3.2533f, -5.4967f, 2.4000f, 4.8000f, 9.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w539",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-0.7400f, -3.2533f, -5.4967f, 1.2000f, 3.6000f, 9.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w540",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.1400f, -2.0533f, 0.5033f, 2.4000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w541",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, -4.4533f, 1.7033f, 2.4000f, 2.4000f, 2.4000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w542",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-0.7400f, -4.4533f, -4.2967f, 3.6000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w543",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.1400f, 2.7467f, -1.8967f, 1.2000f, 2.4000f, 4.8000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w544",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.9400f, 2.7467f, -1.8967f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w545",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-0.7400f, 2.7467f, -4.2967f, 3.6000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w546",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.9400f, -5.6533f, -1.8967f, 3.6000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w547",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.9400f, 3.9467f, -0.6967f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w548",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-0.7400f, 3.9467f, -4.2967f, 2.4000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w549",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(1.6600f, 3.9467f, -3.0967f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w550",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(2.8600f, -2.0533f, -1.8967f, 1.2000f, 4.8000f, 3.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w551",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, -0.8533f, -5.4967f, 2.4000f, 2.4000f, 7.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w552",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-0.7400f, 0.3467f, -5.4967f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w553",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.1400f, -3.2533f, -1.8967f, 2.4000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w554",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, -4.4533f, -4.2967f, 2.4000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w555",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, -5.6533f, -1.8967f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w556",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(1.6600f, -5.6533f, -1.8967f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w557",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.9400f, 1.5467f, -5.4967f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w558",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, -2.0533f, -0.6967f, 2.4000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w559",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.9400f, 3.9467f, -4.2967f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w560",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.1400f, -2.0533f, -1.8967f, 2.4000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w561",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, 1.5467f, -3.0967f, 1.2000f, 3.6000f, 1.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w562",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.9400f, 2.7467f, -4.2967f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w563",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, -2.0533f, -5.4967f, 2.4000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w564",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, -3.2533f, -4.2967f, 2.4000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w565",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, 1.5467f, -4.2967f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w566",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.1400f, 1.5467f, -5.4967f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelFl.addOrReplaceChild("w567",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.9400f, -3.2533f, -5.4967f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        PartDefinition wheelFr = root.addOrReplaceChild("wheel_fr",
                CubeListBuilder.create(), PartPose.offset(13.5134f, 18.8828f, -20.8892f));
        wheelFr.addOrReplaceChild("w568",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.1134f, -3.2828f, -4.3108f, 3.6000f, 7.2000f, 9.6000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w569",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(1.4866f, 2.7172f, 1.6892f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w570",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(1.4866f, -3.2828f, 2.8892f, 1.2000f, 6.0000f, 2.4000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w571",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.1134f, 3.9172f, -4.3108f, 3.6000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w572",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(1.4866f, 3.9172f, 0.4892f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w573",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.3134f, 1.5172f, -3.1108f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w574",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.1134f, -4.4828f, -4.3108f, 4.8000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w575",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.3134f, 2.7172f, -1.9108f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w576",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(1.4866f, 1.5172f, -0.7108f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w577",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.3134f, -3.2828f, -3.1108f, 1.2000f, 4.8000f, 6.0000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w578",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(1.4866f, 0.3172f, 0.4892f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w579",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(1.4866f, -3.2828f, -5.5108f, 1.2000f, 3.6000f, 8.4000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w580",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.1134f, -5.6828f, -1.9108f, 4.8000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w581",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(1.4866f, 2.7172f, -0.7108f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w582",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(1.4866f, 3.9172f, -0.7108f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w583",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(1.4866f, 0.3172f, -5.5108f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w584",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(1.4866f, 1.5172f, -4.3108f, 1.2000f, 3.6000f, 3.6000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w585",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-2.1134f, -3.2828f, -5.5108f, 3.6000f, 6.0000f, 1.2000f),
                PartPose.ZERO);
        wheelFr.addOrReplaceChild("w586",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(1.4866f, 1.5172f, -5.5108f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        PartDefinition wheelRl = root.addOrReplaceChild("wheel_rl",
                CubeListBuilder.create(), PartPose.offset(-13.8397f, 18.4618f, 21.5962f));
        wheelRl.addOrReplaceChild("w587",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, 1.9382f, 3.6038f, 6.0000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w588",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, -2.8618f, 4.8038f, 6.0000f, 4.8000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w589",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, 3.1382f, 2.4038f, 6.0000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w590",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(2.4397f, -2.8618f, -4.7962f, 1.2000f, 6.0000f, 9.6000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w591",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.5603f, -1.6618f, 1.2038f, 2.4000f, 3.6000f, 3.6000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w592",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, -4.0618f, 3.6038f, 3.6000f, 6.0000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w593",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, -4.0618f, 3.6038f, 2.4000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w594",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, 4.3382f, 1.2038f, 6.0000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w595",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(2.4397f, 3.1382f, -3.5962f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w596",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.5603f, 1.9382f, -3.5962f, 2.4000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w597",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, 1.9382f, 2.4038f, 3.6000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w598",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.1603f, -1.6618f, 2.4038f, 1.2000f, 3.6000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w599",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(0.0397f, -2.8618f, -5.9962f, 2.4000f, 4.8000f, 9.6000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w600",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.5603f, -2.8618f, -3.5962f, 2.4000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w601",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, -5.2618f, 2.4038f, 1.2000f, 3.6000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w602",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, -5.2618f, 2.4038f, 2.4000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w603",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(0.0397f, -4.0618f, -4.7962f, 2.4000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w604",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(2.4397f, -4.0618f, -3.5962f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w605",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(0.0397f, -5.2618f, -3.5962f, 2.4000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w606",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.5603f, 3.1382f, -2.3962f, 2.4000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w607",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, 3.1382f, 1.2038f, 3.6000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w608",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.1603f, 1.9382f, 0.0038f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w609",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(0.0397f, 1.9382f, -5.9962f, 2.4000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w610",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, 0.7382f, -5.9962f, 1.2000f, 1.2000f, 8.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w611",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(3.6397f, -1.6618f, -2.3962f, 1.2000f, 3.6000f, 4.8000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w612",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, -1.6618f, -2.3962f, 1.2000f, 2.4000f, 4.8000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w613",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.1603f, -2.8618f, -1.1962f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w614",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.5603f, -4.0618f, -2.3962f, 2.4000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w615",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, -4.0618f, -4.7962f, 1.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w616",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, -5.2618f, -3.5962f, 3.6000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w617",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.5603f, 4.3382f, -1.1962f, 2.4000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w618",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, 4.3382f, -3.5962f, 3.6000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w619",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(2.4397f, 4.3382f, -1.1962f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w620",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.1603f, 3.1382f, 0.0038f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w621",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(0.0397f, 3.1382f, -4.7962f, 2.4000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w622",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-3.5603f, -1.6618f, -1.1962f, 1.2000f, 3.6000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w623",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-2.3603f, 0.7382f, -4.7962f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w624",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.3603f, -0.4618f, -1.1962f, 1.2000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w625",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-2.3603f, -1.6618f, -4.7962f, 1.2000f, 1.2000f, 6.0000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w626",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(2.4397f, -5.2618f, -2.3962f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w627",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, -6.4618f, -1.1962f, 6.0000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w628",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, 1.9382f, -4.7962f, 1.2000f, 2.4000f, 4.8000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w629",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, 4.3382f, -3.5962f, 2.4000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w630",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-3.5603f, -1.6618f, -4.7962f, 1.2000f, 3.6000f, 3.6000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w631",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-2.3603f, -0.4618f, -4.7962f, 1.2000f, 1.2000f, 3.6000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w632",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, -2.8618f, -5.9962f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w633",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, 3.1382f, -4.7962f, 2.4000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w634",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-1.1603f, -1.6618f, -3.5962f, 1.2000f, 2.4000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w635",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, -4.0618f, -4.7962f, 2.4000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w636",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, 1.9382f, -5.9962f, 2.4000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w637",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, -1.6618f, -5.9962f, 1.2000f, 2.4000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w638",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, -2.8618f, -5.9962f, 2.4000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w639",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.1603f, 1.9382f, -5.9962f, 1.2000f, 1.2000f, 1.2000f),
                PartPose.ZERO);
        wheelRl.addOrReplaceChild("w640",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.5603f, -1.6618f, -5.9962f, 2.4000f, 3.6000f, 1.2000f),
                PartPose.ZERO);
        PartDefinition wheelRr = root.addOrReplaceChild("wheel_rr",
                CubeListBuilder.create(), PartPose.offset(13.4437f, 18.4839f, 21.5547f));
        wheelRr.addOrReplaceChild("w641",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.2437f, -2.8839f, -5.9547f, 6.0000f, 6.0000f, 12.0000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w642",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(2.7563f, -1.6839f, -5.9547f, 1.2000f, 3.6000f, 12.0000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w643",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.2437f, 3.1161f, -4.7547f, 7.2000f, 1.2000f, 9.6000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w644",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(2.7563f, 1.9161f, -5.9547f, 1.2000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w645",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(2.7563f, -2.8839f, -5.9547f, 1.2000f, 1.2000f, 10.8000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w646",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.2437f, -4.0839f, -4.7547f, 7.2000f, 1.2000f, 9.6000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w647",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.2437f, 4.3161f, -3.5547f, 7.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w648",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-4.4437f, -2.8839f, -3.5547f, 1.2000f, 6.0000f, 7.2000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w649",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.2437f, -5.2839f, -3.5547f, 7.2000f, 1.2000f, 7.2000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w650",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-4.4437f, 3.1161f, -2.3547f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w651",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-4.4437f, -4.0839f, -2.3547f, 1.2000f, 1.2000f, 4.8000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w652",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-3.2437f, -6.4839f, -1.1547f, 6.0000f, 1.2000f, 2.4000f),
                PartPose.ZERO);
        wheelRr.addOrReplaceChild("w653",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-4.4437f, -1.6839f, -4.7547f, 1.2000f, 3.6000f, 1.2000f),
                PartPose.ZERO);
        root.addOrReplaceChild("taillight0",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-11.4000f, 12.0000f, 34.8000f, 7.2000f, 3.6000f, 2.4000f),
                PartPose.ZERO);
        root.addOrReplaceChild("taillight4",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(4.2000f, 12.0000f, 34.8000f, 7.2000f, 3.6000f, 2.4000f),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(VehicleRenderState state) {
        super.setupAnim(state);
        // 輪子這次真的會轉了。舊版註解說「四顆輪子各自轉起來要四個 pivot，
        // 而在這個尺寸下沒人看得出來」——體素化的模型本來就是分好組的，pivot 是從
        // 每顆輪子的體素重心算出來的，那個成本已經付掉了。
        this.wheelFl.xRot = state.wheelSpin;
        this.wheelFr.xRot = state.wheelSpin;
        this.wheelRl.xRot = state.wheelSpin;
        this.wheelRr.xRot = state.wheelSpin;

        // 只有前輪轉向。xRot 跟 yRot 同時給一個零件是安全的：ModelPart 的旋轉是
        // rotationZYX，**X 先套、Y 後套**，正好就是「輪子先自轉、再整顆偏過去」的順序。
        // 反過來的話輪子會繞著一根歪掉的軸滾。
        float steer = Mth.clamp(state.steer, -STEER_MAX, STEER_MAX) * Mth.DEG_TO_RAD;
        this.wheelFl.yRot = steer;
        this.wheelFr.yRot = steer;
    }
}
