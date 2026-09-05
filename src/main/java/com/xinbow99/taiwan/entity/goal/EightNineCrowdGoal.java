package com.xinbow99.taiwan.entity.goal;

import com.xinbow99.taiwan.entity.EightNine;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * 湊過去。
 *
 * <h2>跟猴子同一套，但門檻不一樣</h2>
 * <p>結構跟 {@link MacaqueTroopGoal} 一樣：每個人只做一件事——離最近的同伴太遠就走過去。
 * 沒有領袖、沒有隊形、沒有共享狀態，但整團看起來就是一起移動的，而且領袖死掉群也不會散。
 *
 * <p>不一樣的是**距離**：猴子那組是 9 / 5 格（動物的鬆散群），這裡是 6 / 2.5 格。
 * 人擠人才叫一掛人——散開九格就只是六個路人剛好走在同一條街上。
 *
 * <p>兩個門檻（跟過去的距離大於停下來的距離）之間是遲滯區，否則走到邊界上會前後抖動。
 * 這跟 {@link EightNine} 的成團判定是同一個道理，只是那個管狀態、這個管移動。
 */
public class EightNineCrowdGoal extends Goal {

    /** 超過這個距離就湊過去。 */
    private static final double LEAVE = 6.0;
    /** 靠到這個距離就停。比 LEAVE 小，中間那段是遲滯區。 */
    private static final double ARRIVE = 2.5;
    /** 找同伴的半徑。 */
    private static final double SEARCH = 24.0;

    private final EightNine self;
    private EightNine friend;
    private int recheck;

    public EightNineCrowdGoal(EightNine self) {
        this.self = self;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // 正在打架、或已經騎在車上，就不用走路去找同伴了
        if (self.getTarget() != null || self.isPassenger()) return false;
        if (recheck-- > 0) return false;
        recheck = 20;

        this.friend = nearest();
        return friend != null && self.distanceToSqr(friend) > LEAVE * LEAVE;
    }

    @Override
    public boolean canContinueToUse() {
        return friend != null && friend.isAlive() && self.getTarget() == null && !self.isPassenger()
                && self.distanceToSqr(friend) > ARRIVE * ARRIVE;
    }

    @Override
    public void start() {
        self.getNavigation().moveTo(friend, 1.0);
    }

    @Override
    public void stop() {
        this.friend = null;
        self.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (friend == null) return;
        // 目標會動，每半秒重下一次路徑
        if (self.tickCount % 10 == 0) {
            self.getNavigation().moveTo(friend, 1.0);
        }
    }

    /**
     * 最近的同伴。
     *
     * <p>自己比距離而不是用 {@code Level} 的輔助方法：那些方法的簽章每個版本都在動，
     * 而這裡要的邏輯只有三行。
     */
    private EightNine nearest() {
        EightNine best = null;
        double bestDist = Double.MAX_VALUE;
        for (EightNine other : self.level().getEntitiesOfClass(EightNine.class,
                self.getBoundingBox().inflate(SEARCH),
                candidate -> candidate != self && candidate.isAlive())) {
            double d = self.distanceToSqr(other);
            if (d < bestDist) {
                bestDist = d;
                best = other;
            }
        }
        return best;
    }
}
