package com.xinbow99.taiwan.entity.goal;

import com.xinbow99.taiwan.entity.EightNine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * 打招呼。
 *
 * <h2>招呼是「看到你了」，不是一段話</h2>
 * <p>玩家、村民、或另一個 8+9 走進 {@value #RANGE} 格就點個頭、抬個手
 *（{@code animation.greet}，三秒）。同一個對象在 {@value #MEMORY} tick 內不會再招呼一次——
 * 沒有這個記憶的話，兩個人在巷口擦身而過會互相點頭點到天荒地老。
 *
 * <h2>為什麼記的是「上一個」而不是一整張表</h2>
 * <p>只記最近招呼過的那一位。實務上會連續碰到的就是剛剛擦身而過的那個人，
 * 而一張 UUID → 時間的表要自己過期、要跟著存檔、還要處理實體被移除。
 * 記一個人就解決了九成的重複，剩下那一成（在兩個人中間來回）看起來反而像真的。
 *
 * <h2>擋住移動，但只擋一下</h2>
 * <p>招呼要停下來才看得出來，所以吃 {@link Flag#MOVE} 與 {@link Flag#LOOK}。
 * 三秒之後就放掉——招呼不該讓一個 NPC 在路中間站到你走掉為止。
 */
public class EightNineGreetGoal extends Goal {

    /** 走到幾格內會被招呼。比講話的六格更近：招呼是擦身而過的距離。 */
    private static final double RANGE = 4.5;
    /** 招呼幾 tick。動作本身是三秒。 */
    private static final int DURATION = 60;
    /** 同一個對象幾 tick 內不再招呼。 */
    private static final int MEMORY = 400;
    /** 兩次招呼之間至少隔幾 tick。不管對象是誰。 */
    private static final int COOLDOWN = 100;
    /**
     * 沒找到人的時候隔幾 tick 再掃一次。
     *
     * <p>{@code canUse} 是**每 tick** 被呼叫的，而裡面是一個範圍查詢。街上站三十個 8+9
     * 就是每秒六百次查詢，而人不會在半秒之內走完四格半——掃這麼密只是白花。
     */
    private static final int SCAN_GAP = 10;

    private final EightNine self;
    private LivingEntity target;
    private UUID lastGreeted;
    private int lastGreetedAt = Integer.MIN_VALUE;
    private int cooldown;
    private int ticksLeft;

    public EightNineGreetGoal(EightNine self) {
        this.self = self;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        this.cooldown = SCAN_GAP;
        if (!(this.self.level() instanceof ServerLevel level)) return false;

        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                this.self.getBoundingBox().inflate(RANGE), this::worthGreeting);
        this.target = nearest(nearby);
        return this.target != null;
    }

    /**
     * 值得招呼的對象：玩家、村民（含流浪商人）、或另一個 8+9。
     *
     * <p>剛招呼過的那一位會被排除，而且是**在這裡**排除而不是等挑完再檢查：
     * 不然巷口只有他一個人的時候，每一次 {@code canUse} 都會挑中他再丟掉，
     * 旁邊真的該被招呼的村民永遠輪不到。
     */
    private boolean worthGreeting(LivingEntity other) {
        if (other == this.self || !other.isAlive()) return false;
        if (other instanceof Player player && player.isSpectator()) return false;
        if (!(other instanceof Player || other instanceof Npc || other instanceof EightNine)) return false;
        if (other.getUUID().equals(this.lastGreeted)
                && this.self.tickCount - this.lastGreetedAt < MEMORY) return false;
        return this.self.hasLineOfSight(other);
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticksLeft > 0 && this.target != null && this.target.isAlive();
    }

    @Override
    public void start() {
        this.ticksLeft = DURATION;
        this.lastGreeted = this.target.getUUID();
        this.lastGreetedAt = this.self.tickCount;
        this.self.getNavigation().stop();
        this.self.triggerGreet();

        // 招呼的那一句只講給聽得到的玩家。對著村民點頭的時候玩家也聽得到——
        // 那是刻意的：路過看到兩個 NPC 互相打招呼，比只有你被搭話更像一條真的街
        if (this.self.level() instanceof ServerLevel level) {
            Component message = Component.literal(
                    "<8+9> " + this.self.variant().greeting(this.self.getRandom()));
            for (Player player : level.getEntitiesOfClass(Player.class,
                    this.self.getBoundingBox().inflate(12.0), player -> !player.isSpectator())) {
                player.sendSystemMessage(message);
            }
        }
    }

    @Override
    public void tick() {
        this.ticksLeft--;
        this.self.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
    }

    @Override
    public void stop() {
        this.target = null;
        this.cooldown = COOLDOWN;
    }

    /** 挑最近的那一個。該不該招呼已經在 {@link #worthGreeting} 篩過了，這裡只管距離。 */
    private LivingEntity nearest(List<LivingEntity> candidates) {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double distance = this.self.distanceToSqr(candidate);
            if (distance < bestDist) {
                bestDist = distance;
                best = candidate;
            }
        }
        return best;
    }
}
