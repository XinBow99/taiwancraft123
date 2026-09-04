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
 * 台灣獼猴的模型。
 *
 * <h2>這個模型是看著渲染圖改出來的，不是憑想像寫的</h2>
 * <p>{@code tools/model-viewer.html} 會用同一組數字把牠從十二個角度畫出來，還會印出
 * 「腳底在 y 多少」。第一版就是靠它抓到腳底停在 y=25——**在遊戲裡那是猴子陷進土裡一格**。
 * 改動下面任何一個數字之後，請重跑那個檢視器再進遊戲。
 *
 * <h2>比例：三件事決定牠像不像猴子</h2>
 * <ol>
 *   <li><b>背是斜的。</b>臀部比肩高（軀幹整體 -6°）。平的背會讀成狗。</li>
 *   <li><b>後肢是屈的。</b>大腿往前 28°、小腿往後 34°。直筒後腿是牛，屈著的才是靈長類。</li>
 *   <li><b>尾巴走一道連續的弧。</b>分三節、每節同向再彎一點。兩節的話中間會出現一個
 *       肘關節，讀起來像旗桿。</li>
 * </ol>
 *
 * <h2>臉不做成方塊</h2>
 * <p>粉紅色的裸臉直接畫在頭部貼圖的正面（UV 34,4）。做成一塊獨立的薄方塊會有兩個問題：
 * 貼在頭表面上會 z-fighting，往外推又會變成一塊掛在臉上的招牌。**只有真的凸出來的口鼻
 * 才值得一個方塊**——那是猴子側面輪廓的關鍵。
 *
 * <h2>耳朵要小</h2>
 * <p>1×2×2，貼著頭。做大就變成兩片粉紅色把手；真的獼猴從正面幾乎看不到耳朵。
 */
public class MacaqueModel extends EntityModel<MacaqueRenderState> {

    /** 各部位的基礎角度（弧度）。setupAnim 是在這些之上疊動作，不是覆蓋它們。 */
    private static final float ARM_BASE = -10f * Mth.DEG_TO_RAD;
    private static final float THIGH_BASE = 28f * Mth.DEG_TO_RAD;

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart armLeft;
    private final ModelPart armRight;
    private final ModelPart thighLeft;
    private final ModelPart thighRight;
    private final ModelPart tail;
    private final ModelPart tailMid;

    public MacaqueModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.armLeft = root.getChild("arm_left");
        this.armRight = root.getChild("arm_right");
        this.thighLeft = root.getChild("thigh_left");
        this.thighRight = root.getChild("thigh_right");
        this.tail = this.body.getChild("tail");
        this.tailMid = this.tail.getChild("tail_mid");
    }

    /**
     * 貼圖 64×64。UV 配置見 README 的素材表。
     *
     * <p>左右成對的部位用 {@code mirror()} 共用同一塊 UV：省貼圖空間，而且左右一定對稱。
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 軀幹。前低後高，所以整塊往上仰 6 度
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.5f, -2.5f, -5.0f, 5, 5, 10),
                PartPose.offsetAndRotation(0.0f, 15.5f, 0.0f, -6f * Mth.DEG_TO_RAD, 0f, 0f));

        // 尾巴三節，愈往後愈細
        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(30, 9)
                        .addBox(-1.0f, -1.0f, 0.0f, 2, 2, 5),
                PartPose.offsetAndRotation(0.0f, -1.8f, 5.0f, -22f * Mth.DEG_TO_RAD, 0f, 0f));
        PartDefinition tailMid = tail.addOrReplaceChild("tail_mid",
                CubeListBuilder.create().texOffs(46, 9)
                        .addBox(-0.5f, -0.5f, 0.0f, 1, 1, 5),
                PartPose.offsetAndRotation(0.0f, 0.0f, 4.6f, 34f * Mth.DEG_TO_RAD, 0f, 0f));
        tailMid.addOrReplaceChild("tail_tip",
                CubeListBuilder.create().texOffs(34, 21)
                        .addBox(-0.5f, -0.5f, 0.0f, 1, 1, 4),
                PartPose.offsetAndRotation(0.0f, 0.0f, 4.6f, 36f * Mth.DEG_TO_RAD, 0f, 0f));

        // 頭寬 5。**這個寬度是被眼睛決定的**：臉的正面就是頭的寬度，寬 4 的話兩隻眼睛
        // 中間留不出一格空隙，在遊戲裡會連成一條黑色橫槓
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(30, 0)
                        .addBox(-2.5f, -2.5f, -2.0f, 5, 5, 4),
                PartPose.offset(0.0f, 12.0f, -5.5f));

        // 口鼻：凸出去 2 格。猴子的側臉就是靠這塊
        head.addOrReplaceChild("muzzle",
                CubeListBuilder.create().texOffs(48, 0)
                        .addBox(-1.0f, 0.0f, -2.0f, 2, 2, 2),
                PartPose.offset(0.0f, 0.5f, -2.0f));

        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(56, 0)
                        .addBox(0.0f, -1.0f, -1.0f, 1, 2, 2),
                PartPose.offset(2.5f, 0.0f, 0.0f));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(56, 0).mirror()
                        .addBox(-1.0f, -1.0f, -1.0f, 1, 2, 2),
                PartPose.offset(-2.5f, 0.0f, 0.0f));

        // 前肢：長，構到地面。獼猴的前肢比後肢長，這是牠跟狗最明顯的差別
        root.addOrReplaceChild("arm_left",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-1.0f, 0.0f, -1.0f, 2, 9, 2),
                PartPose.offsetAndRotation(2.0f, 14.5f, -3.5f, ARM_BASE, 0f, 0f));
        root.addOrReplaceChild("arm_right",
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-1.0f, 0.0f, -1.0f, 2, 9, 2),
                PartPose.offsetAndRotation(-2.0f, 14.5f, -3.5f, ARM_BASE, 0f, 0f));

        // 後肢：大腿往前、小腿往後，屈成一個 Z
        addHindLeg(root, "left", 2.0f, false);
        addHindLeg(root, "right", -2.0f, true);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addHindLeg(PartDefinition root, String side, float x, boolean mirror) {
        PartDefinition thigh = root.addOrReplaceChild("thigh_" + side,
                CubeListBuilder.create().texOffs(10, 16).mirror(mirror)
                        .addBox(-1.5f, 0.0f, -1.5f, 3, 4, 3),
                PartPose.offsetAndRotation(x, 14.5f, 3.2f, THIGH_BASE, 0f, 0f));

        PartDefinition shin = thigh.addOrReplaceChild("shin_" + side,
                CubeListBuilder.create().texOffs(24, 16).mirror(mirror)
                        .addBox(-1.0f, 0.0f, -1.0f, 2, 5, 2),
                PartPose.offsetAndRotation(0.0f, 3.6f, 1.2f, -34f * Mth.DEG_TO_RAD, 0f, 0f));

        // 腳掌往前伸出去一格：踩在地上的那一塊，沒有它後腿會像兩根棍子插進土裡
        shin.addOrReplaceChild("foot_" + side,
                CubeListBuilder.create().texOffs(34, 16).mirror(mirror)
                        .addBox(-1.0f, 4.6f, -2.0f, 2, 1, 3),
                PartPose.ZERO);
    }

    @Override
    public void setupAnim(MacaqueRenderState state) {
        // 這一行會把所有部位歸位到 PartPose，所以下面全部用「疊加」而不是「指定」
        super.setupAnim(state);

        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;

        // 四足小跑：前後肢反相。振幅夾在 1 以內，不然全速時腿會轉一圈
        float swing = state.walkAnimationPos * 0.7f;
        float amount = Math.min(state.walkAnimationSpeed, 1.0f);
        this.armLeft.xRot += Mth.cos(swing + Mth.PI) * 1.15f * amount;
        this.armRight.xRot += Mth.cos(swing) * 1.15f * amount;
        this.thighLeft.xRot += Mth.cos(swing) * 0.75f * amount;
        this.thighRight.xRot += Mth.cos(swing + Mth.PI) * 0.75f * amount;

        // 尾巴慢慢晃。完全不動的尾巴會讓整隻看起來像標本
        this.tail.yRot = Mth.cos(state.ageInTicks * 0.10f) * 0.20f;
        this.tail.xRot += Mth.sin(state.ageInTicks * 0.08f) * 0.10f;
        this.tailMid.yRot = Mth.cos(state.ageInTicks * 0.10f - 0.6f) * 0.22f;

        // 抱著贓物：兩隻前肢收到胸口。這是「牠拿走了你的東西」在遠處唯一讀得出來的訊號
        if (state.carrying) {
            this.armLeft.xRot = -1.30f;
            this.armRight.xRot = -1.30f;
            this.armLeft.zRot = -0.30f;
            this.armRight.zRot = 0.30f;
            this.body.xRot = -0.35f;      // 上半身跟著挺起來
        }
    }
}
