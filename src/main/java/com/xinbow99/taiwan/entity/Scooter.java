package com.xinbow99.taiwan.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    private static final float MAX_SPEED = 0.42f;
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
    /** 轉向：每 tick 最多轉幾度。低速轉得快、高速轉得慢，不然高速會像在原地打轉。 */
    private static final float TURN_RATE = 5.5f;
    /** 撞牆超過這個速度就損壞。 */
    private static final float CRASH_SPEED = 0.28f;

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
            this.speed *= wet ? 0.5f : IDLE_FRICTION;
            if (Math.abs(this.speed) < 0.003f) this.speed = 0f;
            if (!this.level().isClientSide()) {
                this.entityData.set(DATA_STEER,
                        Mth.lerp(0.3f, this.entityData.get(DATA_STEER), 0f));
            }
        }

        Vec3 forward = new Vec3(-Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), 0.0,
                Mth.cos(this.getYRot() * Mth.DEG_TO_RAD));
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(forward.x * this.speed,
                this.onGround() ? Math.max(motion.y, -0.08) : motion.y - 0.08,
                forward.z * this.speed);

        this.move(MoverType.SELF, this.getDeltaMovement());
        checkCrash();

        if (!this.level().isClientSide()) {
            this.applyEffectsFromBlocks();
        }
        if (wet && this.level() instanceof ServerLevel server && this.tickCount % 10 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6, this.getZ(),
                    3, 0.15, 0.1, 0.15, 0.01);
        }
    }

    /**
     * 油門、煞車、倒車、轉向。
     *
     * <p>轉向速率跟著速度遞減：定值的話，高速時車頭會轉得比車身還快，玩起來像在冰上
     * 原地打轉。低速好轉、高速穩，才是騎車的手感。
     */
    private void drive(Player rider) {
        float turn = -rider.xxa;
        float gas = rider.zza;

        if (Math.abs(turn) > 0.01f) {
            // 停著也轉得動（慢慢牽），只是比騎起來慢一半。原本要求「速度 > 0」才給轉，
            // 結果是停下來就完全鎖死，玩起來像卡住
            float pace = Math.min(Math.abs(this.speed) / MAX_SPEED, 1f);
            float agility = TURN_RATE * (0.5f + 0.5f * Math.min(pace * 3f, 1f)) * (1f - pace * 0.55f);
            // **不乘 signum(speed)**：倒車時方向盤反向在真車上成立，但在遊戲裡玩家
            // 只會覺得「按左卻往右」。一律照按鍵的方向轉
            this.setYRot(this.getYRot() + turn * agility);
            this.setYHeadRot(this.getYRot());
        }
        // 龍頭的視覺角度。平滑地趨近按鍵方向，放手就回正——直接跳到極值會像在抽搐
        float wanted = Math.abs(turn) > 0.01f ? Math.signum(turn) * 26f : 0f;
        this.entityData.set(DATA_STEER, Mth.lerp(0.25f, this.entityData.get(DATA_STEER), wanted));

        if (gas > 0.01f) {
            this.speed = Math.min(this.speed + THROTTLE, MAX_SPEED);
        } else if (gas < -0.01f) {
            // 同一個鍵：還在前進就是煞車，停住之後才變倒車
            this.speed = this.speed > 0.01f
                    ? Math.max(this.speed - BRAKE, 0f)
                    : Math.max(this.speed - THROTTLE * 0.6f, -MAX_REVERSE);
        } else {
            this.speed *= COAST_FRICTION;
            if (Math.abs(this.speed) < 0.004f) this.speed = 0f;
        }
        this.setYHeadRot(this.getYRot());
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

    /**
     * 引擎聲。
     *
     * <h3>怠速也要有聲音</h3>
     * <p>只在移動時出聲的話，停紅燈那一刻車會變成一塊完全安靜的鐵，玩家會以為熄火了。
     * 只要有人騎著就一直有聲音，停著時低沉而慢。
     *
     * <h3>轉速要靠「間隔」，不能只調音高</h3>
     * <p>只把音高拉上去，高速聽起來只是同一個聲音變尖。**同時把兩次之間的間隔縮短**，
     * 「噠噠噠」才會跟著變密——那才聽得出來是轉速上去了。
     *
     * <p>熄火時完全不出聲。那一秒的安靜就是「你把車騎進水裡了」的回饋。
     */
    @Override
    public void baseTick() {
        super.baseTick();
        if (this.level().isClientSide() || stalled()) return;
        if (this.getControllingPassenger() == null) return;

        float pace = Math.min(Math.abs(this.speed) / MAX_SPEED, 1f);
        int gap = Math.max(2, Math.round(9f - pace * 7f));
        if (this.tickCount % gap != 0) return;

        this.level().playSound(null, this, SoundEvents.MINECART_INSIDE, SoundSource.NEUTRAL,
                0.18f + pace * 0.45f, 0.62f + pace * 1.5f);
    }

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
