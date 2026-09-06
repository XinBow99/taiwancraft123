package com.xinbow99.taiwan.net;

import com.xinbow99.taiwan.Taiwan;
import com.xinbow99.taiwan.entity.RoadVehicle;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 「切換大燈」的封包。客戶端 → 伺服器，沒有內容。
 *
 * <h2>為什麼是空封包</h2>
 * <p>裡面不帶「開」或「關」，也不帶車的 id。**狀態的真相在伺服器**——客戶端只說「我按了」，
 * 伺服器自己查那個玩家騎在哪台車上、自己翻轉。帶著布林值送過來的話，兩邊在同一 tick
 * 內各自翻轉會打架；帶車 id 的話則是讓客戶端指定要操作誰，那是一個可以被偽造的參數。
 *
 * <p>同理，伺服器不信任「客戶端說牠在車上」：{@code context.player().getVehicle()} 是伺服器
 * 自己的狀態。玩家沒在車上就什麼都不做。
 */
public record HeadlightPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HeadlightPayload> TYPE =
            new CustomPacketPayload.Type<>(Taiwan.id("headlight"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeadlightPayload> CODEC =
            StreamCodec.unit(new HeadlightPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 伺服器端註冊。型別必須兩邊都註冊，否則封包會在解碼階段就被丟掉。 */
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) ->
                context.server().execute(() -> {
                    if (context.player().getVehicle() instanceof RoadVehicle vehicle) {
                        vehicle.toggleHeadlight();
                    }
                }));
    }
}
