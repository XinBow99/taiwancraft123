package com.xinbow99.taiwan.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import com.xinbow99.taiwan.Taiwan;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 台灣島的地形。
 *
 * <h2>三個入口算的是同一件事</h2>
 * <p>{@link #fillFromNoise}（填方塊）、{@link #getBaseHeight}（高度圖）、
 * {@link #getBaseColumn}（結構物與傳送門取樣）必須給出一致的答案，所以它們共用
 * {@link Terrain#height} 這唯一一個「地面在哪」的來源。各自寫一份的話，高度圖跟實際方塊
 * 會慢慢對不上，而那種錯誤的症狀是玩家掉進地板或卡在半空。
 *
 * <h2>為什麼可以不管客戶端</h2>
 * <p>地形生成整條管線都不過網路：{@code LEVEL_STEM}（維度連同它的生成器）不在
 * {@code RegistryDataLoader.SYNCHRONIZED_REGISTRIES} 裡。客戶端收到的只有算好的方塊。
 * 生態系用的又全是原版的 id，所以 Phase 1 這一段**沒裝模組的客戶端也連得進來**。
 * （載具跟 NPC 一進來就不是了，那是 Phase 2 的事。）
 */
public class TaiwanChunkGenerator extends ChunkGenerator {

    public static final MapCodec<TaiwanChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
            Settings.CODEC.optionalFieldOf("settings", Settings.DEFAULT).forGetter(g -> g.settings)
    ).apply(i, TaiwanChunkGenerator::new));

    /** 地形雜訊的命名空間。同一個世界種子配同一個字串，得到的島永遠一樣。 */
    private static final Identifier TERRAIN = Identifier.parse("taiwan:terrain");

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();

    private final Settings settings;

    /**
     * 地形雜訊的鹽，從世界種子導出。
     *
     * <p>沒有它的話每一個世界的島都長得一模一樣。兩條執行緒同時算出來也沒關係，值一樣；
     * 先寫值再寫旗標，讀到旗標就一定讀得到值。
     */
    private volatile int salt;
    private volatile boolean saltReady;

    public TaiwanChunkGenerator(BiomeSource biomeSource, Settings settings) {
        super(biomeSource);
        this.settings = settings;
    }

    public Settings settings() {
        return settings;
    }

    public int salt(RandomState random) {
        if (!saltReady) {
            salt = random.getOrCreateRandomFactory(TERRAIN).at(0, 0, 0).nextInt();
            saltReady = true;
        }
        return salt;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /**
     * 生態系比方塊早算。
     *
     * <p>覆寫它只為了一件事：{@link TaiwanBiomeSource} 拿不到種子也拿不到參數，得在**第一次
     * 被查之前**收到。放在 {@code fillFromNoise} 太晚——那時候生態系已經填完了。
     */
    @Override
    public CompletableFuture<ChunkAccess> createBiomes(RandomState random, Blender blender,
                                                       StructureManager structures, ChunkAccess chunk) {
        if (getBiomeSource() instanceof TaiwanBiomeSource source) {
            source.bind(settings, salt(random));
        }
        return super.createBiomes(random, blender, structures, chunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random,
                                                        StructureManager structures, ChunkAccess chunk) {
        int x0 = chunk.getPos().getMinBlockX();
        int z0 = chunk.getPos().getMinBlockZ();
        int floor = chunk.getMinY();
        int roof = floor + chunk.getHeight() - 1;
        int sea = settings.seaLevel();
        int salt = salt(random);

        Heightmap ocean = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        ColumnCursor column = new ColumnCursor(chunk, ocean, surface, cursor, floor, roof, x0, z0);

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = x0 + lx;
                int wz = z0 + lz;

                // 聚落會把地整平，所以「地面在哪」要問 Urban 而不是 Terrain。
                // 問後者的話高度圖會停在整平前的地形上，玩家走進城鎮就掉進地板
                Town town = Urban.town(wx, wz, settings, salt);
                int natural = Terrain.height(wx, wz, settings, salt);
                int top = Math.clamp(town == null ? natural : Urban.ground(wx, wz, natural, town),
                        floor + 1, roof);
                Surface.Zone zone = Surface.zone(wx, wz, top, settings, salt);

                put(chunk, ocean, surface, cursor, lx, floor, lz, BEDROCK);
                for (int y = floor + 1; y < top; y++) {
                    put(chunk, ocean, surface, cursor, lx, y, lz,
                            Surface.below(wx, wz, y, top - y, zone, settings, salt));
                }

                BlockState paving = town == null ? null : Urban.surface(wx, wz, town);
                put(chunk, ocean, surface, cursor, lx, top, lz,
                        paving != null ? paving : Surface.top(wx, wz, top, zone, settings, salt));

                // 水面是**絕對高度**，所以每一片水都是平的。跟著地形起伏的話那不是水，
                // 是一層藍色的漆
                for (int y = top + 1; y <= Math.min(sea, roof); y++) {
                    put(chunk, ocean, surface, cursor, lx, y, lz, WATER);
                }

                if (town != null) {
                    column.at(lx, lz);
                    Urban.build(wx, wz, top, town, settings, salt, column);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * 建築寫方塊的出口。
     *
     * <p>一個區塊只配一個，逐柱換座標：{@link Urban.Cursor} 如果做成 lambda，256 柱就是
     * 256 個物件，而區塊生成是這個模組最熱的路徑。
     *
     * <p>高度夾在區塊的上下限裡。**超出去的寫入原版會安靜丟掉**，症狀是山頂的房子少一層屋頂，
     * 而不是報錯——那種 bug 很難查，所以在這裡就擋掉。
     */
    private static final class ColumnCursor implements Urban.Cursor {
        private final ChunkAccess chunk;
        private final Heightmap ocean;
        private final Heightmap surface;
        private final BlockPos.MutableBlockPos cursor;
        private final int floor;
        private final int roof;
        private final int x0;
        private final int z0;
        private int lx;
        private int lz;

        ColumnCursor(ChunkAccess chunk, Heightmap ocean, Heightmap surface,
                     BlockPos.MutableBlockPos cursor, int floor, int roof, int x0, int z0) {
            this.chunk = chunk;
            this.ocean = ocean;
            this.surface = surface;
            this.cursor = cursor;
            this.floor = floor;
            this.roof = roof;
            this.x0 = x0;
            this.z0 = z0;
        }

        void at(int lx, int lz) {
            this.lx = lx;
            this.lz = lz;
        }

        @Override
        public void set(int y, BlockState state) {
            if (y <= floor || y > roof) return;
            put(chunk, ocean, surface, cursor, lx, y, lz, state);
        }

        /**
         * 招牌：方塊之外還要配一份 block entity 資料，字才存得下來。
         *
         * <h3>為什麼是 NBT 而不是直接建一個 {@code SignBlockEntity}</h3>
         * <p>{@code SignBlockEntity.setText} 會呼叫 {@code markUpdated()}，而那個方法**沒有
         * 檢查 {@code level} 是不是 null** 就直接 {@code level.sendBlockUpdated(...)}。
         * 世界生成階段根本還沒有 {@code Level}（手上只有 {@link ChunkAccess}），所以那條路
         * 必然 NPE。
         *
         * <p>{@link ChunkAccess#setBlockEntityNbt} 是原版自己給結構物用的延遲路徑：先把資料
         * 掛在區塊上，等區塊真的載入、block entity 真的被建出來時才套用。那時候 level 已經有了。
         *
         * <p>正反面寫同一份：招牌背後如果是別人家的牆，玩家永遠看不到背面；但如果是路口，
         * 兩面都看得到，而空白的那一面會很明顯。
         *
         * <p>{@code setHasGlowingText} 是關鍵：台灣的招牌到晚上是亮的。不發光的字入夜之後
         * 整條街會退回一排灰色的板子。
         */
        @Override
        public void sign(int y, BlockState state, DyeColor color, String... lines) {
            if (y <= floor || y > roof) return;
            put(chunk, ocean, surface, cursor, lx, y, lz, state);

            SignText text = new SignText().setColor(color).setHasGlowingText(true);
            for (int i = 0; i < lines.length && i < 4; i++) {
                text = text.setMessage(i, Component.literal(lines[i]));
            }
            Tag encoded = SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, text)
                    .resultOrPartial(Taiwan.LOGGER::error)
                    .orElse(null);
            if (encoded == null) return;

            CompoundTag tag = new CompoundTag();
            tag.putString("id", "minecraft:sign");
            tag.putInt("x", x0 + lx);
            tag.putInt("y", y);
            tag.putInt("z", z0 + lz);
            tag.put("front_text", encoded);
            tag.put("back_text", encoded);
            chunk.setBlockEntityNbt(tag);
        }
    }

    private static void put(ChunkAccess chunk, Heightmap ocean, Heightmap surface,
                            BlockPos.MutableBlockPos cursor, int lx, int y, int lz, BlockState state) {
        cursor.set(lx, y, lz);
        chunk.setBlockState(cursor, state);
        ocean.update(lx, y, lz, state);
        surface.update(lx, y, lz, state);
    }

    /**
     * 地表之上第一個空的高度。水面上的那一格也算空——原版的 {@code OCEAN_FLOOR} 與
     * {@code WORLD_SURFACE} 差別就在這裡，由呼叫端的 {@code type} 決定。
     */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        int salt = salt(random);
        int land = Urban.ground(x, z, settings, salt);
        int sea = settings.seaLevel();
        if (type == Heightmap.Types.WORLD_SURFACE || type == Heightmap.Types.WORLD_SURFACE_WG
                || type == Heightmap.Types.MOTION_BLOCKING || type == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES) {
            return Math.max(land, sea) + 1;
        }
        return land + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int floor = level.getMinY();
        int span = level.getHeight();
        int salt = salt(random);
        // 這裡只給地形，**不含建築**：這個入口是給結構物選址與傳送門用的，它們要問的是
        // 「地在哪」，不是「屋頂在哪」
        int land = Urban.ground(x, z, settings, salt);
        int sea = settings.seaLevel();
        Surface.Zone zone = Surface.zone(x, z, land, settings, salt);

        BlockState[] column = new BlockState[span];
        for (int i = 0; i < span; i++) {
            int y = floor + i;
            if (y == floor) {
                column[i] = BEDROCK;
            } else if (y < land) {
                column[i] = Surface.below(x, z, y, land - y, zone, settings, salt);
            } else if (y == land) {
                column[i] = Surface.top(x, z, land, zone, settings, salt);
            } else {
                column[i] = y <= sea ? WATER : AIR;
            }
        }
        return new NoiseColumn(floor, column);
    }

    /**
     * 出生高度。拿不到座標，所以回傳海平面上面一點——真正的出生點是原版用高度圖找的，
     * 這裡只是保險。
     */
    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return settings.seaLevel() + 8;
    }

    @Override
    public int getSeaLevel() {
        return settings.seaLevel();
    }

    @Override
    public int getMinY() {
        return settings.minY();
    }

    @Override
    public int getGenDepth() {
        return settings.worldHeight();
    }

    /**
     * 洞穴。**Phase 1 先留空**。
     *
     * <p>{@code applyCarvers} 在 {@link ChunkGenerator} 是抽象的，所以「沿用原版」不是一個選項
     * ——原版的實作在 {@code NoiseBasedChunkGenerator} 裡，而那需要一整套噪聲設定。
     * 要有洞穴就得自己跑一次生態系裡的 carver，那是獨立的一件事，不該跟地形綁在同一次驗收。
     */
    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random,
                             BiomeManager biomes, StructureManager structures, ChunkAccess chunk) {
    }

    /**
     * 表層處理。留空：{@link #fillFromNoise} 已經是最終樣貌。
     *
     * <p>原版是在這裡用 surface rule 把石頭換成草皮沙灘，但那套規則吃的是噪聲設定的資料，
     * 這個維度沒有。直接在填充時就決定材質比較單純，而且**只有一個地方決定地表**。
     */
    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures,
                             RandomState random, ChunkAccess chunk) {
    }

    /**
     * 生態系裝飾（樹、草、花、湖、礦脈）。**聚落核心整個跳過。**
     *
     * <p>原版的 feature 是照 {@code WORLD_SURFACE_WG} 高度圖放的，而在城鎮裡那個高度是屋頂。
     * 不擋的話樹會長在四樓的頂樓加蓋上、草會鋪滿柏油路。那不是小瑕疵，是一眼就毀掉整條街。
     *
     * <p>代價：聚落正下方也沒有礦脈了。礦跟樹在原版是同一個裝飾階段，這個介面沒有分開的餘地。
     * 「城鎮底下挖不到礦」比「屋頂長樹」好得多，而且只影響地坪覆蓋過半的那幾個區塊。
     */
    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structures) {
        ChunkPos pos = chunk.getPos();
        // salt 在 createBiomes 就設好了，而生態系一定比裝飾早跑。萬一沒有（不該發生），
        // 寧可讓原版照常裝飾，也不要因為讀到 0 而把整張地圖的樹都關掉
        if (saltReady && Urban.suppressDecoration(pos.getMinBlockX() >> 4, pos.getMinBlockZ() >> 4,
                settings, salt)) {
            return;
        }
        super.applyBiomeDecoration(level, chunk, structures);
    }

    /**
     * 區塊第一次生成時的那批動物。
     *
     * <p>照抄原版的作法：問這個區塊的生態系，讓 {@link NaturalSpawner} 依生態系的 spawner
     * 清單擲。這是「用原版生態系」的第二個好處——羊、牛、雞是免費的。
     */
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        ChunkPos pos = region.getCenter();
        Holder<Biome> biome = region.getBiome(pos.getWorldPosition().atY(region.getMaxY() - 1));
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        random.setDecorationSeed(region.getSeed(), pos.getMinBlockX(), pos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(region, biome, pos, random);
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState random, BlockPos pos) {
        int salt = salt(random);
        int h = Terrain.height(pos.getX(), pos.getZ(), settings, salt);
        lines.add("Taiwan: " + Surface.zone(pos.getX(), pos.getZ(), h, settings, salt)
                + " h=" + h
                + " c=" + String.format("%.3f", Terrain.continent(pos.getX(), pos.getZ(), settings, salt))
                + " river=" + String.format("%.2f", Terrain.riverCarve(pos.getX(), pos.getZ(), settings, salt)));
    }
}
