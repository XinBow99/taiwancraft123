package com.xinbow99.taiwan.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xinbow99.taiwan.entity.RoadVehicle;
import com.xinbow99.taiwan.net.HeadlightPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.xinbow99.taiwan.Taiwan;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * 這個 mod 的按鍵綁定。
 *
 * <h2>H：大燈</h2>
 * <p>只在**騎在車上**的時候才送封包。不在車上就按了也沒事——伺服器那邊本來也會擋
 * （見 {@link HeadlightPayload}），這裡先擋一次是為了不要每按一次就送一個註定被丟掉的封包。
 *
 * <p>用 {@code consumeClick()} 而不是 {@code isDown()}：前者是「這個 tick 有沒有被按下過」
 * 並且會把事件吃掉，後者是「現在是不是按著」。用 isDown 的話按住 H 會每 tick 送一次封包，
 * 大燈變成每秒閃 20 下。
 */
public final class TaiwanKeys {

    /** 這個 mod 自己的按鍵分類。26.2 的 {@code KeyMapping.Category} 是 record，要註冊過才排得進選項頁。 */
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Taiwan.id("main"));

    public static final KeyMapping HEADLIGHT = new KeyMapping(
            "key.taiwan.headlight",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY);

    private TaiwanKeys() {
    }

    public static void register() {
        KeyMappingHelper.registerKeyMapping(HEADLIGHT);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // while 而不是 if：一個 tick 內可能累積了多次按鍵（低幀率時），
            // 但我們只要處理一次就好，其餘的吃掉丟棄
            boolean pressed = false;
            while (HEADLIGHT.consumeClick()) pressed = true;
            if (!pressed) return;
            Minecraft mc = client;
            if (mc.player == null || !(mc.player.getVehicle() instanceof RoadVehicle)) return;
            ClientPlayNetworking.send(new HeadlightPayload());
        });
    }
}
