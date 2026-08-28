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
 * <h2>比例</h2>
 * <p>獼猴不是縮小的人，也不是加了長尾巴的狗。牠的辨識點有三個：**頭大**（頭寬接近身體寬）、
 * **前肢比後肢長**、**尾巴細長且會翹**。三個都做到就認得出是猴子，少一個就變成別的動物。
 *
 * <p>臉盤（{@code face}）獨立成一塊薄的、往前突出的方塊。台灣獼猴的臉是**紅的、光的、
 * 沒有毛**，跟身上的毛色對比很強——那是牠最好認的特徵，值得多一塊方塊。
 *
 * <h2>動作</h2>
 * <p>四足小跑：前後肢反相擺動。搶到東西的時候前肢抬起來抱在胸前（{@code carrying}），
 * 那是「牠拿走了你的東西」在遠處唯一讀得出來的訊號。
 */
public class MacaqueModel extends EntityModel<MacaqueRenderState> {

    private final ModelPart head;
    private final ModelPart face;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tail;

    public MacaqueModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.face = this.head.getChild("face");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.tail = this.body.getChild("tail");
    }

    /**
     * 貼圖 64×64。各部位的 UV 起點見
     * {@code docs} 或素材清單——**改動這裡就要同步改貼圖**。
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 身體：四足姿態，所以是躺著的長方體
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-3.0f, -3.0f, -5.0f, 6, 6, 10),
                PartPose.offset(0.0f, 16.0f, 0.0f));

        // 尾巴：細、長、往上翹。往下垂的話會讀成老鼠
        body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(34, 16)
                        .addBox(-1.0f, -1.0f, 0.0f, 2, 2, 10),
                PartPose.offsetAndRotation(0.0f, -2.0f, 5.0f, -0.6f, 0.0f, 0.0f));

        // 頭：大。頭寬 7 對身體寬 6——猴子的頭幾乎跟身體一樣寬
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.5f, -3.5f, -3.5f, 7, 7, 7),
                PartPose.offset(0.0f, 13.0f, -5.0f));

        // 臉盤：薄薄一片突出來，貼圖上是紅色無毛的那塊
        head.addOrReplaceChild("face",
                CubeListBuilder.create().texOffs(28, 0)
                        .addBox(-2.0f, -1.5f, -1.5f, 4, 4, 2),
                PartPose.offset(0.0f, 0.5f, -3.5f));

        // 前肢比後肢長一格：這是猴子跟狗最明顯的差別
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-1.0f, 0.0f, -1.0f, 2, 8, 2),
                PartPose.offset(2.0f, 16.0f, -3.5f));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(0, 32).mirror()
                        .addBox(-1.0f, 0.0f, -1.0f, 2, 8, 2),
                PartPose.offset(-2.0f, 16.0f, -3.5f));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(10, 32)
                        .addBox(-1.0f, 0.0f, -1.0f, 2, 7, 2),
                PartPose.offset(2.0f, 17.0f, 3.5f));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(10, 32).mirror()
                        .addBox(-1.0f, 0.0f, -1.0f, 2, 7, 2),
                PartPose.offset(-2.0f, 17.0f, 3.5f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(MacaqueRenderState state) {
        super.setupAnim(state);

        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;

        // 四足小跑：前後反相。振幅限制在 1.0 以內，不然全速時腿會轉一圈
        float swing = state.walkAnimationPos;
        float amount = Math.min(state.walkAnimationSpeed, 1.0f);
        this.leftLeg.xRot = Mth.cos(swing * 0.7f) * 1.2f * amount;
        this.rightLeg.xRot = Mth.cos(swing * 0.7f + Mth.PI) * 1.2f * amount;
        this.leftArm.xRot = Mth.cos(swing * 0.7f + Mth.PI) * 1.2f * amount;
        this.rightArm.xRot = Mth.cos(swing * 0.7f) * 1.2f * amount;

        // 尾巴慢慢左右擺。完全不動的尾巴會讓整隻看起來像標本
        this.tail.yRot = Mth.cos(state.ageInTicks * 0.12f) * 0.25f;
        this.tail.xRot = -0.6f + Mth.sin(state.ageInTicks * 0.09f) * 0.12f;

        // 抱著贓物：兩隻前肢往前收到胸口，蓋掉走路的擺動
        if (state.carrying) {
            this.leftArm.xRot = -1.35f;
            this.rightArm.xRot = -1.35f;
            this.leftArm.zRot = -0.35f;
            this.rightArm.zRot = 0.35f;
        } else {
            this.leftArm.zRot = 0.0f;
            this.rightArm.zRot = 0.0f;
        }
    }
}
