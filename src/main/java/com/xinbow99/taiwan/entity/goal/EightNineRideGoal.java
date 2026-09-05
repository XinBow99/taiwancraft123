package com.xinbow99.taiwan.entity.goal;

import com.xinbow99.taiwan.entity.EightNine;
import com.xinbow99.taiwan.entity.Scooter;
import com.xinbow99.taiwan.worldgen.Palette;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * 去牽一台車。
 *
 * <h2>只牽沒有車主的</h2>
 * <p>{@link Scooter#owner()} 是空的才碰。玩家騎過的車就有主人了——路邊那排違停是無主的，
 * 那才是他們會牽走的。少了這個判斷，你停在門口的車會被騎走，那是 bug 不是特色。
 *
 * <h2>成團才會想騎</h2>
 * <p>一個人不會沒事去牽車，一群人才會「走啦，騎車去」。所以落單的機率低很多——
 * 這跟講話、跟站姿是同一個機制（見 {@link EightNine}）的第三個出口。
 */
public class EightNineRideGoal extends Goal {

    /** 找車的半徑。 */
    private static final double SEARCH = 20.0;
    /** 騎得上去的距離。 */
    private static final double MOUNT = 2.0;
    /** 多久重新想一次要不要騎車。 */
    private static final int RETHINK = 60;

    private final EightNine self;
    private Scooter target;
    private int cooldown;

    public EightNineRideGoal(EightNine self) {
        this.self = self;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (self.isPassenger() || self.getTarget() != null) return false;
        if (--cooldown > 0) return false;
        cooldown = RETHINK;

        // 成團時一成五機率想騎，落單只有三厘。人多才會揪。
        // 三成試過，一團裡幾乎每個人都會去牽車，整條街變成停車場搬家
        float urge = self.inCrowd() ? 0.15f : 0.03f;
        if (self.getRandom().nextFloat() > urge) return false;

        this.target = freeScooter();
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && target.getPassengers().isEmpty()
                && !self.isPassenger() && self.getTarget() == null;
    }

    @Override
    public void start() {
        self.getNavigation().moveTo(target, 1.15);
    }

    @Override
    public void stop() {
        this.target = null;
        self.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) return;
        if (self.distanceToSqr(target) < MOUNT * MOUNT) {
            self.startRiding(target);
            return;
        }
        if (self.tickCount % 10 == 0) self.getNavigation().moveTo(target, 1.15);
    }

    /**
     * 這台車停在鋪面上嗎。
     *
     * <p>停在草地或別人院子裡的車不牽——**騎上去的第一件事就是把車開出去**，
     * 而 {@link EightNineCruiseGoal} 是沿著鋪面探路的：起點不在路上的話，
     * 它探不到任何方向，人就會坐在車上不動，看起來像壞掉。
     */
    private static boolean onPaving(Scooter scooter) {
        for (int dy = 0; dy >= -2; dy--) {
            BlockPos pos = scooter.blockPosition().offset(0, dy - 1, 0);
            if (Palette.isPaving(scooter.level().getBlockState(pos))) return true;
        }
        return false;
    }

    /** 最近的一台無主、沒人騎的車。 */
    private Scooter freeScooter() {
        Scooter best = null;
        double bestDist = Double.MAX_VALUE;
        for (Scooter scooter : self.level().getEntitiesOfClass(Scooter.class,
                self.getBoundingBox().inflate(SEARCH),
                candidate -> candidate.isAlive()
                        && candidate.getPassengers().isEmpty()
                        && candidate.owner().isEmpty()
                        && onPaving(candidate))) {
            double d = self.distanceToSqr(scooter);
            if (d < bestDist) {
                bestDist = d;
                best = scooter;
            }
        }
        return best;
    }
}
