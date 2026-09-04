package com.xinbow99.taiwan.entity;

import com.xinbow99.taiwan.Taiwan;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
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
    }
}
