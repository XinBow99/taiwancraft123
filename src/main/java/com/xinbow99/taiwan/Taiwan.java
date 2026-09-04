package com.xinbow99.taiwan;

import com.xinbow99.taiwan.entity.TaiwanEntities;
import com.xinbow99.taiwan.entity.TaiwanItems;
import com.xinbow99.taiwan.worldgen.TaiwanBiomeSource;
import com.xinbow99.taiwan.worldgen.TaiwanChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模組進入點。
 *
 * <h2>為什麼註冊這兩個不會擋掉原版客戶端</h2>
 * <p>Fabric 只把「封包裡會用原始 id 指涉」的 registry 標成 {@code SYNCED}：
 * {@code BLOCK}、{@code ITEM}、{@code ENTITY_TYPE}、{@code SOUND_EVENT}⋯⋯ 往那些裡面加東西，
 * id 對照表就會跟原版對不上，沒裝模組的人連不進來。
 *
 * <p>{@code CHUNK_GENERATOR} 與 {@code BIOME_SOURCE} 不在那份清單裡，因為沒有任何封包提到
 * 生成器——客戶端只收算好的方塊。所以 Phase 1 的東西在網路上完全看不見。
 *
 * <p>這件事到 Phase 2 就結束了：載具是 {@code ENTITY_TYPE}，那個是同步的。所以載具一加進來，
 * 客戶端就非裝不可。這是設計上的分水嶺，值得知道它落在哪裡。
 */
public class Taiwan implements ModInitializer {
	public static final String MOD_ID = "taiwan";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Registry.register(
				BuiltInRegistries.CHUNK_GENERATOR,
				id("taiwan"),
				TaiwanChunkGenerator.CODEC);

		Registry.register(
				BuiltInRegistries.BIOME_SOURCE,
				id("taiwan"),
				TaiwanBiomeSource.CODEC);

		TaiwanEntities.register();
		TaiwanItems.register();

		// 伺服器一起來就把生態系來源綁好。
		//
		// TaiwanBiomeSource 拿不到種子也拿不到參數，原本只在 createBiomes 裡塞進去——
		// 但那只有「生成新區塊」時才會發生。伺服器重開之後如果沒有任何新區塊被生成，
		// 它就一直是未綁定狀態，而未綁定時 getNoiseBiome 一律回平原。
		// 症狀：/locate biome 找不到任何森林或竹林，地圖上的生態系顏色也全錯。
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			for (ServerLevel level : server.getAllLevels()) {
				if (level.getChunkSource().getGenerator() instanceof TaiwanChunkGenerator generator
						&& generator.getBiomeSource() instanceof TaiwanBiomeSource source) {
					source.bind(generator.settings(),
							generator.salt(level.getChunkSource().randomState()));
				}
			}
		});

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registry, environment) -> TaiwanCommands.register(dispatcher));

		LOGGER.info("Taiwan world type registered");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
