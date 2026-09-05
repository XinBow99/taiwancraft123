package com.xinbow99.taiwan;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.xinbow99.taiwan.entity.EightNine;
import com.xinbow99.taiwan.entity.EightNineVariant;
import com.xinbow99.taiwan.entity.TaiwanEntities;
import net.minecraft.world.entity.EntitySpawnReason;
import com.mojang.brigadier.CommandDispatcher;
import com.xinbow99.taiwan.worldgen.Settings;
import com.xinbow99.taiwan.worldgen.TaiwanChunkGenerator;
import com.xinbow99.taiwan.worldgen.Town;
import com.xinbow99.taiwan.worldgen.Urban;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * {@code /taiwan locate <town|temple|market>}：找出最近的聚落或指定建築。
 *
 * <h2>為什麼可以用掃的，而不需要像原版那樣存一份索引</h2>
 * <p>原版的 {@code /locate} 要查結構物的索引，因為結構物是生成時才決定、決定完就寫進存檔的。
 * 這裡的聚落選址是**座標的純函數**——{@link Town#at} 只吃網格座標與世界的 salt，
 * 不需要那個區塊生成過。所以「最近的廟在哪」可以直接算，連地圖都不用先跑過。
 *
 * <p>這也是為什麼這個指令比原版的 {@code /locate} 快：它完全不碰硬碟。
 *
 * <h2>為什麼需要這個指令</h2>
 * <p>聚落在 384 格的網格上、只有 42% 的格子有、還要地夠平海拔夠低；而廟是十六分之一的街廓。
 * 沒有指令的話，找一間廟得用飛的掃過好幾平方公里——那不是遊戲，那是苦工。
 */
public final class TaiwanCommands {

    /** {@code /taiwan gather} 不帶參數時放幾個。要大於成團門檻，不然看不出效果。 */
    private static final int GATHER_DEFAULT = 5;

    /** 最多往外掃幾圈網格。12 圈 ＝ 半徑約 4600 格，夠遠了。 */
    private static final int RINGS = 12;

    private TaiwanCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("taiwan")
                .then(Commands.literal("locate")
                        .then(Commands.literal("town")
                                .executes(ctx -> locate(ctx.getSource(), null)))
                        .then(Commands.literal("temple")
                                .executes(ctx -> locate(ctx.getSource(), Urban.Lot.TEMPLE)))
                        .then(Commands.literal("market")
                                .executes(ctx -> locate(ctx.getSource(), Urban.Lot.MARKET))))
                .then(Commands.literal("gather")
                        .executes(ctx -> gather(ctx.getSource(), 5))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                                .executes(ctx -> gather(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "count"))))));
    }

    /**
     * 在腳邊放一團 8+9。
     *
     * <h3>為什麼需要這個指令</h3>
     * <p>8+9 是**在區塊第一次生成時**放的（跟違停機車同一個時機）。已經探索過的聚落
     * 不會回填——那些區塊早就生成完了。而 {@code MobCategory.CREATURE} 之後幾乎不會
     * 自然補生（原版的動物也是這樣，殺光就不會再有）。
     *
     * <p>所以要驗證這個實體，要嘛去沒去過的聚落，要嘛用這個指令。一次放 {@value #GATHER_DEFAULT}
     * 個是刻意的：成團門檻是三個人，放一個看不出這個實體真正的樣子。
     */
    private static int gather(CommandSourceStack source, int count) {
        ServerLevel level = source.getLevel();
        Vec3 at = source.getPosition();
        EightNineVariant look = EightNineVariant.random(level.getRandom());

        int placed = 0;
        for (int i = 0; i < count; i++) {
            EightNine guy = TaiwanEntities.EIGHTNINE.create(level, EntitySpawnReason.COMMAND);
            if (guy == null) continue;
            // 散在三格內，不是疊在同一格
            double dx = (level.getRandom().nextDouble() - 0.5) * 5.0;
            double dz = (level.getRandom().nextDouble() - 0.5) * 5.0;
            guy.setVariant(look);
            guy.snapTo(at.x + dx, at.y, at.z + dz, level.getRandom().nextFloat() * 360f, 0f);
            if (level.addFreshEntity(guy)) placed++;
        }

        int done = placed;
        source.sendSuccess(() -> Component.literal(
                "放了 " + done + " 個 8+9（" + look.getSerializedName() + "）"
                        + (done >= EightNine.CROWD ? "　夠成團，等一下就會放歌" : "　不夠三個，不會成團")), false);
        return placed;
    }

    private static int locate(CommandSourceStack source, Urban.Lot want) {
        ServerLevel level = source.getLevel();
        if (!(level.getChunkSource().getGenerator() instanceof TaiwanChunkGenerator generator)) {
            source.sendFailure(Component.literal("這個維度不是用台灣生成器產生的"));
            return 0;
        }

        Settings s = generator.settings();
        int salt = generator.salt(level.getChunkSource().randomState());
        Vec3 from = source.getPosition();
        int originX = Math.floorDiv((int) from.x, s.cell());
        int originZ = Math.floorDiv((int) from.z, s.cell());

        // 一圈一圈往外掃。同一圈裡再比實際距離，因為網格是方的而距離是圓的
        for (int ring = 0; ring <= RINGS; ring++) {
            Hit best = null;
            for (int dz = -ring; dz <= ring; dz++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    // 只掃這一圈新增的那一環，內圈上一輪已經看過了
                    if (ring > 0 && Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;

                    Town town = Town.at(originX + dx, originZ + dz, s, salt);
                    if (town == null) continue;

                    Hit hit = want == null ? townHit(town, from) : lotHit(town, want, s, from);
                    if (hit == null) continue;
                    if (best == null || hit.dist < best.dist) best = hit;
                }
            }
            if (best != null) {
                report(source, best, want);
                return 1;
            }
        }

        source.sendFailure(Component.literal(
                "附近 " + (RINGS * s.cell()) + " 格內找不到"
                        + (want == null ? "聚落" : want == Urban.Lot.TEMPLE ? "宮廟" : "市場")
                        + "。往平原或河口方向再試一次。"));
        return 0;
    }

    private static Hit townHit(Town town, Vec3 from) {
        return new Hit(town.centerX(), town.baseY(), town.centerZ(),
                dist(from, town.centerX(), town.centerZ()), town);
    }

    /**
     * 這座聚落裡最近的目標街廓。
     *
     * <p>掃的範圍由半徑推出來，多掃一圈當緩衝。要問 {@link Town#lotPaved}——沒整平的街廓
     * 是不會蓋東西的，報一個空地的座標比說找不到更糟。
     */
    private static Hit lotHit(Town town, Urban.Lot want, Settings s, Vec3 from) {
        int span = town.reach() / town.pitch() + 1;
        Hit best = null;
        for (int lz = -span; lz <= span; lz++) {
            for (int lx = -span; lx <= span; lx++) {
                if (Urban.lot(town, lx, lz, s) != want) continue;
                if (!town.lotPaved(lx, lz)) continue;

                int cx = (town.lotMinX(lx) + town.lotMaxX(lx)) / 2;
                int cz = (town.lotMinZ(lz) + town.lotMaxZ(lz)) / 2;
                double d = dist(from, cx, cz);
                if (best == null || d < best.dist) {
                    best = new Hit(cx, town.baseY(), cz, d, town);
                }
            }
        }
        return best;
    }

    private static double dist(Vec3 from, int x, int z) {
        double dx = from.x - x;
        double dz = from.z - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static void report(CommandSourceStack source, Hit hit, Urban.Lot want) {
        String what = want == null ? "聚落" : want == Urban.Lot.TEMPLE ? "宮廟" : "市場";
        source.sendSuccess(() -> Component.literal(
                "最近的" + what + "：[" + hit.x + ", " + hit.y + ", " + hit.z + "]"
                        + "　距離 " + Math.round(hit.dist) + " 格"
                        + "　/tp @s " + hit.x + " " + (hit.y + 2) + " " + hit.z), false);
        if (want != null) {
            source.sendSuccess(() -> Component.literal(
                    "所屬聚落中心：[" + hit.town.centerX() + ", " + hit.town.baseY()
                            + ", " + hit.town.centerZ() + "]"), false);
        }
    }

    private record Hit(int x, int y, int z, double dist, Town town) {
    }
}
