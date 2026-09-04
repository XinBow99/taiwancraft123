package com.xinbow99.taiwan.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * 生態系直接由地形決定：海拔幾格、是不是河、離海多遠。
 *
 * <h2>為什麼不用 multi_noise</h2>
 * <p>{@code multi_noise} 是拿一份**跟地形無關**的氣候雜訊去查表。原版能對得上，是因為原版的
 * 地形也是同一批密度函數算的。這個維度的地形是自己算的，硬接 multi_noise 等於讓兩份互不
 * 相干的雜訊各說各話——症狀是海底鋪著草地、山頂是沙灘。
 *
 * <p>直接問 {@link Terrain} 就沒有這個問題：生態系是地形的**函數**，不可能對不上。
 *
 * <h2>用原版的生態系</h2>
 * <p>這裡的 {@code Holder<Biome>} 全部指向 {@code minecraft:} 的生態系（見 world_preset）。
 * 這樣做有三個好處：原版生態系那一整串 feature（樹、草、礦脈、湖）照常跑；生物照常生成；
 * 而且**生態系註冊表是同步的**，指向原版的 id 就不會讓沒裝模組的客戶端對不上。
 *
 * <h2>種子</h2>
 * <p>{@link BiomeSource} 拿不到世界種子也拿不到生成器的參數（介面沒給），所以由
 * {@link TaiwanChunkGenerator#createBiomes} 在第一次被查之前塞進來。
 */
public class TaiwanBiomeSource extends BiomeSource {

    public static final MapCodec<TaiwanBiomeSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Biome.CODEC.fieldOf("ocean").forGetter(s -> s.ocean),
            Biome.CODEC.fieldOf("beach").forGetter(s -> s.beach),
            Biome.CODEC.fieldOf("river").forGetter(s -> s.river),
            Biome.CODEC.fieldOf("plain").forGetter(s -> s.plain),
            Biome.CODEC.fieldOf("farmland").forGetter(s -> s.farmland),
            Biome.CODEC.fieldOf("hill").forGetter(s -> s.hill),
            Biome.CODEC.fieldOf("mountain").forGetter(s -> s.mountain),
            Biome.CODEC.fieldOf("peak").forGetter(s -> s.peak)
    ).apply(i, TaiwanBiomeSource::new));

    private final Holder<Biome> ocean;
    private final Holder<Biome> beach;
    private final Holder<Biome> river;
    private final Holder<Biome> plain;
    private final Holder<Biome> farmland;
    private final Holder<Biome> hill;
    private final Holder<Biome> mountain;
    private final Holder<Biome> peak;

    /** 由生成器填進來，見類別說明。兩條執行緒同時寫進同一個值也無所謂。 */
    private volatile Settings settings;
    private volatile int salt;

    /**
     * 一柱一個答案。
     *
     * <p>{@link #getNoiseBiome} 是**逐個 y** 問的（一個區塊要問近一千次），而這裡的答案只跟
     * 平面位置有關——不快取的話同一柱的地形雜訊會被重算九十幾遍，而地形雜訊是這個模組
     * 最貴的東西。
     */
    private final ConcurrentHashMap<Long, Holder<Biome>> cache = new ConcurrentHashMap<>();

    public TaiwanBiomeSource(Holder<Biome> ocean, Holder<Biome> beach, Holder<Biome> river,
                             Holder<Biome> plain, Holder<Biome> farmland, Holder<Biome> hill,
                             Holder<Biome> mountain, Holder<Biome> peak) {
        this.ocean = ocean;
        this.beach = beach;
        this.river = river;
        this.plain = plain;
        this.farmland = farmland;
        this.hill = hill;
        this.mountain = mountain;
        this.peak = peak;
    }

    public void bind(Settings settings, int salt) {
        this.settings = settings;
        this.salt = salt;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(ocean, beach, river, plain, farmland, hill, mountain, peak);
    }

    /**
     * @param x 四分之一格座標（一格生態系＝ 4×4×4 個方塊），所以要左移兩位才是世界座標
     */
    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        // 還沒被 bind 就被問：只可能發生在生成器建好之前，回一個安全的答案而不是炸掉
        Settings s = settings;
        if (s == null) return plain;

        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
        Holder<Biome> hit = cache.get(key);
        if (hit != null) return hit;

        if (cache.size() > 1 << 16) cache.clear();
        Holder<Biome> rolled = pick(x << 2, z << 2, s);
        cache.put(key, rolled);
        return rolled;
    }

    private Holder<Biome> pick(int bx, int bz, Settings s) {
        int h = Terrain.height(bx, bz, s, salt);
        Surface.Zone zone = Surface.zone(bx, bz, h, s, salt);

        return switch (zone) {
            case OCEAN -> ocean;
            case BEACH -> beach;
            case RIVER -> river;
            // 平原分兩種：一種是聚落與農地，一種是雜木林。全部同一種的話，
            // 走幾公里都是同一片綠，而台灣的平原是被田、竹叢、聚落切碎的
            case PLAIN -> Noise.warped(bx, bz, 320f, 3, salt ^ 0x4A1E, 120f) < 0.5f ? plain : farmland;
            case HILL -> hill;
            case MOUNTAIN -> mountain;
            case PEAK -> peak;
        };
    }
}
