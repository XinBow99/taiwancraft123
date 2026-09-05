package com.xinbow99.taiwan.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/**
 * 速克達。
 *
 * <h2>它跟船最大的差別是「停下來就真的停住」</h2>
 * <p>原版的船有滑行慣性，因為那是水上載具。機車停好之後不該慢慢飄走——台灣的機車是
 * 停在騎樓邊、推得動但不會自己滑的。所以這裡把無人時的摩擦力調得很重
 * （{@link #IDLE_FRICTION}），而不是沿用船那種 0.9 的慣性。
 *
 * <h2>落水熄火</h2>
 * <p>泡到水就熄火：油門失效、速度快速歸零，要牽上岸才能重新發動。這不只是懲罰，
 * 它讓「河」在地圖上真的變成障礙——否則機車可以直接騎過去，整個地形設計就白做了。
 *
 * <h2>上鎖</h2>
 * <p>第一個騎上去的人成為車主，之後只有車主騎得動。**沒有鑰匙物品**：鑰匙會變成一個
 * 要管理的欄位、會掉、會被偷，而這裡要的只是「別人不能騎走我的車」。
 */
public class Scooter extends VehicleEntity {

    /**
     * 格/tick 換算成 km/h 的係數。
     *
     * <p>Minecraft 的一格是一公尺，一秒是 20 tick，所以 1 格/tick ＝ 20 m/s ＝ 72 km/h。
     * 內部的物理一律用格/tick（那是 {@code move()} 吃的單位），只有儀表板用 km/h。
     */
    public static final float KMH_PER_BLOCK_TICK = 72f;
    /** 最高速（km/h）。儀表板的滿刻度就是這個數字。 */
    public static final float MAX_SPEED_KMH = 120f;
    /** 最高速（格/tick）。120 km/h ≒ 1.67 格/tick，約是玩家衝刺的 6 倍。 */
    public static final float MAX_SPEED = MAX_SPEED_KMH / KMH_PER_BLOCK_TICK;
    /** 倒車最高速（≒ 35 km/h）。倒車要慢，不然玩家會用倒車當第二個前進檔。 */
    private static final float MAX_REVERSE = 0.48f;
    /**
     * 0 加速到 100 km/h 要幾秒。**調加速就調這個數字**，不要直接改 {@link #THROTTLE}。
     *
     * <p>油門那一段沒有空氣阻力，加速度是定值，所以這兩個是可以互相換算的；用「幾秒破百」
     * 當旋鈕是因為那是你講得出來、也感覺得到的量，而 0.0139 格/tick² 不是。
     */
    private static final float ZERO_TO_HUNDRED_SEC = 5f;
    /**
     * 油門（格/tick²）。由 {@link #ZERO_TO_HUNDRED_SEC} 推出來：
     * {@code (100 km/h 換成格/tick) ÷ (秒數 × 20 tick)}。
     *
     * <p>0.0139 ＝ 破百 100 tick、全速（120）150 tick（7.5 秒）。
     */
    private static final float THROTTLE =
            (100f / KMH_PER_BLOCK_TICK) / (ZERO_TO_HUNDRED_SEC * 20f);
    /** 煞車。比油門強，煞得住才敢騎快——全速煞停約 19 tick（不到一秒）。 */
    private static final float BRAKE = 0.09f;
    /** 沒有人騎的時候的摩擦。夠重，所以停好就不會漂。 */
    private static final float IDLE_FRICTION = 0.72f;
    /** 有人騎但沒給油的滑行摩擦。 */
    private static final float COAST_FRICTION = 0.955f;
    /**
     * 慢速時龍頭能打到幾度。
     *
     * <p>45 度 → 最小迴轉半徑 1.5 格，停車場裡原地繞圈那種。調小（例如 30 度）半徑會拉到
     * 2.6 格，牽車入位會開始需要來回喬；調大到 60 度以上半徑剩 0.9 格，車會像在轉陀螺。
     */
    private static final float STEER_LOCK_SLOW = 45f;
    /**
     * 全速時龍頭只能打到幾度。
     *
     * <p>不是為了限制玩家，是真的騎車就這樣：速度越快龍頭動得越少，高速全打死等於摔車。
     *
     * <p>不過在 120 km/h 這個新的最高速下，決定彎有多大的其實不是這個角度而是輪胎的
     * 抓地力上限：8 度算出來的幾何半徑是 10.7 格，但那需要 0.26 格/tick² 的向心加速度，
     * 超過 {@link #GRIP_FRONT} + {@link #GRIP_REAR} 的 0.14，所以輪胎會先飽和、車往外推，
     * 實際半徑落在 20 格。也就是說全速過彎現在是**轉向不足**在決定的——這是對的，
     * 真的騎快車就是這樣，要過彎得先減速。
     */
    private static final float STEER_LOCK_FAST = 8f;
    /** 把手轉動的跟隨速度。0.25 ≒ 三格內轉到位；放開方向鍵時目標是 0，所以同一條式子也負責回正。 */
    private static final float STEER_LERP = 0.25f;
    /** 過彎時車身最多傾幾度。純視覺。 */
    private static final float MAX_LEAN = 22f;
    /** 撞牆超過這個速度就損壞。 */
    private static final float CRASH_SPEED = 1.0f;

    // ---- 輪胎 ---------------------------------------------------------------
    //
    // 這一組取代了上一版的「側向慣性留幾成」。差別是根本性的：那個版本的車身朝向是龍頭
    // 角度的直接函數（Δyaw = v/L × tan δ），車只是照著幾何公式畫圓，輪胎不存在、轉動慣量
    // 不存在，所以一打方向就是等速自轉，怎麼調都調不掉。
    //
    // 引擎（PhysX / Bullet 的 raycast vehicle、Unity WheelCollider、Unreal ChaosVehicles）
    // 的作法是反過來的：先算每個輪子「指的方向」與「實際在走的方向」差幾度（側滑角），
    // 由它算出輪胎的側向力，再由力矩去積分角速度。yaw 於是有了慣性、也有了阻尼——後輪
    // 一旦被甩開就產生反向的側滑角，自己把車拉回來。

    /** 重心到前軸的距離（格）。 */
    private static final double DIST_FRONT = 0.75;
    /** 重心到後軸的距離（格）。 */
    private static final double DIST_REAR = 0.75;
    /**
     * 側滑剛度：每 1 弧度的側滑角產生多少側向加速度（格/tick²）。
     *
     * <p>後輪比前輪硬是刻意的，這是所有量產車的設定：後輪抓得比前輪牢 → 轉向不足
     * （understeer）→ 車子是穩定的，推過頭只會往外滑出去，不會原地打轉。反過來
     * （前硬後軟）叫轉向過度，是甩尾車的設定，也是「車身自己轉圈圈」的處方。
     */
    private static final double CORNER_STIFF_FRONT = 0.43;
    /** 同上，後輪。必須大於前輪，見 {@link #CORNER_STIFF_FRONT}。 */
    private static final double CORNER_STIFF_REAR = 0.50;
    /**
     * 前輪抓地力上限（格/tick²）。輪胎能產生的側向力有天花板，超過就是打滑。
     *
     * <p><b>抓地力必須跟著最高速的平方走。</b>過彎需要的向心加速度是 {@code v² ÷ 半徑}，
     * 所以最高速從 30 km/h 拉到 120 km/h（4 倍）之後，同一個彎需要的力是 16 倍。沿用舊值
     * 的話全速過彎半徑會變成 100 格以上——在巷子裡等於不能轉。
     *
     * <p>0.065 + 0.075 ＝ 0.14 格/tick²，全速（1.67 格/tick）的最小過彎半徑
     * {@code v² ÷ a} ≒ 20 格；60 km/h 時縮到 5 格。換算成 G 值是超現實的，但 120 km/h
     * 要在 Minecraft 的街道裡轉得過來，就只能這樣——真實的機車在 120 km/h 的最小半徑
     * 是 120 公尺，那是一整個區塊。
     */
    private static final double GRIP_FRONT = 0.065;
    /** 後輪抓地力上限。 */
    private static final double GRIP_REAR = 0.075;
    /** 手煞車時後輪剩下幾成抓地力。鎖死的輪子側向力幾乎歸零，車尾於是滑出去——甩尾就是這樣來的。 */
    private static final double DRIFT_REAR_GRIP = 0.4;
    /**
     * 繞 Y 軸的轉動慣量除以質量（格²）。決定車身「轉起來有多重」。
     *
     * <p>調大 → 打方向後要等一下車頭才動，像卡車；調小 → 反應快到像沒有重量，
     * 而且輪胎力矩會把它推得太快，開始有轉過頭的傾向。
     */
    private static final double YAW_INERTIA = 1.0;
    /**
     * 一個 tick 切成幾個子步。
     *
     * <p>不是為了精度，是為了穩定：輪胎力在低速時非常「硬」（側滑角的分母是速度），
     * 用一整個 tick 去積分會發散成越震越大的抖動。引擎裡的 substep 就是為了這件事。
     */
    private static final int SUBSTEPS = 8;
    /** 算側滑角時速度的下限。速度趨近 0 時分母不能是 0，否則角度會亂跳。 */
    private static final double MIN_SLIP_SPEED = 0.2;
    /** 幾乎停住時，每個子步吃掉多少角速度與側向速度。輪胎不轉了，車身也不該繼續自轉。 */
    private static final double REST_DAMP = 0.25;
    /** 低於這個速度就當作停住（格/tick，≒ 0.9 km/h）。使用時機與兩個陷阱見 {@link #settle()}。 */
    private static final double STOP_EPSILON = 0.012;
    /** 角速度上限（弧度/tick）。物理上的保險絲，正常騎乘碰不到。 */
    private static final double MAX_YAW_RATE = 0.25;
    /** 壓車角度換算用的等效重力。傾角 = atan(側向加速度 ÷ 這個值)——機車就是靠傾角平衡離心力的。 */
    private static final double LEAN_G = 0.25;

    /** 甩尾的最低速度。太慢就甩不動——低速原地轉圈不是甩尾，是鬼打牆。 */
    private static final double DRIFT_MIN_SPEED = 0.7;
    /** 甩尾要撐滿幾 tick 才有加速。約 0.9 秒：夠久到是個決定，不會不小心按到。 */
    private static final int DRIFT_CHARGE = 18;
    /** 加速持續幾 tick。 */
    private static final int BOOST_TICKS = 20;
    /** 加速期間可以超過最高速多少。 */
    private static final float BOOST_OVERSPEED = 1.28f;

    /** 車主只存在伺服器端：客戶端不需要知道，「這台車不是你的」是伺服器判斷後才送訊息的。 */
    private UUID owner;
    /** 熄火中。同步給客戶端是為了讓引擎聲與車頭燈跟著停。 */
    private static final EntityDataAccessor<Boolean> DATA_STALLED =
            SynchedEntityData.defineId(Scooter.class, EntityDataSerializers.BOOLEAN);
    /** 把手角度（度）。要同步，別人才看得到你的龍頭在轉。 */
    private static final EntityDataAccessor<Float> DATA_STEER =
            SynchedEntityData.defineId(Scooter.class, EntityDataSerializers.FLOAT);
    /** 車款。要同步：算繪端靠它挑模型與貼圖。 */
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(Scooter.class, EntityDataSerializers.INT);
    /** 壓車角度（度）。同上，純視覺。 */
    private static final EntityDataAccessor<Float> DATA_LEAN =
            SynchedEntityData.defineId(Scooter.class, EntityDataSerializers.FLOAT);

    // 狀態分三層，不要混在一起。

    // ---- 第一層：玩家輸入。跟速度無關，車停著也存在 ----

    /** 龍頭角度（度，正值往右）。車停著也打得動——只是車不會轉。 */
    private float steerAngle;
    /** 油門，-1（煞車／倒車）到 1（全開）。 */
    private float throttle;

    // ---- 第二層：車體狀態。全部是**積分出來的**，沒有一項是輸入的直接函數 ----

    /** 車身座標系的縱向速度（格/tick，沿著車頭）。 */
    private double speed;
    /**
     * 車身座標系的側向速度（格/tick，正值往右）。
     *
     * <p>這是上一版沒有的東西，也是「車跟著前輪走」看不看得出來的關鍵。有了它，車的
     * 行進方向才可以跟車頭方向不一樣——過彎推出去、車尾滑出來，都是這個量在動。
     */
    private double lateralSpeed;
    /**
     * 角速度（弧度/tick）。
     *
     * <p>上一版沒有這個欄位，yaw 是每 tick 由龍頭角度重算的——那等於宣告車身的轉動
     * 沒有慣性、也沒有阻尼，打多少方向就轉多快，放開就立刻停止轉。轉圈圈是它的必然結果。
     * 現在它是一個由輪胎力矩累積、也被輪胎力矩拉回來的狀態。
     */
    private double yawRate;
    /** 上一 tick 的速度，撞擊判定用。 */
    private double lastSpeed;
    /** 壓車角度（度）。純視覺，不影響任何物理。 */
    private float leanAngle;

    // ---- 第三層：世界狀態 ----

    /** 水平速度向量（世界座標）。由第二層的 speed／lateralSpeed 與車頭方向組出來。 */
    private Vec3 planar = Vec3.ZERO;

    /** AI 騎士的轉向輸入。玩家騎的時候不看它。 */
    private float aiSteer;
    /** AI 騎士的油門輸入。 */
    private float aiThrottle;

    /** 甩尾已經撐了幾 tick。放開時用它決定給不給加速。 */
    private int driftTicks;
    /** 加速還剩幾 tick。 */
    private int boostTicks;
    private boolean drifting;

    public Scooter(EntityType<? extends Scooter> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STALLED, false);
        builder.define(DATA_STEER, 0f);
        builder.define(DATA_LEAN, 0f);
        builder.define(DATA_VARIANT, ScooterVariant.CLASSIC.ordinal());
    }

    // ------------------------------------------------------------------ 車主

    public Optional<UUID> owner() {
        return Optional.ofNullable(this.owner);
    }

    public boolean mayRide(Player player) {
        return owner().map(id -> id.equals(player.getUUID())).orElse(true);
    }

    /**
     * 龍頭角度（度）。算繪器用。
     *
     * <p>自己騎的那台用本地算出來的值（沒有網路延遲，龍頭跟著手感動）；別人的車沒有輸入
     * 可以算，只能用同步過來的。
     */
    public float steerAngle() {
        return this.isLocalInstanceAuthoritative() ? this.steerAngle : this.entityData.get(DATA_STEER);
    }

    /** 壓車角度（度，負值往右倒）。算繪器用；來源同上。 */
    public float leanAngle() {
        return this.isLocalInstanceAuthoritative() ? this.leanAngle : this.entityData.get(DATA_LEAN);
    }

    /** 車款。外觀由它決定，物理目前完全共用——見 {@link ScooterVariant}。 */
    public ScooterVariant variant() {
        return ScooterVariant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(ScooterVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.ordinal());
        refreshDimensions();
    }

    public boolean stalled() {
        return this.entityData.get(DATA_STALLED);
    }

    /**
     * 儀表板上的時速（km/h）。
     *
     * <p>用**實際的水平位移**而不是內部的 {@link #speed}，理由有兩個：一是騎士自己的
     * 客戶端才有那個欄位（車是客戶端在模擬的），別人的車讀到的會是 0；二是撞牆、卡在
     * 坡上這種「油門在給但車沒動」的情況，指針應該掉下來——儀表板要說的是車真的跑多快，
     * 不是引擎以為它跑多快。
     */
    public float speedKmh() {
        return (float) this.getDeltaMovement().horizontalDistance() * KMH_PER_BLOCK_TICK;
    }

    // ------------------------------------------------------------------ 互動

    @Override
    public InteractionResult interact(Player player, net.minecraft.world.InteractionHand hand,
                                      net.minecraft.world.phys.Vec3 hit) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

        if (!mayRide(player)) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("這台車不是你的"));
            return InteractionResult.FAIL;
        }
        // 第一個騎上去的人就是車主。沒有鑰匙物品——鑰匙會掉、會被偷，
        // 而這裡要的只是「別人不能騎走我的車」
        if (owner().isEmpty()) {
            this.owner = player.getUUID();
        }
        return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    // ------------------------------------------------------------------ 乘客

    /** 載兩個人：騎士 ＋ 後座。 */
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 2;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        // 玩家或 8+9 都騎得動。回傳 null 的話這台車在物理上等於沒人騎
        return this.getFirstPassenger() instanceof Player player ? player
                : this.getFirstPassenger() instanceof EightNine guy ? guy : null;
    }

    /**
     * AI 騎士的輸入（轉向 −1~1 往右為正、油門 −1~1）。
     *
     * <p>玩家的輸入是從 {@code xxa}/{@code zza} 讀的——那是「按鍵」的欄位，
     * 生物身上沒有意義的值。所以 AI 走另一條路：由 goal 直接寫這兩個欄位。
     *
     * <p>**轉向的語意跟玩家那條一致**：都是「龍頭要打幾成」，而不是「車身要轉多少」。
     * 走另一條語意的話，AI 騎的車跟玩家騎的車物理會不一樣，那就等於兩台車。
     */
    public void setAiInput(float steer, float throttle) {
        this.aiSteer = Mth.clamp(steer, -1f, 1f);
        this.aiThrottle = Mth.clamp(throttle, -1f, 1f);
    }

    /**
     * 座位。前面那個是騎士，後面那個是後座。
     *
     * <h3>座位是**車款**的資料，不是寫死的一組數字</h3>
     * <p>兩台車的坐墊高度與前後位置本來就不一樣（勁戰放大 1.3 倍之後差了三分之一格），
     * 共用一組的話，人不是浮在坐墊上方就是半個屁股陷進車裡。
     *
     * <p>之前的 z 幾乎是 0（車身中心），但坐墊在模型的 +Z 側——所以騎士其實是坐在
     * 踏板前緣、不是坐墊上。數字現在從 {@link ScooterVariant#seat} 來，跟模型對齊。
     *
     * <p>坐姿本身不用管：{@code HumanoidRenderState.isPassenger} 只要是乘客就會是坐姿，
     * 原版自己處理。這裡只負責「坐在哪」。
     */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dim, float scale) {
        boolean pillion = !this.getPassengers().isEmpty() && this.getPassengers().get(0) != passenger;
        return variant().seat(pillion);
    }

    /**
     * 碰撞箱跟著車款走。
     *
     * <p>勁戰比通用款大一圈，用同一個碰撞箱的話不是它卡在門口、就是通用款騎起來像張床。
     */
    @Override
    public EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        return variant().dimensions();
    }

    /**
     * 車款一從網路同步進來就要重算碰撞箱。
     *
     * <p>{@link #getDimensions} 的結果被快取在實體上，不呼叫 {@code refreshDimensions()}
     * 的話，客戶端會一直用「生出來當下」那個車款的大小——放好的勁戰在客戶端會是
     * 通用款的碰撞箱，玩家會覺得車體跟碰撞對不上。
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_VARIANT.equals(key)) refreshDimensions();
    }

    /**
     * 上車就把視角轉到車頭方向。
     *
     * <p>沒有這一下，「前面」是你上車那一刻剛好在看的方向：從側邊上車，按 W 車往前衝，
     * 畫面上看起來卻是往右跑；從後面上車就變成「按 W 倒退」。左右也跟著錯亂——
     * 方向鍵轉的是車，而車跟你的畫面沒有對齊。
     *
     * <p>{@code addPassenger} 兩邊都會跑到（客戶端是收到乘客封包時），所以本地玩家的
     * 鏡頭真的會轉過去——鏡頭是客戶端說了算的，只在伺服器上轉沒有用。
     */
    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        faceForward(passenger);
    }

    private void faceForward(Entity passenger) {
        float yaw = this.getYRot();
        passenger.setYRot(yaw);
        passenger.yRotO = yaw;
        passenger.setYHeadRot(yaw);
        if (passenger instanceof LivingEntity living) {
            living.yHeadRot = yaw;
            living.yHeadRotO = yaw;
            living.yBodyRot = yaw;
            living.yBodyRotO = yaw;
        }
    }

    /**
     * 鏡頭完全不鎖。
     *
     * <p>這裡唯一做的事是把騎士的**身體**轉向車頭——人是跨坐在車上的，身體不可能面向
     * 側面。視角（{@code yRot}）一個字都不碰：不夾角度、不跟著車轉、也不自動回正。
     * 滑鼠是你的，車是車的。
     *
     * <p>代價你要知道：車轉了 180 度而你沒動滑鼠的話，油門仍然是往車頭的方向給，
     * 畫面上看起來就是往後跑。這是解鎖鏡頭必然的副作用，不是 bug。
     */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        super.positionRider(passenger, move);
        passenger.setYBodyRot(this.getYRot());
    }

    // ------------------------------------------------------------------ 物理

    @Override
    public void tick() {
        this.lastSpeed = this.speed;
        super.tick();

        boolean wet = this.isInWater() || this.isUnderWater();
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_STALLED, wet);
        }

        tickPhysics(wet);

        if (!this.level().isClientSide()) {
            this.applyEffectsFromBlocks();
        }
        particles(wet);
    }

    /**
     * 一個 tick 的物理。
     *
     * <h3>車身朝向不是算出來的，是積出來的</h3>
     * <p>上一版寫的是 {@code Δyaw = (速度 ÷ 軸距) × tan(龍頭角度)}——那是**運動學**的
     * 自行車模型：它假設輪胎永遠不滑，於是車身朝向變成龍頭角度的直接函數。這種模型只有
     * 一種行為：龍頭固定不放，角速度就是常數，車就永遠繞同一個圓。車身「自己在轉」，跟
     * 它實際往哪走沒有關係——這就是不物理的地方，也是轉圈圈調不掉的原因。
     *
     * <p>現在改成**動力學**模型，跟引擎裡的載具是同一套：
     * <ol>
     *   <li>算每個輪子的側滑角＝「輪子指的方向」與「它實際在走的方向」差幾度；
     *   <li>側滑角乘上側滑剛度得到輪胎的側向力，超過抓地力上限就打滑（飽和）；
     *   <li>兩個輪子的力對重心取力矩，除以轉動慣量得到**角加速度**，積分成角速度；
     *   <li>力本身則積分成車身的縱向與側向速度。
     * </ol>
     *
     * <p>差別在於這條迴路是有負回授的：車身一旦轉得比行進方向快，後輪的側滑角就變大、
     * 產生反向力矩把車拉回來。轉圈圈不需要靠參數壓下去，它是被物理本身擋掉的。
     *
     * <h3>順序是規格的一部分</h3>
     * <p>子步積分（第二步）一定要在算世界速度向量（第三步）之前跑完，因為速度向量要用
     * **更新後**的車頭方向去組。反過來的話車會先用上一格的方向走一格再把車頭轉過來，
     * 過彎時會抖。
     */
    private void tickPhysics(boolean wet) {
        LivingEntity pilot = wet ? null : this.getControllingPassenger();
        Player rider = pilot instanceof Player p ? p : null;

        // ---- 1. 輸入：油門與龍頭 -------------------------------------------------
        //
        // 玩家讀按鍵欄位（xxa/zza），AI 讀 setAiInput 寫進來的值。兩條路在這裡就合流，
        // 下面的物理完全不知道騎士是誰——AI 騎的車跟玩家騎的車是同一套物理
        float steerInput;
        if (rider != null) {
            steerInput = Mth.clamp(-rider.xxa, -1f, 1f);
            this.throttle = Mth.clamp(rider.zza, -1f, 1f);
        } else if (pilot != null) {
            steerInput = this.aiSteer;
            this.throttle = this.aiThrottle;
        } else {
            steerInput = 0f;
            this.throttle = 0f;
        }
        updateDrift(rider, steerInput);
        if (this.boostTicks > 0) this.boostTicks--;

        // 速度越快，龍頭能打的角度越小。真的騎車就是這樣，高速全打死等於摔車
        float pace = (float) Math.min(Math.abs(this.speed) / MAX_SPEED, 1.0);
        float lock = Mth.lerp(pace, STEER_LOCK_SLOW, STEER_LOCK_FAST);
        this.steerAngle = Mth.lerp(STEER_LERP, this.steerAngle, steerInput * lock);
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_STEER, this.steerAngle);
        }

        // ---- 2. 動力學：輪胎力 → 加速度與角加速度 → 積分 -------------------------
        boolean ridden = pilot != null;
        for (int i = 0; i < SUBSTEPS; i++) {
            integrate(1.0 / SUBSTEPS, ridden, wet);
        }
        settle();
        this.setYHeadRot(this.getYRot());

        // ---- 3. 車身速度換算成世界速度 -------------------------------------------
        Vec3 forward = new Vec3(-Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), 0.0,
                Mth.cos(this.getYRot() * Mth.DEG_TO_RAD));
        Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
        this.planar = forward.scale(this.speed).add(side.scale(this.lateralSpeed));

        // ---- 4. 重力與位移 -------------------------------------------------------
        Vec3 was = this.position();
        double vy = this.onGround()
                ? Math.max(this.getDeltaMovement().y, -0.08)
                : this.getDeltaMovement().y - 0.08;
        this.setDeltaMovement(this.planar.x, vy, this.planar.z);
        this.move(MoverType.SELF, this.getDeltaMovement());

        // ---- 5. 依碰撞結果回寫速度 -----------------------------------------------
        applyCollision(was);

        // ---- 6. 壓車角度（純視覺）-----------------------------------------------
        //
        // 這也改成從物理量算：機車是靠傾角去平衡離心力的，傾角 = atan(側向加速度 ÷ g)。
        // 而側向加速度 = 速度 × 角速度——都是上面積分出來的真東西，不是龍頭角度的插值。
        // 所以打滑的時候（角速度跟不上龍頭）車不會傻傻地繼續壓，看起來才對
        double latAccel = this.speed * this.yawRate;
        float targetLean = (float) -Mth.clamp(
                Math.toDegrees(Math.atan(latAccel / LEAN_G)), -MAX_LEAN, MAX_LEAN);
        this.leanAngle = Mth.lerp(0.25f, this.leanAngle, targetLean);
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_LEAN, this.leanAngle);
        }
    }

    /**
     * 一個子步的車輛動力學。座標系是車身座標：x 沿車頭、y 往右、yaw 往右為正。
     *
     * <p>推導（{@code ψ̇} 是角速度）：車身座標系本身在轉，所以微分要補上科氏項——
     * {@code a = (v̇x − vy·ψ̇)·前 + (v̇y + vx·ψ̇)·右}。移項就是底下的 {@code ax}／{@code ay}。
     * 少了這兩項，車過彎時會憑空生出或吃掉速度。
     */
    private void integrate(double dt, boolean ridden, boolean wet) {
        double delta = Math.toRadians(this.steerAngle);
        double cos = Math.cos(delta);

        // 側滑角：輪子指的方向 vs 輪子實際在走的方向。
        //
        // 前輪在重心前方 DIST_FRONT，所以它的側向速度要加上車身自轉帶來的 ψ̇·a；
        // 後輪在後方，所以是減。這一組正負號就是前後輪行為不同的全部來源——
        // 也是後輪之所以會「把車拉回來」的原因
        double ref = Math.max(Math.abs(this.speed), MIN_SLIP_SPEED);
        double dir = this.speed < 0.0 ? -1.0 : 1.0;
        double slipFront = Math.atan2(this.lateralSpeed + this.yawRate * DIST_FRONT, ref) - delta * dir;
        double slipRear = Math.atan2(this.lateralSpeed - this.yawRate * DIST_REAR, ref);

        // 輪胎：側向力與側滑角成正比，直到抓地力上限為止（飽和＝打滑）。
        // 手煞車把後輪鎖死，它的上限崩掉，車尾就滑出去了——甩尾不再需要特例的加成倍率
        double gripRear = this.drifting ? GRIP_REAR * DRIFT_REAR_GRIP : GRIP_REAR;
        double forceFront = tyre(slipFront, CORNER_STIFF_FRONT, GRIP_FRONT);
        double forceRear = tyre(slipRear, CORNER_STIFF_REAR, gripRear);
        // 低速淡出。
        //
        // 上面那個 MIN_SLIP_SPEED 的下限是為了不讓分母變成 0，但它有副作用：速度真的是 0
        // 的時候，它會把靜止偽裝成「以 0.05 前進」，於是前輪憑空生出側向力——停在原地打
        // 方向鍵，車會自己轉起來還會平移。實測過，60 tick 轉了 103 度。
        //
        // 物理上速度 0 就沒有相對滑動，也就沒有輪胎力。這一項把力隨速度收掉，順便補上
        // 停住時的轉動阻尼——不然力歸零之後，殘留的角速度會沒有東西去吃掉它
        double fade = Mth.clamp(Math.abs(this.speed) / MIN_SLIP_SPEED, 0.0, 1.0);
        forceFront *= fade;
        forceRear *= fade;

        // 騰空的輪子沒有接觸點，就沒有側向力。這是飛坡時車身會保持姿態的原因
        if (!this.onGround()) {
            forceFront = 0.0;
            forceRear = 0.0;
        }

        double drive = longitudinalAccel(ridden, wet);

        double ax = drive + this.yawRate * this.lateralSpeed;
        double ay = forceFront * cos + forceRear - this.yawRate * this.speed;
        double yawAccel = (DIST_FRONT * forceFront * cos - DIST_REAR * forceRear) / YAW_INERTIA;

        this.speed += ax * dt;
        this.lateralSpeed += ay * dt;
        this.yawRate = Mth.clamp(this.yawRate + yawAccel * dt, -MAX_YAW_RATE, MAX_YAW_RATE);

        double rest = 1.0 - (1.0 - fade) * REST_DAMP;
        this.yawRate *= rest;
        this.lateralSpeed *= rest;

        this.setYRot(this.getYRot() + (float) Math.toDegrees(this.yawRate * dt));
    }

    /**
     * 收掉停止前的殘量，不然車會用 0.0001 格/tick 永遠緩緩地漂。
     *
     * <p>這個判斷踩過兩次同一個坑，所以現在有兩道防線：
     *
     * <p><b>一、一個 tick 只做一次，不放進子步裡。</b>油門分給 {@value #SUBSTEPS} 個子步之後
     * 每一步只加八分之一，會小於死區——放在子步裡的話，剛加上去的速度會在同一步內被歸零，
     * 車永遠出不了起步。（真的發生過：死區從 0.003 調成 0.012 之後車就不動了。）
     *
     * <p><b>二、油門在給的時候完全不作用。</b>死區的用途是收掉滑行的殘量，不是跟油門搶。
     * 加上這道之後，就算之後把「幾秒破百」調到很慢、一個 tick 的增量掉到死區以下，
     * 車還是動得了——這個坑就從「要記得檢查」變成「不可能發生」。
     *
     * <p>煞車鍵不在豁免範圍內是故意的：{@code throttle < 0} 時速度本來就該滑過 0 進入倒車，
     * 那是「按著 S 會倒車」這個設計要的行為。
     */
    private void settle() {
        if (Math.abs(this.throttle) < 0.01f && Math.abs(this.speed) < STOP_EPSILON) {
            this.speed = 0.0;
        }
        if (Math.abs(this.lateralSpeed) < STOP_EPSILON) this.lateralSpeed = 0.0;
        if (this.speed == 0.0 && Math.abs(this.yawRate) < 0.004) this.yawRate = 0.0;
    }

    /**
     * 輪胎的側向力（其實是加速度，因為質量歸一了）。
     *
     * <p>線性段 ＋ 飽和。真的輪胎曲線在飽和後會回落（Pacejka 的 magic formula），
     * 但那一段就是「已經滑了還救得回來」的手感，對這台車來說是負擔不是價值。
     */
    private static double tyre(double slip, double stiffness, double grip) {
        return Mth.clamp(-stiffness * slip, -grip, grip);
    }

    /**
     * 縱向的加速度（格/tick²）：油門、煞車、倒車、滑行阻力。
     *
     * <p>回傳的是加速度而不是直接改速度，因為它每個子步都會被呼叫一次——摩擦這種
     * 「乘上一個係數」的寫法在子步裡會被開 8 次方，所以一律換算成加速度：
     * {@code 速度 × (係數 − 1)}。
     *
     * <p>這裡完全不碰方向。方向是輪胎的事。
     */
    private double longitudinalAccel(boolean ridden, boolean wet) {
        if (!ridden) {
            // 沒人騎（或熄火）：很快停住。船那種滑行慣性放在機車上會變成停好之後自己飄走
            return this.speed * ((wet ? 0.5 : IDLE_FRICTION) - 1.0);
        }

        double cap = this.boostTicks > 0 ? MAX_SPEED * BOOST_OVERSPEED : MAX_SPEED;
        if (this.throttle > 0.01f) {
            // 到頂之後不要硬切速度，讓它自己收回來——硬切會在最高速附近抖
            if (this.speed >= cap) return (cap - this.speed) * 0.03;
            return THROTTLE * (this.boostTicks > 0 ? 1.8 : 1.0);
        }
        if (this.throttle < -0.01f) {
            // 同一個鍵：還在前進就是煞車，停住之後才變倒車。
            // 甩尾中只給三成煞車：甩尾要保住速度，不然過彎永遠比直直騎慢
            if (this.speed > 0.01) return this.drifting ? -BRAKE * 0.3 : -BRAKE;
            return this.speed > -MAX_REVERSE ? -THROTTLE * 0.6 : 0.0;
        }
        return this.speed * (COAST_FRICTION - 1.0);
    }

    /**
     * 撞到東西：位移被擋下來，速度也要跟著掉。
     *
     * <p>比對「想走多遠」與「實際走了多遠」，而不是看 {@code horizontalCollision} 旗標。
     * 貼著牆慢慢騎也會一直是 collision，用旗標判定的話車會在牆邊被慢慢磨壞；而只擋位移
     * 不扣速度的話，車會「貼著牆全速蹭」，一離開牆面又瞬間彈出去。
     */
    private void applyCollision(Vec3 was) {
        double intended = Math.sqrt(this.planar.x * this.planar.x + this.planar.z * this.planar.z);
        if (intended < 1.0e-6) return;

        Vec3 moved = this.position().subtract(was);
        double actual = Math.sqrt(moved.x * moved.x + moved.z * moved.z);
        if (actual >= intended - 1.0e-4) return;

        double kept = actual / intended;
        this.speed *= kept;
        this.lateralSpeed *= kept;
        // 撞到東西也會把車身的轉動吃掉一部分——車卡在牆上還在原地自轉是最假的一種畫面
        this.yawRate *= kept;
        this.planar = this.planar.scale(kept);

        // 真的撞上去才算撞車：夠快，而且大部分的速度是這一格掉的
        if (Math.abs(this.lastSpeed) >= CRASH_SPEED && kept < 0.5 && !this.level().isClientSide()) {
            this.speed = 0.0;
            this.lateralSpeed = 0.0;
            this.yawRate = 0.0;
            this.setDamage(this.getDamage() + (float) Math.abs(this.lastSpeed) * 22f);
            this.setHurtTime(10);
            this.playSound(SoundEvents.ANVIL_LAND, 0.6f, 1.6f);
            if (this.getDamage() > 40f) {
                this.destroy(this.level().getServer().overworld(), TaiwanItems.SCOOTER);
            }
        }
    }

    /**
     * 甩尾（手煞車）：按著跳躍鍵轉彎。
     *
     * <p>手煞車把後輪鎖死，它的抓地力上限掉到 {@link #DRIFT_REAR_GRIP}——前輪照樣咬著地，
     * 後輪咬不住，力矩失衡，車尾就滑出去了。甩尾不再是「額外多轉幾度」的加成，
     * 而是同一組輪胎方程式在後輪失去抓地力時自己算出來的結果。
     *
     * <p>撐過 {@link #DRIFT_CHARGE} tick 再放開就有一段加速——過彎不是損失而是收益，
     * 這條規則就是跑跑卡丁車的彎道比直線好玩的原因。
     *
     * <p>用跳躍鍵是因為蹲下鍵在 Minecraft 裡是「下車」，而前後鍵是同一個軸：
     * 同時按 W 和 S 會相消成 0，「油門＋煞車」這種常見的甩尾組合在這裡讀不出來。
     */
    private void updateDrift(Player rider, float steerInput) {
        boolean want = rider != null && jumpHeld(rider) && Math.abs(steerInput) > 0.1f
                && this.speed > DRIFT_MIN_SPEED && this.onGround();
        if (want) {
            this.driftTicks++;
        } else if (this.driftTicks > 0) {
            if (this.driftTicks >= DRIFT_CHARGE) {
                this.boostTicks = BOOST_TICKS;
                this.speed = Math.min(this.speed + 0.24, MAX_SPEED * BOOST_OVERSPEED);
                if (this.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.3,
                            this.getZ(), 8, 0.2, 0.1, 0.2, 0.02);
                }
            }
            this.driftTicks = 0;
        }
        this.drifting = want;
    }

    /**
     * 騎士有沒有按著跳躍鍵。
     *
     * <p>兩邊拿的是同一份輸入，只是入口不同：伺服器端只有 {@code ServerPlayer} 收得到輸入
     * 封包；客戶端的 {@code LocalPlayer} 則在 {@code applyInput()} 裡把跳躍鍵寫進
     * {@code jumping}——跟 {@code xxa}／{@code zza} 同一個地方，所以兩邊算出來會一致。
     */
    private static boolean jumpHeld(Player rider) {
        return rider instanceof net.minecraft.server.level.ServerPlayer server
                ? server.getLastClientInput().jump()
                : rider.isJumping();
    }

    /** 落水的白煙與甩尾的燒胎煙。 */
    private void particles(boolean wet) {
        if (!(this.level() instanceof ServerLevel server)) return;
        if (wet && this.tickCount % 10 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6, this.getZ(),
                    3, 0.15, 0.1, 0.15, 0.01);
        }
        // 甩尾在畫面上要看得出來，不然玩家只會覺得「車怎麼在飄」
        if (this.drifting) {
            Vec3 back = new Vec3(Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), 0.0,
                    -Mth.cos(this.getYRot() * Mth.DEG_TO_RAD)).scale(0.55);
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX() + back.x, this.getY() + 0.12, this.getZ() + back.z,
                    2, 0.08, 0.02, 0.08, 0.005);
        }
    }

    /**
     * 上得了騎樓與路緣石。
     *
     * <p>台灣的路邊到處是 10～20 公分的高低差，在 Minecraft 裡那就是一整格。上不去的話，
     * 機車只能騎在馬路正中央，而「停到騎樓邊」是這台車存在的理由之一。
     */
    @Override
    public float maxUpStep() {
        return 1.0f;
    }

    /**
     * **沒有這個就騎不上去。**
     *
     * <p>{@code Entity.isPickable()} 預設是 false，而玩家右鍵時的射線只打得到 pickable 的
     * 實體——所以車看得到、撞得到，右鍵卻完全沒反應，而且不會有任何錯誤訊息。
     * 原版的船有覆寫它，我照抄的時候漏了這一個。
     */
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeCollidedWith(Entity by) {
        return true;
    }

    @Override
    public boolean isPushable() {
        // 推得動（走過去頂它），但因為 IDLE_FRICTION 很重，推完就停住不會漂
        return true;
    }

    @Override
    protected Item getDropItem() {
        // 掉回自己那一款，不是一律掉通用款——不然把勁戰打壞會換到一台別的車
        return variant() == ScooterVariant.CYGNUS ? TaiwanItems.CYGNUS : TaiwanItems.SCOOTER;
    }

    @Override
    public void playerTouch(Player player) {
    }

    // 引擎聲不在這裡。
    //
    // 原本是每幾 tick 播一次原版礦車聲，用間隔的疏密假裝轉速——那聽起來是「噠、噠、噠」的
    // 斷點，不是一具引擎。現在改成客戶端掛一段無縫循環，持續改它的 pitch 與音量
    //（ScooterSoundInstance）。伺服器不必為此送任何封包：客戶端從車的位移就看得出來它跑多快。

    // ------------------------------------------------------------------ 存檔

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.owner = input.read("owner", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        this.speed = input.getFloatOr("speed", 0f);
        // 舊存檔沒有這個欄位，讀不到就是通用款——這正是 byName 不丟例外的理由
        setVariant(ScooterVariant.byName(input.getStringOr("variant", "")));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.owner != null) output.store("owner", net.minecraft.core.UUIDUtil.CODEC, this.owner);
        output.putFloat("speed", (float) this.speed);
        output.putString("variant", variant().getSerializedName());
    }
}
