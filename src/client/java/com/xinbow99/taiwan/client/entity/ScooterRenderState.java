package com.xinbow99.taiwan.client.entity;

import com.xinbow99.taiwan.entity.ScooterVariant;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * 速克達的算繪狀態。
 *
 * <p>算繪跑在另一條執行緒上，不能直接讀 entity——每一幀先把需要的東西抄過來。
 */
public class ScooterRenderState extends EntityRenderState {
    /** 龍頭角度（度）。 */
    public float steer;
    /** 車輪的滾動角（弧度）。 */
    public float wheelSpin;
    /** 車身左右傾（度）。轉彎壓車用。 */
    public float lean;
    /** 車頭朝向（度）。EntityRenderState 沒有這個欄位，要自己抄。 */
    public float yRot;
    /** 車款。決定用哪個模型與哪張貼圖。 */
    public ScooterVariant variant = ScooterVariant.CLASSIC;
}
