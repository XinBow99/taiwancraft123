package com.xinbow99.taiwan.entity;

import com.xinbow99.taiwan.entity.goal.MacaqueStealGoal;
import com.xinbow99.taiwan.entity.goal.MacaqueTroopGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 台灣獼猴。
 *
 * <h2>牠的行為只有一條主線：搶了就跑</h2>
 * <p>台灣獼猴之所以出名，不是因為牠長什麼樣，是因為**牠會搶你手上的東西**。所以這裡的
 * 設計重心全部放在那件事上：看到食物 → 靠近 → 搶一個 → 轉頭就跑 → 找地方吃掉。
 * 少了「跑掉」那一段，牠就只是一隻會咬人的動物；有了那一段，牠才是獼猴。
 *
 * <h2>群</h2>
 * <p>群體大小由生成設定決定（3～8 隻），移動靠 {@link MacaqueTroopGoal} 互相拉著。
 * 反擊靠 {@code HurtByTargetGoal.setAlertOthers}——打一隻，附近同類全部轉頭看你。
 * 這是原版就有的機制，不用自己寫。
 *
 * <h2>爬樹</h2>
 * <p>{@link #onClimbable()} 只在**貼著原木或樹葉**的時候回 true。直接回
 * {@code horizontalCollision}（蜘蛛的做法）的話，牠會沿著玩家蓋的牆爬上去，
 * 那不是猴子，那是蜘蛛。
 */
public class Macaque extends PathfinderMob {

    /** 偷到東西之後幾 tick 開始吃。跑掉的時間要夠長，不然玩家一轉身就搶得回來。 */
    private static final int EAT_DELAY = 120;

    private int eatTimer;

    public Macaque(EntityType<? extends Macaque> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                // 比玩家走路快一點點：追得上、但玩家跑起來逃得掉。
                // 追不上的話搶劫永遠不會發生，追太快的話玩家永遠逃不掉，兩種都不好玩
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 搶劫排在攻擊前面：獼猴的預設狀態是小偷不是打手
        this.goalSelector.addGoal(1, new MacaqueStealGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new MacaqueTroopGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // 打一隻，整群轉頭。這是「群體反擊」那條需求的落點，原版就有，不用自己寫
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(Macaque.class));
    }

    // ------------------------------------------------------------------ 贓物

    /** 手上有沒有搶到的東西。裝備欄本來就會同步，不需要另外開一個 DataTracker 欄位。 */
    public boolean hasLoot() {
        return !this.getMainHandItem().isEmpty();
    }

    public void takeLoot(ItemStack stack) {
        this.setItemSlot(EquipmentSlot.MAINHAND, stack);
        this.eatTimer = EAT_DELAY;
        this.playSound(SoundEvents.ITEM_PICKUP, 1.0f, 1.4f);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() || !hasLoot()) return;

        if (--this.eatTimer > 0) return;
        eat();
    }

    /**
     * 把搶到的東西吃掉。
     *
     * <p>吃掉而不是丟掉：玩家要能看出東西回不來了。粒子與聲音是唯一的回饋——
     * 沒有那一下，物品只是無聲消失，看起來像 bug。
     */
    private void eat() {
        ItemStack loot = this.getMainHandItem();
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, loot.getItem()),
                    this.getX(), this.getY() + 0.6, this.getZ(), 12, 0.15, 0.1, 0.15, 0.03);
        }
        this.playSound(SoundEvents.GENERIC_EAT.value(), 1.0f, 1.1f);
        this.heal(2.0f);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    // ------------------------------------------------------------------ 爬樹

    /**
     * 只在貼著樹的時候可以往上爬。
     *
     * <p>回傳 true 之後，原版的移動邏輯會把牠當成在爬梯子。所以判斷條件就是整個「會爬樹」
     * 這個需求——不需要自己寫任何移動程式。
     */
    @Override
    public boolean onClimbable() {
        if (!this.horizontalCollision) return false;

        BlockPos pos = this.blockPosition();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState state = this.level().getBlockState(pos.relative(dir));
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) return true;
            // 頭部那一格也算：樹幹粗的時候腳邊碰到的是空氣
            BlockState above = this.level().getBlockState(pos.above().relative(dir));
            if (above.is(BlockTags.LOGS) || above.is(BlockTags.LEAVES)) return true;
        }
        return false;
    }

    /**
     * 生成條件：跟原版動物一樣（站得住的方塊、夠亮），但不繼承 {@code Animal}。
     *
     * <p>不繼承是因為 {@code Animal} 會帶進一整套繁殖行為（餵食、進入愛心模式、生小孩），
     * 而獼猴不該被餵食馴服——**牠跟玩家的關係是搶劫，不是畜牧**。繼承了就得再把那些關掉，
     * 不如不繼承。代價只有這個方法要自己寫。
     */
    public static boolean spawnRules(EntityType<Macaque> type, ServerLevelAccessor level,
                                     EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        // ignoresLightRequirements 這個短路**不能省**。CREATURE 類的生物幾乎只在區塊生成
        // 那一刻生成（EntitySpawnReason.CHUNK_GENERATION），而那時候光照還沒算完，
        // getRawBrightness 一律回 0。少了它，獼猴永遠一隻都不會自然生成——
        // 而症狀是「什麼事都沒發生」，沒有任何錯誤訊息。
        boolean lit = EntitySpawnReason.ignoresLightRequirements(reason)
                || level.getRawBrightness(pos, 0) > 8;
        boolean ground = level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON);
        return lit && ground;
    }

    /** 從樹上掉下來不該摔死——牠整天待在樹上。 */
    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return super.causeFallDamage(Math.max(0.0, distance - 5.0), multiplier, source);
    }

    // ------------------------------------------------------------------ 聲音

    /** 暫時借用狐狸的聲音，之後換成自己的（見素材清單）。 */
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }
}
