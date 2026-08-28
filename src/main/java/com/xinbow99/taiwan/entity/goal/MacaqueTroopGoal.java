package com.xinbow99.taiwan.entity.goal;

import com.xinbow99.taiwan.entity.Macaque;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * 跟上猴群。
 *
 * <h2>不需要「群長」</h2>
 * <p>每一隻只做一件事：離最近的同伴太遠就走過去。沒有領袖、沒有隊形、沒有共享狀態——
 * 但整群看起來就是一起移動的。這是最便宜的群體行為，而且**不會有領袖死掉群就散掉**
 * 這種問題。
 *
 * <p>門檻用兩個數字（跟過去的距離大於停下來的距離），否則猴子會在邊界上前後抖動：
 * 走近一格就滿足了、下一 tick 又不滿足。這跟 Phase 3 陣頭少年的 courage 要 hysteresis
 * 是同一件事。
 */
public class MacaqueTroopGoal extends Goal {

    /** 超過這個距離就去追同伴。 */
    private static final double LEAVE = 9.0;
    /** 靠到這個距離就停。比 LEAVE 小，中間那段是遲滯區。 */
    private static final double ARRIVE = 5.0;
    /** 找同伴的半徑。 */
    private static final double SEARCH = 20.0;

    private final Macaque macaque;
    private Macaque friend;
    private int recheck;

    public MacaqueTroopGoal(Macaque macaque) {
        this.macaque = macaque;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // 手上有贓物的時候不跟群走：那時候的任務是逃跑，跟同伴會合等於把苦主帶回家
        if (macaque.hasLoot()) return false;
        if (recheck-- > 0) return false;
        recheck = 20;

        this.friend = nearest();
        return friend != null && macaque.distanceToSqr(friend) > LEAVE * LEAVE;
    }

    @Override
    public boolean canContinueToUse() {
        return friend != null && friend.isAlive() && !macaque.hasLoot()
                && macaque.distanceToSqr(friend) > ARRIVE * ARRIVE;
    }

    @Override
    public void start() {
        macaque.getNavigation().moveTo(friend, 1.05);
    }

    @Override
    public void stop() {
        this.friend = null;
        macaque.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (friend == null) return;
        if (macaque.tickCount % 10 == 0) {
            macaque.getNavigation().moveTo(friend, 1.05);
        }
    }

    /**
     * 最近的同伴。
     *
     * <p>自己比距離而不是用 {@code Level} 的輔助方法：那些方法的簽章每個版本都在動，
     * 而這裡要的邏輯只有三行。
     */
    private Macaque nearest() {
        Macaque best = null;
        double bestDist = Double.MAX_VALUE;
        for (Macaque other : macaque.level().getEntitiesOfClass(Macaque.class,
                macaque.getBoundingBox().inflate(SEARCH),
                candidate -> candidate != macaque && candidate.isAlive())) {
            double d = macaque.distanceToSqr(other);
            if (d < bestDist) {
                bestDist = d;
                best = other;
            }
        }
        return best;
    }
}
