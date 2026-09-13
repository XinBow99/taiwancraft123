package com.xinbow99.taiwan.client.entity;

import com.xinbow99.taiwan.entity.EightNineVariant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/**
 * 8+9 的算繪狀態。
 *
 * <p>算繪執行緒不能碰 entity——它跑在另一條執行緒上，而 entity 隨時可能被 tick 改掉。
 * 所以每一幀先把需要的東西抄進這個物件（{@code extractRenderState}），模型只讀它。
 *
 * <p>三個 {@link AnimationState} 是**用 copyFrom 抄的**，不是把實體上那一份傳過來：
 * 動畫狀態裡存的是「從哪一個 tick 開始播」，實體隨時可能重新開始，
 * 直接持有參考的話算繪到一半會讀到剛被改掉的起始時間。
 */
public class EightNineRenderState extends LivingEntityRenderState {
    public EightNineVariant variant = EightNineVariant.TEMPLE;
    /** 成團中。走路的幅度看它——人多的時候走得更大搖大擺。 */
    public boolean inCrowd;
    /** 抽菸中。手上那根菸只有這個時候看得見。 */
    public boolean smoking;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState greetAnimationState = new AnimationState();
    public final AnimationState smokeAnimationState = new AnimationState();
}
