package com.xinbow99.taiwan.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xinbow99.taiwan.entity.VehicleModel;
import com.xinbow99.taiwan.Taiwan;
import com.xinbow99.taiwan.entity.RoadVehicle;
import net.minecraft.client.Minecraft;
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
            VehicleModel.LANBAO, Taiwan.id("models/entity/lanbao.json"));

    /** 全亮的 lightmap 座標（區塊光 15、天光 15）。這個版本沒有具名常數，只能寫值。 */
    private static final int FULL_BRIGHT = 0xF000F0;

    // ---- 網格車的動作時鐘 ---------------------------------------------------
    //
    // 這幾個數字對得上 models/new_lambo.bbmodel 的時間軸。動畫的長度改了，這裡也要改，
    // 不然「開到底」的那一格會對不上——這是刻意寫成常數而不是硬塞在算式裡的理由。

    /** 開關門那段動作幾秒。 */
    private static final float DOOR_SECONDS = 1.8f;
    /** 有人上下車之後門維持開著幾秒。 */
    private static final float DOOR_HOLD = 1.5f;
    /** {@code drive} 一輪是車輪整整一圈，幾秒。 */
    private static final float WHEEL_TURN_SECONDS = 1.2f;
    /** {@code steer_left}／{@code steer_right} 打到底那一格的時間。 */
    private static final float STEER_SECONDS = 0.8f;
    /** 那兩段轉向動作打到底是幾度。實體的鎖角比這個大，所以取值要夾住。 */
    private static final float STEER_FULL_DEGREES = 24.0f;
    /** 展示模式的觀眾距離（格）。走近一台停著的超跑，它才表演。 */
    private static final double SHOWCASE_RANGE = 6.0;
    /** 時速超過多少就不算停著了（km/h）。展示模式與怠速抖動都看它。 */
    private static final float STANDSTILL_KMH = 1.0f;

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
        // 起步抬頭要的縱向加速度。實體那邊沒有現成的欄位可抄（見 VehicleRenderState.accel），
        // 所以在這裡對時速做差分。extractRenderState 是每一幀跑、不是每一 tick，差分出來的
        // 量級會跟著畫面更新率跑，所以夾住再低通——這個值只用來驅動一個 3 度的姿態，
        // 準不準沒關係，不會抖才重要。
        //
        // 算繪狀態是**每個實體留一份**、跨幀重用的，所以 prevSpeedKmh 存得住。萬一哪天不是，
        // 差分永遠讀到 NaN 的那一支，accel 就一直是 0：車不會抬頭，但也不會亂跳。
        state.speedKmh = entity.speedKmh();
        float prevKmh = state.prevSpeedKmh;
        state.prevSpeedKmh = state.speedKmh;
        if (!Float.isNaN(prevKmh)) {
            float raw = Mth.clamp((state.speedKmh - prevKmh) * 0.6f, -1.0f, 1.0f);
            state.accel += (raw - state.accel) * 0.25f;
        }
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
        tickMeshAnim(entity, state, partialTick);
    }

    /**
     * 網格車的動作狀態機。
     *
     * <p>這些是**算繪端自己推的**，不是實體同步過來的：門開幾成、要不要表演，
     * 這幾件事純粹是畫面，看錯一幀沒有任何後果，為它們各開一個同步欄位不划算。
     *
     * <p>時間差要自己算——{@code extractRenderState} 是每一幀跑（不是每 tick），
     * 用固定步長的話開門速度會跟著畫面更新率跑。算繪狀態是每個實體留一份、跨幀重用的，
     * 所以 {@code prevClock} 存得住；萬一哪天不是，差分永遠讀到 NaN 的那一支 dt 就一直是 0：
     * 門不會動，但也不會亂跳。
     */
    private static void tickMeshAnim(RoadVehicle entity, VehicleRenderState state, float partialTick) {
        float clock = (entity.tickCount + partialTick) / 20.0f;
        float dt = Float.isNaN(state.prevClock) ? 0.0f : Math.max(clock - state.prevClock, 0.0f);
        state.prevClock = clock;
        state.clock = clock;

        // 有人上下車就重新計時。用「有沒有乘客」的**變化**而不是當下的值：
        // 門是為了上下車那一下開的，不是沒人就一直開著
        if (state.parked != state.prevParked) {
            state.prevParked = state.parked;
            state.doorHold = DOOR_HOLD;
        }
        state.doorHold = Math.max(state.doorHold - dt, 0.0f);

        float target = state.doorHold > 0.0f ? 1.0f : 0.0f;
        float step = dt / DOOR_SECONDS;
        if (state.doorOpen < target) {
            state.doorOpen = Math.min(target, state.doorOpen + step);
            state.doorOpening = true;
        } else if (state.doorOpen > target) {
            state.doorOpen = Math.max(target, state.doorOpen - step);
            state.doorOpening = false;
        }

        // 展示：停著、沒人、而且旁邊有人在看。看的人是**本機玩家**——展示是演給你看的，
        // 沒必要為了另一個玩家站在旁邊而在你的畫面上表演
        var viewer = Minecraft.getInstance().player;
        state.showcase = state.parked
                && state.speedKmh < STANDSTILL_KMH
                && viewer != null
                && viewer.distanceToSqr(entity) < SHOWCASE_RANGE * SHOWCASE_RANGE;
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
        MeshGeometry.get(MESH.get(state.variant)).ifPresent(geometry -> {
            MeshGeometry.Pose posed = geometry.newPose();
            pose(geometry, posed, state);
            collector.submitCustomGeometry(pose,
                    RenderTypes.entityCutout(state.variant.texture()),
                    (p, buffer) -> geometry.render(posed, p, buffer,
                            state.lightCoords, OverlayTexture.NO_OVERLAY, -1));
        });
    }

    /**
     * 決定這一幀播哪幾段動作、播到哪裡。
     *
     * <p>每一段的**時間都不是自己在跑的**，而是從車的狀態推回去的：車輪滾到哪一格
     * 由走過的距離決定、方向盤打到哪一格由龍頭角度決定、門開到哪一格由開門進度決定。
     * 這樣動畫跟物理不可能對不上——用自走的時鐘再去追物理，遲早會在加減速的時候脫節。
     *
     * <p>只有怠速與展示是真的吃時間的，因為它們本來就是「閒著沒事做的樣子」。
     */
    private static void pose(MeshGeometry geometry, MeshGeometry.Pose posed, VehicleRenderState state) {
        // 展示模式獨佔門與方向盤：兩邊同時播會互相打架，而且停著的車本來也沒人在開門
        if (state.showcase) {
            geometry.apply(posed, "showcase", state.clock, 1.0f);
            geometry.apply(posed, "idle", state.clock, 1.0f);
            return;
        }

        // 怠速抖動隨速度淡出：停紅燈時整台在抖，跑起來就該是路面在震而不是引擎
        float idle = 1.0f - Mth.clamp(state.speedKmh / 20.0f, 0.0f, 1.0f);
        geometry.apply(posed, "idle", state.clock, idle);

        // 車輪一圈剛好是 drive 的一輪，所以拿滾動角當時鐘，輪子就一定跟路面同步
        geometry.apply(posed, "drive",
                state.wheelSpin / Mth.TWO_PI * WHEEL_TURN_SECONDS, 1.0f);

        // 實體的鎖角（34 度）比動作打到底的 24 度大，所以要夾住——不夾的話取樣會超出
        // 最後一格，而最後一格之後是維持不動，看起來像方向盤打到一半卡住
        float steer = Mth.clamp(Math.abs(state.steer) / STEER_FULL_DEGREES, 0.0f, 1.0f) * STEER_SECONDS;
        geometry.apply(posed, state.steer >= 0.0f ? "steer_right" : "steer_left", steer, 1.0f);

        if (state.doorOpen > 0.0f) {
            // 開與關是同一條曲線的正反兩面，分成兩段是為了以後想讓關門比開門慢時
            // 有地方可以改——現在兩段對稱，看起來會完全一樣
            geometry.apply(posed,
                    state.doorOpening ? "doors_open" : "doors_close",
                    (state.doorOpening ? state.doorOpen : 1.0f - state.doorOpen) * DOOR_SECONDS,
                    1.0f);
        }
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
