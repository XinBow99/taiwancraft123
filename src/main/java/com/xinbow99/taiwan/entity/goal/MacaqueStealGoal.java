package com.xinbow99.taiwan.entity.goal;

import com.xinbow99.taiwan.entity.Macaque;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 搶食物，然後跑。**這一個 goal 就是整隻獼猴的個性。**
 *
 * <h2>兩段式，不是兩個 goal</h2>
 * <p>「靠近搶」跟「搶完跑」寫在同一個 goal 裡，因為它們共用同一個目標玩家。拆成兩個的話，
 * 逃跑那個 goal 得自己再找一次「剛剛是誰」——而那個人可能已經走遠、死了、或換了一個人
 * 站在原地。共用狀態比重新尋找可靠。
 *
 * <h2>只搶快捷列</h2>
 * <p>背包深處的東西看不見，搶了玩家也不知道發生什麼事。快捷列與手上的東西玩家隨時看得到，
 * 少一個會**當場發現**——那個「欸？」的瞬間才是這隻生物存在的理由。
 */
public class MacaqueStealGoal extends Goal {

    /** 開始注意玩家的距離。 */
    private static final double NOTICE = 10.0;
    /** 搶得到的距離（平方）。 */
    private static final double REACH_SQ = 4.0;
    /** 逃跑要跑多遠才算安全。 */
    private static final double SAFE = 14.0;

    private final Macaque macaque;
    private Player mark;
    private int cooldown;

    public MacaqueStealGoal(Macaque macaque) {
        this.macaque = macaque;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 手上已經有贓物就不再找下一個：一次搶一樣，不然牠會變成吸塵器
        if (macaque.hasLoot()) return true;
        if (cooldown-- > 0) return false;

        Player player = macaque.level().getNearestPlayer(macaque, NOTICE);
        if (player == null || player.isCreative() || player.isSpectator()) return false;
        if (findFood(player) < 0) return false;

        this.mark = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (macaque.hasLoot()) return true;
        return mark != null && mark.isAlive() && findFood(mark) >= 0
                && macaque.distanceToSqr(mark) < NOTICE * NOTICE * 2;
    }

    @Override
    public void stop() {
        this.mark = null;
        this.cooldown = 60;
        macaque.getNavigation().stop();
    }

    @Override
    public void tick() {
        // 第二段：手上有東西了，逃
        if (macaque.hasLoot()) {
            flee();
            return;
        }
        if (mark == null) return;

        // 第一段：靠近
        macaque.getLookControl().setLookAt(mark, 30.0f, 30.0f);
        if (macaque.distanceToSqr(mark) > REACH_SQ) {
            macaque.getNavigation().moveTo(mark, 1.25);
            return;
        }
        steal();
    }

    /**
     * 從快捷列拿走一個。
     *
     * <p>只拿一個而不是整疊：整疊被搶走玩家會覺得被搶劫，拿一個玩家會覺得被戲弄——
     * 後者比較接近真實的獼猴，而且不會讓人想關掉這個生物。
     */
    private void steal() {
        int slot = findFood(mark);
        if (slot < 0) return;

        ItemStack stack = mark.getInventory().getItem(slot);
        ItemStack loot = stack.copyWithCount(1);
        stack.shrink(1);
        mark.getInventory().setItem(slot, stack);
        mark.inventoryMenu.broadcastChanges();

        macaque.takeLoot(loot);
        // 搶完立刻不要再看著人：轉頭就跑，那個動作本身就是回饋
        macaque.getNavigation().stop();
    }

    /** 跑到離苦主夠遠的地方。跑不到路就隨便挑一個反方向的點。 */
    private void flee() {
        if (mark == null || !mark.isAlive()) return;
        if (macaque.distanceToSqr(mark) > SAFE * SAFE) return;

        Vec3 away = macaque.position().subtract(mark.position()).normalize().scale(10.0);
        Vec3 target = macaque.position().add(away.x, 0, away.z);
        macaque.getNavigation().moveTo(target.x, target.y, target.z, 1.45);
    }

    /**
     * 快捷列（含手上那一格）裡第一個食物的欄位；沒有就回 -1。
     *
     * <p>用 {@code DataComponents.FOOD} 而不是列一張白名單：任何模組加的食物都算，
     * 而白名單只會在別人加東西的時候失效。
     */
    private int findFood(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD)) return i;
        }
        return -1;
    }
}
