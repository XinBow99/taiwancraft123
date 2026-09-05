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

    /** 最高速（格/tick）。0.42 約等於玩家衝刺的 1.5 倍。 */
    public static final float MAX_SPEED = 0.42f;
    /** 倒車最高速。倒車要慢，不然玩家會用倒車當第二個前進檔。 */
    private static final float MAX_REVERSE = 0.12f;
    /** 油門。加速要快——「加速快」是機車相對於汽車的賣點。 */
    private static final float THROTTLE = 0.035f;
    /** 煞車。比油門強，煞得住才敢騎快。 */
    private static final float BRAKE = 0.09f;
    /** 沒有人騎的時候的摩擦。夠重，所以停好就不會漂。 */
    private static final float IDLE_FRICTION = 0.72f;
    /** 有人騎但沒給油的滑行摩擦。 */
    private static final float COAST_FRICTION = 0.955f;
    /**
     * 軸距（格）。前輪到後輪的距離。
     *
     * <p>它決定同一個把手角度會畫出多大的圓：{@code 迴轉半徑 = 軸距 ÷ tan(把手角度)}。
     * 調大 → 每個角度的圓都變大，車開起來像巴士，巷子轉不進去；調小 → 迴轉半徑跟著縮，
     * 車會靈活到不像有重量，1.0 以下轉起來近乎原地打轉。1.5 配上 45 度的滿舵約是
     * 1.5 格的最小迴轉半徑，剛好過得了一個標準路口。
     */
    private static final double WHEELBASE = 1.5;
    /**
     * 慢速時把手能打到幾度。
     *
     * <p>45 度 → 最小迴轉半徑 1.5 格，停車場裡原地繞圈那種。調小（例如 30 度）半徑會拉到
     * 2.6 格，牽車入位會開始需要來回喬；調大到 60 度以上半徑剩 0.9 格，車會像在轉陀螺。
     */
    private static final float STEER_LOCK_SLOW = 45f;
    /**
     * 全速時把手只能打到幾度。
     *
     * <p>不是為了限制玩家，是真的騎車就這樣：速度越快把手動得越少，高速全打死等於摔車。
     * 8 度 → 全速迴轉半徑 10.7 格，是 GTA 那種大彎的手感。調大到 15 度半徑掉到 5.6 格，
     * 高速變得很好轉、但也很容易一個彎就切進對向；調小到 5 度半徑 17 格，
     * 高速幾乎只能直線，路口要先減速才過得去。
     */
    private static final float STEER_LOCK_FAST = 8f;
    /** 把手轉動的跟隨速度。0.25 ≒ 三格內轉到位；放開方向鍵時目標是 0，所以同一條式子也負責回正。 */
    private static final float STEER_LERP = 0.25f;
    /** 過彎時車身最多傾幾度。純視覺。 */
    private static final float MAX_LEAN = 22f;
    /** 撞牆超過這個速度就損壞。 */
    private static final float CRASH_SPEED = 0.28f;

    /**
     * 平常騎乘時，側向的慣性有多少留到下一 tick。
     *
     * <p>0.12 ＝ 幾乎全被輪胎吃掉：車頭指哪裡就往哪裡走，轉完立刻直行。
     */
    private static final double SIDE_KEEP_GRIP = 0.12;
    /** 甩尾（手煞車）時留下多少側向慣性。0.85 ＝ 幾乎不吃，車身橫著滑出去。 */
    private static final double SIDE_KEEP_DRIFT = 0.85;
    /** 甩尾時車身額外多轉的倍率。後輪在滑，轉得比幾何算出來的多——這是手感，不是物理。 */
    private static final float DRIFT_YAW_GAIN = 1.35f;
    /** 甩尾的最低速度。太慢就甩不動——低速原地轉圈不是甩尾，是鬼打牆。 */
    private static final double DRIFT_MIN_SPEED = 0.18;
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
    /** 壓車角度（度）。同上，純視覺。 */
    private static final EntityDataAccessor<Float> DATA_LEAN =
            SynchedEntityData.defineId(Scooter.class, EntityDataSerializers.FLOAT);

    // 狀態分三層，不要混在一起。混在一起就是上一版的錯誤：方向鍵（第一層）被直接寫進
    // 車身朝向（第二層），於是「轉向」變成一件跟輪胎、跟速度都無關的事。

    // ---- 第一層：玩家輸入。跟速度無關，車停著也存在 ----

    /** 把手角度（度，正值往右）。車停著也打得動——只是車不會轉。 */
    private float steerAngle;
    /** 油門，-1（煞車／倒車）到 1（全開）。 */
    private float throttle;

    // ---- 第二層：車輛狀態。由第一層推導，不接受輸入直接寫入 ----

    /** 沿著車頭方向的速度純量（格/tick）。 */
    private double speed;
    /** 上一 tick 的速度，撞擊判定用。 */
    private double lastSpeed;
    /** 壓車角度（度）。純視覺，不影響任何物理。 */
    private float leanAngle;
    // 車身朝向就是 Entity.yRot，唯一能改它的地方是 tickPhysics() 的第三步。

    // ---- 第三層：世界狀態 ----

    /** 水平速度向量。留著它才有側向慣性可以吃——那是抓地力與甩尾的來源。 */
    private Vec3 planar = Vec3.ZERO;

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

    public boolean stalled() {
        return this.entityData.get(DATA_STALLED);
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
        return this.getFirstPassenger() instanceof Player player ? player : null;
    }

    /**
     * 座位。前面那個是騎士，後面那個是後座。
     *
     * <p>Y 用 0.82：模型的座墊頂面在 13px 高，換算就是這個數字。跟模型對不上的話，
     * 玩家會浮在座墊上方或半個屁股陷進車裡。
     */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dim, float scale) {
        boolean pillion = !this.getPassengers().isEmpty() && this.getPassengers().get(0) != passenger;
        return new Vec3(0.0, 0.82, pillion ? -0.42 : 0.02);
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
     * 車轉多少，乘客的視角就跟著轉多少。
     *
     * <p>這是「機車沒有跟座標軸綁在一起」的真正原因：之前只把乘客的視角**夾**在車頭
     * ±105 度以內，所以車在你底下轉了 90 度，鏡頭卻一動也不動——轉個兩次，你的畫面
     * 就朝著側面甚至後面，按 W 於是「像倒著跑」。
     *
     * <p>改成跟著轉之後，車頭永遠是畫面的正前方，而 ±105 度的夾角仍然留著：你還是
     * 可以轉頭看旁邊，只是放開滑鼠時整台車跟畫面是對齊的。
     *
     * <p>{@code yRotO} 之類的「上一 tick」欄位要一起加，不然算繪的內插會把這一格
     * 的轉動畫成一次回甩，畫面會抖。
     */
    private static void rotateWith(Entity passenger, float delta) {
        passenger.setYRot(passenger.getYRot() + delta);
        passenger.yRotO += delta;
        passenger.setYHeadRot(passenger.getYHeadRot() + delta);
        if (passenger instanceof LivingEntity living) {
            living.yHeadRotO += delta;
            living.yBodyRot += delta;
            living.yBodyRotO += delta;
        }
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        super.positionRider(passenger, move);
        // 身體固定朝車頭，只有頭可以轉。人是跨坐在車上的，身體不可能面向側面
        passenger.setYBodyRot(this.getYRot());
        // 視角可以左右各看 105 度（看後照鏡、看巷口），但不能超過——超過就代表
        // 畫面跟車已經脫節了
        float diff = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
        passenger.setYRot(this.getYRot() + Mth.clamp(diff, -105f, 105f));
        passenger.setYHeadRot(passenger.getYRot());
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
     * <h3>順序是規格的一部分</h3>
     * <p>底下八步的先後不能動。最容易出錯的是三跟四：速度向量一定要用**更新後**的車頭方向
     * 去算。先算向量再轉車頭的話，車會先用上一格的方向走一格、再把車頭轉過來，過彎時就會
     * 出現「慣性方向對不上車頭」的抖動。
     *
     * <h3>yaw 是衍生量</h3>
     * <p>整個類別裡只有第三步碰 {@code yRot}，而且只用那一條公式。這是這次重寫的重點：
     * 之前是把方向鍵直接加到車身朝向上，那等於宣稱「車子自己會轉」，跟輪胎、跟速度都沒有
     * 關係——停著原地打轉、高速轉不過來，是同一個錯誤的兩種症狀。
     */
    private void tickPhysics(boolean wet) {
        Player rider = !wet && this.getControllingPassenger() instanceof Player p ? p : null;

        // ---- 1. 輸入：油門與把手 -------------------------------------------------
        float steerInput = rider != null ? Mth.clamp(-rider.xxa, -1f, 1f) : 0f;
        this.throttle = rider != null ? Mth.clamp(rider.zza, -1f, 1f) : 0f;
        updateDrift(rider, steerInput);

        // 速度越快，把手能打的角度越小。「高速轉不動」這件事現在由這裡負責，而不是去衰減
        // 車身的轉速——差別在於前者是輪胎的角度，玩家看得到，也解釋得通
        float pace = (float) Math.min(Math.abs(this.speed) / MAX_SPEED, 1.0);
        float lock = Mth.lerp(pace, STEER_LOCK_SLOW, STEER_LOCK_FAST);
        this.steerAngle = Mth.lerp(STEER_LERP, this.steerAngle, steerInput * lock);
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_STEER, this.steerAngle);
        }

        // ---- 2. 速度 -------------------------------------------------------------
        updateSpeed(rider != null, wet);

        // ---- 3. 車身朝向（整個檔案裡唯一改 yaw 的地方）---------------------------
        //
        //     Δyaw = (速度 ÷ 軸距) × tan(把手角度)
        //
        // 車會轉，是因為「正在前進」而且「前輪有角度」。速度是 0 的時候分子就是 0，
        // 原地打轉在結構上不可能發生——不需要任何 if (speed < x) 的特判去補
        double angularVel = (this.speed / WHEELBASE) * Math.tan(Math.toRadians(this.steerAngle));
        float yawDelta = (float) Math.toDegrees(angularVel);
        // 甩尾時後輪在滑，車身轉得比幾何算出來的更多。這一項是手感，不是物理
        if (this.drifting) yawDelta *= DRIFT_YAW_GAIN;
        if (yawDelta != 0f) {
            this.setYRot(this.getYRot() + yawDelta);
            this.setYHeadRot(this.getYRot());
            // 車轉多少，騎士的視角就跟著轉多少
            for (Entity passenger : this.getPassengers()) rotateWith(passenger, yawDelta);
        }

        // ---- 4. 用更新後的車頭方向算出目標速度 -----------------------------------
        Vec3 forward = new Vec3(-Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), 0.0,
                Mth.cos(this.getYRot() * Mth.DEG_TO_RAD));
        Vec3 side = new Vec3(-forward.z, 0.0, forward.x);

        // ---- 5. 抓地力：側向分量要被輪胎吃掉 -------------------------------------
        //
        // 上一 tick 的速度是沿著「上一個」車頭方向的；車頭轉過去之後，那股慣性就有一部分
        // 變成側向的。輪胎的工作就是把它吃掉——吃得越乾淨，車越像走在軌道上；留得越多，
        // 車越像在冰上。SIDE_KEEP_* 是「留下來的比例」，不是抓地力本身
        double lateral = this.planar.dot(side)
                * (this.drifting ? SIDE_KEEP_DRIFT : SIDE_KEEP_GRIP);
        this.planar = forward.scale(this.speed).add(side.scale(lateral));

        // ---- 6. 重力與位移 -------------------------------------------------------
        Vec3 was = this.position();
        double vy = this.onGround()
                ? Math.max(this.getDeltaMovement().y, -0.08)
                : this.getDeltaMovement().y - 0.08;
        this.setDeltaMovement(this.planar.x, vy, this.planar.z);
        this.move(MoverType.SELF, this.getDeltaMovement());

        // ---- 7. 依碰撞結果回寫速度 -----------------------------------------------
        applyCollision(was);

        // ---- 8. 壓車角度（純視覺）-----------------------------------------------
        //
        // 分母用「目前的把手上限」而不是固定的 45 度：高速時把手只能打 8 度，除以 45
        // 的話全速過彎只傾 4 度，看起來像在滑冰。機車過彎就是靠傾的，而這是畫面上唯一
        // 看得出「他正在過彎」的東西
        float targetLean = -(this.steerAngle / lock) * MAX_LEAN * pace;
        this.leanAngle = Mth.lerp(0.25f, this.leanAngle, targetLean);
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_LEAN, this.leanAngle);
        }
    }

    /**
     * 速度：油門、煞車、倒車、滑行阻力。
     *
     * <p>這裡完全不碰方向。速度是純量，方向是 {@link #tickPhysics} 第三步的事。
     */
    private void updateSpeed(boolean ridden, boolean wet) {
        if (this.boostTicks > 0) this.boostTicks--;

        if (!ridden) {
            // 沒人騎（或熄火）：很快停住。船那種滑行慣性放在機車上會變成停好之後自己飄走
            this.speed *= wet ? 0.5 : IDLE_FRICTION;
            if (Math.abs(this.speed) < 0.003) this.speed = 0.0;
            return;
        }

        double cap = this.boostTicks > 0 ? MAX_SPEED * BOOST_OVERSPEED : MAX_SPEED;
        if (this.throttle > 0.01f) {
            this.speed = Math.min(this.speed + THROTTLE * (this.boostTicks > 0 ? 1.8 : 1.0), cap);
        } else if (this.throttle < -0.01f) {
            // 同一個鍵：還在前進就是煞車，停住之後才變倒車。
            // 甩尾中只給三成煞車：甩尾要保住速度，不然過彎永遠比直直騎慢
            double brake = this.drifting ? BRAKE * 0.3 : BRAKE;
            this.speed = this.speed > 0.01
                    ? Math.max(this.speed - brake, 0.0)
                    : Math.max(this.speed - THROTTLE * 0.6, -MAX_REVERSE);
        } else {
            this.speed *= COAST_FRICTION;
            if (Math.abs(this.speed) < 0.004) this.speed = 0.0;
        }
        // 加速結束後速度會超過上限，讓它自己收回來，不要硬切
        if (this.speed > cap) this.speed = Math.max(cap, this.speed * 0.97);
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
        this.planar = this.planar.scale(kept);

        // 真的撞上去才算撞車：夠快，而且大部分的速度是這一格掉的
        if (Math.abs(this.lastSpeed) >= CRASH_SPEED && kept < 0.5 && !this.level().isClientSide()) {
            this.speed = 0.0;
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
     * <p>輪胎不再吃掉側向的慣性（{@link #SIDE_KEEP_DRIFT}），車身於是橫著滑出去，油門照給。
     * 撐過 {@link #DRIFT_CHARGE} tick 再放開就有一段加速——過彎不是損失而是收益，
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
                this.speed = Math.min(this.speed + 0.06, MAX_SPEED * BOOST_OVERSPEED);
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
        return TaiwanItems.SCOOTER;
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
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.owner != null) output.store("owner", net.minecraft.core.UUIDUtil.CODEC, this.owner);
        output.putFloat("speed", (float) this.speed);
    }
}
