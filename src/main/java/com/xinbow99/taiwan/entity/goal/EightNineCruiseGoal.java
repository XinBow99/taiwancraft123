package com.xinbow99.taiwan.entity.goal;

import com.xinbow99.taiwan.entity.EightNine;
import com.xinbow99.taiwan.entity.RoadVehicle;
import com.xinbow99.taiwan.worldgen.Palette;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/**
 * 騎上車之後怎麼開。
 *
 * <h2>沿著路開，不是朝著目標開</h2>
 * <p>第一版是「算出目標方向、打對應的舵」——結果他們直接騎進房子裡卡住。目標方向
 * 完全不知道路在哪，而機車沒有導航（{@code PathNavigation} 管的是走路的那具身體，
 * 不是車）。
 *
 * <p>現在的作法是**探路**：每隔幾 tick 往前方扇形的九個方向各探一次，看哪個方向的
 * 鋪面延伸得最遠，就往那邊打舵。路口會自然出現兩三個等長的選項，這時候才用「離頭多近」
 * 去挑——所以他們會沿著街跑、在路口轉彎，而不是筆直撞牆。
 *
 * <p>探不到路就收油門停下來。卡在牆上空催油門比停著更難看，而且會把車卡進方塊裡。
 *
 * <h2>成團＝飆車，落單＝晃</h2>
 * <p>這是「群膽」機制在騎車上的出口：一個人騎車就是三成油門慢慢晃，一群人一起騎
 * 就是全油門追著前車。同一個實體、同一台車，差別只在旁邊有沒有人。
 *
 * <h2>沒有隊形，只有「跟著前面那個」</h2>
 * <p>車隊的頭是**團裡實體 id 最小的那個騎士**——跟放音樂用同一條規則，理由也一樣：
 * 不需要協調、不用同步、頭走掉之後下一個自然接手。其他人只是在挑方向時偏向頭的那一邊。
 */
public class EightNineCruiseGoal extends Goal {

    /** 探路的扇形半角（度）。超過這個角度的路口就當作沒看到——迴轉不是騎車該做的事。 */
    private static final float FAN = 75f;
    /** 扇形切幾個方向。九個 ≒ 每 18.75 度一條，夠細到認得出路口。 */
    private static final int RAYS = 9;
    /** 每條射線最遠探幾格。 */
    private static final int REACH = 20;
    /** 探路的取樣間距（格）。街道最窄是三格，用 2 不會漏掉。 */
    private static final int STEP = 2;
    /** 幾 tick 重探一次路。每 tick 探是浪費——路不會動。 */
    private static final int RESCAN = 6;
    /** 夾角到幾度就打滿舵。 */
    private static final float FULL_LOCK = 40f;
    /** 找車隊成員的半徑。 */
    private static final double PACK = 40.0;
    /** 卡住幾 tick 就下車。連續沒動就是撞牆或陷在方塊裡了。 */
    private static final int STUCK_LIMIT = 80;

    private final EightNine self;
    private float chosenYaw;
    private int rescan;
    private int stuck;
    private double lastX;
    private double lastZ;

    public EightNineCruiseGoal(EightNine self) {
        this.self = self;
        // 不佔 MOVE：騎車的時候導航是關掉的，這個 goal 直接寫車的輸入
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        return self.getVehicle() instanceof RoadVehicle;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.chosenYaw = self.getVehicle().getYRot();
        this.rescan = 0;
        this.stuck = 0;
        this.lastX = self.getX();
        this.lastZ = self.getZ();
    }

    @Override
    public void stop() {
        if (self.getVehicle() instanceof RoadVehicle scooter) scooter.setAiInput(0f, 0f);
    }

    @Override
    public void tick() {
        if (!(self.getVehicle() instanceof RoadVehicle scooter)) return;

        // 卡住就下車。空催油門只會把車卡得更深
        double moved = Math.hypot(self.getX() - lastX, self.getZ() - lastZ);
        lastX = self.getX();
        lastZ = self.getZ();
        stuck = moved < 0.02 ? stuck + 1 : 0;
        if (stuck > STUCK_LIMIT) {
            scooter.setAiInput(0f, 0f);
            self.stopRiding();
            return;
        }

        if (--rescan <= 0) {
            rescan = RESCAN;
            chosenYaw = pickHeading(scooter);
        }

        float diff = Mth.wrapDegrees(chosenYaw - scooter.getYRot());
        float steer = Mth.clamp(diff / FULL_LOCK, -1f, 1f);

        // 成團全油門，落單三成
        float throttle = self.inCrowd() ? 1.0f : 0.3f;
        // 夾角太大先減速再轉。全速滿舵在這套物理下只會推出去（轉向不足），轉不進去
        if (Math.abs(diff) > 55f) throttle *= 0.35f;
        // 前面沒路就停。這是不撞牆的最後一道
        if (roadRun(scooter, scooter.getYRot()) < STEP * 2 && Math.abs(diff) < 20f) throttle = 0f;

        scooter.setAiInput(steer, throttle);
    }

    /**
     * 從前方扇形裡挑一個方向。
     *
     * <p>分數＝鋪面延伸的距離，再加上「朝向車隊的頭」的加分。所以直路上大家照著路走，
     * 路口才會因為加分而選擇轉向頭的那一邊——**路的形狀是主，跟車是次**。
     * 反過來的話就會為了追上頭而切過人家的客廳。
     */
    private float pickHeading(RoadVehicle scooter) {
        EightNine leader = packLeader();
        Double toLeader = null;
        if (leader != null && leader != self) {
            double dx = leader.getX() - scooter.getX();
            double dz = leader.getZ() - scooter.getZ();
            if (dx * dx + dz * dz > 4.0) {
                toLeader = (double) ((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f);
            }
        }

        float base = scooter.getYRot();
        float bestYaw = base;
        double bestScore = -1;
        for (int i = 0; i < RAYS; i++) {
            float yaw = base - FAN + (2 * FAN) * i / (RAYS - 1f);
            double score = roadRun(scooter, yaw);
            if (score <= 0) continue;
            // 直走小加分：等長的兩條路優先選不用轉的那條，車才不會在直路上蛇行
            score += (1.0 - Math.abs(Mth.wrapDegrees(yaw - base)) / FAN) * 1.5;
            if (toLeader != null) {
                double off = Math.abs(Mth.wrapDegrees((float) (yaw - toLeader)));
                score += (1.0 - Math.min(off, 180.0) / 180.0) * 6.0;
            }
            if (score > bestScore) {
                bestScore = score;
                bestYaw = yaw;
            }
        }
        return bestYaw;
    }

    /**
     * 沿著這個方向，鋪面連續延伸幾格。
     *
     * <p>垂直方向容忍 ±1 格：街道會有高低差，只看正下方一格的話，一個路緣就會被判成沒路。
     */
    private double roadRun(RoadVehicle scooter, float yaw) {
        Level level = scooter.level();
        double rad = yaw * Mth.DEG_TO_RAD;
        double dx = -Math.sin(rad);
        double dz = Math.cos(rad);
        int y = Mth.floor(scooter.getY());

        for (int d = STEP; d <= REACH; d += STEP) {
            int x = Mth.floor(scooter.getX() + dx * d);
            int z = Mth.floor(scooter.getZ() + dz * d);
            if (!paved(level, x, y, z)) return d - STEP;
        }
        return REACH;
    }

    private static boolean paved(Level level, int x, int y, int z) {
        for (int dy = 0; dy >= -2; dy--) {
            if (Palette.isPaving(level.getBlockState(new BlockPos(x, y + dy, z)))) return true;
        }
        return false;
    }

    /** 車隊的頭：附近所有「騎在車上的 8+9」裡實體 id 最小的那個。 */
    private EightNine packLeader() {
        EightNine best = null;
        for (EightNine other : self.level().getEntitiesOfClass(EightNine.class,
                self.getBoundingBox().inflate(PACK),
                candidate -> candidate.isAlive() && candidate.getVehicle() instanceof RoadVehicle)) {
            if (best == null || other.getId() < best.getId()) best = other;
        }
        return best;
    }
}
