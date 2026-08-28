package com.xinbow99.taiwan.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 聚落層的門面：**「這一柱有沒有城鎮、地面被整到哪、上面該蓋什麼」全部從這裡問。**
 *
 * <h2>為什麼要有快取</h2>
 * <p>{@link Town#at} 要算一次地形高度、一次河、還有五次高度取樣算坡度——七次地形雜訊。
 * 而判斷「這一柱屬於哪座聚落」得掃 3×3 個網格，等於一柱 63 次。一個區塊 256 柱，
 * 那是一萬六千次多餘的雜訊——地形本身才 256 次。
 *
 * <p>所以用網格座標當鍵快取整座聚落。{@link Town#at} 是純函數，快取不會改變結果，
 * 只是把「同一格被問幾百次」壓成一次。
 *
 * <h2>為什麼掃 3×3 就夠</h2>
 * <p>{@link Town#reach} 上限是 148 格，而網格邊長預設 384。一座聚落最遠只能碰到隔壁那一格，
 * 碰不到隔了兩格的。掃 5×5 是白花錢，掃 1×1 會讓跨越網格邊界的聚落被切掉一半——
 * 而那個切面是一道垂直的斷崖。
 */
public final class Urban {

    /** 一柱的方塊寫入。由生成器提供，因為只有它知道區塊的高度上下限。 */
    public interface Cursor {
        void set(int y, BlockState state);

        /**
         * 放一塊有字的招牌。
         *
         * <p>要另開一個方法而不是塞進 {@link #set}，是因為招牌是 block entity：光有方塊狀態
         * 沒有字。而字是這整個模組裡最便宜也最有效的一招——一塊彩色的板子只是一塊板子，
         * 上面有「陸拾嵐」四個字才是台灣。
         */
        default void sign(int y, BlockState state, DyeColor color, String... lines) {
            set(y, state);
        }
    }

    /** 街廓的用途。 */
    public enum Lot {
        /** 沿街的連棟街屋（含一樓的飲料店與超商）。 */
        STREET,
        /** 宮廟與廟埕。 */
        TEMPLE,
        /** 傳統市場／夜市。 */
        MARKET
    }

    private static final Object NONE = new Object();
    private static final ConcurrentHashMap<Long, Object> CELLS = new ConcurrentHashMap<>();
    private static volatile int cachedSalt;

    private Urban() {
    }

    /** 這一柱所屬的聚落，沒有就 {@code null}。 */
    public static Town town(int x, int z, Settings s, int salt) {
        int cx = Math.floorDiv(x, s.cell());
        int cz = Math.floorDiv(z, s.cell());

        Town best = null;
        long bestDist = Long.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Town t = cell(cx + dx, cz + dz, s, salt);
                if (t == null) continue;
                long ox = x - t.centerX();
                long oz = z - t.centerZ();
                long d2 = ox * ox + oz * oz;
                long reach = t.reach();
                if (d2 > reach * reach) continue;
                if (d2 < bestDist) {
                    bestDist = d2;
                    best = t;
                }
            }
        }
        return best;
    }

    private static Town cell(int cellX, int cellZ, Settings s, int salt) {
        // 換了世界（或換了種子）就整份丟掉。比在鍵裡塞 salt 便宜，而且同一個 JVM 裡
        // 通常只有一個世界在生成
        if (salt != cachedSalt) {
            CELLS.clear();
            cachedSalt = salt;
        }
        long key = ((long) cellX << 32) | (cellZ & 0xFFFFFFFFL);
        Object hit = CELLS.get(key);
        if (hit == null) {
            Town t = Town.at(cellX, cellZ, s, salt);
            hit = t == null ? NONE : t;
            if (CELLS.size() > 1 << 14) CELLS.clear();
            CELLS.put(key, hit);
        }
        return hit == NONE ? null : (Town) hit;
    }

    /**
     * 這一柱的地面高度，**已經算進聚落的整平**。
     *
     * <p>{@link Terrain#height} 是「自然地形」，這裡是「玩家會站上去的地面」。生成器的三個
     * 入口（填方塊、高度圖、柱體取樣）全部要問這一個，問前者的話高度圖會停在整平前的地形上，
     * 而症狀是玩家走進城鎮就掉進地板。
     */
    public static int ground(int x, int z, Settings s, int salt) {
        int raw = Terrain.height(x, z, s, salt);
        Town t = town(x, z, s, salt);
        return t == null ? raw : ground(x, z, raw, t);
    }

    /** 已經知道聚落的版本，省一次查詢。 */
    public static int ground(int x, int z, int raw, Town t) {
        float p = t.pad(x, z);
        if (p <= 0f) return raw;
        return Math.round(Noise.lerp(raw, t.baseY(), p));
    }

    /**
     * 這個街廓拿來做什麼；{@code null} ＝ 空地。
     *
     * <p>權重直接來自 {@link Settings.Buildings}，所以**把某一類的權重設成 0 就是關掉它**
     * ——這是設定檔開關那條需求的落點。宮廟權重 1 配上其他共 15，一座聚落大約十幾個街廓，
     * 剛好是「一個鎮一間廟」。
     */
    public static Lot lot(Town t, int lx, int lz, Settings s) {
        Settings.Buildings b = s.buildings();
        int street = b.tenement() + b.drinkShop() + b.convenience();
        int total = street + b.temple() + b.market();
        if (total <= 0) return null;

        int roll = Math.floorMod(t.lotSalt(lx, lz) >> 11, total);
        if (roll < b.temple()) return Lot.TEMPLE;
        roll -= b.temple();
        if (roll < b.market()) return Lot.MARKET;
        return street > 0 ? Lot.STREET : null;
    }

    /**
     * 地坪那一格該鋪什麼；{@code null} ＝ 用自然地表。
     *
     * <p>街道是柏油，其他是水泥。這一格在**整平後的地面高度**上，建築從它上面一格起算。
     */
    public static BlockState surface(int x, int z, Town t) {
        if (!t.paved(x, z)) return null;
        if (!t.road(x, z)) return Palette.PAVEMENT;

        // 分向線畫在中線上，兩格白兩格黑。實線的話路會讀成一條跑道
        boolean northSouth = t.depthX(x) < 0;
        if (northSouth && t.lineDistX(x) == 0 && t.depthZ(z) >= 0) {
            return Math.floorMod(z, 4) < 2 ? Palette.ROAD_LINE : Palette.ASPHALT;
        }
        if (!northSouth && t.lineDistZ(z) == 0 && t.depthX(x) >= 0) {
            return Math.floorMod(x, 4) < 2 ? Palette.ROAD_LINE : Palette.ASPHALT;
        }
        // 街緣的禁停線。它同時是騎樓與馬路的分界，人一眼就知道哪裡可以走
        if (t.depthX(x) == -1 || t.depthZ(z) == -1) return Palette.CURB_LINE;
        return Palette.ASPHALT;
    }

    /**
     * 把這一柱的聚落內容寫上去，從 {@code base + 1} 開始往上。
     *
     * @param base 整平後的地面高度（街道的高度）
     */
    public static void build(int x, int z, int base, Town t, Settings s, int salt, Cursor out) {
        if (!t.paved(x, z)) return;

        int lx = t.lotX(x);
        int lz = t.lotZ(z);
        Lot kind = lot(t, lx, lz, s);

        if (t.road(x, z)) {
            Street.build(x, z, base, t, out);
            // 街道上唯一會有東西的是**隔壁店家的突出招牌**。這是台灣街景的一半，
            // 而它照定義就是伸出騎樓外緣、蓋在馬路上空的
            if (kind == Lot.STREET && t.lotPaved(lx, lz)) {
                Shophouse.build(x, z, base, t, s, out);
            }
            return;
        }

        if (kind == null || !t.lotPaved(lx, lz)) return;

        switch (kind) {
            case STREET -> Shophouse.build(x, z, base, t, s, out);
            case TEMPLE -> Temple.build(x, z, base, t, lx, lz, out);
            case MARKET -> Market.build(x, z, base, t, lx, lz, out);
        }
    }

    /**
     * 這個區塊要不要跳過原版的生態系裝飾（樹、草、花、湖）。
     *
     * <h3>為什麼非跳不可</h3>
     * <p>原版的 feature 是照 {@code WORLD_SURFACE_WG} 高度圖放的，而在城鎮裡那個高度是**屋頂**。
     * 不擋的話樹會長在四樓的頂樓加蓋上，草會鋪滿柏油路。那不是小瑕疵，是一眼就毀掉整條街。
     *
     * <p>代價是城鎮底下也沒有礦脈了——礦跟樹在原版是同一個裝飾階段，這個介面沒有分開的餘地。
     * 「城鎮正下方挖不到礦」比「屋頂長樹」好得多，而且只影響聚落核心那幾個區塊。
     */
    public static boolean suppressDecoration(int chunkX, int chunkZ, Settings s, int salt) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        Town t = town(x, z, s, salt);
        return t != null && t.pad(x, z) > 0.5f;
    }

    // ------------------------------------------------------------------ 方向工具

    /** 從「街在哪一側」換成「面向街的方向」。 */
    public static Direction faceToStreet(boolean alongZ, int side) {
        if (alongZ) return side > 0 ? Direction.WEST : Direction.EAST;
        return side > 0 ? Direction.NORTH : Direction.SOUTH;
    }
}
