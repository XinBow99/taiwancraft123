package com.xinbow99.taiwan;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * 音效。
 *
 * <h2>為什麼引擎聲是自己合成的</h2>
 * <p>{@code tools/engine-sound.mjs} 會生出這兩個檔案。用素材站的錄音要處理授權與出處，
 * 而且下一個人想調整就只能重新去找；一支腳本則是改個參數再跑一次的事。
 * 想換成真實錄音的話，蓋掉 {@code assets/taiwan/sounds/} 底下的 ogg 就好，程式不用動。
 *
 * <h2>引擎聲是「一個循環 ＋ 變速」，不是一直重播短音效</h2>
 * <p>之前是每幾 tick 播一次原版的礦車聲，那聽起來是「噠、噠、噠」的斷點，不是引擎。
 * 現在是一段無縫循環（1 秒 ＝ 50 次點火），由客戶端一直改它的 pitch 來表示轉速——
 * 見 {@code ScooterSoundInstance}。
 *
 * <h2>SOUND_EVENT 是同步的 registry</h2>
 * <p>加東西進去會改變 id 對照表，沒裝模組的客戶端就連不進來。不過載具（ENTITY_TYPE）
 * 早就跨過這條線了，所以這裡不是新的代價。
 */
public final class TaiwanSounds {

    public static final SoundEvent SCOOTER_ENGINE = create("scooter_engine");
    public static final SoundEvent SCOOTER_START = create("scooter_start");

    private static SoundEvent create(String name) {
        Identifier id = Taiwan.id(name);
        // createVariableRangeEvent：傳播距離跟著音量走。引擎聲的音量會隨著轉速變，
        // 用固定距離的話，怠速的細碎聲會跟全速一樣傳得那麼遠
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    /** 觸發 class 初始化。欄位是 static final，碰一下就註冊完了。 */
    public static void register() {
    }

    private TaiwanSounds() {
    }
}
