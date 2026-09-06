package com.xinbow99.taiwan.client.entity;

import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;

/**
 * 獼猴的算繪狀態。
 *
 * <p>算繪執行緒不能碰 entity——它跑在另一條執行緒上，而 entity 隨時可能被 tick 改掉。
 * 所以每一幀先把需要的東西抄進這個物件（{@code extractRenderState}），模型只讀它。
 *
 * <p>繼承 {@link HoldingEntityRenderState} 是為了那個 {@code heldItem} 欄位：贓物要畫出來，
 * 就得先把主手物品烘成 {@code ItemStackRenderState}。烘的動作由父類別的靜態方法做，
 * 不要自己抄 ItemStack 進來——ItemStack 是可變的，算繪中途被 tick 改掉會閃爍。
 *
 * <p>{@link #carrying} 跟 {@code heldItem.isEmpty()} 講的是同一件事，但分開放：模型只管姿勢、
 * 不該知道物品算繪這一套；layer 只管物品、不該知道姿勢。
 */
public class MacaqueRenderState extends HoldingEntityRenderState {
    public boolean carrying;

    /**
     * 坐下的程度，0＝四足站著、1＝坐在地上。
     *
     * <p>不是布林。站姿與坐姿差了六十幾度，直接切會是一格內從站變坐；這個值在 entity 端
     * 已經補成連續的，模型只要拿它去做線性混合就好——**所有站姿的動作乘以 (1 - 這個值)、
     * 所有坐姿的角度乘以這個值**，中間的每一幀都是合法的姿勢。
     */
    public float sitAmount;

    /**
     * 揮擊的程度，一次攻擊之間走 0 → 1 → 0。
     *
     * <p>不需要同步旗標：{@code Mob.doHurtTarget} 本來就會呼叫 {@code swing()}，動畫封包
     * 會把 {@code attackAnim} 送到客戶端。所以這個值是從原版既有的揮擊計時算出來的，
     * 沒有多加任何一個網路欄位、也沒有延遲任何行為。
     */
    public float swipeAmount;
}
