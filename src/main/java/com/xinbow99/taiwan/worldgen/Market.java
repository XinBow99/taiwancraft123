package com.xinbow99.taiwan.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 傳統市場／夜市：一格一格的攤位、遮陽棚、塑膠椅、油鍋、遊戲攤。
 *
 * <h2>市場是「走道」的形狀，不是「建築」的形狀</h2>
 * <p>攤位本身不重要，重要的是攤位之間那條**只有三格寬的走道**。夜市之所以是夜市，
 * 是因為它擠。攤位排得太鬆就變成園遊會，排得太密人走不過去——4 格攤位配 3 格走道，
 * 是實際走起來剛好的比例。
 *
 * <h2>棚子要低</h2>
 * <p>棚頂壓在頭上四格高。挑高的話那是體育館，不是市場——市場的天花板是伸手就摸得到的。
 * 而且低棚會把光關在裡面，燈籠才有作用。
 */
public final class Market {

    /** 攤位邊長。 */
    private static final int STALL = 4;
    /** 走道寬。 */
    private static final int AISLE = 3;
    /** 攤位加走道的週期。 */
    private static final int PERIOD = STALL + AISLE;
    /** 棚頂離地幾格。 */
    private static final int CANOPY = 4;

    private Market() {
    }

    public static void build(int x, int z, int base, Town t, int lx, int lz, Urban.Cursor out) {
        int x0 = t.lotMinX(lx);
        int z0 = t.lotMinZ(lz);
        int w = t.lotMaxX(lx) - x0 + 1;
        int h = t.lotMaxZ(lz) - z0 + 1;

        int u = x - x0;
        int v = z - z0;
        int floorY = base + 1;

        // 市場的地是水泥，跟街道的柏油分得開。腳下換材質，人就知道自己走進市場了
        out.set(floorY, Palette.CONCRETE_POLISHED);

        // 外圈留一格當出入口，攤位不貼著街緣擺——那一格是騎摩托車進來停的地方
        if (u < 1 || v < 1 || u >= w - 1 || v >= h - 1) return;

        int su = Math.floorMod(u - 1, PERIOD);
        int sv = Math.floorMod(v - 1, PERIOD);
        int cellU = Math.floorDiv(u - 1, PERIOD);
        int cellV = Math.floorDiv(v - 1, PERIOD);

        if (su >= STALL || sv >= STALL) {
            aisle(u, v, floorY, t, cellU, cellV, out);
            return;
        }
        // 攤位整格要塞得進街廓，半個攤子比沒有更醜
        if ((cellU + 1) * PERIOD >= w - 1 || (cellV + 1) * PERIOD >= h - 1) return;

        stall(su, sv, floorY, Noise.hash(cellU, t.lotSalt(lx, lz), cellV), out);
    }

    /**
     * 一個攤位。
     *
     * <p>四角立柱、頂上一塊布、面向走道那一邊是檯面，裡面擺一台機器。所有攤子共用這個骨架，
     * 差別只在那台機器——真實的夜市也是這樣，攤車是同一批鐵工廠做的。
     */
    private static void stall(int su, int sv, int floorY, int salt, Urban.Cursor out) {
        boolean corner = (su == 0 || su == STALL - 1) && (sv == 0 || sv == STALL - 1);

        // 棚架的四根柱子
        if (corner) {
            for (int i = 1; i < CANOPY; i++) out.set(floorY + i, Palette.STALL_POST);
        }
        // 遮陽棚。紅白藍那種條紋布，一個攤子一個顏色
        out.set(floorY + CANOPY, Palette.awning(salt >>> 3));

        // 面向走道那一側的檯面
        if (sv == STALL - 1 && su >= 1 && su <= STALL - 2) {
            out.set(floorY + 1, Palette.COUNTER);
            return;
        }
        if (corner) return;

        // 攤子後面那塊布。它同時是招牌的靠背——沒有它，寫著「蚵仔煎」的牌子撐不住
        if (sv == 0) {
            for (int i = 1; i < CANOPY; i++) out.set(floorY + i, Palette.awning(salt));
            return;
        }
        // 品項招牌，面向走道。夜市的攤子彼此長得一樣，靠的就是這塊牌子分辨
        if (sv == 1 && su == 1) {
            out.sign(floorY + 3, Palette.wallSign(Direction.SOUTH),
                    Palette.signColor(salt >>> 5), ShopName.stall(salt >>> 11));
        }

        // 攤子的內容。權重不用設定檔——市場本身已經是一個可以關掉的建築類型了
        int kind = Math.floorMod(salt >>> 7, 5);
        if (su == 1 && sv == 1) {
            out.set(floorY + 1, switch (kind) {
                case 0 -> Palette.FRYER;          // 油鍋
                case 1 -> Palette.SMOKER;         // 烤台
                case 2 -> Palette.STALL_TABLE;    // 生鮮攤的檯子
                case 3 -> Palette.CRATE;          // 蔬果箱
                default -> Palette.GAME_TARGET;   // 遊戲攤
            });
        }
        if (su == 2 && sv == 1) {
            out.set(floorY + 1, kind == 4 ? Palette.GAME_TARGET : Palette.CRATE);
        }
        // 棚下那盞燈。市場入夜之後全靠它
        if (su == 1 && sv == 2) out.set(floorY + CANOPY - 1, Palette.LANTERN);
    }

    /** 走道：大部分留空，偶爾一張塑膠椅或一箱貨。 */
    private static void aisle(int u, int v, int floorY, Town t, int cellU, int cellV, Urban.Cursor out) {
        int n = Noise.hash(u, t.salt() ^ 0x77A1, v);
        if (Math.floorMod(n >>> 5, 11) != 0) return;

        BlockState prop = Math.floorMod(n >>> 9, 3) == 0 ? Palette.CRATE : Palette.PLASTIC_STOOL;
        out.set(floorY + 1, prop);
    }
}
