package com.xinbow99.taiwan.client.hud;

import com.xinbow99.taiwan.entity.RoadVehicle;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

/**
 * 騎車時畫在畫面左邊的速度表。
 *
 * <h2>為什麼是類比指針而不是一行數字</h2>
 * <p>數字要用讀的，指針用瞄的。騎車的時候你的注意力在路上，餘光只能分辨「指針在哪個
 * 區域」——那正是類比錶面存在的理由。中間仍然放了數字，但它是給你想確認的時候看的，
 * 不是主要的資訊來源。
 *
 * <h2>畫圓這件事</h2>
 * <p>沒有貼圖，所有東西都是 {@code fill()} 出來的矩形。圓形用水平掃描線疊出來
 * （{@link #disc}），刻度與指針則是把座標系轉過去之後填一個細長方形——
 * {@code GuiGraphicsExtractor.pose()} 是 2D 矩陣堆疊，轉得動。
 *
 * <p>一幀大約 150 個 {@code fill}。聽起來多，但它們全部進同一批 GUI 頂點緩衝，
 * 比多載一張貼圖便宜，而且改半徑、改角度不用重畫圖。
 */
public class VehicleDashboard implements HudElement {

    /** 錶面半徑（像素）。 */
    private static final int RADIUS = 46;
    /** 錶面離畫面左緣與下緣的距離。 */
    private static final int MARGIN_X = 18;
    private static final int MARGIN_Y = 18;

    /**
     * 指針的掃掠角度。
     *
     * <p>240 度是機車儀表的慣例：0 在左下、滿刻度在右下，底部留 120 度的缺口放數字。
     * 用滿 360 度的話，指針在「快滿」與「歸零」的位置會靠得太近，一眼看過去會誤判。
     */
    private static final float SWEEP = 240f;
    /**
     * 0 的位置，從畫面正上方順時針算（負值＝逆時針）。
     *
     * <p>{@code -SWEEP/2} 讓整個刻度以正上方為中心對稱：0 在左下（逆時針 120 度）、
     * 滿刻度在右下（順時針 120 度），底部剩 120 度的缺口給中央的數字。
     */
    private static final float START = -SWEEP / 2f;

    /** 每幾 km/h 一個小刻度。 */
    private static final int MINOR_STEP = 10;
    /** 每幾 km/h 一個標數字的大刻度。 */
    private static final int MAJOR_STEP = 20;
    /** 超過幾 km/h 算紅線區。 */
    /** 紅線區從滿刻度的幾成開始。滿刻度是跟著車款走的，所以這裡只能是比例。 */
    private static final float REDLINE_FRACTION = 0.8f;
    /** 目前這台車的滿刻度（km/h）。每一幀從騎的那台抄過來——機車 120、跑車 260。 */
    private float scale = 120f;

    private static final int FACE = 0xC8101418;
    private static final int RIM = 0xFF2B3A44;
    private static final int TICK = 0xFF8FA3AE;
    private static final int TICK_MAJOR = 0xFFE6EEF2;
    private static final int TICK_RED = 0xFFD84A3A;
    private static final int NEEDLE = 0xFFE8503C;
    private static final int HUB = 0xFF1B2228;
    private static final int TEXT = 0xFFE6EEF2;
    private static final int TEXT_DIM = 0xFF7C8B95;
    private static final int STALL = 0xFFD84A3A;

    /**
     * 指針的顯示值，跟著真實時速跑但有阻尼。
     *
     * <p>真實的時速每 tick 都在小幅跳動（碰撞、上下坡、輪胎滑移），指針直接跟的話會抖到
     * 讀不出來。真的儀表也有這個阻尼——它是機械的，這裡是數值的，效果一樣。
     *
     * <p>存在這裡而不是實體上：它純粹是顯示用的平滑，跟車的狀態無關，也不需要同步。
     */
    private float shown;

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        // F1 不用自己判斷：這一版的 Options 已經沒有 hideGui 了，隱藏 HUD 的時候整個
        // 圖層根本不會被呼叫到
        if (mc.player == null) return;
        if (!(mc.player.getVehicle() instanceof RoadVehicle scooter)) {
            this.shown = 0f;
            return;
        }

        this.scale = scooter.variant().maxSpeedKmh();
        float target = Mth.clamp(scooter.speedKmh(), 0f, this.scale);
        // 用畫面的時間而不是 tick：低 FPS 時阻尼才不會變成慢動作
        float follow = Mth.clamp(delta.getRealtimeDeltaTicks() * 0.35f, 0f, 1f);
        this.shown = Mth.lerp(follow, this.shown, target);

        int cx = MARGIN_X + RADIUS;
        int cy = gui.guiHeight() - MARGIN_Y - RADIUS;

        face(gui, cx, cy);
        ticks(gui, cx, cy);
        needle(gui, cx, cy, this.shown);
        readout(gui, mc.font, cx, cy, scooter);
    }

    /** 錶面：一個深色的圓，外面一圈框。 */
    private static void face(GuiGraphicsExtractor gui, int cx, int cy) {
        disc(gui, cx, cy, RADIUS + 2, RIM);
        disc(gui, cx, cy, RADIUS, FACE);
    }

    /**
     * 用水平掃描線填一個圓。
     *
     * <p>每一列算出圓在該高度的半寬，填成一條橫線。{@code r + 0.5} 是為了讓邊緣落在
     * 像素中心上——少了它，圓的上下兩端會各缺一列，看起來像被切平的。
     */
    private static void disc(GuiGraphicsExtractor gui, int cx, int cy, int r, int colour) {
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.sqrt((r + 0.5) * (r + 0.5) - dy * dy);
            gui.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, colour);
        }
    }

    /** 刻度：每 10 km/h 一根短的，每 20 km/h 一根長的並標上數字。紅線區的刻度變紅。 */
    private void ticks(GuiGraphicsExtractor gui, int cx, int cy) {
        Font font = Minecraft.getInstance().font;
        for (int kmh = 0; kmh <= (int) scale; kmh += MINOR_STEP) {
            boolean major = kmh % MAJOR_STEP == 0;
            int colour = kmh >= scale * REDLINE_FRACTION ? TICK_RED : (major ? TICK_MAJOR : TICK);
            int length = major ? 8 : 4;
            int width = major ? 2 : 1;

            float angle = angleOf(kmh);
            bar(gui, cx, cy, angle, RADIUS - 4 - length, length, width, colour);

            if (major) {
                // 數字排在刻度內側。用刻度的角度去算位置，錶面轉幾度數字就跟著繞幾度
                double rad = Math.toRadians(angle);
                int tx = cx + (int) Math.round(Math.sin(rad) * (RADIUS - 20));
                int ty = cy - (int) Math.round(Math.cos(rad) * (RADIUS - 20));
                String label = Integer.toString(kmh);
                gui.text(font, label, tx - font.width(label) / 2, ty - 4,
                        kmh >= scale * REDLINE_FRACTION ? TICK_RED : TEXT_DIM);
            }
        }
    }

    /** 指針，加上中央的軸心蓋住根部。 */
    private void needle(GuiGraphicsExtractor gui, int cx, int cy, float kmh) {
        bar(gui, cx, cy, angleOf(kmh), 6, RADIUS - 14, 2, NEEDLE);
        disc(gui, cx, cy, 4, HUB);
        disc(gui, cx, cy, 2, NEEDLE);
    }

    /** 中央的數字與單位；熄火時整個換成提示。 */
    private void readout(GuiGraphicsExtractor gui, Font font, int cx, int cy, RoadVehicle scooter) {
        if (scooter.stalled()) {
            String msg = "熄火";
            gui.text(font, msg, cx - font.width(msg) / 2, cy + RADIUS / 2 - 4, STALL);
            return;
        }
        String value = Integer.toString(Math.round(scooter.speedKmh()));
        gui.text(font, value, cx - font.width(value) / 2, cy + RADIUS / 2 - 8, TEXT);
        gui.text(font, "km/h", cx - font.width("km/h") / 2, cy + RADIUS / 2 + 2, TEXT_DIM);
    }

    /** 時速對應到錶面上的角度（度，從正上方順時針）。 */
    private float angleOf(float kmh) {
        return START + SWEEP * Mth.clamp(kmh / scale, 0f, 1f);
    }

    /**
     * 從圓心往外、在指定角度上畫一根長條（刻度或指針都用它）。
     *
     * <p>轉座標系比自己算四個角的座標可靠：算座標的話每一根都要處理三角函數的正負號與
     * 取整，斜著的刻度還會因為捨去而長短不一。這裡改成把整個畫布轉到刻度的方向，
     * 填一個正正方方的矩形，再轉回來。
     */
    private static void bar(GuiGraphicsExtractor gui, int cx, int cy,
                           float angle, int from, int length, int width, int colour) {
        Matrix3x2fStack pose = gui.pose();
        pose.pushMatrix();
        pose.translate(cx + 0.5f, cy + 0.5f);
        // 錶面的 0 度在正上方（螢幕的 -Y），而矩陣的 0 度在 +X，所以要退 90 度
        pose.rotate((float) Math.toRadians(angle - 90.0));
        gui.fill(from, -width / 2, from + length, -width / 2 + width, colour);
        pose.popMatrix();
    }
}
