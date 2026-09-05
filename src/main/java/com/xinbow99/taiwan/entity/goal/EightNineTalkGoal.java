package com.xinbow99.taiwan.entity.goal;

import com.xinbow99.taiwan.entity.EightNine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;

/**
 * 講話。
 *
 * <h2>只在有人聽得到的時候講</h2>
 * <p>附近沒有玩家就完全不執行——這不只是省效能，是因為聊天訊息是**全域的**：
 * 沒有這個檢查的話，地圖另一頭三十個 8+9 會把你的聊天欄洗滿。
 *
 * <p>訊息只送給範圍內的玩家（{@code sendSystemMessage} 逐一送），不是廣播。
 *
 * <h2>成團與落單講的話不一樣</h2>
 * <p>這是整個族群最好認的特徵，也是 {@link EightNine} 的核心機制。人多的時候是招呼、
 * 是揪團、是「兄弟們」；一個人的時候音量會降下來。台詞在 {@code EightNineVariant}。
 *
 * <h2>整團不會一起開口</h2>
 * <p>每個人的冷卻是各自隨機的（{@value #MIN_GAP}~{@value #MAX_GAP} tick），
 * 而且開口前還要再擲一次骰。六個人同時喊同一句話會很像 bug。
 */
public class EightNineTalkGoal extends Goal {

    /** 講話的間隔下限（tick）。15 秒。 */
    private static final int MIN_GAP = 300;
    /** 上限。45 秒。 */
    private static final int MAX_GAP = 900;
    /** 玩家要多近才聽得到。 */
    private static final double AUDIENCE = 12.0;

    private final EightNine self;
    private int cooldown;

    public EightNineTalkGoal(EightNine self) {
        this.self = self;
        // 不佔任何 flag：講話不影響移動也不影響看的方向，可以跟其他 goal 並存
        this.setFlags(EnumSet.noneOf(Flag.class));
        this.cooldown = nextGap();
    }

    @Override
    public boolean canUse() {
        if (--cooldown > 0) return false;
        cooldown = nextGap();
        // 冷卻到了還要再擲一次：不然大家的節奏會慢慢對齊成整齊的輪流發言
        return self.getRandom().nextFloat() < 0.6f && !audience().isEmpty();
    }

    /** 一次就講完，不需要持續執行。 */
    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        List<Player> listeners = audience();
        if (listeners.isEmpty()) return;

        String line = self.variant().line(self.getRandom(), self.inCrowd());
        Component message = Component.literal("<8+9> " + line);
        for (Player player : listeners) {
            player.sendSystemMessage(message);
        }
    }

    private int nextGap() {
        return MIN_GAP + self.getRandom().nextInt(MAX_GAP - MIN_GAP);
    }

    private List<Player> audience() {
        if (!(self.level() instanceof ServerLevel level)) return List.of();
        return level.getEntitiesOfClass(Player.class,
                self.getBoundingBox().inflate(AUDIENCE),
                player -> !player.isSpectator());
    }
}
