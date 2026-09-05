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
 * 見 {@code VehicleSoundInstance}。
 *
 * <h2>SOUND_EVENT 是同步的 registry</h2>
 * <p>加東西進去會改變 id 對照表，沒裝模組的客戶端就連不進來。不過載具（ENTITY_TYPE）
 * 早就跨過這條線了，所以這裡不是新的代價。
 */
public final class TaiwanSounds {

    public static final SoundEvent SCOOTER_ENGINE = create("scooter_engine");
    public static final SoundEvent SCOOTER_START = create("scooter_start");

    /**
     * 8+9 成團時放的歌（6 分 33 秒，循環）。
     *
     * <p>**單聲道**是硬性要求，不是為了省空間：Minecraft 只會對單聲道的音源做距離與
     * 方位衰減，立體聲檔案會變成「不管你走多遠都一樣大聲、而且沒有方向」的背景音。
     * 這首歌是要從那一團人身上放出來的，方位必須是對的。
     *
     * <p>{@code stream: true}（見 sounds.json）：3.3 MB 的檔案不要整個載進記憶體。
     * 原版的唱片也是這樣設定的。
     */
    public static final SoundEvent EIGHTNINE_ANTHEM = create("eightnine_anthem");
    public static final SoundEvent EIGHTNINE_HURT = create("eightnine_hurt");

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
