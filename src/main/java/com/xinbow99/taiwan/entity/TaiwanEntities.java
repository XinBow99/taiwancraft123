package com.xinbow99.taiwan.entity;

import com.xinbow99.taiwan.Taiwan;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import com.xinbow99.taiwan.worldgen.Palette;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 生物的註冊。
 *
 * <h2>這裡是「原版客戶端還連得進來」的終點</h2>
 * <p>{@code ENTITY_TYPE} 在 {@code RegistryDataLoader.SYNCHRONIZED_REGISTRIES} 裡：封包用
 * 原始 id 指涉生物，所以只要往這個 registry 加東西，沒裝模組的客戶端 id 表就對不上。
 *
 * <p>Phase 1 的地形與聚落完全不碰網路（生成器與生態系來源都不同步，生態系又全用原版 id），
 * 所以那時候原版客戶端連得進來。**這個檔案一存在，那件事就結束了。**
 * 這是設計上的分水嶺，值得知道它落在哪裡。
 */
public final class TaiwanEntities {

    public static final ResourceKey<EntityType<?>> MACAQUE_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Taiwan.id("macaque"));

    public static final ResourceKey<EntityType<?>> EIGHTNINE_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Taiwan.id("eightnine"));

    public static final ResourceKey<EntityType<?>> SCOOTER_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Taiwan.id("scooter"));

    /**
     * 台灣獼猴。
     *
     * <p>{@code sized(0.7f, 0.9f)}：比玩家矮一截，鑽得進一格高的空隙。獼猴的體型是牠
     * 討人厭的一半原因——太大隻就變成猩猩，太小隻搶不到東西。
     */
    public static final EntityType<Macaque> MACAQUE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            MACAQUE_KEY,
            FabricEntityType.Builder.<Macaque>createMob(
                            Macaque::new,
                            MobCategory.CREATURE,
                            // 用敘述式而不是串接：defaultAttributes 有兩個多載
                            // （一個回 Mob、一個回 Living），串接時 javac 選不出來
                            builder -> {
                                builder.defaultAttributes(Macaque::createAttributes);
                                // 跟原版動物同一組生成條件：地面、夠亮、方塊站得住
                                builder.spawnPlacement(SpawnPlacementTypes.ON_GROUND,
                                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                        Macaque::spawnRules);
                                return builder;
                            })
                    .sized(0.7f, 0.9f)
                    .clientTrackingRange(10)
                    .build(MACAQUE_KEY));

    /**
     * 速克達。
     *
     * <p>{@code sized(0.8f, 1.4f)}：寬度**必須小於 1**，否則穿不過一格的縫隙——
     * 而「鑽得過去」是機車相對於汽車的核心賣點，也是規格書明寫的要求。
     * 後照鏡在模型上超出這個寬度是刻意的，跟船槳一樣只是視覺。
     */
    public static final EntityType<Scooter> SCOOTER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            SCOOTER_KEY,
            EntityType.Builder.<Scooter>of(Scooter::new, MobCategory.MISC)
                    .sized(0.8f, 1.4f)
                    .clientTrackingRange(10)
                    .build(SCOOTER_KEY));

    /**
     * 8+9（陣頭少年）。
     *
     * <p>{@code sized(0.6f, 1.95f)}：比玩家（0.6 × 1.8）高一點。這不是隨便加的——
     * 站姿是三七步、重心壓在一腳上，模型本身就比立正的人略高一截；碰撞箱跟著
     * 模型走，不然頭會穿進一格半高的天花板裡。
     */
    public static final EntityType<EightNine> EIGHTNINE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            EIGHTNINE_KEY,
            FabricEntityType.Builder.<EightNine>createMob(
                            EightNine::new,
                            MobCategory.CREATURE,
                            builder -> {
                                builder.defaultAttributes(EightNine::createAttributes);
                                builder.spawnPlacement(SpawnPlacementTypes.ON_GROUND,
                                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                        TaiwanEntities::onStreet);
                                return builder;
                            })
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(12)
                    .build(EIGHTNINE_KEY));

    /**
     * 只在街道的鋪面上生成。
     *
     * <h3>為什麼不是「在平原上生成」</h3>
     * <p>生態系的 spawner 是整個生態系隨機灑的。聚落只佔平原的很小一部分，所以灑出去的
     * 絕大多數落在野外草地上——**聚落裡反而是空的**。8+9 站在荒郊野外也不合理。
     *
     * <h3>用混凝土當「這裡是街道」的標記</h3>
     * <p>判斷交給 {@link Palette#isPaving}——鋪面用哪些方塊是那個檔案的知識。
     * 混凝土在自然地形完全不會出現，所以「腳下是鋪面」就等於「這裡是街道」，
     * 不必把 Town 的幾何再算一次，也不用跟 worldgen 共享狀態。
     *
     * <p>代價：玩家自己蓋的混凝土地板也會生。那是可以接受的——鋪一片黑混凝土當停車場，
     * 然後就有人來晃，其實還蠻對的。
     */
    /** 半徑幾格內算「同一區」。24 格 ≒ 一個街廓。 */
    private static final double CROWD_SPACING = 24.0;
    /** 同一區最多幾個。成團要三個，六個就是兩團——再多就是塞車了。 */
    private static final int CROWD_CAP = 6;

    private static boolean onStreet(EntityType<EightNine> type, ServerLevelAccessor level,
                                    EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!Palette.isPaving(level.getBlockState(pos.below()))) return false;

        // 區域族群上限。**這一條是「越生越多」唯一擋得住的地方。**
        //
        // MobCategory.CREATURE 的上限是「每區塊平均幾隻」的全域計算，而 8+9 被限制在
        // 街道上——街道只佔聚落的一小塊，於是整個生態系的配額全部擠進那幾條街，
        // 玩家看到的就是路上人越來越多。全域的帳算得再對，體感還是爆的。
        //
        // 所以改成問一個玩家真的感覺得到的問題：我站的地方附近已經幾個人了？
        int nearby = level.getEntitiesOfClass(EightNine.class,
                new AABB(pos).inflate(CROWD_SPACING)).size();
        return nearby < CROWD_CAP;
    }

    private TaiwanEntities() {
    }

    public static void register() {
        registerSpawns();
        Taiwan.LOGGER.info("Taiwan entities registered");
    }

    /**
     * 山區與森林。
     *
     * <h3>權重與群體大小</h3>
     * <p>{@code weight = 8}：比原版的狼（5）高一點、比綿羊（12）低。獼猴應該常見到「上山
     * 一定會遇到」，但不能常見到變成雜訊。
     *
     * <p>{@code 3～8 隻}：這是「成群移動」那條需求真正的落點——群體大小是**生成時**決定的，
     * 不是靠 AI 聚起來的。靠 AI 聚集的話，第一批散開的猴子永遠湊不回一群。
     *
     * <p>選的三個生態系正好是這個世界的丘陵（竹林）、山地（針葉林）與平原雜木林——
     * 見 world_preset 裡 biome_source 的對應。
     */
    private static void registerSpawns() {
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.BAMBOO_JUNGLE,
                        Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                        Biomes.FOREST),
                MobCategory.CREATURE, MACAQUE, 24, 3, 8);

        // 8+9 生在平原與稀樹草原——那是這個世界裡聚落所在的地形。
        //
        // **每批 3~6 人**：這個數字直接決定成團的門檻（EightNine.CROWD 是 3）會不會
        // 自然發生。一次生一個的話，玩家永遠只會遇到落單的、看不到這個實體真正的樣子。
        //
        // 權重 2 比獼猴（24）低一個量級，群體也只有 2~3——聚落裡的密度主要由區塊生成
        // 那一批決定（見 TaiwanChunkGenerator.gatherEightNine），這條只是讓既有聚落慢慢補人。
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SAVANNA),
                MobCategory.CREATURE, EIGHTNINE, 2, 2, 3);
    }
}
