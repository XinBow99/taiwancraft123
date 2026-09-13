package com.xinbow99.taiwan.entity;

import com.xinbow99.taiwan.TaiwanSounds;
import com.xinbow99.taiwan.entity.goal.EightNineCrowdGoal;
import com.xinbow99.taiwan.entity.goal.EightNineCruiseGoal;
import com.xinbow99.taiwan.entity.goal.EightNineGreetGoal;
import com.xinbow99.taiwan.entity.goal.EightNineRideGoal;
import com.xinbow99.taiwan.entity.goal.EightNineTalkGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 8+9（陣頭少年）。
 *
 * <h2>核心機制是「群膽」</h2>
 * <p>這個族群最真實、也最好玩的特徵是：**人多的時候跟一個人的時候，完全是兩個樣子**。
 * 所以整個實體只圍繞一件事——身邊有幾個同伴。
 *
 * <p>{@link #courage} 由附近的同伴數推出來，而且**帶遲滯**（hysteresis）：
 * 聚集要 {@value #CROWD} 人才算成團，但要掉到 {@value #DISBAND} 人才算散掉。
 * 兩個門檻中間那一格是遲滯區。少了它，站在邊界上的人會每個 tick 在「大聲」與「安靜」
 * 之間跳，台詞會抽搐、音樂會一直開開關關。
 *
 * <h2>群膽有三個出口</h2>
 * <ul>
 *   <li>{@link EightNineCrowdGoal}：離同伴太遠就走過去。沒有領袖、沒有隊形——
 *       跟 {@link com.xinbow99.taiwan.entity.goal.MacaqueTroopGoal} 同一套，
 *       最便宜而且領袖死掉群也不會散。
 *   <li>{@link EightNineTalkGoal}：偶爾講一句。成團與落單講的話**不是同一組**。
 *   <li>{@link EightNineRideGoal} 與 {@link EightNineCruiseGoal}：去牽一台無主的機車騎走。
 *       成團時是全油門的車隊，落單時只是三成油門慢慢晃。
 * </ul>
 *
 * <p>加上算繪端的姿勢幅度（成團時放大 1.45 倍），同一個機制一共有四個出口。
 * 這是刻意的：一個只影響台詞的旗標玩家感覺不到，要讓它同時改變**說什麼、怎麼站、
 * 騎多快、放不放歌**，那個機制才存在。
 *
 * <h2>他們不主動打人</h2>
 * <p>只有被打才會還手（{@code HurtByTargetGoal} 加 {@code setAlertOthers}——打一個，
 * 整群轉頭）。刻意的：8+9 這個詞的來源是廟宇陣頭，不是幫派；做成見人就打
 * 只是把刻板印象再演一次。他們吵、他們成群、他們講話很大聲，但先動手的不是他們。
 */
public class EightNine extends PathfinderMob {

    /** 附近有幾個同伴就算「成團」。三個人就開始有陣仗，這也是播音樂的門檻。 */
    public static final int CROWD = 3;
    /**
     * 掉到幾個人才算散掉。**必須小於** {@link #CROWD}。
     *
     * <p>這兩個數字之間就是遲滯區。用同一個門檻的話，第三個人在邊界上走來走去，
     * 整團會每個 tick 在成團／散掉之間跳。
     */
    public static final int DISBAND = 2;
    /** 算同伴的半徑（格）。一個路口的大小。 */
    public static final double CROWD_RADIUS = 10.0;

    /** 幾 tick 重數一次人。不用每 tick——這是個範圍查詢，而且人不會瞬間出現。 */
    private static final int RECOUNT = 20;

    /** 型。要同步：算繪端靠它挑貼圖。 */
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(EightNine.class, EntityDataSerializers.INT);
    /**
     * 成團中。要同步的理由有兩個：算繪端要靠它決定姿勢（成團時比較張揚），
     * 客戶端也要靠它決定放不放音樂。
     */
    private static final EntityDataAccessor<Boolean> DATA_CROWD =
            SynchedEntityData.defineId(EightNine.class, EntityDataSerializers.BOOLEAN);
    /**
     * 正在抽菸。要同步：手上那根菸只有抽的時候才畫得出來，而**菸在不在**是全部人
     * 都看得到的事，不能各自在客戶端擲骰——不然同一個人在兩個玩家的畫面上一個叼菸一個沒有。
     */
    private static final EntityDataAccessor<Boolean> DATA_SMOKING =
            SynchedEntityData.defineId(EightNine.class, EntityDataSerializers.BOOLEAN);

    /**
     * 打招呼的實體事件。
     *
     * <p>用事件而不是同步欄位：招呼是**一次性**的，同步欄位只表達得了狀態，
     * 要靠翻轉一個布林值去代表「又招呼了一次」，連續兩次招呼中間沒有翻轉就會漏掉。
     * 原版所有一次性的動作（羊駝吐口水、劫掠獸咆哮）都是走這條路。
     *
     * <p>90 是刻意挑的：原版的 {@code EntityEvent} 目前用到 70，留一段距離，
     * 以後原版加新事件不會撞到。
     */
    private static final byte GREET_EVENT = 90;

    /** 抽一次菸抽多久（tick）。{@code animation.smoke} 是八秒一輪，這裡是兩輪。 */
    private static final int SMOKE_TICKS = 320;
    /** 兩次抽菸之間至少隔多久（tick）。 */
    private static final int SMOKE_GAP = 600;

    /** 附近的同伴數。只存在伺服器端，客戶端只需要知道成團與否。 */
    private int courage;
    private int recount;
    /** 這根菸還要抽幾 tick。0 代表沒在抽。只有伺服器端有意義。 */
    private int smokeLeft;
    /** 距離下一次能點菸還有幾 tick。 */
    private int smokeCooldown;

    // ---- 算繪端的動作狀態 ---------------------------------------------------
    //
    // 這三個只有客戶端在動（{@link #tick} 裡有 isClientSide 的分支），但欄位放在實體上
    // 而不是算繪狀態上：算繪狀態每一幀重建的語意是「抄一份快照」，而動畫要記得
    // 「從哪一個 tick 開始播」——那是跨幀的，必須有個活得比一幀久的地方放。
    //
    // 走路沒有對應的一份：它是靠 walkAnimationPos 驅動的，不吃時間，所以不需要狀態。

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState greetAnimationState = new AnimationState();
    public final AnimationState smokeAnimationState = new AnimationState();

    public EightNine(EntityType<? extends EightNine> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                // 跟玩家走路差不多。他們是在街上晃，不是在追人
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 被打才還手——見類別說明，他們不主動找事
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, false));
        // 騎車排在群聚前面：騎上車之後就不該再用走的去找同伴了
        this.goalSelector.addGoal(2, new EightNineCruiseGoal(this));
        this.goalSelector.addGoal(3, new EightNineRideGoal(this));
        // 招呼排在群聚與閒晃前面：擦身而過的那一下要當場有反應，
        // 排在後面的話正在走向同伴的人永遠不會停下來看你一眼
        this.goalSelector.addGoal(4, new EightNineGreetGoal(this));
        this.goalSelector.addGoal(5, new EightNineCrowdGoal(this));
        this.goalSelector.addGoal(6, new EightNineTalkGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // 打一個，附近的同伴全部轉頭。這是原版就有的機制
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, EightNine.class)
                .setAlertOthers(EightNine.class));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, EightNineVariant.TEMPLE.ordinal());
        builder.define(DATA_CROWD, false);
        builder.define(DATA_SMOKING, false);
    }

    public EightNineVariant variant() {
        return EightNineVariant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(EightNineVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.ordinal());
    }

    /** 抽菸中。算繪端靠它決定手上那根菸畫不畫。 */
    public boolean isSmoking() {
        return this.entityData.get(DATA_SMOKING);
    }

    /**
     * 讓所有看得到他的客戶端播一次招呼。
     *
     * <p>伺服器端專用——{@code broadcastEntityEvent} 在客戶端是沒有作用的空操作，
     * 所以不會有「自己招呼給自己看」這種事。
     */
    public void triggerGreet() {
        this.level().broadcastEntityEvent(this, GREET_EVENT);
    }

    /** 成團中。算繪端與音樂都看它。 */
    public boolean inCrowd() {
        return this.entityData.get(DATA_CROWD);
    }

    /** 附近的同伴數（含自己）。只在伺服器端有意義。 */
    public int courage() {
        return this.courage;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            tickAnimations();
            return;
        }
        if (this.recount-- <= 0) {
            this.recount = RECOUNT;
            updateCourage();
        }
        tickSmoking();
    }

    /**
     * 客戶端：把三段動作開開關關。
     *
     * <p>{@code animateWhen} 是冪等的——條件成立時「若尚未開始才開始」，
     * 所以每 tick 呼叫不會讓動畫一直從頭播。
     *
     * <p>站著的動作只在**真的站著**時播。用 {@code walkAnimation.isMoving()} 而不是
     * 速度是否為零：後者在被推、在水裡漂的時候也是非零，站著的人會一直抽搐。
     */
    private void tickAnimations() {
        this.idleAnimationState.animateWhen(!this.walkAnimation.isMoving(), this.tickCount);
        this.smokeAnimationState.animateWhen(isSmoking(), this.tickCount);
    }

    /**
     * 伺服器端：什麼時候點一根。
     *
     * <p>只有**站著**才會點菸，而且走起來就掐掉——抽菸那段動作是手舉到嘴邊的，
     * 邊走邊抽會跟走路的擺手打架。這也剛好是對的：真的要抽會先停下來。
     */
    private void tickSmoking() {
        if (this.smokeLeft > 0) {
            this.smokeLeft--;
            if (this.smokeLeft == 0 || this.walkAnimation.isMoving() || this.isInWater()) {
                this.smokeLeft = 0;
                this.smokeCooldown = SMOKE_GAP;
                this.entityData.set(DATA_SMOKING, false);
            }
            return;
        }
        if (this.smokeCooldown > 0) {
            this.smokeCooldown--;
            return;
        }
        // 站著、沒在水裡、而且骰到——一百二十分之一，平均六秒一次機會，
        // 所以一群人不會同時點菸
        if (!this.walkAnimation.isMoving() && !this.isInWater()
                && this.getRandom().nextInt(120) == 0) {
            this.smokeLeft = SMOKE_TICKS;
            this.entityData.set(DATA_SMOKING, true);
        }
    }

    /**
     * 收到招呼事件就播一次。
     *
     * <p>{@code start} 而不是 {@code startIfStopped}：連續兩次招呼要從頭播，
     * 不是接在上一次還沒播完的地方。
     */
    @Override
    public void handleEntityEvent(byte event) {
        if (event == GREET_EVENT) {
            this.greetAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(event);
        }
    }

    /**
     * 數人，然後套遲滯。
     *
     * <p>成團要 {@value #CROWD} 人，散掉要掉到 {@value #DISBAND} 人以下——中間那一格
     * 維持原狀。這就是遲滯：狀態不是「現在幾個人」的函數，而是「幾個人**以及**
     * 你原本是什麼狀態」的函數。
     */
    private void updateCourage() {
        List<EightNine> nearby = this.level().getEntitiesOfClass(EightNine.class,
                this.getBoundingBox().inflate(CROWD_RADIUS),
                other -> other != this && other.isAlive());
        this.courage = nearby.size() + 1;

        boolean was = inCrowd();
        boolean now = was ? this.courage > DISBAND : this.courage >= CROWD;
        if (now != was) this.entityData.set(DATA_CROWD, now);
    }

    /**
     * 生成時決定型。
     *
     * <p>同一批生出來的**盡量同型**：一團全部是白衣白褲，或全部是機車少年，才像一個
     * 陣頭／一掛人。完全隨機的話每一團都是六型各一個，看起來像 NPC 展示櫃。
     */
    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        RandomSource random = level.getRandom();
        if (data instanceof GroupLook look) {
            // 同一團有八成機率同型，剩下兩成讓它混一點，不然太整齊
            setVariant(random.nextFloat() < 0.8f ? look.variant() : EightNineVariant.random(random));
        } else {
            EightNineVariant picked = EightNineVariant.random(random);
            setVariant(picked);
            data = new GroupLook(picked);
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    /** 一批生成共用的「這團穿什麼」。 */
    private record GroupLook(EightNineVariant variant) implements SpawnGroupData {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;   // 說話由 EightNineTalkGoal 負責，不用原版的 ambient
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return TaiwanSounds.EIGHTNINE_HURT;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(EightNineVariant.byName(input.getStringOr("variant", "")));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("variant", variant().getSerializedName());
    }
}
