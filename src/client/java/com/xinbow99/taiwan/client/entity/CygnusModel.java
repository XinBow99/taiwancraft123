package com.xinbow99.taiwan.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * 勁戰四代 125 的模型。
 *
 * <h2>來源：models/cygnus.bbmodel，而那個是從真車的 GLB 網格體素化出來的</h2>
 * <p>流程是：手寫 GLB 解析器取出 15.6 萬頂點／31.2 萬三角面 → 依三角面邊長做重心取樣打進
 * 48×33×20 的體素格 → **沿寬度方向填實**（機車左右對稱，所以填出來是實心車體，而跨腿開口
 * 那裡本來就沒有表面體素，不會被填掉）→ greedy 合併成 371 個方塊。
 *
 * <p>**不要手改這裡的數字。**要改形狀請開 .bbmodel 重匯，這個檔案是產生出來的。
 *
 * <h2>比例是量出來的，不是設計的</h2>
 * <p>輪半徑 7.5（用 {@code w² = 2rh − h²} 從各層弦寬反推，直接量最底層會嚴重低估）、
 * 軸距 34 = 2.27 個輪徑、總長 3.37 個輪徑、車高 2.20 個輪徑。這組數字跟實車規格
 * （長 1860mm、輪外徑約 550mm、高 1150mm）吻合。
 *
 * <h2>貼圖宣告 512×512，實際檔案是 768×768</h2>
 * <p>跟獼猴同一招：{@link LayerDefinition#create} 的尺寸只用來把 UV 正規化，取樣用實際圖檔，
 * 所以丟一張更大的圖進去等於提高貼圖密度。**改這裡的數字會讓整張貼圖錯位。**
 */
public class CygnusModel extends EntityModel<VehicleRenderState> {

    /** 收起側柱時要轉多少。騎乘中側柱必須收起來，不然會插在地上跟著跑。 */
    private static final float STAND_UP = -78f * Mth.DEG_TO_RAD;

    /**
     * 整車縮放。
     *
     * <p>.bbmodel 是照真車比例做的，搬過來有 50.5 長 × 33 高（3.2 × 2.1 個方塊），但實體是
     * {@code sized(0.8f, 1.4f)}＝12.8 × 22.4 格。**0.64 是拿舊模型的尺寸回推的**
     * （舊版 32 長 × 21 高，33 × 0.64 ≈ 21），這樣新舊車款擺在一起大小才一致。
     *
     * <p>縮放掛在 {@code bike} 這根骨架上，而它的樞軸就在地面（Blockbench 的 [0,0,0]
     * ＝ MC 的 y=24），所以縮完輪子還是踩在地上，不用另外補位移。
     */
    private static final float SCALE = 0.64f;

    private final ModelPart bike;
    private final ModelPart steer;
    private final ModelPart wheelFront;
    private final ModelPart wheelRear;
    private final ModelPart sideStand;

    public CygnusModel(ModelPart root) {
        super(root);
        this.bike = root.getChild("bike");
        ModelPart chassis = this.bike.getChild("chassis");
        this.steer = chassis.getChild("steer");
        this.wheelFront = this.steer.getChild("wheel_front");
        this.wheelRear = chassis.getChild("swingarm").getChild("wheel_rear");
        this.sideStand = chassis.getChild("side_stand");
    }

    public static LayerDefinition createBodyLayer() {
MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bike = root.addOrReplaceChild("bike", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition chassis = bike.addOrReplaceChild("chassis", CubeListBuilder.create().texOffs(158, 140).addBox(-4.0F, -12.0F, 14.0F, 1.0F, 2.0F, 10.0F)
        .texOffs(186, 167).addBox(-5.0F, -12.0F, 16.0F, 1.0F, 1.0F, 8.0F)
        .texOffs(455, 167).addBox(-6.0F, -12.0F, 17.0F, 1.0F, 1.0F, 7.0F)
        .texOffs(283, 178).addBox(-7.0F, -12.0F, 18.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(440, 187).addBox(-6.0F, -13.0F, 19.0F, 3.0F, 1.0F, 5.0F)
        .texOffs(19, 202).addBox(-7.0F, -13.0F, 20.0F, 1.0F, 1.0F, 4.0F)
        .texOffs(392, 202).addBox(3.0F, -14.0F, 20.0F, 1.0F, 2.0F, 2.0F)
        .texOffs(456, 209).addBox(-2.0F, -15.0F, 21.0F, 5.0F, 1.0F, 1.0F)
        .texOffs(30, 202).addBox(-2.0F, -16.0F, 18.0F, 4.0F, 1.0F, 4.0F)
        .texOffs(53, 40).addBox(-1.0F, -21.0F, 0.0F, 2.0F, 1.0F, 22.0F)
        .texOffs(399, 202).addBox(-4.0F, -16.0F, 20.0F, 1.0F, 3.0F, 1.0F)
        .texOffs(404, 202).addBox(3.0F, -16.0F, 19.0F, 1.0F, 2.0F, 2.0F)
        .texOffs(411, 202).addBox(2.0F, -16.0F, 18.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(420, 202).addBox(-3.0F, -16.0F, 18.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(376, 0).addBox(-2.0F, -19.0F, -2.0F, 4.0F, 1.0F, 23.0F)
        .texOffs(294, 40).addBox(-2.0F, -20.0F, 0.0F, 5.0F, 1.0F, 21.0F)
        .texOffs(43, 65).addBox(-3.0F, -20.0F, 1.0F, 1.0F, 1.0F, 20.0F)
        .texOffs(187, 88).addBox(1.0F, -21.0F, 5.0F, 2.0F, 1.0F, 16.0F)
        .texOffs(86, 65).addBox(-2.0F, -21.0F, 1.0F, 1.0F, 1.0F, 20.0F)
        .texOffs(224, 88).addBox(-3.0F, -21.0F, 5.0F, 1.0F, 1.0F, 16.0F)
        .texOffs(425, 108).addBox(-2.0F, -22.0F, 8.0F, 4.0F, 1.0F, 13.0F)
        .texOffs(472, 167).addBox(-1.0F, -23.0F, 14.0F, 3.0F, 1.0F, 7.0F)
        .texOffs(47, 202).addBox(-2.0F, -23.0F, 17.0F, 1.0F, 1.0F, 4.0F)
        .texOffs(199, 209).addBox(-4.0F, -16.0F, 19.0F, 1.0F, 2.0F, 1.0F)
        .texOffs(469, 209).addBox(4.0F, -16.0F, 19.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(431, 0).addBox(2.0F, -18.0F, -2.0F, 1.0F, 2.0F, 22.0F)
        .texOffs(0, 40).addBox(-1.0F, -18.0F, -3.0F, 3.0F, 1.0F, 23.0F)
        .texOffs(102, 40).addBox(-2.0F, -18.0F, -2.0F, 1.0F, 1.0F, 22.0F)
        .texOffs(149, 40).addBox(-3.0F, -19.0F, -1.0F, 1.0F, 2.0F, 21.0F)
        .texOffs(264, 65).addBox(2.0F, -19.0F, 1.0F, 2.0F, 1.0F, 19.0F)
        .texOffs(39, 88).addBox(-4.0F, -19.0F, 3.0F, 1.0F, 1.0F, 17.0F)
        .texOffs(76, 88).addBox(3.0F, -20.0F, 3.0F, 1.0F, 1.0F, 17.0F)
        .texOffs(259, 88).addBox(-4.0F, -20.0F, 4.0F, 1.0F, 1.0F, 16.0F)
        .texOffs(33, 108).addBox(3.0F, -21.0F, 6.0F, 1.0F, 1.0F, 14.0F)
        .texOffs(208, 140).addBox(2.0F, -22.0F, 9.0F, 1.0F, 1.0F, 11.0F)
        .texOffs(233, 140).addBox(-3.0F, -22.0F, 9.0F, 1.0F, 1.0F, 11.0F)
        .texOffs(429, 202).addBox(2.0F, -23.0F, 17.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(438, 202).addBox(-3.0F, -23.0F, 17.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(129, 65).addBox(3.0F, -18.0F, 0.0F, 1.0F, 2.0F, 19.0F)
        .texOffs(194, 40).addBox(-2.0F, -17.0F, -3.0F, 4.0F, 1.0F, 22.0F)
        .texOffs(347, 40).addBox(-3.0F, -17.0F, -2.0F, 1.0F, 1.0F, 21.0F)
        .texOffs(354, 65).addBox(-4.0F, -18.0F, 1.0F, 1.0F, 1.0F, 18.0F)
        .texOffs(64, 108).addBox(4.0F, -19.0F, 5.0F, 1.0F, 1.0F, 14.0F)
        .texOffs(460, 108).addBox(4.0F, -20.0F, 6.0F, 1.0F, 1.0F, 13.0F)
        .texOffs(484, 154).addBox(3.0F, -22.0F, 10.0F, 1.0F, 1.0F, 9.0F)
        .texOffs(79, 0).addBox(-4.0F, -7.0F, -12.0F, 1.0F, 1.0F, 30.0F)
        .texOffs(393, 65).addBox(3.0F, -16.0F, 0.0F, 1.0F, 1.0F, 18.0F)
        .texOffs(469, 88).addBox(4.0F, -18.0F, 4.0F, 1.0F, 2.0F, 14.0F)
        .texOffs(113, 88).addBox(-4.0F, -17.0F, 1.0F, 1.0F, 1.0F, 17.0F)
        .texOffs(95, 108).addBox(-5.0F, -19.0F, 5.0F, 1.0F, 2.0F, 13.0F)
        .texOffs(258, 140).addBox(-5.0F, -20.0F, 7.0F, 1.0F, 1.0F, 11.0F)
        .texOffs(58, 202).addBox(-4.0F, -22.0F, 14.0F, 1.0F, 1.0F, 4.0F)
        .texOffs(142, 0).addBox(3.0F, -8.0F, -13.0F, 1.0F, 1.0F, 30.0F)
        .texOffs(124, 108).addBox(4.0F, -16.0F, 3.0F, 1.0F, 1.0F, 14.0F)
        .texOffs(0, 108).addBox(-4.0F, -16.0F, 2.0F, 1.0F, 1.0F, 15.0F)
        .texOffs(155, 108).addBox(-5.0F, -16.0F, 3.0F, 1.0F, 1.0F, 14.0F)
        .texOffs(0, 125).addBox(-5.0F, -17.0F, 4.0F, 1.0F, 1.0F, 13.0F)
        .texOffs(150, 154).addBox(-4.0F, -21.0F, 7.0F, 1.0F, 1.0F, 10.0F)
        .texOffs(205, 0).addBox(3.0F, -7.0F, -12.0F, 1.0F, 1.0F, 28.0F)
        .texOffs(80, 202).addBox(-4.0F, -15.0F, 14.0F, 1.0F, 3.0F, 2.0F)
        .texOffs(294, 88).addBox(3.0F, -14.0F, 0.0F, 1.0F, 1.0F, 16.0F)
        .texOffs(186, 108).addBox(3.0F, -15.0F, 2.0F, 2.0F, 1.0F, 14.0F)
        .texOffs(21, 167).addBox(5.0F, -18.0F, 7.0F, 1.0F, 1.0F, 9.0F)
        .texOffs(0, 178).addBox(5.0F, -19.0F, 9.0F, 1.0F, 1.0F, 7.0F)
        .texOffs(224, 167).addBox(4.0F, -21.0F, 8.0F, 1.0F, 1.0F, 8.0F)
        .texOffs(264, 0).addBox(-4.0F, -6.0F, -12.0F, 1.0F, 1.0F, 27.0F)
        .texOffs(87, 202).addBox(3.0F, -13.0F, 14.0F, 1.0F, 4.0F, 1.0F)
        .texOffs(321, 125).addBox(5.0F, -15.0F, 3.0F, 1.0F, 1.0F, 12.0F)
        .texOffs(29, 125).addBox(-5.0F, -15.0F, 2.0F, 1.0F, 1.0F, 13.0F)
        .texOffs(283, 140).addBox(5.0F, -16.0F, 4.0F, 1.0F, 1.0F, 11.0F)
        .texOffs(432, 65).addBox(-2.0F, -16.0F, -3.0F, 5.0F, 1.0F, 18.0F)
        .texOffs(150, 88).addBox(-3.0F, -16.0F, -2.0F, 1.0F, 1.0F, 17.0F)
        .texOffs(196, 154).addBox(5.0F, -17.0F, 5.0F, 1.0F, 1.0F, 10.0F)
        .texOffs(17, 178).addBox(-5.0F, -21.0F, 8.0F, 1.0F, 1.0F, 7.0F)
        .texOffs(321, 0).addBox(3.0F, -6.0F, -12.0F, 1.0F, 1.0F, 26.0F)
        .texOffs(219, 154).addBox(4.0F, -14.0F, 4.0F, 2.0F, 1.0F, 10.0F)
        .texOffs(219, 108).addBox(-4.0F, -14.0F, 0.0F, 1.0F, 1.0F, 14.0F)
        .texOffs(348, 125).addBox(-5.0F, -14.0F, 2.0F, 1.0F, 1.0F, 12.0F)
        .texOffs(58, 125).addBox(-4.0F, -15.0F, 1.0F, 1.0F, 1.0F, 13.0F)
        .texOffs(243, 167).addBox(-6.0F, -17.0F, 6.0F, 1.0F, 1.0F, 8.0F)
        .texOffs(305, 178).addBox(-6.0F, -18.0F, 8.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(375, 125).addBox(3.0F, -13.0F, 1.0F, 2.0F, 1.0F, 12.0F)
        .texOffs(87, 125).addBox(-4.0F, -13.0F, 0.0F, 1.0F, 1.0F, 13.0F)
        .texOffs(308, 140).addBox(-5.0F, -13.0F, 2.0F, 1.0F, 1.0F, 11.0F)
        .texOffs(42, 167).addBox(-6.0F, -16.0F, 6.0F, 1.0F, 3.0F, 7.0F)
        .texOffs(145, 125).addBox(-4.0F, -12.0F, -1.0F, 1.0F, 1.0F, 13.0F)
        .texOffs(404, 125).addBox(-5.0F, -12.0F, 0.0F, 1.0F, 1.0F, 12.0F)
        .texOffs(281, 167).addBox(5.0F, -13.0F, 4.0F, 1.0F, 1.0F, 8.0F)
        .texOffs(320, 178).addBox(-6.0F, -13.0F, 6.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(250, 108).addBox(-2.0F, -15.0F, -2.0F, 5.0F, 1.0F, 14.0F)
        .texOffs(431, 125).addBox(-3.0F, -15.0F, 0.0F, 1.0F, 1.0F, 12.0F)
        .texOffs(204, 209).addBox(5.0F, -20.0F, 10.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(244, 154).addBox(3.0F, -12.0F, 1.0F, 3.0F, 1.0F, 10.0F)
        .texOffs(59, 167).addBox(-6.0F, -12.0F, 2.0F, 1.0F, 1.0F, 9.0F)
        .texOffs(458, 125).addBox(-2.0F, -14.0F, -1.0F, 5.0F, 1.0F, 12.0F)
        .texOffs(333, 140).addBox(-3.0F, -14.0F, 0.0F, 1.0F, 1.0F, 11.0F)
        .texOffs(218, 209).addBox(-6.0F, -19.0F, 9.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(474, 209).addBox(-6.0F, -20.0F, 10.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(414, 140).addBox(-3.0F, -13.0F, -1.0F, 6.0F, 1.0F, 11.0F)
        .texOffs(447, 202).addBox(6.0F, -14.0F, 7.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(170, 65).addBox(-3.0F, -6.0F, -10.0F, 6.0F, 2.0F, 19.0F)
        .texOffs(247, 40).addBox(-4.0F, -8.0F, -13.0F, 1.0F, 1.0F, 22.0F)
        .texOffs(289, 108).addBox(-3.0F, -10.0F, -5.0F, 9.0F, 1.0F, 14.0F)
        .texOffs(329, 88).addBox(-5.0F, -10.0F, -7.0F, 1.0F, 1.0F, 16.0F)
        .texOffs(0, 140).addBox(-3.0F, -12.0F, -2.0F, 6.0F, 2.0F, 11.0F)
        .texOffs(232, 209).addBox(6.0F, -13.0F, 7.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(307, 65).addBox(-3.0F, -9.0F, -9.0F, 6.0F, 3.0F, 17.0F)
        .texOffs(392, 40).addBox(3.0F, -9.0F, -13.0F, 1.0F, 1.0F, 21.0F)
        .texOffs(437, 40).addBox(-4.0F, -9.0F, -13.0F, 1.0F, 1.0F, 21.0F)
        .texOffs(449, 140).addBox(-6.0F, -10.0F, -3.0F, 1.0F, 1.0F, 11.0F)
        .texOffs(497, 209).addBox(6.0F, -12.0F, 7.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(0, 65).addBox(4.0F, -9.0F, -13.0F, 1.0F, 2.0F, 20.0F)
        .texOffs(174, 125).addBox(5.0F, -9.0F, -6.0F, 1.0F, 1.0F, 13.0F)
        .texOffs(221, 65).addBox(-5.0F, -9.0F, -13.0F, 1.0F, 1.0F, 20.0F)
        .texOffs(271, 154).addBox(-6.0F, -9.0F, -4.0F, 1.0F, 1.0F, 10.0F)
        .texOffs(239, 209).addBox(-6.0F, -16.0F, 5.0F, 1.0F, 2.0F, 1.0F)
        .texOffs(364, 88).addBox(3.0F, -5.0F, -11.0F, 1.0F, 1.0F, 16.0F)
        .texOffs(474, 140).addBox(5.0F, -8.0F, -6.0F, 1.0F, 1.0F, 11.0F)
        .texOffs(0, 88).addBox(-5.0F, -8.0F, -13.0F, 1.0F, 1.0F, 18.0F)
        .texOffs(294, 154).addBox(-6.0F, -8.0F, -5.0F, 1.0F, 1.0F, 10.0F)
        .texOffs(0, 195).addBox(6.0F, -10.0F, 0.0F, 1.0F, 1.0F, 5.0F)
        .texOffs(13, 195).addBox(1.0F, -21.0F, 0.0F, 1.0F, 1.0F, 5.0F)
        .texOffs(399, 88).addBox(4.0F, -7.0F, -12.0F, 1.0F, 1.0F, 16.0F)
        .texOffs(434, 88).addBox(-5.0F, -7.0F, -12.0F, 1.0F, 1.0F, 16.0F)
        .texOffs(483, 202).addBox(4.0F, -14.0F, 1.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(203, 125).addBox(-4.0F, -5.0F, -10.0F, 1.0F, 1.0F, 13.0F)
        .texOffs(317, 154).addBox(5.0F, -7.0F, -7.0F, 1.0F, 1.0F, 10.0F)
        .texOffs(336, 108).addBox(4.0F, -6.0F, -12.0F, 1.0F, 1.0F, 14.0F)
        .texOffs(35, 140).addBox(-5.0F, -6.0F, -10.0F, 1.0F, 1.0F, 12.0F)
        .texOffs(502, 209).addBox(3.0F, -15.0F, 1.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(0, 214).addBox(3.0F, -12.0F, 0.0F, 2.0F, 1.0F, 1.0F)
        .texOffs(244, 209).addBox(3.0F, -13.0F, -1.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(251, 209).addBox(2.0F, -19.0F, -1.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(7, 214).addBox(3.0F, -12.0F, -1.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(12, 214).addBox(-2.0F, -20.0F, -1.0F, 4.0F, 1.0F, 1.0F)
        .texOffs(23, 214).addBox(-2.0F, -13.0F, -2.0F, 4.0F, 1.0F, 1.0F)
        .texOffs(34, 214).addBox(-2.0F, -11.0F, -3.0F, 4.0F, 1.0F, 1.0F)
        .texOffs(45, 214).addBox(-2.0F, -15.0F, -3.0F, 4.0F, 1.0F, 1.0F)
        .texOffs(258, 209).addBox(-3.0F, -10.0F, -7.0F, 8.0F, 1.0F, 2.0F)
        .texOffs(340, 154).addBox(0.0F, -18.0F, -13.0F, 2.0F, 4.0F, 7.0F)
        .texOffs(62, 140).addBox(2.0F, -18.0F, -16.0F, 1.0F, 3.0F, 10.0F)
        .texOffs(85, 140).addBox(-2.0F, -18.0F, -16.0F, 2.0F, 3.0F, 10.0F)
        .texOffs(367, 108).addBox(-3.0F, -17.0F, -20.0F, 1.0F, 1.0F, 14.0F)
        .texOffs(232, 125).addBox(-3.0F, -18.0F, -19.0F, 1.0F, 1.0F, 13.0F)
        .texOffs(81, 178).addBox(3.0F, -10.0F, -14.0F, 1.0F, 1.0F, 7.0F)
        .texOffs(342, 167).addBox(2.0F, -15.0F, -10.0F, 1.0F, 6.0F, 3.0F)
        .texOffs(98, 178).addBox(-2.0F, -14.0F, -10.0F, 4.0F, 5.0F, 3.0F)
        .texOffs(32, 209).addBox(-3.0F, -10.0F, -10.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(113, 178).addBox(-3.0F, -16.0F, -10.0F, 1.0F, 5.0F, 3.0F)
        .texOffs(352, 178).addBox(-2.0F, -15.0F, -13.0F, 2.0F, 1.0F, 6.0F)
        .texOffs(398, 108).addBox(3.0F, -18.0F, -19.0F, 1.0F, 3.0F, 12.0F)
        .texOffs(23, 154).addBox(-4.0F, -18.0F, -18.0F, 1.0F, 1.0F, 11.0F)
        .texOffs(48, 154).addBox(-2.0F, -20.0F, -18.0F, 4.0F, 1.0F, 11.0F)
        .texOffs(359, 154).addBox(-2.0F, -21.0F, -17.0F, 5.0F, 1.0F, 10.0F)
        .texOffs(165, 202).addBox(5.0F, -9.0F, -11.0F, 1.0F, 2.0F, 3.0F)
        .texOffs(369, 178).addBox(4.0F, -10.0F, -14.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(390, 154).addBox(3.0F, -15.0F, -14.0F, 1.0F, 5.0F, 6.0F)
        .texOffs(334, 209).addBox(-3.0F, -11.0F, -10.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(110, 140).addBox(-4.0F, -17.0F, -18.0F, 1.0F, 3.0F, 10.0F)
        .texOffs(405, 154).addBox(-3.0F, -19.0F, -18.0F, 7.0F, 1.0F, 10.0F)
        .texOffs(131, 167).addBox(-4.0F, -19.0F, -17.0F, 1.0F, 1.0F, 9.0F)
        .texOffs(440, 154).addBox(2.0F, -20.0F, -18.0F, 1.0F, 1.0F, 10.0F)
        .texOffs(463, 154).addBox(-3.0F, -21.0F, -17.0F, 1.0F, 2.0F, 9.0F)
        .texOffs(378, 167).addBox(3.0F, -21.0F, -16.0F, 1.0F, 1.0F, 8.0F)
        .texOffs(341, 209).addBox(-6.0F, -10.0F, -10.0F, 1.0F, 2.0F, 1.0F)
        .texOffs(196, 202).addBox(5.0F, -11.0F, -12.0F, 1.0F, 2.0F, 3.0F)
        .texOffs(399, 178).addBox(-5.0F, -11.0F, -14.0F, 1.0F, 2.0F, 5.0F)
        .texOffs(94, 195).addBox(4.0F, -11.0F, -14.0F, 1.0F, 1.0F, 5.0F)
        .texOffs(152, 167).addBox(-4.0F, -14.0F, -15.0F, 1.0F, 4.0F, 6.0F)
        .texOffs(397, 167).addBox(3.0F, -20.0F, -17.0F, 1.0F, 1.0F, 8.0F)
        .texOffs(416, 167).addBox(-4.0F, -20.0F, -17.0F, 1.0F, 1.0F, 8.0F)
        .texOffs(190, 178).addBox(-4.0F, -21.0F, -16.0F, 1.0F, 1.0F, 7.0F)
        .texOffs(90, 209).addBox(-6.0F, -12.0F, -11.0F, 1.0F, 3.0F, 1.0F)
        .texOffs(95, 209).addBox(4.0F, -12.0F, -13.0F, 2.0F, 1.0F, 3.0F)
        .texOffs(427, 178).addBox(-5.0F, -13.0F, -15.0F, 1.0F, 2.0F, 5.0F)
        .texOffs(440, 178).addBox(4.0F, -13.0F, -16.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(106, 209).addBox(2.0F, -15.0F, -11.0F, 1.0F, 3.0F, 1.0F)
        .texOffs(346, 209).addBox(-3.0F, -14.0F, -11.0F, 5.0F, 2.0F, 1.0F)
        .texOffs(216, 202).addBox(-3.0F, -16.0F, -13.0F, 1.0F, 2.0F, 3.0F)
        .texOffs(455, 178).addBox(4.0F, -20.0F, -16.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(120, 195).addBox(4.0F, -21.0F, -15.0F, 1.0F, 1.0F, 5.0F)
        .texOffs(225, 202).addBox(-5.0F, -21.0F, -14.0F, 1.0F, 1.0F, 4.0F)
        .texOffs(147, 214).addBox(5.0F, -9.0F, -12.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(241, 202).addBox(-6.0F, -14.0F, -12.0F, 1.0F, 4.0F, 1.0F)
        .texOffs(246, 202).addBox(5.0F, -14.0F, -14.0F, 1.0F, 2.0F, 3.0F)
        .texOffs(470, 178).addBox(4.0F, -14.0F, -17.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(359, 209).addBox(2.0F, -15.0F, -12.0F, 1.0F, 2.0F, 1.0F)
        .texOffs(152, 214).addBox(-3.0F, -14.0F, -12.0F, 5.0F, 1.0F, 1.0F)
        .texOffs(485, 178).addBox(-5.0F, -14.0F, -17.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(207, 178).addBox(4.0F, -18.0F, -18.0F, 1.0F, 1.0F, 7.0F)
        .texOffs(0, 187).addBox(4.0F, -19.0F, -17.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(15, 187).addBox(-5.0F, -19.0F, -17.0F, 1.0F, 1.0F, 6.0F)
        .texOffs(111, 209).addBox(5.0F, -20.0F, -14.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(255, 202).addBox(-5.0F, -20.0F, -15.0F, 1.0F, 1.0F, 4.0F)
        .texOffs(364, 209).addBox(5.0F, -21.0F, -13.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(170, 214).addBox(5.0F, -11.0F, -13.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(266, 202).addBox(-6.0F, -15.0F, -13.0F, 1.0F, 4.0F, 1.0F)
        .texOffs(271, 202).addBox(4.0F, -15.0F, -16.0F, 2.0F, 1.0F, 4.0F)
        .texOffs(175, 214).addBox(2.0F, -15.0F, -13.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(435, 167).addBox(-5.0F, -18.0F, -17.0F, 1.0F, 4.0F, 5.0F)
        .texOffs(224, 178).addBox(4.0F, -17.0F, -18.0F, 1.0F, 2.0F, 6.0F)
        .texOffs(133, 195).addBox(5.0F, -19.0F, -16.0F, 1.0F, 2.0F, 4.0F)
        .texOffs(120, 209).addBox(-6.0F, -19.0F, -15.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(381, 209).addBox(-6.0F, -20.0F, -14.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(388, 209).addBox(4.0F, -12.0F, -15.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(144, 195).addBox(-6.0F, -18.0F, -14.0F, 1.0F, 5.0F, 1.0F)
        .texOffs(284, 202).addBox(5.0F, -17.0F, -16.0F, 1.0F, 2.0F, 3.0F)
        .texOffs(149, 195).addBox(0.0F, -18.0F, -16.0F, 2.0F, 3.0F, 3.0F)
        .texOffs(129, 209).addBox(-3.0F, -16.0F, -16.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(187, 214).addBox(-5.0F, -11.0F, -15.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(293, 202).addBox(3.0F, -12.0F, -18.0F, 1.0F, 1.0F, 4.0F)
        .texOffs(160, 195).addBox(3.0F, -15.0F, -17.0F, 1.0F, 3.0F, 3.0F)
        .texOffs(192, 214).addBox(5.0F, -14.0F, -15.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(304, 202).addBox(-6.0F, -18.0F, -15.0F, 1.0F, 4.0F, 1.0F)
        .texOffs(138, 209).addBox(3.0F, -11.0F, -18.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(395, 209).addBox(-4.0F, -14.0F, -16.0F, 1.0F, 2.0F, 1.0F)
        .texOffs(197, 214).addBox(-5.0F, -13.0F, -16.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(147, 209).addBox(3.0F, -9.0F, -19.0F, 1.0F, 1.0F, 3.0F)
        .texOffs(400, 209).addBox(3.0F, -10.0F, -18.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(213, 214).addBox(-4.0F, -14.0F, -17.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(218, 214).addBox(4.0F, -15.0F, -17.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(309, 202).addBox(-2.0F, -17.0F, -20.0F, 5.0F, 1.0F, 4.0F)
        .texOffs(156, 209).addBox(-2.0F, -18.0F, -19.0F, 5.0F, 1.0F, 3.0F)
        .texOffs(328, 202).addBox(3.0F, -7.0F, -20.0F, 1.0F, 2.0F, 3.0F)
        .texOffs(407, 209).addBox(3.0F, -8.0F, -19.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(223, 214).addBox(3.0F, -15.0F, -18.0F, 1.0F, 1.0F, 1.0F)
        .texOffs(414, 209).addBox(3.0F, -5.0F, -20.0F, 1.0F, 1.0F, 2.0F)
        .texOffs(421, 209).addBox(-4.0F, -17.0F, -19.0F, 1.0F, 2.0F, 1.0F)
        .texOffs(228, 214).addBox(-2.0F, -19.0F, -19.0F, 5.0F, 1.0F, 1.0F)
        .texOffs(241, 214).addBox(-3.0F, -16.0F, -20.0F, 6.0F, 1.0F, 1.0F)
        .texOffs(256, 214).addBox(-2.0F, -18.0F, -20.0F, 4.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition steer = chassis.addOrReplaceChild("steer", CubeListBuilder.create().texOffs(0, 154).addBox(-1.0F, -5.0F, -0.5F, 3.0F, 4.0F, 8.0F)
        .texOffs(300, 167).addBox(-2.0F, -2.0F, -0.5F, 1.0F, 1.0F, 8.0F)
        .texOffs(80, 167).addBox(2.0F, -5.0F, 0.5F, 1.0F, 3.0F, 7.0F)
        .texOffs(97, 167).addBox(-3.0F, -5.0F, 0.5F, 2.0F, 3.0F, 7.0F)
        .texOffs(51, 178).addBox(2.0F, -2.0F, -0.5F, 1.0F, 1.0F, 7.0F)
        .texOffs(114, 202).addBox(3.0F, -5.0F, 4.5F, 6.0F, 3.0F, 2.0F)
        .texOffs(116, 167).addBox(-4.0F, -6.0F, 0.5F, 1.0F, 4.0F, 6.0F)
        .texOffs(68, 178).addBox(-5.0F, -5.0F, 1.5F, 1.0F, 3.0F, 5.0F)
        .texOffs(26, 195).addBox(-8.0F, -4.0F, 2.5F, 3.0F, 2.0F, 4.0F)
        .texOffs(131, 202).addBox(-9.0F, -5.0F, 4.5F, 1.0F, 3.0F, 2.0F)
        .texOffs(492, 202).addBox(-6.0F, -5.0F, 3.5F, 1.0F, 1.0F, 3.0F)
        .texOffs(279, 209).addBox(-8.0F, -5.0F, 4.5F, 2.0F, 1.0F, 2.0F)
        .texOffs(41, 195).addBox(-3.0F, -6.0F, 1.5F, 8.0F, 1.0F, 5.0F)
        .texOffs(138, 202).addBox(5.5F, -10.0F, 3.5F, 4.5F, 2.0F, 3.0F)
        .texOffs(288, 209).addBox(5.5F, -11.0F, 4.5F, 4.5F, 1.0F, 2.0F)
        .texOffs(56, 214).addBox(-10.0F, -11.0F, 5.5F, 1.0F, 1.0F, 1.0F)
        .texOffs(319, 167).addBox(-1.0F, -1.0F, -2.5F, 3.0F, 1.0F, 8.0F)
        .texOffs(501, 202).addBox(5.0F, -6.0F, 2.5F, 1.0F, 1.0F, 3.0F)
        .texOffs(154, 202).addBox(-5.0F, -6.0F, 1.5F, 1.0F, 1.0F, 4.0F)
        .texOffs(0, 209).addBox(-4.0F, -7.0F, 2.5F, 9.0F, 1.0F, 3.0F)
        .texOffs(25, 209).addBox(-5.0F, -8.0F, 3.5F, 1.0F, 2.0F, 2.0F)
        .texOffs(61, 214).addBox(5.5F, -8.0F, 4.5F, 4.5F, 1.0F, 1.0F)
        .texOffs(302, 209).addBox(-9.0F, -8.0F, 3.5F, 4.0F, 1.0F, 2.0F)
        .texOffs(351, 167).addBox(-2.0F, 0.0F, -3.5F, 5.0F, 1.0F, 8.0F)
        .texOffs(122, 178).addBox(2.0F, -1.0F, -2.5F, 1.0F, 1.0F, 7.0F)
        .texOffs(139, 178).addBox(-2.0F, -1.0F, -2.5F, 1.0F, 1.0F, 7.0F)
        .texOffs(41, 209).addBox(3.0F, -4.0F, 2.5F, 5.0F, 2.0F, 2.0F)
        .texOffs(315, 209).addBox(8.0F, -4.0F, 2.5F, 1.0F, 1.0F, 2.0F)
        .texOffs(322, 209).addBox(-9.0F, -4.0F, 2.5F, 1.0F, 1.0F, 2.0F)
        .texOffs(73, 214).addBox(3.0F, -5.0F, 3.5F, 4.0F, 1.0F, 1.0F)
        .texOffs(329, 209).addBox(5.0F, -8.0F, 3.5F, 1.0F, 2.0F, 1.0F)
        .texOffs(84, 214).addBox(6.0F, -8.0F, 3.5F, 3.0F, 1.0F, 1.0F)
        .texOffs(156, 178).addBox(3.0F, 0.0F, -3.5F, 1.0F, 1.0F, 7.0F)
        .texOffs(173, 178).addBox(-3.0F, 0.0F, -3.5F, 1.0F, 1.0F, 7.0F)
        .texOffs(68, 195).addBox(3.0F, -1.0F, -1.5F, 1.0F, 1.0F, 5.0F)
        .texOffs(384, 178).addBox(-3.0F, -1.0F, -2.5F, 1.0F, 1.0F, 6.0F)
        .texOffs(81, 195).addBox(-4.0F, -1.0F, -1.5F, 1.0F, 1.0F, 5.0F)
        .texOffs(174, 202).addBox(3.0F, -2.0F, -0.5F, 1.0F, 1.0F, 4.0F)
        .texOffs(185, 202).addBox(-3.0F, -2.0F, -0.5F, 1.0F, 1.0F, 4.0F)
        .texOffs(56, 209).addBox(-4.0F, -2.0F, 0.5F, 1.0F, 1.0F, 3.0F)
        .texOffs(93, 214).addBox(8.0F, -3.0F, 2.5F, 1.0F, 1.0F, 1.0F)
        .texOffs(98, 214).addBox(-9.0F, -3.0F, 2.5F, 1.0F, 1.0F, 1.0F)
        .texOffs(103, 214).addBox(3.0F, -5.0F, 2.5F, 3.0F, 1.0F, 1.0F)
        .texOffs(107, 195).addBox(4.0F, 0.0F, -2.5F, 1.0F, 1.0F, 5.0F)
        .texOffs(412, 178).addBox(-4.0F, 0.0F, -3.5F, 1.0F, 1.0F, 6.0F)
        .texOffs(205, 202).addBox(-5.0F, 0.0F, -1.5F, 1.0F, 1.0F, 4.0F)
        .texOffs(65, 209).addBox(4.0F, -1.0F, -0.5F, 1.0F, 1.0F, 3.0F)
        .texOffs(74, 209).addBox(-5.0F, -1.0F, -0.5F, 1.0F, 1.0F, 3.0F)
        .texOffs(83, 209).addBox(3.0F, -5.0F, 1.5F, 2.0F, 3.0F, 1.0F)
        .texOffs(112, 214).addBox(5.0F, -4.0F, 1.5F, 1.0F, 1.0F, 1.0F)
        .texOffs(117, 214).addBox(-3.0F, -7.0F, 1.5F, 6.0F, 1.0F, 1.0F)
        .texOffs(236, 202).addBox(3.0F, -6.0F, 0.5F, 1.0F, 4.0F, 1.0F)
        .texOffs(132, 214).addBox(-3.0F, -6.0F, 0.5F, 6.0F, 1.0F, 1.0F)
        .texOffs(371, 209).addBox(2.0F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F)
        .texOffs(376, 209).addBox(-2.0F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F)
        .texOffs(165, 214).addBox(-3.0F, -5.0F, -0.5F, 1.0F, 1.0F, 1.0F)
        .texOffs(180, 214).addBox(-1.0F, -4.0F, -1.5F, 2.0F, 1.0F, 1.0F)
        .texOffs(202, 214).addBox(-2.0F, 0.0F, -4.5F, 4.0F, 1.0F, 1.0F)
        .texOffs(173, 209).addBox(-5.6F, -5.0F, -2.4F, 11.2F, 3.0F, 1.0F)
        .texOffs(426, 209).addBox(-8.2F, -4.2F, -2.4F, 2.6F, 1.8F, 1.0F)
        .texOffs(435, 209).addBox(5.6F, -4.2F, -2.4F, 2.6F, 1.8F, 1.0F)
        .texOffs(337, 202).addBox(-11.6F, -3.6F, 1.9F, 3.0F, 2.0F, 2.8F)
        .texOffs(350, 202).addBox(8.6F, -3.6F, 1.9F, 3.0F, 2.0F, 2.8F)
        .texOffs(376, 202).addBox(-10.0F, -10.0F, 3.5F, 4.5F, 2.0F, 3.0F)
        .texOffs(444, 209).addBox(-9.0F, -11.0F, 4.5F, 3.5F, 1.0F, 2.0F), PartPose.offset(0.0F, -22.0F, -11.5F));

        PartDefinition wheel_front = steer.addOrReplaceChild("wheel_front", CubeListBuilder.create().texOffs(30, 187).addBox(-2.4F, -7.5F, -2.1357F, 4.8F, 2.1F, 4.2715F)
        .texOffs(169, 195).addBox(-2.7F, -5.45F, -1.2503F, 5.4F, 3.45F, 2.5007F)
        .texOffs(239, 178).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 14.5F, -6.0F));

        PartDefinition wf_rim9_r1 = wheel_front.addOrReplaceChild("wf_rim9_r1", CubeListBuilder.create().texOffs(322, 195).addBox(-2.7F, -1.725F, -1.2503F, 5.4F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, -3.0136F, -2.1895F, -5.6549F, 0.0F, 0.0F));

        PartDefinition wf_rim8_r1 = wheel_front.addOrReplaceChild("wf_rim8_r1", CubeListBuilder.create().texOffs(305, 195).addBox(-2.7F, -1.725F, -1.2503F, 5.4F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, -1.1511F, -3.5427F, -5.0265F, 0.0F, 0.0F));

        PartDefinition wf_rim7_r1 = wheel_front.addOrReplaceChild("wf_rim7_r1", CubeListBuilder.create().texOffs(288, 195).addBox(-2.7F, -1.725F, -1.2503F, 5.4F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 1.1511F, -3.5427F, -4.3982F, 0.0F, 0.0F));

        PartDefinition wf_rim6_r1 = wheel_front.addOrReplaceChild("wf_rim6_r1", CubeListBuilder.create().texOffs(271, 195).addBox(-2.7F, -1.725F, -1.2503F, 5.4F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 3.0136F, -2.1895F, -3.7699F, 0.0F, 0.0F));

        PartDefinition wf_rim5_r1 = wheel_front.addOrReplaceChild("wf_rim5_r1", CubeListBuilder.create().texOffs(254, 195).addBox(-2.7F, -1.725F, -1.2503F, 5.4F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 3.725F, 0.0F, -3.1416F, 0.0F, 0.0F));

        PartDefinition wf_rim4_r1 = wheel_front.addOrReplaceChild("wf_rim4_r1", CubeListBuilder.create().texOffs(237, 195).addBox(-2.7F, -1.725F, -1.2503F, 5.4F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 3.0136F, 2.1895F, -2.5133F, 0.0F, 0.0F));

        PartDefinition wf_rim3_r1 = wheel_front.addOrReplaceChild("wf_rim3_r1", CubeListBuilder.create().texOffs(220, 195).addBox(-2.7F, -1.725F, -1.2503F, 5.4F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 1.1511F, 3.5427F, -1.885F, 0.0F, 0.0F));

        PartDefinition wf_rim2_r1 = wheel_front.addOrReplaceChild("wf_rim2_r1", CubeListBuilder.create().texOffs(203, 195).addBox(-2.7F, -1.725F, -1.2503F, 5.4F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, -1.1511F, 3.5427F, -1.2566F, 0.0F, 0.0F));

        PartDefinition wf_rim1_r1 = wheel_front.addOrReplaceChild("wf_rim1_r1", CubeListBuilder.create().texOffs(186, 195).addBox(-2.7F, -1.725F, -1.2503F, 5.4F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, -3.0136F, 2.1895F, -0.6283F, 0.0F, 0.0F));

        PartDefinition wf_tire9_r1 = wheel_front.addOrReplaceChild("wf_tire9_r1", CubeListBuilder.create().texOffs(210, 187).addBox(-2.4F, -1.05F, -2.1357F, 4.8F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, -5.2182F, -3.7912F, -5.6549F, 0.0F, 0.0F));

        PartDefinition wf_tire8_r1 = wheel_front.addOrReplaceChild("wf_tire8_r1", CubeListBuilder.create().texOffs(190, 187).addBox(-2.4F, -1.05F, -2.1357F, 4.8F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, -1.9932F, -6.1343F, -5.0265F, 0.0F, 0.0F));

        PartDefinition wf_tire7_r1 = wheel_front.addOrReplaceChild("wf_tire7_r1", CubeListBuilder.create().texOffs(170, 187).addBox(-2.4F, -1.05F, -2.1357F, 4.8F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 1.9932F, -6.1343F, -4.3982F, 0.0F, 0.0F));

        PartDefinition wf_tire6_r1 = wheel_front.addOrReplaceChild("wf_tire6_r1", CubeListBuilder.create().texOffs(150, 187).addBox(-2.4F, -1.05F, -2.1357F, 4.8F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 5.2182F, -3.7912F, -3.7699F, 0.0F, 0.0F));

        PartDefinition wf_tire5_r1 = wheel_front.addOrReplaceChild("wf_tire5_r1", CubeListBuilder.create().texOffs(130, 187).addBox(-2.4F, -1.05F, -2.1357F, 4.8F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 6.45F, 0.0F, -3.1416F, 0.0F, 0.0F));

        PartDefinition wf_tire4_r1 = wheel_front.addOrReplaceChild("wf_tire4_r1", CubeListBuilder.create().texOffs(110, 187).addBox(-2.4F, -1.05F, -2.1357F, 4.8F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 5.2182F, 3.7912F, -2.5133F, 0.0F, 0.0F));

        PartDefinition wf_tire3_r1 = wheel_front.addOrReplaceChild("wf_tire3_r1", CubeListBuilder.create().texOffs(90, 187).addBox(-2.4F, -1.05F, -2.1357F, 4.8F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 1.9932F, 6.1343F, -1.885F, 0.0F, 0.0F));

        PartDefinition wf_tire2_r1 = wheel_front.addOrReplaceChild("wf_tire2_r1", CubeListBuilder.create().texOffs(70, 187).addBox(-2.4F, -1.05F, -2.1357F, 4.8F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, -1.9932F, 6.1343F, -1.2566F, 0.0F, 0.0F));

        PartDefinition wf_tire1_r1 = wheel_front.addOrReplaceChild("wf_tire1_r1", CubeListBuilder.create().texOffs(50, 187).addBox(-2.4F, -1.05F, -2.1357F, 4.8F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, -5.2182F, 3.7912F, -0.6283F, 0.0F, 0.0F));

        PartDefinition swingarm = chassis.addOrReplaceChild("swingarm", CubeListBuilder.create(), PartPose.offset(0.0F, -8.5F, 7.5F));

        PartDefinition wheel_rear = swingarm.addOrReplaceChild("wheel_rear", CubeListBuilder.create().texOffs(230, 187).addBox(-2.7F, -7.5F, -2.1357F, 5.4F, 2.1F, 4.2715F)
        .texOffs(339, 195).addBox(-3.0F, -5.45F, -1.2503F, 6.0F, 3.45F, 2.5007F)
        .texOffs(260, 178).addBox(-3.3F, -2.0F, -2.0F, 6.6F, 4.0F, 4.0F), PartPose.offset(0.0F, 1.0F, 9.0F));

        PartDefinition wr_rim9_r1 = wheel_rear.addOrReplaceChild("wr_rim9_r1", CubeListBuilder.create().texOffs(0, 202).addBox(-3.0F, -1.725F, -1.2503F, 6.0F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, -3.0136F, -2.1895F, -5.6549F, 0.0F, 0.0F));

        PartDefinition wr_rim8_r1 = wheel_rear.addOrReplaceChild("wr_rim8_r1", CubeListBuilder.create().texOffs(491, 195).addBox(-3.0F, -1.725F, -1.2503F, 6.0F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, -1.1511F, -3.5427F, -5.0265F, 0.0F, 0.0F));

        PartDefinition wr_rim7_r1 = wheel_rear.addOrReplaceChild("wr_rim7_r1", CubeListBuilder.create().texOffs(472, 195).addBox(-3.0F, -1.725F, -1.2503F, 6.0F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 1.1511F, -3.5427F, -4.3982F, 0.0F, 0.0F));

        PartDefinition wr_rim6_r1 = wheel_rear.addOrReplaceChild("wr_rim6_r1", CubeListBuilder.create().texOffs(453, 195).addBox(-3.0F, -1.725F, -1.2503F, 6.0F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 3.0136F, -2.1895F, -3.7699F, 0.0F, 0.0F));

        PartDefinition wr_rim5_r1 = wheel_rear.addOrReplaceChild("wr_rim5_r1", CubeListBuilder.create().texOffs(434, 195).addBox(-3.0F, -1.725F, -1.2503F, 6.0F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 3.725F, 0.0F, -3.1416F, 0.0F, 0.0F));

        PartDefinition wr_rim4_r1 = wheel_rear.addOrReplaceChild("wr_rim4_r1", CubeListBuilder.create().texOffs(415, 195).addBox(-3.0F, -1.725F, -1.2503F, 6.0F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 3.0136F, 2.1895F, -2.5133F, 0.0F, 0.0F));

        PartDefinition wr_rim3_r1 = wheel_rear.addOrReplaceChild("wr_rim3_r1", CubeListBuilder.create().texOffs(396, 195).addBox(-3.0F, -1.725F, -1.2503F, 6.0F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, 1.1511F, 3.5427F, -1.885F, 0.0F, 0.0F));

        PartDefinition wr_rim2_r1 = wheel_rear.addOrReplaceChild("wr_rim2_r1", CubeListBuilder.create().texOffs(377, 195).addBox(-3.0F, -1.725F, -1.2503F, 6.0F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, -1.1511F, 3.5427F, -1.2566F, 0.0F, 0.0F));

        PartDefinition wr_rim1_r1 = wheel_rear.addOrReplaceChild("wr_rim1_r1", CubeListBuilder.create().texOffs(358, 195).addBox(-3.0F, -1.725F, -1.2503F, 6.0F, 3.45F, 2.5007F), PartPose.offsetAndRotation(0.0F, -3.0136F, 2.1895F, -0.6283F, 0.0F, 0.0F));

        PartDefinition wr_tire9_r1 = wheel_rear.addOrReplaceChild("wr_tire9_r1", CubeListBuilder.create().texOffs(419, 187).addBox(-2.7F, -1.05F, -2.1357F, 5.4F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, -5.2182F, -3.7912F, -5.6549F, 0.0F, 0.0F));

        PartDefinition wr_tire8_r1 = wheel_rear.addOrReplaceChild("wr_tire8_r1", CubeListBuilder.create().texOffs(398, 187).addBox(-2.7F, -1.05F, -2.1357F, 5.4F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, -1.9932F, -6.1343F, -5.0265F, 0.0F, 0.0F));

        PartDefinition wr_tire7_r1 = wheel_rear.addOrReplaceChild("wr_tire7_r1", CubeListBuilder.create().texOffs(377, 187).addBox(-2.7F, -1.05F, -2.1357F, 5.4F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 1.9932F, -6.1343F, -4.3982F, 0.0F, 0.0F));

        PartDefinition wr_tire6_r1 = wheel_rear.addOrReplaceChild("wr_tire6_r1", CubeListBuilder.create().texOffs(356, 187).addBox(-2.7F, -1.05F, -2.1357F, 5.4F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 5.2182F, -3.7912F, -3.7699F, 0.0F, 0.0F));

        PartDefinition wr_tire5_r1 = wheel_rear.addOrReplaceChild("wr_tire5_r1", CubeListBuilder.create().texOffs(335, 187).addBox(-2.7F, -1.05F, -2.1357F, 5.4F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 6.45F, 0.0F, -3.1416F, 0.0F, 0.0F));

        PartDefinition wr_tire4_r1 = wheel_rear.addOrReplaceChild("wr_tire4_r1", CubeListBuilder.create().texOffs(314, 187).addBox(-2.7F, -1.05F, -2.1357F, 5.4F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 5.2182F, 3.7912F, -2.5133F, 0.0F, 0.0F));

        PartDefinition wr_tire3_r1 = wheel_rear.addOrReplaceChild("wr_tire3_r1", CubeListBuilder.create().texOffs(293, 187).addBox(-2.7F, -1.05F, -2.1357F, 5.4F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, 1.9932F, 6.1343F, -1.885F, 0.0F, 0.0F));

        PartDefinition wr_tire2_r1 = wheel_rear.addOrReplaceChild("wr_tire2_r1", CubeListBuilder.create().texOffs(272, 187).addBox(-2.7F, -1.05F, -2.1357F, 5.4F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, -1.9932F, 6.1343F, -1.2566F, 0.0F, 0.0F));

        PartDefinition wr_tire1_r1 = wheel_rear.addOrReplaceChild("wr_tire1_r1", CubeListBuilder.create().texOffs(251, 187).addBox(-2.7F, -1.05F, -2.1357F, 5.4F, 2.1F, 4.2715F), PartPose.offsetAndRotation(0.0F, -5.2182F, 3.7912F, -0.6283F, 0.0F, 0.0F));

        PartDefinition side_stand = chassis.addOrReplaceChild("side_stand", CubeListBuilder.create().texOffs(448, 167).addBox(-0.6F, 0.0F, -0.7F, 1.2F, 7.2F, 1.4F)
        .texOffs(363, 202).addBox(-0.8F, 6.9F, -1.8F, 2.6F, 1.2F, 3.2F), PartPose.offset(6.0F, -8.6F, 1.6F));

        PartDefinition engine = chassis.addOrReplaceChild("engine", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -2.0F, -24.5F, 1.0F, 1.0F, 38.0F)
        .texOffs(261, 125).addBox(-6.0F, -2.0F, 1.5F, 2.0F, 1.0F, 12.0F)
        .texOffs(133, 140).addBox(-7.0F, -2.0F, 2.5F, 1.0F, 1.0F, 11.0F)
        .texOffs(79, 154).addBox(-6.0F, -3.0F, 3.5F, 2.0F, 1.0F, 10.0F)
        .texOffs(167, 167).addBox(-7.0F, -3.0F, 5.5F, 1.0F, 1.0F, 8.0F)
        .texOffs(290, 125).addBox(-6.0F, -1.0F, 0.5F, 3.0F, 1.0F, 12.0F)
        .texOffs(104, 154).addBox(-7.0F, -1.0F, 1.5F, 1.0F, 1.0F, 10.0F)
        .texOffs(181, 140).addBox(-5.0F, 0.0F, -0.5F, 2.0F, 1.0F, 11.0F)
        .texOffs(127, 154).addBox(-6.0F, 0.0F, 0.5F, 1.0F, 1.0F, 10.0F)
        .texOffs(493, 167).addBox(-7.0F, 0.0F, 1.5F, 1.0F, 1.0F, 7.0F)
        .texOffs(0, 167).addBox(-5.0F, 1.0F, -1.5F, 1.0F, 1.0F, 9.0F)
        .texOffs(205, 167).addBox(-6.0F, 1.0F, -0.5F, 1.0F, 1.0F, 8.0F)
        .texOffs(298, 178).addBox(3.0F, -5.0F, 4.5F, 1.0F, 5.0F, 2.0F)
        .texOffs(69, 202).addBox(-7.0F, 1.0F, 1.5F, 1.0F, 1.0F, 4.0F)
        .texOffs(173, 154).addBox(-5.0F, 2.0F, -5.5F, 1.0F, 1.0F, 10.0F)
        .texOffs(92, 202).addBox(-6.0F, 2.0F, -0.5F, 1.0F, 1.0F, 4.0F)
        .texOffs(457, 187).addBox(4.0F, 1.0F, -1.5F, 1.0F, 1.0F, 5.0F)
        .texOffs(103, 202).addBox(4.0F, 0.0F, -0.5F, 1.0F, 1.0F, 4.0F)
        .texOffs(262, 167).addBox(4.0F, 2.0F, -5.5F, 1.0F, 1.0F, 8.0F)
        .texOffs(470, 187).addBox(3.0F, -2.0F, -1.5F, 1.0F, 2.0F, 4.0F)
        .texOffs(116, 125).addBox(3.0F, -3.0F, -11.5F, 1.0F, 1.0F, 13.0F)
        .texOffs(335, 178).addBox(3.0F, 3.0F, -5.5F, 2.0F, 1.0F, 6.0F)
        .texOffs(34, 178).addBox(-4.0F, 3.0F, -6.5F, 1.0F, 1.0F, 7.0F)
        .texOffs(481, 187).addBox(-5.0F, 3.0F, -4.5F, 1.0F, 1.0F, 5.0F)
        .texOffs(494, 187).addBox(5.0F, 2.0F, -4.5F, 1.0F, 1.0F, 5.0F)
        .texOffs(211, 209).addBox(5.0F, 1.0F, -1.5F, 1.0F, 1.0F, 2.0F)
        .texOffs(358, 140).addBox(4.0F, -3.0F, -11.5F, 2.0F, 1.0F, 11.0F)
        .texOffs(385, 140).addBox(-6.0F, -3.0F, -11.5F, 3.0F, 1.0F, 11.0F)
        .texOffs(456, 202).addBox(6.0F, 2.0F, -4.5F, 1.0F, 1.0F, 3.0F)
        .texOffs(465, 202).addBox(-6.0F, 2.0F, -4.5F, 1.0F, 1.0F, 3.0F)
        .texOffs(225, 209).addBox(-7.0F, 2.0F, -3.5F, 1.0F, 1.0F, 2.0F)
        .texOffs(479, 209).addBox(4.0F, 1.0F, -3.5F, 3.0F, 1.0F, 1.0F)
        .texOffs(488, 209).addBox(-7.0F, 1.0F, -3.5F, 3.0F, 1.0F, 1.0F)
        .texOffs(474, 202).addBox(6.0F, -3.0F, -7.5F, 1.0F, 1.0F, 3.0F), PartPose.offset(0.0F, -8.0F, 10.5F));
                return LayerDefinition.create(mesh, 512, 512);
    }

    @Override
    public void setupAnim(VehicleRenderState state) {
        super.setupAnim(state);
        this.bike.xScale = SCALE;
        this.bike.yScale = SCALE;
        this.bike.zScale = SCALE;
        // 龍頭夾在 ±45 度：再多就會穿進車殼，而且實車也轉不到那麼多
        this.steer.yRot = Mth.clamp(state.steer, -45f, 45f) * Mth.DEG_TO_RAD;
        this.wheelFront.xRot = state.wheelSpin;
        this.wheelRear.xRot = state.wheelSpin;
        // 有人騎就收側柱。沒有這一行，側柱會插在地上跟著車一起跑
        this.sideStand.zRot = state.parked ? 0f : STAND_UP;
    }
}
