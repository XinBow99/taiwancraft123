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
import net.minecraft.client.renderer.state.level.CameraRenderState;
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

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Taiwan.id("scooter"), "main");
    public static final ModelLayerLocation LANBAO_LAYER =
            new ModelLayerLocation(Taiwan.id("lanbao"), "main");
    public static final ModelLayerLocation MASHALA_LAYER =
            new ModelLayerLocation(Taiwan.id("mashala"), "main");
    public static final ModelLayerLocation CYGNUS_LAYER =
            new ModelLayerLocation(Taiwan.id("cygnus"), "main");

    /**
     * 兩款車的模型都在這裡烘好，不是每次要畫才建。
     *
     * <p>{@code bakeLayer} 會把整棵零件樹展開成頂點資料，那是建構期的工作；放進
     * {@code submit()} 的話每一幀、每一台車都要重來一次。兩個模型的記憶體成本是常數，
     * 用一個 {@code EnumMap} 換掉 if-else 是為了以後加第三款車時不用再動這個方法。
     */
    private final Map<VehicleModel, EntityModel<VehicleRenderState>> models;

    public VehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.models = new EnumMap<>(VehicleModel.class);
        this.models.put(VehicleModel.CLASSIC, new ScooterModel(context.bakeLayer(LAYER)));
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
        pose.popPose();

        super.submit(state, pose, collector, camera);
    }
}
