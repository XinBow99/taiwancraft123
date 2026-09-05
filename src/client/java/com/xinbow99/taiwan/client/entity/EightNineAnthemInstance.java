package com.xinbow99.taiwan.client.entity;

import com.xinbow99.taiwan.TaiwanSounds;
import com.xinbow99.taiwan.entity.EightNine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;

/**
 * 一團 8+9 聚在一起時放的歌。
 *
 * <h2>音量 0 的音源會被直接丟掉</h2>
 * <p>第一版是在實體載入時就建立音源、音量從 0 淡進來。**那樣永遠不會有聲音**：
 * {@code SoundEngine} 在 {@code play()} 當下就會檢查音量，是 0 的話直接跳過
 * （原始碼裡那句 log 是「Skipped playing sound {}, volume was zero.」），
 * 音源根本不會進到 tick 迴圈，之後怎麼調 volume 都沒用。
 *
 * <p>所以改成**成團的那一刻才建立**（由 {@link #tickClient} 巡邏），建立時就是滿音量。
 * 代價是團散掉再重組時歌會從頭開始——可以接受，甚至更合理。
 *
 * <h2>整團只放一份</h2>
 * <p>音源掛在團裡**實體 id 最小的那一個**身上。六個人各自放一份會疊成六倍音量，
 * 而且六個音源的播放進度不同步，聽起來是一團糊。
 *
 * <p>「id 最小」是刻意挑的：它不需要任何協調、不用同步欄位、也不用選舉——每個客戶端
 * 各自算都會得到同一個答案。而且那個人走掉或死掉之後，下一個最小的自然接手。
 */
public class EightNineAnthemInstance extends AbstractTickableSoundInstance {

    /** 成團時的音量。 */
    private static final float LOUD = 0.85f;
    /** 每 tick 的淡出速度。0.05 ≒ 一秒：團散掉時是淡出不是硬切。 */
    private static final float FADE = 0.05f;
    /** 巡邏的間隔（tick）。範圍查詢不用每 tick 做，人不會瞬間出現。 */
    private static final int PATROL = 20;
    /** 巡邏的半徑。超過這個距離的團就不開音源了——聽不到也沒必要。 */
    private static final double PATROL_RANGE = 64.0;

    /** 目前有音源的實體 id。避免同一個人被開第二個音源。 */
    private static final Map<Integer, EightNineAnthemInstance> LIVE = new HashMap<>();

    private final EightNine owner;

    private EightNineAnthemInstance(EightNine owner) {
        // record 分類：跟唱片同一類，玩家在設定裡關掉音樂就一起關掉
        super(TaiwanSounds.EIGHTNINE_ANTHEM, SoundSource.RECORDS, RandomSource.create());
        this.owner = owner;
        this.looping = true;
        this.delay = 0;
        // **不能從 0 開始**，見類別說明
        this.volume = LOUD;
        this.pitch = 1f;
        this.x = owner.getX();
        this.y = owner.getY();
        this.z = owner.getZ();
    }

    /**
     * 每 tick 從客戶端呼叫：看看附近有沒有哪一團該開始放歌。
     *
     * <p>掃的是玩家附近的 8+9，不是全世界——遠處的團開了音源也聽不到，只是白白佔資源。
     */
    public static void tickClient(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        LIVE.entrySet().removeIf(e -> e.getValue().isStopped());
        if (mc.player.tickCount % PATROL != 0) return;

        for (EightNine candidate : mc.level.getEntitiesOfClass(EightNine.class,
                mc.player.getBoundingBox().inflate(PATROL_RANGE),
                other -> other.isAlive() && other.inCrowd())) {
            if (LIVE.containsKey(candidate.getId())) continue;
            if (!isSpeaker(candidate)) continue;
            EightNineAnthemInstance instance = new EightNineAnthemInstance(candidate);
            LIVE.put(candidate.getId(), instance);
            mc.getSoundManager().play(instance);
        }
    }

    /** 這一團現在該由誰放。見類別說明：全部客戶端算出來的答案都一樣。 */
    private static boolean isSpeaker(EightNine self) {
        for (EightNine other : self.level().getEntitiesOfClass(EightNine.class,
                self.getBoundingBox().inflate(EightNine.CROWD_RADIUS),
                candidate -> candidate.isAlive() && candidate.inCrowd())) {
            if (other.getId() < self.getId()) return false;
        }
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return !this.owner.isSilent();
    }

    @Override
    public void tick() {
        if (this.owner.isRemoved()) {
            this.stop();
            return;
        }
        this.x = this.owner.getX();
        this.y = this.owner.getY();
        this.z = this.owner.getZ();

        boolean keep = this.owner.inCrowd() && isSpeaker(this.owner);
        this.volume = Mth.lerp(FADE, this.volume, keep ? LOUD : 0f);
        // 淡到聽不見才真的停。硬切在遠處聽起來像有人把音響電源拔掉
        if (!keep && this.volume < 0.01f) this.stop();
    }
}
