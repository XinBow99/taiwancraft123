package com.xinbow99.taiwan.client.entity;

import com.xinbow99.taiwan.entity.VehicleModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * 速克達的算繪狀態。
 *
 * <p>算繪跑在另一條執行緒上，不能直接讀 entity——每一幀先把需要的東西抄過來。
 */
public class VehicleRenderState extends EntityRenderState {
    /** 龍頭角度（度）。 */
    public float steer;
    /** 車輪的滾動角（弧度）。 */
    public float wheelSpin;
    /** 儀表時速（km/h）。怠速微震靠它判斷「車真的停著」。 */
    public float speedKmh;
    /**
     * 平滑過的縱向加速度，-1 到 1。起步抬頭用。
     *
     * <p>不是從實體讀的——{@link com.xinbow99.taiwan.entity.RoadVehicle} 沒有這個欄位，
     * 而它的 {@code speed} 只有騎士自己的客戶端算得出來。這裡改成在算繪端
     * 對時速做一階差分再低通。
     */
    public float accel;
    /** 上一幀的時速。NaN 代表這個算繪狀態剛建立，還沒有可以做差分的基準。 */
    public float prevSpeedKmh = Float.NaN;
    /** 車身左右傾（度）。轉彎壓車用。 */
    public float lean;
    /** 車頭朝向（度）。EntityRenderState 沒有這個欄位，要自己抄。 */
    public float yRot;
    /** 車款。決定用哪個模型與哪張貼圖。 */
    public VehicleModel variant = VehicleModel.CYGNUS;

    /**
     * 停著沒人騎。目前只有勁戰用得到——決定側柱要放下還是收起來。
     *
     * <p>沒有這個旗標的話，側柱會一直插在地上跟著車跑。用「有沒有乘客」而不是「速度為零」
     * 是因為速度會在紅燈、卡住的時候歸零，那時候側柱不該掉下來。
     */
    public boolean parked = true;

    /** 大燈開著。開著才會疊那層只有燈罩不透明的發光貼圖。 */
    public boolean headlight;
}
