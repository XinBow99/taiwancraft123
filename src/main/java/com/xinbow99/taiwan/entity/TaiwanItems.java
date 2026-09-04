package com.xinbow99.taiwan.entity;

import com.xinbow99.taiwan.Taiwan;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/**
 * 物品。目前只有獼猴的生怪蛋。
 *
 * <h2>26.2 的生怪蛋不再帶 EntityType 參數</h2>
 * <p>舊版是 {@code new SpawnEggItem(type, baseColor, overlayColor, props)}。現在生物種類存在
 * 物品的 data component 裡，由 {@code Item.Properties.spawnEgg(type)} 掛上去；顏色也不再是
 * 兩個 int，**每一顆蛋有自己的一張貼圖**。
 *
 * <p>所以這裡要配三個檔案：物品定義（{@code assets/taiwan/items/}）、模型
 * （{@code assets/taiwan/models/item/}）、貼圖（{@code assets/taiwan/textures/item/}）。
 * 少任何一個，遊戲裡會是一團紫黑色的方塊。
 */
public final class TaiwanItems {

    public static final ResourceKey<Item> MACAQUE_SPAWN_EGG_KEY =
            ResourceKey.create(Registries.ITEM, Taiwan.id("macaque_spawn_egg"));

    public static final Item MACAQUE_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            MACAQUE_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties()
                    .spawnEgg(TaiwanEntities.MACAQUE)
                    .setId(MACAQUE_SPAWN_EGG_KEY)));

    private TaiwanItems() {
    }

    /**
     * 只是為了觸發這個類別的靜態初始化——註冊發生在上面那個欄位的初始化裡。
     *
     * <p>這裡沒有「把蛋塞進創造模式分頁」的程式碼：這個版本的 Fabric API 沒有
     * {@code ItemGroupEvents}，而 26.2 的生怪蛋是 component 驅動的，原版的分頁應該會
     * 自己收錄。如果沒有，用 {@code /give} 一樣拿得到。
     */
    public static void register() {
        Taiwan.LOGGER.info("Taiwan items registered");
    }
}
