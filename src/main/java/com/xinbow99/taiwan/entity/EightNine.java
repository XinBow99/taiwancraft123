package com.xinbow99.taiwan.entity;

import com.xinbow99.taiwan.TaiwanSounds;
import com.xinbow99.taiwan.entity.goal.EightNineCrowdGoal;
import com.xinbow99.taiwan.entity.goal.EightNineCruiseGoal;
import com.xinbow99.taiwan.entity.goal.EightNineRideGoal;
import com.xinbow99.taiwan.entity.goal.EightNineTalkGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
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

    /** 附近的同伴數。只存在伺服器端，客戶端只需要知道成團與否。 */
    private int courage;
    private int recount;

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
        this.goalSelector.addGoal(4, new EightNineCrowdGoal(this));
        this.goalSelector.addGoal(5, new EightNineTalkGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // 打一個，附近的同伴全部轉頭。這是原版就有的機制
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, EightNine.class)
                .setAlertOthers(EightNine.class));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, EightNineVariant.TEMPLE.ordinal());
        builder.define(DATA_CROWD, false);
    }

    public EightNineVariant variant() {
        return EightNineVariant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(EightNineVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.ordinal());
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
        if (!this.level().isClientSide() && this.recount-- <= 0) {
            this.recount = RECOUNT;
            updateCourage();
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
