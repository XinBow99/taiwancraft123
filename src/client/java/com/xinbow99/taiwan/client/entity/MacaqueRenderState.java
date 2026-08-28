package com.xinbow99.taiwan.client.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * 獼猴的算繪狀態。
 *
 * <p>算繪執行緒不能碰 entity——它跑在另一條執行緒上，而 entity 隨時可能被 tick 改掉。
 * 所以每一幀先把需要的東西抄進這個物件（{@code extractRenderState}），模型只讀它。
 *
 * <p>這裡只多一個欄位：手上有沒有贓物。抱著東西的姿勢是「牠剛搶了你」在遠處唯一
 * 讀得出來的訊號。
 */
public class MacaqueRenderState extends LivingEntityRenderState {
    public boolean carrying;
}
