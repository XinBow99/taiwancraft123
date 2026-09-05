package com.xinbow99.taiwan.client.entity;

import com.xinbow99.taiwan.entity.EightNineVariant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * 8+9 的算繪狀態。
 *
 * <p>算繪執行緒不能碰 entity——它跑在另一條執行緒上，而 entity 隨時可能被 tick 改掉。
 * 所以每一幀先把需要的東西抄進這個物件（{@code extractRenderState}），模型只讀它。
 *
 * <p>兩個欄位剛好對應這個實體的兩件事：**型**決定貼圖與配件，**成團**決定姿勢的幅度。
 * 後者是核心機制（見 {@code EightNine}）——人多的時候不只講話變大聲，站姿也更張揚。
 */
public class EightNineRenderState extends LivingEntityRenderState {
    public EightNineVariant variant = EightNineVariant.TEMPLE;
    public boolean inCrowd;
}
