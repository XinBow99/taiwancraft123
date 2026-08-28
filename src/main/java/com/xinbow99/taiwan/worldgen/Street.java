package com.xinbow99.taiwan.worldgen;

import net.minecraft.core.Direction;

/**
 * 街道上方的東西：電線桿、電線、街緣。
 *
 * <h2>電線是這條街的天花板</h2>
 * <p>台灣的街道抬頭看到的不是天空，是一層橫過去的電纜。少了它，同樣一排房子會讀成
 * 「某個亞洲城市」；有了它才會讀成台灣。這是整個聚落裡**投資報酬率最高的十行程式**。
 *
 * <p>電線用橫躺的鏈條：原版的鏈條有 {@code AXIS}，所以它可以是水平的，而且它細——
 * 用任何實心方塊做電線，那都會變成一道橋。
 *
 * <h2>桿距 13 格</h2>
 * <p>比一個店面單元（5～7 格）大、比一個街廓（30 格）小。跟店面同週期的話電線桿會
 * 每一戶門口一根，看起來像柵欄；跟街廓同週期的話一條街只有兩根，電線會垂成一條長線。
 */
public final class Street {

    /** 電線桿的間距。 */
    private static final int SPACING = 13;
    /** 桿高（從地坪算起）。電線掛在頂端下面一格。 */
    private static final int POLE_HEIGHT = 7;

    private Street() {
    }

    /** 街道上方，從 {@code base + 1} 起。 */
    public static void build(int x, int z, int base, Town t, Urban.Cursor out) {
        int dx = t.depthX(x);
        int dz = t.depthZ(z);

        // 南北向的街：電線桿排在街緣，電線沿 Z 走。路口不放——那裡要留給轉彎的視線
        if (dx == -1 && dz >= 0) {
            line(x, z, base, t, out, z, Direction.Axis.Z);
            return;
        }
        if (dz == -1 && dx >= 0) {
            line(x, z, base, t, out, x, Direction.Axis.X);
        }
    }

    /**
     * 街緣的一格：不是電線桿就是從它底下穿過去的電線。
     *
     * @param along 沿街的座標，用它決定這裡是不是桿位
     * @param axis  電線的走向
     */
    private static void line(int x, int z, int base, Town t, Urban.Cursor out, int along, Direction.Axis axis) {
        // 街緣抬高半格。這一階是騎樓與馬路的分界，人走上去會感覺到
        out.set(base + 1, Palette.CURB);

        if (Math.floorMod(along, SPACING) == 0) {
            for (int i = 1; i < POLE_HEIGHT; i++) {
                out.set(base + 1 + i, Palette.POLE);
            }
            out.set(base + POLE_HEIGHT + 1, Palette.POLE_TOP);
            // 桿上那盞路燈。台灣的路燈就是掛在電線桿上的，不是獨立的燈柱
            if (Math.floorMod(Noise.hash(x, t.salt(), z), 2) == 0) {
                out.set(base + POLE_HEIGHT, Palette.LANTERN);
            }
            return;
        }

        // 只掛最上面那一條。低的那一條會跟店家的突出招牌搶同一格，而招牌後蓋，
        // 結果是每隔五六格就在電線上開一個洞——一條斷斷續續的電線比沒有電線更糟
        out.set(base + POLE_HEIGHT, Palette.wire(axis));
    }
}
