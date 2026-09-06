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
 * <h2>來源：{@code models/monkey.bbmodel}</h2>
 * <p>幾何是從那個 Blockbench 專案用 Modded Entity 匯出、再翻成 Mojmap 的，**不是手算的**。
 * 要改形狀請開 .bbmodel 改完重匯，不要直接改這裡的數字——.bbmodel 是唯一的來源。
 *
 * <p>注意 {@code tools/models.js} 裡的 macaque 還是舊模型，已經不對應這裡了；形狀改用
 * Blockbench 直接看，{@code tools/model-viewer.html} 對這隻已經沒有意義。
 *
 * <h2>座標系：Blockbench 的 +X 是模型的右邊，Minecraft 的 +X 是模型的左邊</h2>
 * <p>匯出時 X 會被翻號。所以 .bbmodel 裡叫 {@code arm_left} 的那隻，在這裡是
 * {@link #armRight}（x = -2.5）。左右對稱所以外觀沒差，但**抱贓物的是哪隻手**會對調——
 * 這裡沿用 .bbmodel，抱東西的是 x 為負的那隻。
 *
 * <h2>比例：四足站姿，不是直立的卡通猴</h2>
 * <ol>
 *   <li><b>身體是橫的。</b>軀幹 8×7×11 躺平，頭在前端、只比背高 3 格。第一版把頭做得
 *       又高又前伸，整隻讀成駱駝。</li>
 *   <li><b>臉不是方塊。</b>裸露的粉紅皮膚整片畫在頭部貼圖正面，只有口鼻是一塊
 *       **深 0.6 格**的淺凸起。之前做成深 2 格的方塊，從側面看就是一隻豬。</li>
 *   <li><b>尾巴六節。</b>節數少了轉折會變成折角。六節才彎得出影片裡那道連續的弧，
 *       而且抬起／垂下兩種姿態共用同一組比例（見 {@link #TAIL_LIFT_RATIO}）。</li>
 * </ol>
 *
 * <h2>貼圖宣告 64×64，實際檔案是 128×128</h2>
 * <p>不是筆誤。{@link LayerDefinition#create} 的尺寸只用來把 UV 正規化成 0~1，取樣時
 * 用的是實際圖檔——所以丟一張兩倍大的圖進去，等於整隻的貼圖密度加倍，臉才畫得下眼睛和鼻孔。
 * 這也是原版換 HD 面板材質的做法。**改這裡的 64 會讓整張貼圖錯位**。
 */
public class MacaqueModel extends EntityModel<MacaqueRenderState> {

    /** 尾巴基礎角度（度）。負值＝垂下；setupAnim 往上加就會抬起來。 */
    private static final float[] TAIL_BASE = { -35f, -20f, -18f, -15f, -12f, -10f };

    /**
     * 抬尾巴時各節分到的比例。
     *
     * <p>整條尾巴的姿態只由一個純量控制（第一節抬多少），其餘節按這組比例跟著彎——
     * 走路的 35/20/16/10/4 度和抱贓物的 70/40/33/20/7 度，比例是同一組。所以「抬高一點」
     * 只要調一個數字，不會把弧線調歪。
     */
    private static final float[] TAIL_LIFT_RATIO = { 1.00f, 0.57f, 0.47f, 0.29f, 0.10f, 0.00f };

    /** 站著不動 / 走路 / 抱著贓物時第一節抬起的角度。影片裡獼猴走動時尾巴就是舉著的。 */
    private static final float TAIL_LIFT_IDLE = 20f * Mth.DEG_TO_RAD;
    private static final float TAIL_LIFT_WALK = 35f * Mth.DEG_TO_RAD;
    private static final float TAIL_LIFT_CARRY = 70f * Mth.DEG_TO_RAD;

    // ---------------------------------------------------------------- 坐姿
    //
    // 全部是「相對站姿要加多少」，不是絕對角度——這樣才混合得起來。數值來自
    // models/monkey.bbmodel 的 sit 動畫，Blockbench 的動畫通道跟 MC 的 xRot 剛好同號，
    // 可以直接加。

    /** 軀幹立起 65 度。影片裡牠在冰箱前就是幾乎直立的；只立 35 度會讀成趴著。 */
    private static final float SIT_BODY_PITCH = -65f * Mth.DEG_TO_RAD;
    /** 立起來之後臀部要落到地上，不然整隻會浮在半空。 */
    private static final float SIT_BODY_DROP = 4.1f;
    /** 後腿往前折。沒有膝關節，所以是「人坐著把腿伸出去」那種折法。 */
    private static final float SIT_LEG = -6f * Mth.DEG_TO_RAD;
    /** 前肢垂下來撐在身前的地上。 */
    private static final float SIT_ARM = 43f * Mth.DEG_TO_RAD;
    /** 抵銷軀幹的仰角，讓頭維持看著前方而不是仰望天空。 */
    private static final float SIT_HEAD = 55f * Mth.DEG_TO_RAD;
    /** 坐著時尾巴平貼地面往後拉直——跟走路時翹起來是完全相反的姿態。 */
    private static final float[] SIT_TAIL = { 55f, 40f, 38f, 20f, 12f, 10f };

    // ---------------------------------------------------------------- 揮擊
    //
    // 姿勢沿用 .bbmodel 的 reach（用後腿撐起來、一隻前肢往前上方伸），改綁在攻擊上。
    // 那個動作本來是設計來構冰箱門的，拿來當撲擊剛好——獼猴打人就是這樣立起來抓一把。
    // 幅度比原本的 reach 收斂，因為這是一瞬間的動作不是維持的姿勢。

    private static final float SWIPE_BODY_PITCH = -28f * Mth.DEG_TO_RAD;
    private static final float SWIPE_BODY_DROP = 1.6f;
    /** 後腿往前一點吃重：整隻用後腿撐起來的時候重心要往前移。 */
    private static final float SWIPE_LEG = 8f * Mth.DEG_TO_RAD;
    /** 揮出去的那隻手。角度是相對軀幹的，軀幹已經仰了，所以世界角度更高。 */
    private static final float SWIPE_ARM_MAIN = -95f * Mth.DEG_TO_RAD;
    /** 另一隻也抬一點，兩隻都垂著會像被吊起來。 */
    private static final float SWIPE_ARM_OFF = -25f * Mth.DEG_TO_RAD;
    private static final float SWIPE_HEAD = 30f * Mth.DEG_TO_RAD;

    // ---------------------------------------------------------------- 抱贓物

    /** 站著跑的時候手臂的角度；坐著吃的時候要收高一點靠近嘴巴。 */
    private static final float CARRY_ARM_STAND = -1.75f;
    private static final float CARRY_ARM_SIT = -0.85f;
    private static final float CARRY_ARM_ROLL = 0.35f;
    private static final float CARRY_BODY_PITCH = -0.14f;

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart armLeft;
    private final ModelPart armRight;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart[] tail = new ModelPart[6];

    public MacaqueModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.armLeft = this.body.getChild("arm_left");
        this.armRight = this.body.getChild("arm_right");
        this.legLeft = this.body.getChild("leg_left");
        this.legRight = this.body.getChild("leg_right");

        ModelPart segment = this.body;
        for (int i = 0; i < this.tail.length; i++) {
            segment = segment.getChild("tail_" + (i + 1));
            this.tail[i] = segment;
        }
    }

    /** 軀幹。贓物的 render layer 要從這裡開始沿著骨架走下去。 */
    public ModelPart bodyPart() {
        return this.body;
    }

    /**
     * 抱贓物的那隻前肢。
     *
     * <p>是 .bbmodel 裡的 {@code arm_left}——匯出時 X 被翻號，所以在這裡是 x = -2.5 的
     * {@link #armRight}。{@link MacaqueLootLayer} 要跟著它走，別在那邊另外寫死座標。
     */
    public ModelPart carryingArm() {
        return this.armRight;
    }

    /**
     * 每個方塊都是獨立的 part，連貼著父件不動的手掌、腳掌、耳朵也是。
     *
     * <p>看起來囉唆，但這樣 {@code check-model.mjs} 才比得動——它比的是 addBox 與 PartPose
     * 的多重集合，一個 part 塞兩個 addBox 會讓兩邊的筆數對不起來。左右也不用 {@code mirror()}，
     * 因為 .bbmodel 給左右各排了獨立的 UV 區塊。
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 軀幹。整隻的根：頭、四肢、尾巴都掛在它下面，所以身體一起伏全身跟著動
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0f, -4.0f, -5.0f, 8.0f, 7.0f, 11.0f),
                PartPose.offset(0.0f, 13.0f, 0.0f));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(38, 0)
                        .addBox(-3.5f, -5.0f, -6.0f, 7.0f, 8.0f, 6.0f),
                PartPose.offset(0.0f, -3.0f, -3.0f));

        // 口鼻只凸 0.6 格。凸 2 格的話側面剪影就是豬鼻子——這是整隻最容易做壞的地方
        head.addOrReplaceChild("muzzle",
                CubeListBuilder.create().texOffs(48, 18)
                        .addBox(-1.5f, -1.0f, -6.6f, 3.0f, 3.0f, 0.6f),
                PartPose.ZERO);

        // 耳朵 1×2×2，貼著頭。真的獼猴從正面幾乎看不到耳朵
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(58, 18)
                        .addBox(-4.7f, -3.0f, -4.0f, 1.0f, 2.0f, 2.0f),
                PartPose.ZERO);
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(0, 35)
                        .addBox(3.7f, -3.0f, -4.0f, 1.0f, 2.0f, 2.0f),
                PartPose.ZERO);

        // 前肢在胸下、後肢在臀下。掌部往前伸 5 格，是真正踩在地上的那一塊
        PartDefinition armRight = body.addOrReplaceChild("arm_right",
                CubeListBuilder.create().texOffs(0, 18)
                        .addBox(-1.5f, 0.0f, -1.5f, 3.0f, 7.0f, 3.0f),
                PartPose.offset(-2.5f, 2.0f, -2.5f));
        armRight.addOrReplaceChild("hand_right",
                CubeListBuilder.create().texOffs(0, 28)
                        .addBox(-1.5f, 7.0f, -2.5f, 3.0f, 2.0f, 5.0f),
                PartPose.ZERO);

        PartDefinition armLeft = body.addOrReplaceChild("arm_left",
                CubeListBuilder.create().texOffs(12, 18)
                        .addBox(-1.5f, 0.0f, -1.5f, 3.0f, 7.0f, 3.0f),
                PartPose.offset(2.5f, 2.0f, -2.5f));
        armLeft.addOrReplaceChild("hand_left",
                CubeListBuilder.create().texOffs(16, 28)
                        .addBox(-1.5f, 7.0f, -2.5f, 3.0f, 2.0f, 5.0f),
                PartPose.ZERO);

        PartDefinition legRight = body.addOrReplaceChild("leg_right",
                CubeListBuilder.create().texOffs(24, 18)
                        .addBox(-1.5f, 0.0f, -1.5f, 3.0f, 7.0f, 3.0f),
                PartPose.offset(-2.5f, 2.0f, 3.5f));
        legRight.addOrReplaceChild("foot_right",
                CubeListBuilder.create().texOffs(32, 28)
                        .addBox(-1.5f, 7.0f, -2.5f, 3.0f, 2.0f, 5.0f),
                PartPose.ZERO);

        PartDefinition legLeft = body.addOrReplaceChild("leg_left",
                CubeListBuilder.create().texOffs(36, 18)
                        .addBox(-1.5f, 0.0f, -1.5f, 3.0f, 7.0f, 3.0f),
                PartPose.offset(2.5f, 2.0f, 3.5f));
        legLeft.addOrReplaceChild("foot_left",
                CubeListBuilder.create().texOffs(48, 28)
                        .addBox(-1.5f, 7.0f, -2.5f, 3.0f, 2.0f, 5.0f),
                PartPose.ZERO);

        // 尾巴：六節等長，每節再往下折一點，接起來是一道連續的弧
        int[] tailU = { 6, 14, 22, 30, 38, 46 };
        PartDefinition segment = body;
        for (int i = 0; i < TAIL_BASE.length; i++) {
            segment = segment.addOrReplaceChild("tail_" + (i + 1),
                    CubeListBuilder.create().texOffs(tailU[i], 35)
                            .addBox(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 2.0f),
                    // 第一節掛在臀部上緣，之後每節接在前一節的末端
                    i == 0
                            ? PartPose.offsetAndRotation(0.0f, -2.0f, 6.0f,
                                    TAIL_BASE[i] * Mth.DEG_TO_RAD, 0.0f, 0.0f)
                            : PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f,
                                    TAIL_BASE[i] * Mth.DEG_TO_RAD, 0.0f, 0.0f));
        }

        // 64×64 是 UV 空間，實際貼圖 128×128（見類別註解）
        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * 三種姿態用權重混合，不是 if / else 切換。
     *
     * <h2>為什麼不是 if</h2>
     * <p>站姿與坐姿的軀幹差 65 度。用 if 挑一個的話，狀態一變就是**一格內從站變坐**——
     * 在遊戲裡那看起來是模型壞掉，不是動作。所以改成三個權重：
     * {@code upright + sit + swipe = 1}，站姿的動作乘 {@code upright}、坐姿的角度乘
     * {@code sit}，中間每一幀都是合法的中間姿勢。補間本身在 entity 端做（那裡才知道
     * partialTick），這裡只負責把權重套上去。
     *
     * <h2>坐下就是待機</h2>
     * <p>{@code sitAmount} 由「導航停了、沒有目標、站著沒動超過兩秒」決定，散步 goal 一動
     * 就自動歸零。所以不需要另外寫一個 idle 動畫去跟 sit 搶——**站著不動的那個狀態本身
     * 就是坐著**，而走路是從坐姿混回站姿的過程。
     */
    @Override
    public void setupAnim(MacaqueRenderState state) {
        // 這一行把所有部位歸位到 PartPose，所以下面凡是要保留基礎角度的都用 +=
        super.setupAnim(state);

        // 揮擊優先於坐下：打人的時候一定是站起來的
        float swipe = Mth.clamp(state.swipeAmount, 0.0f, 1.0f);
        float sit = Mth.clamp(state.sitAmount, 0.0f, 1.0f) * (1.0f - swipe);
        float upright = 1.0f - sit - swipe;

        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;

        // 四足對角步態：右前肢與左後肢同相，左前肢與右後肢反相。
        // 振幅夾在 1 以內，不然全速時腿會轉一整圈；再乘 upright，坐下時腿就不再擺
        float swing = state.walkAnimationPos * 0.7f;
        float amount = Math.min(state.walkAnimationSpeed, 1.0f);
        float gait = amount * upright;
        float front = Mth.cos(swing) * 0.49f * gait;
        float hind = Mth.cos(swing) * 0.52f * gait;
        this.armRight.xRot += front;
        this.legLeft.xRot += hind;
        this.armLeft.xRot -= front;
        this.legRight.xRot -= hind;

        // 身體一步彈兩次（四肢交錯而過的瞬間最高）。MC 的 y 往下為正，所以抬高是減
        this.body.y -= Math.abs(Mth.sin(swing)) * 0.5f * gait;
        // 跑起來多一份前後俯仰。這就是 .bbmodel 裡 run 跟 walk 的差別——不需要另一個狀態，
        // 振幅本來就跟著 walkAnimationSpeed 長
        this.body.xRot += Mth.cos(swing * 2.0f) * 0.06f * gait;

        // 尾巴：站著微抬、走路抬高、抱贓物抬到最高、坐著平放在地上。
        // 這是影片裡最明顯的一件事——獼猴走動時尾巴是舉著的，垂下來的尾巴會讀成狗
        float lift = state.carrying
                ? TAIL_LIFT_CARRY
                : Mth.lerp(amount, TAIL_LIFT_IDLE, TAIL_LIFT_WALK);
        for (int i = 0; i < this.tail.length; i++) {
            float standing = lift * TAIL_LIFT_RATIO[i];
            float seated = SIT_TAIL[i] * Mth.DEG_TO_RAD;
            // 揮擊時重心往後，尾巴也跟著往下放一些，但不像坐著那麼平
            this.tail[i].xRot += standing * upright + seated * sit + seated * 0.6f * swipe;
        }

        // 左右擺動用 yRot 不是 zRot：尾巴是沿著 +Z 長的，繞 Z 轉只是原地打滾，看不出來。
        // 三節之間錯開相位，尾端才會有拖尾的感覺。坐著貼在地上就幾乎不擺
        float sway = 0.2f + 0.8f * upright;
        float t = state.ageInTicks * 0.10f;
        this.tail[0].yRot = Mth.cos(t) * 0.10f * sway;
        this.tail[2].yRot = Mth.cos(t - 0.6f) * 0.12f * sway;
        this.tail[4].yRot = Mth.cos(t - 1.2f) * 0.14f * sway;

        // 坐姿
        if (sit > 0.0f) {
            this.body.xRot += SIT_BODY_PITCH * sit;
            this.body.y += SIT_BODY_DROP * sit;
            this.legLeft.xRot += SIT_LEG * sit;
            this.legRight.xRot += SIT_LEG * sit;
            this.armLeft.xRot += SIT_ARM * sit;
            this.armRight.xRot += SIT_ARM * sit;
            this.head.xRot += SIT_HEAD * sit;
        }

        // 揮擊：用後腿撐起來、一隻前肢往前上方掃。由原版的 attackAnim 驅動，
        // 一次攻擊之間走 0 → 1 → 0
        if (swipe > 0.0f) {
            this.body.xRot += SWIPE_BODY_PITCH * swipe;
            this.body.y += SWIPE_BODY_DROP * swipe;
            this.legLeft.xRot += SWIPE_LEG * swipe;
            this.legRight.xRot += SWIPE_LEG * swipe;
            this.armRight.xRot += SWIPE_ARM_MAIN * swipe;
            this.armLeft.xRot += SWIPE_ARM_OFF * swipe;
            this.head.xRot += SWIPE_HEAD * swipe;
        }

        // 抱著贓物：一隻前肢收到胸前。
        // 這是「牠剛搶了你的東西」在遠處讀得出來的訊號，所以動作要大到剪影看得出來。
        // 這裡是**指定**不是疊加：這隻手由贓物決定，上面的步態與坐姿都不算數
        if (state.carrying) {
            this.armRight.xRot = Mth.lerp(sit, CARRY_ARM_STAND, CARRY_ARM_SIT);
            this.armRight.zRot = CARRY_ARM_ROLL;
            // 站著跑才需要把上身挺起來；坐著吃的時候軀幹已經是立的了
            this.body.xRot += CARRY_BODY_PITCH * upright;
            this.head.xRot -= 0.10f * upright;
        }
    }
}
