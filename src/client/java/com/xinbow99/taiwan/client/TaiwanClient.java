package com.xinbow99.taiwan.client;

import com.xinbow99.taiwan.client.entity.MacaqueModel;
import com.xinbow99.taiwan.client.entity.MacaqueRenderer;
import com.xinbow99.taiwan.client.entity.ScooterModel;
import com.xinbow99.taiwan.client.entity.ScooterRenderer;
import com.xinbow99.taiwan.client.entity.ScooterSoundInstance;
import com.xinbow99.taiwan.entity.Scooter;
import com.xinbow99.taiwan.entity.TaiwanEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.Minecraft;

/**
 * 客戶端的東西：模型、算繪器與引擎聲。
 *
 * <p>順序有意義——{@code registerModelLayer} 必須在 {@code EntityRendererRegistry} 之前，
 * 因為算繪器建構時會 {@code bakeLayer}，那時候 layer 得已經在冊。
 */
public class TaiwanClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(MacaqueRenderer.LAYER, MacaqueModel::createBodyLayer);
		EntityRendererRegistry.register(TaiwanEntities.MACAQUE, MacaqueRenderer::new);

		ModelLayerRegistry.registerModelLayer(ScooterRenderer.LAYER, ScooterModel::createBodyLayer);
		EntityRendererRegistry.register(TaiwanEntities.SCOOTER, ScooterRenderer::new);

		// 每一台車一進入視野就掛一個循環音源，跟著它直到它被移除。
		//
		// 掛在「實體載入」而不是「有人騎上去」：路邊那排違停的車也要能在被發動的那一刻
		// 出聲，而「有沒有在運轉」是 ScooterSoundInstance 自己每 tick 判斷的
		ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof Scooter scooter) {
				Minecraft.getInstance().getSoundManager().play(new ScooterSoundInstance(scooter));
			}
		});
	}
}
