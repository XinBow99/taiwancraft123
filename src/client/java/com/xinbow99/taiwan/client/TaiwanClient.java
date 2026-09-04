package com.xinbow99.taiwan.client;

import com.xinbow99.taiwan.client.entity.MacaqueModel;
import com.xinbow99.taiwan.client.entity.MacaqueRenderer;
import com.xinbow99.taiwan.client.entity.ScooterModel;
import com.xinbow99.taiwan.client.entity.ScooterRenderer;
import com.xinbow99.taiwan.entity.TaiwanEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

/**
 * 客戶端的東西：模型與算繪器。
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
	}
}
