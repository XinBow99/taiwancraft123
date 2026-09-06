package com.xinbow99.taiwan.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xinbow99.taiwan.entity.VehicleModel;
import com.xinbow99.taiwan.Taiwan;
import com.xinbow99.taiwan.entity.RoadVehicle;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Axis;

import java.util.EnumMap;
import java.util.Map;

/**
 * 速克達的算繪器。
 *
 * <p>模型的正面是 -Z，而實體的 yaw 0 面向 +Z，所以要轉 180 度——這跟原版的船同一個約定。
 * 少了這一下，車會倒著跑。
 */
public class VehicleRenderer extends EntityRenderer<RoadVehicle, VehicleRenderState> {

    public static final ModelLayerLocation LANBAO_LAYER =
            new ModelLayerLocation(Taiwan.id("lanbao"), "main");
    public static final ModelLayerLocation MASHALA_LAYER =
            new ModelLayerLocation(Taiwan.id("mashala"), "main");
    public static final ModelLayerLocation CYGNUS_LAYER =
            new ModelLayerLocation(Taiwan.id("cygnus"), "main");

    /**
     * 開大燈時要疊上去的發光貼圖。只有做了的車款才有。
     *
     * <p>那張圖除了燈罩以外全是透明的——所以第二次算繪只會畫出燈罩，其餘像素被 cutout
     * 丟掉。沒有這一項的車款（目前是兩台跑車）按了大燈鍵不會有反應。
     */
    private static final Map<VehicleModel, Identifier> LIT_TEXTURE = Map.of(
            VehicleModel.CYGNUS, Taiwan.id("textures/entity/cygnus_lit.png"));

    /**
     * 走網格算繪的車款，對到它的幾何資源。
     *
     * <p>這些車沒有 {@code ModelPart} 模型——原版的零件樹只畫得了長方體。低多邊形的車殼
     * 走 {@code submitCustomGeometry} 直接送頂點，見 {@link MeshGeometry}。
     */
    private static final Map<VehicleModel, Identifier> MESH = Map.of(
            VehicleModel.TRUCK, Taiwan.id("models/entity/truck.json"));

    /** 全亮的 lightmap 座標（區塊光 15、天光 15）。這個版本沒有具名常數，只能寫值。 */
    private static final int FULL_BRIGHT = 0xF000F0;

    /**
     * 各車款的模型都在這裡烘好，不是每次要畫才建。
     *
     * <p>{@code bakeLayer} 會把整棵零件樹展開成頂點資料，那是建構期的工作；放進
     * {@code submit()} 的話每一幀、每一台車都要重來一次。模型的記憶體成本是常數，
     * 用一個 {@code EnumMap} 換掉 if-else 是為了以後加車款時不用再動這個方法。
     */
    private final Map<VehicleModel, EntityModel<VehicleRenderState>> models;

    public VehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.models = new EnumMap<>(VehicleModel.class);
        this.models.put(VehicleModel.CYGNUS, new CygnusModel(context.bakeLayer(CYGNUS_LAYER)));
        this.models.put(VehicleModel.LANBAO, new LanbaoModel(context.bakeLayer(LANBAO_LAYER)));
        this.models.put(VehicleModel.MASHALA, new MashalaModel(context.bakeLayer(MASHALA_LAYER)));
        this.shadowRadius = 0.6f;
    }

    @Override
    public VehicleRenderState createRenderState() {
        return new VehicleRenderState();
    }

    @Override
    public void extractRenderState(RoadVehicle entity, VehicleRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        Vec3 motion = entity.getDeltaMovement();
        float horizontal = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        state.wheelSpin = (entity.tickCount + partialTick) * horizontal * 1.6f;
        state.steer = entity.steerAngle();
        // 壓車角度是實體自己算的（把手角度 × 速度），不是這裡從把手角度推的：
        // 高速時把手只打得動 8 度，用「龍頭的一半」去傾，全速過彎會只傾 4 度像在滑冰
        state.lean = entity.leanAngle();
        // **一定要用 partialTick 內插**，不能直接讀 getYRot()。
        //
        // 這是「車體轉向跟整體轉向對不上」的來源。實體的 yaw 一個 tick 只更新一次（20Hz），
        // 但位置是每一幀內插的（60Hz+）。直接抄 getYRot() 的話，車子是「平滑地移動 ＋ 每三幀
        // 跳 2 度」——移動跟旋轉不同步。在第一人稱這特別明顯，因為模型就貼在鏡頭旁邊，
        // 一格 1 公尺外的 2 度跳動看起來就是車身在鏡頭底下來回甩。
        //
        // getYRot(partialTick) 會在 yRotO（tick 開始時的 yaw）與現在的 yaw 之間內插，
        // 跟位置用的是同一個時間軸。原版每一個實體算繪器都是這樣做的
        state.yRot = entity.getYRot(partialTick);
        state.variant = entity.variant();
        state.parked = entity.getPassengers().isEmpty();
        state.headlight = entity.headlightOn();
    }

    /**
     * 送網格幾何。
     *
     * <p>回呼只給一個 {@code Pose}（沒有 PoseStack），所以骨骼的變換由 {@link MeshGeometry}
     * 自己複製一份 Pose 去做。輪子靠名字認：以 {@code wheel_} 開頭的骨骼吃 {@code wheelSpin}。
     *
     * <p>用 {@code entityCutout} 而不是 {@code entityCutoutCull}：26.2 的命名跟直覺相反，
     * 前者才是**不剔除背面**的那個。低多邊形模型不保證封閉，剔除背面會在
     * 開口處看到破洞。多畫的那一點面在 408 個面的規模下不值得省。
     */
    private void submitMesh(VehicleRenderState state, PoseStack pose, SubmitNodeCollector collector) {
        MeshGeometry.get(MESH.get(state.variant)).ifPresent(geometry ->
                collector.submitCustomGeometry(pose,
                        RenderTypes.entityCutout(state.variant.texture()),
                        (p, buffer) -> {
                            for (MeshGeometry.Bone bone : geometry.bones()) {
                                float spin = bone.name().startsWith("wheel_") ? state.wheelSpin : 0.0f;
                                MeshGeometry.render(bone, p, buffer,
                                        state.lightCoords, OverlayTexture.NO_OVERLAY, -1, spin);
                            }
                        }));
    }

    @Override
    public void submit(VehicleRenderState state, PoseStack pose,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        // 模型是以 y=24 為地面畫的，往上抬 1.5 格才會站在實體的腳下
        pose.translate(0.0f, 1.5f, 0.0f);
        // 順序不能反過來：**先轉 yaw，翻正放最裡面**。
        //
        // 這裡原本是先 ZP(180) 再 YP(180 − yaw)。ZP(180) 會把 Y 軸翻成 −Y，所以緊接著的
        // YP 是繞著翻過去的軸在轉——方向整個相反。矩陣上就是 Z(180)·Y(θ) = Y(−θ)·Z(180)：
        // 畫出來的車頭朝向變成實體 yaw 的**相反數**。
        //
        // 症狀很難聯想到這裡：車正好朝 0 度或 180 度時完全正確，其他角度才是鏡像；而且
        // 車一轉彎，模型與真實朝向的誤差是以「兩倍轉向速率」在拉開的，所以看起來像是
        // 車體自己在轉圈圈、轉得比車實際的轉向還快。物理沒有問題，是這裡畫反了。
        //
        // 壓車角度跟 180 度的翻正是同一個軸，合成一次轉完；它本來就在最裡面（車身自己的
        // 縱軸），這次沒有改變它相對於翻正的位置
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - state.yRot));
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f + state.lean));

        // 模型與貼圖都由車款決定。兩者一定要一起取——拿 A 的模型配 B 的貼圖，
        // 顏色會整台錯位（色票版型雖然一樣，填的顏色不一樣）
        if (MESH.containsKey(state.variant)) {
            submitMesh(state, pose, collector);
            pose.popPose();
            super.submit(state, pose, collector, camera);
            return;
        }

        EntityModel<VehicleRenderState> model = this.models.get(state.variant);
        model.setupAnim(state);
        // 最後那個 int 是**外框顏色**，不是模型顏色。
        //
        // 這裡本來寫死 -1（＝0xFFFFFFFF，不透明白色），於是每一台機車都被畫上一圈白色描邊，
        // 而描邊是那種會穿過牆壁畫在最上層的東西——整座城的機車在山的另一頭都看得到。
        // 會踩到是因為這個多載的參數表是 (貼圖, 亮度, overlay, 外框, 剝落貼圖)，
        // 中間沒有「顏色」那一格；-1 在別的算繪 API 裡通常代表「不染色」，抄過來就中了。
        // 用 state.outlineColor：平常是 0（不畫），實體真的在發光時才是隊伍顏色
        collector.submitModel(model, state, pose, state.variant.texture(), state.lightCoords,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                state.outlineColor, null);

        // 大燈：整台再畫一次，但換成「只有燈罩不透明、其餘全透明」的貼圖，亮度寫死全亮。
        //
        // 為什麼是疊一層而不是把燈罩畫亮：實體算繪一次只吃一個亮度值，整台用全亮的話
        // 連輪胎和坐墊都會在夜裡發光。分兩次畫、第二次只有燈罩有像素，才只有燈亮。
        //
        // 這是**視覺上的**發光，不會真的照亮周圍方塊——原版沒有動態光源，那要另外
        // 塞光源方塊或靠 LambDynamicLights 之類的模組。
        if (state.headlight && LIT_TEXTURE.containsKey(state.variant)) {
            collector.submitModel(model, state, pose, LIT_TEXTURE.get(state.variant),
                    FULL_BRIGHT,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    state.outlineColor, null);
        }
        pose.popPose();

        super.submit(state, pose, collector, camera);
    }
}
