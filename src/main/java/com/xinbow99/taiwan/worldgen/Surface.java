package com.xinbow99.taiwan.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 地表與地表以下的材質。
 *
 * <h2>一個海拔帶決定所有東西</h2>
 * <p>{@link Zone} 同時餵給這裡（鋪什麼方塊）跟 {@link TaiwanBiomeSource}（算什麼生態系）。
 * 兩邊各判斷一次的話，會出現「生態系說這裡是海，地上卻是草」——而那種錯誤只有在天空的
 * 顏色跟腳下的地不搭時才會被發現，很難查。
 *
 * <h2>不種草也不種樹</h2>
 * <p>這個維度用的是**原版的生態系**（{@code minecraft:plains} 等等），所以原版生態系裡
 * 那一整串 feature——樹、草、花、湖、礦脈——會照常跑。自己再種一次只會疊在上面，
 * 而且長得跟原版不一樣，看起來像兩套世界打架。
 *
 * <p>這也是選「用原版生態系」而不是「自己註冊生態系」的主要理由：礦跟樹是免費的。
 */
public final class Surface {

    /**
     * 海拔帶。
     *
     * <p>{@link #PEAK} 跟 {@link #MOUNTAIN} 分開只為了雪線：一條水平的雪線是高山唯一
     * 一眼就讀得出來的尺度參照。
     */
    public enum Zone {
        OCEAN, BEACH, RIVER, PLAIN, HILL, MOUNTAIN, PEAK
    }

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
    private static final BlockState ANDESITE = Blocks.ANDESITE.defaultBlockState();
    private static final BlockState TUFF = Blocks.TUFF.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState COARSE_DIRT = Blocks.COARSE_DIRT.defaultBlockState();
    private static final BlockState GRASS_BLOCK = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState PODZOL = Blocks.PODZOL.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState CLAY = Blocks.CLAY.defaultBlockState();
    private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();

    private Surface() {
    }

    /**
     * 這一柱屬於哪一帶。
     *
     * <p>順序有意義：水（海、河）優先於海拔，因為一條河可以流過任何海拔，而「河」這件事
     * 比「這裡是丘陵」重要——玩家看到的是河。
     */
    public static Zone zone(int x, int z, int height, Settings s, int salt) {
        int sea = s.seaLevel();
        float c = Terrain.continent(x, z, s, salt);

        if (height < sea) {
            // 河谷切到海平面以下也算河：河口那一段兩者本來就分不開，
            // 但「近岸的淺水」讀成海比讀成河合理
            return Terrain.riverCarve(x, z, s, salt) > 0.5f && c > Terrain.SEA_EDGE + 0.04f
                    ? Zone.RIVER : Zone.OCEAN;
        }
        if (Terrain.riverCarve(x, z, s, salt) > 0.45f && height <= sea + 4) return Zone.RIVER;
        if (height <= sea + 2 && c < Terrain.SEA_EDGE + 0.05f) return Zone.BEACH;

        int rise = height - sea;
        if (rise < 18) return Zone.PLAIN;
        if (rise < 52) return Zone.HILL;
        if (rise < 118) return Zone.MOUNTAIN;
        return Zone.PEAK;
    }

    /**
     * 地表那一格。
     *
     * <p>混合都用同一份雜訊切門檻，而不是各自擲：門檻是連續區間，交界因此自動變成漸層。
     * 分開擲的話每一種材質都有自己的邊界，疊在一起是雜亂而不是層次。
     */
    public static BlockState top(int x, int z, int height, Zone zone, Settings s, int salt) {
        float n = Noise.fbm(x, z, 28f, 2, salt ^ 0x7C1D);
        int sea = s.seaLevel();

        return switch (zone) {
            // 近岸是沙，深一點是泥與礫。黏土只在最深處，它是唯一會被誤認成人工的材質，
            // 所以要放在潛不下去的地方
            case OCEAN -> height > sea - 6 ? SAND : n < 0.38f ? CLAY : n < 0.72f ? GRAVEL : DIRT;
            case BEACH -> SAND;
            // 有水的河床是沙與礫，乾的溪谷是粗泥——台灣的河大半年是乾的礫石灘
            case RIVER -> height < sea ? (n < 0.55f ? SAND : GRAVEL) : (n < 0.6f ? GRAVEL : COARSE_DIRT);
            case PLAIN -> n < 0.12f ? COARSE_DIRT : GRASS_BLOCK;
            case HILL -> n < 0.30f ? PODZOL : GRASS_BLOCK;
            // 高山的森林界線：往上草地愈來愈少，最後只剩岩石。用海拔當門檻的一半、
            // 雜訊當另一半，界線才不會是一條等高線
            case MOUNTAIN -> {
                float bare = Noise.smoothstep(sea + 60f, sea + 112f, height);
                yield n < bare ? (n < bare * 0.5f ? STONE : GRAVEL) : (n < 0.5f ? PODZOL : GRASS_BLOCK);
            }
            case PEAK -> height > sea + 132 ? SNOW_BLOCK : n < 0.5f ? STONE : GRAVEL;
        };
    }

    /**
     * 地表底下第 {@code depth} 格（{@code depth} 從 1 起算）。
     *
     * <p>土層只有三四格，再下去就是岩盤：土層太厚的話山會變成一座土堆，挖進去看不到石頭，
     * 而礦脈全部埋在土裡看起來像 bug。
     */
    public static BlockState below(int x, int z, int y, int depth, Zone zone, Settings s, int salt) {
        int soil = switch (zone) {
            case OCEAN, RIVER, BEACH -> 4;
            case PLAIN -> 5;
            case HILL -> 3;
            case MOUNTAIN -> 2;
            case PEAK -> 1;
        };
        if (depth <= soil) {
            return switch (zone) {
                case BEACH, OCEAN -> SAND;
                case RIVER -> depth <= 2 ? SAND : DIRT;
                case PEAK, MOUNTAIN -> depth <= 1 ? COARSE_DIRT : rock(x, y, z, salt);
                default -> DIRT;
            };
        }
        return rock(x, y, z, salt);
    }

    /**
     * 岩盤。y=0 以下換板岩——原版就是這個高度，跟著它走，玩家的直覺才不會壞掉。
     *
     * <p>安山岩與凝灰岩的斑塊尺度訂在九格：比一個礦脈大、比一個山頭小。再大就變成分區塗裝，
     * 再小就變成雜點。
     */
    private static BlockState rock(int x, int y, int z, int salt) {
        BlockState base = y < 0 ? DEEPSLATE : STONE;
        if (y < 0) return base;
        float n = Noise.fbm(x + y * 0.5f, z - y * 0.5f, 9f, 2, salt ^ 0x2B0C);
        if (n > 0.78f) return TUFF;
        if (n > 0.63f) return ANDESITE;
        return base;
    }
}
