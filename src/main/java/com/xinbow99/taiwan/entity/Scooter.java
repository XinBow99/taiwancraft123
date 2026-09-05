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
    /** 轉向：每 tick 最多轉幾度。6.5 度 ≒ 每秒 130 度，一個路口的直角彎大約一秒轉完。 */
    private static final float TURN_RATE = 6.5f;
    /** 撞牆超過這個速度就損壞。 */
    private static final float CRASH_SPEED = 0.28f;

    /**
     * 甩尾時輪胎剩下多少抓地力。
     *
     * <p>0.18 讓側滑角穩定在 40 度上下——車身明顯橫著走，但還沒到「面向側面全速前進」
     * 那種荒謬的程度，而且沒有頂到上限，所以方向鍵仍然改得動滑出去的角度。
     */
    private static final float DRIFT_GRIP = 0.18f;
    /** 甩尾的最低速度。太慢就甩不動——低速原地轉圈不是甩尾，是鬼打牆。 */
    private static final float DRIFT_MIN_SPEED = 0.18f;
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
    /** 龍頭的視覺角度（度）。要同步，客戶端才畫得出把手轉動。 */
    private static final EntityDataAccessor<Float> DATA_STEER =
            SynchedEntityData.defineId(Scooter.class, EntityDataSerializers.FLOAT);

    private float speed;
    /** 上一 tick 的速度，撞擊判定用。 */
    private float lastSpeed;
    /**
     * 行進方向（度）。
     *
     * <p>**這是這次改動的核心**：車頭朝哪裡（{@link #getYRot()}）跟車實際往哪裡走是兩件事。
     * 之前兩者永遠相等，所以轉頭就等於瞬間換方向，車像一個會旋轉的箭頭而不是一台有重量的車。
     * 分開之後，行進方向是「追」著車頭跑的，過彎才有重心轉移的感覺，甩尾也才成立。
     */
    private float velYaw;
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
    }

    // ------------------------------------------------------------------ 車主

    public Optional<UUID> owner() {
        return Optional.ofNullable(this.owner);
    }

    public boolean mayRide(Player player) {
        return owner().map(id -> id.equals(player.getUUID())).orElse(true);
    }

    /** 龍頭角度（度）。算繪器用。 */
    public float steerAngle() {
        return this.entityData.get(DATA_STEER);
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

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        super.positionRider(passenger, move);
        // 讓乘客跟著車頭方向，不然人會朝著自己上車前的方向坐著
        passenger.setYBodyRot(this.getYRot());
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

        LivingEntity rider = this.getControllingPassenger();
        if (rider instanceof Player player && !wet) {
            drive(player);
        } else {
            // 沒人騎（或熄火）：很快停住。船那種滑行慣性放在機車上會變成停好之後自己飄走
            this.drifting = false;
            this.driftTicks = 0;
            this.speed *= wet ? 0.5f : IDLE_FRICTION;
            if (Math.abs(this.speed) < 0.003f) this.speed = 0f;
            if (!this.level().isClientSide()) {
                this.entityData.set(DATA_STEER,
                        Mth.lerp(0.3f, this.entityData.get(DATA_STEER), 0f));
            }
        }

        grip();

        // 往「行進方向」走，不是往車頭方向走。差別就是過彎時那半秒的外拋
        Vec3 heading = new Vec3(-Mth.sin(this.velYaw * Mth.DEG_TO_RAD), 0.0,
                Mth.cos(this.velYaw * Mth.DEG_TO_RAD));
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(heading.x * this.speed,
                this.onGround() ? Math.max(motion.y, -0.08) : motion.y - 0.08,
                heading.z * this.speed);

        this.move(MoverType.SELF, this.getDeltaMovement());
        checkCrash();

        if (!this.level().isClientSide()) {
            this.applyEffectsFromBlocks();
        }
        if (wet && this.level() instanceof ServerLevel server && this.tickCount % 10 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6, this.getZ(),
                    3, 0.15, 0.1, 0.15, 0.01);
        }
        // 燒胎的白煙。甩尾在畫面上要看得出來，不然玩家只會覺得「車怎麼在飄」
        if (this.drifting && this.level() instanceof ServerLevel server) {
            Vec3 back = new Vec3(Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), 0.0,
                    -Mth.cos(this.getYRot() * Mth.DEG_TO_RAD)).scale(0.55);
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX() + back.x, this.getY() + 0.12, this.getZ() + back.z,
                    2, 0.08, 0.02, 0.08, 0.005);
        }
    }

    /**
     * 油門、煞車、倒車、轉向、甩尾。
     *
     * <h3>轉向不再隨速度垮掉</h3>
     * <p>之前高速時轉向率被砍掉一半以上（×0.45），本意是「高速要穩」，實際的手感是
     * 騎快了就轉不動——玩家按著方向鍵，車卻慢慢地畫一個大圓，這就是「轉向有問題」的來源。
     * 現在高速只扣兩成，路口該轉得過去就轉得過去。真正負責「高速比較難控制」的，
     * 改由抓地力（{@link #grip()}）處理：車頭轉得動，但車身會外拋，那才是速度的代價。
     *
     * <h3>甩尾：跳躍鍵 ＋ 方向</h3>
     * <p>按著跳躍鍵轉彎就進入甩尾：輪胎失去抓地力，車身橫著滑出去，但油門照給。
     * 撐過 {@link #DRIFT_CHARGE} tick 再放開就有一段加速——這是跑跑卡丁車那條「過彎不是損失，
     * 是收益」的規則，也是為什麼那個遊戲的彎道比直線好玩。
     *
     * <p>用跳躍鍵是因為蹲下鍵在 Minecraft 裡是「下車」，而前後鍵是同一個軸：
     * 同時按 W 和 S 會相消成 0，所以「油門＋煞車」這種常見的甩尾組合在這裡讀不出來。
     */
    private void drive(Player rider) {
        float turn = -rider.xxa;
        float gas = rider.zza;
        float pace = Math.min(Math.abs(this.speed) / MAX_SPEED, 1f);

        boolean wantDrift = jumpHeld(rider) && Math.abs(turn) > 0.1f
                && this.speed > DRIFT_MIN_SPEED && this.onGround();
        if (wantDrift) {
            this.driftTicks++;
        } else if (this.driftTicks > 0) {
            releaseDrift();
        }
        this.drifting = wantDrift;

        if (Math.abs(turn) > 0.01f) {
            // 停著也轉得動（慢慢牽），只是比騎起來慢一半。原本要求「速度 > 0」才給轉，
            // 結果是停下來就完全鎖死，玩起來像卡住
            float agility = TURN_RATE * (0.55f + 0.45f * Math.min(pace * 4f, 1f)) * (1f - pace * 0.2f);
            // 甩尾中車頭轉得更快：滑出去的時候要有辦法用車頭去指你要走的方向，
            // 不然甩尾只是失控
            if (this.drifting) agility *= 1.35f;
            // **不乘 signum(speed)**：倒車時方向盤反向在真車上成立，但在遊戲裡玩家
            // 只會覺得「按左卻往右」。一律照按鍵的方向轉
            this.setYRot(this.getYRot() + turn * agility);
            this.setYHeadRot(this.getYRot());
        }
        // 龍頭的視覺角度。平滑地趨近按鍵方向，放手就回正——直接跳到極值會像在抽搐。
        // 甩尾時壓得更低（算繪器把這個角度的一半當作車身傾角）
        float wanted = Math.abs(turn) > 0.01f ? Math.signum(turn) * (this.drifting ? 38f : 26f) : 0f;
        this.entityData.set(DATA_STEER, Mth.lerp(0.25f, this.entityData.get(DATA_STEER), wanted));

        if (this.boostTicks > 0) this.boostTicks--;
        float cap = this.boostTicks > 0 ? MAX_SPEED * BOOST_OVERSPEED : MAX_SPEED;

        if (gas > 0.01f) {
            this.speed = Math.min(this.speed + THROTTLE * (this.boostTicks > 0 ? 1.8f : 1f), cap);
        } else if (gas < -0.01f) {
            // 同一個鍵：還在前進就是煞車，停住之後才變倒車。
            // 甩尾中只給三成煞車：甩尾要保住速度，不然過彎永遠比直直騎慢
            float brake = this.drifting ? BRAKE * 0.3f : BRAKE;
            this.speed = this.speed > 0.01f
                    ? Math.max(this.speed - brake, 0f)
                    : Math.max(this.speed - THROTTLE * 0.6f, -MAX_REVERSE);
        } else {
            this.speed *= COAST_FRICTION;
            if (Math.abs(this.speed) < 0.004f) this.speed = 0f;
        }
        // 加速結束後速度會超過上限，讓它自己收回來，不要硬切
        if (this.speed > cap) this.speed = Math.max(cap, this.speed * 0.97f);
        this.setYHeadRot(this.getYRot());
    }

    /**
     * 抓地力：把行進方向拉向車頭方向。
     *
     * <p>一台車的速度不會因為你轉了車頭就跟著轉——那是慣性。這裡每 tick 把行進方向往
     * 車頭方向拉一部分（{@code grip}），拉不完的差額就是側滑角，也就是「外拋」的量。
     *
     * <ul>
     *   <li>低速抓地力接近滿：巷子裡慢慢騎不會滑，牽車也不會歪。</li>
     *   <li>高速抓地力下降：同樣的方向鍵，快的時候車會往彎外多帶一點。</li>
     *   <li>甩尾時只剩 {@link #DRIFT_GRIP}：車身幾乎是橫著走的。</li>
     * </ul>
     *
     * <p>側滑角有上限，而且側滑要吃掉速度。沒有這兩條的話，車會變成可以無成本橫移的東西：
     * 一邊全速前進一邊面向側面，看起來很蠢，玩起來也沒有取捨。
     */
    private void grip() {
        float pace = Math.min(Math.abs(this.speed) / MAX_SPEED, 1f);
        if (Math.abs(this.speed) < 0.06f) {
            // 幾乎停住：直接對齊。也順便處理「剛讀檔進來」的情況——velYaw 沒有存檔，
            // 一開始是 0，不對齊的話車會朝著北方橫著滑出去
            this.velYaw = this.getYRot();
            return;
        }

        float grip = this.drifting ? DRIFT_GRIP : 0.62f - 0.34f * pace;
        this.velYaw += Mth.wrapDegrees(this.getYRot() - this.velYaw) * grip;

        float slip = Mth.wrapDegrees(this.getYRot() - this.velYaw);
        float max = this.drifting ? 45f : 30f;
        if (Math.abs(slip) > max) {
            slip = Math.signum(slip) * max;
            this.velYaw = this.getYRot() - slip;
        }
        // 橫著走要付出速度。甩尾付得少一點，那是它的獎勵
        this.speed *= 1f - Math.abs(slip) * (this.drifting ? 0.0006f : 0.0011f);
    }

    /**
     * 放開甩尾。撐得夠久就給一段加速。
     *
     * <p>加速的量刻意不大（超過最高速兩成多、持續一秒）：它要值得為它甩一次尾，
     * 但不能變成「不甩尾就別想跟人比」。
     */
    private void releaseDrift() {
        if (this.driftTicks >= DRIFT_CHARGE) {
            this.boostTicks = BOOST_TICKS;
            this.speed = Math.min(this.speed + 0.06f, MAX_SPEED * BOOST_OVERSPEED);
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.3, this.getZ(),
                        8, 0.2, 0.1, 0.2, 0.02);
            }
        }
        this.driftTicks = 0;
    }

    /**
     * 騎士有沒有按著跳躍鍵。
     *
     * <p>兩邊拿的是同一份輸入，只是入口不同：伺服器端只有 {@code ServerPlayer} 收得到
     * 輸入封包；客戶端的 {@code LocalPlayer} 則在 {@code applyInput()} 裡把跳躍鍵寫進
     * {@code jumping}——跟 {@code xxa}／{@code zza} 同一個地方，所以兩邊算出來會一致。
     * 不一致的話，甩尾在伺服器上發生、在你的畫面上沒有，車就會一直被拉回去。
     */
    private static boolean jumpHeld(Player rider) {
        return rider instanceof net.minecraft.server.level.ServerPlayer server
                ? server.getLastClientInput().jump()
                : rider.isJumping();
    }

    /**
     * 撞牆。
     *
     * <p>用「這一 tick 掉了多少速度」判定，而不是用碰撞旗標：低速貼著牆走也會一直
     * {@code horizontalCollision}，那樣車會在牆邊慢慢被磨壞。只有真的高速撞上去才算。
     */
    private void checkCrash() {
        if (!this.horizontalCollision) return;
        float lost = Math.abs(this.lastSpeed) - Math.abs(this.speed);
        if (Math.abs(this.lastSpeed) < CRASH_SPEED || lost < CRASH_SPEED * 0.5f) {
            this.speed = 0f;
            return;
        }
        this.speed = 0f;
        if (this.level().isClientSide()) return;

        this.setDamage(this.getDamage() + Math.abs(this.lastSpeed) * 22f);
        this.setHurtTime(10);
        this.playSound(SoundEvents.ANVIL_LAND, 0.6f, 1.6f);
        if (this.getDamage() > 40f) {
            this.destroy(this.level().getServer().overworld(), TaiwanItems.SCOOTER);
        }
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
        output.putFloat("speed", this.speed);
    }
}
