package com.xinbow99.taiwan.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

/**
 * 宮廟：廟埕、三川殿、龍柱、燕尾脊、金爐、香爐。
 *
 * <h2>廟埕跟廟一樣重要</h2>
 * <p>台灣的廟不是一棟建築，是**一塊空地加上空地盡頭的一棟建築**。廟埕是市場、是停車場、
 * 是廟會的場地。把整個街廓塞滿廟身的話，那會讀成一座宮殿，不是一間庄頭廟。
 * 所以前面五分之二留空，而且鋪成跟街道不同的材質。
 *
 * <h2>正規化座標</h2>
 * <p>廟要面向街，而街廓的四邊都可能是街。與其寫四份程式，不如**把座標轉到正面朝
 * {@code fv = 0} 的方向**再蓋，出口再轉回去。{@link #canon} 就是那個轉換。
 * 這也是為什麼廟身必須畫在一個正方形裡：非正方形的旋轉會超出街廓。
 *
 * <h2>燕尾脊</h2>
 * <p>屋脊兩端往上翹。那是台灣廟宇從遠處唯一認得出來的輪廓——比屋瓦的顏色更早被看到。
 * 只翹兩格，翹太多會變成日式的鴟尾。
 */
public final class Temple {

    /** 廟埕佔街廓進深的幾分之幾。 */
    private static final float PLAZA = 0.42f;
    /** 廟身離街廓邊界留幾格。 */
    private static final int MARGIN = 4;
    /** 牆高。 */
    private static final int WALL = 5;

    private Temple() {
    }

    public static void build(int x, int z, int base, Town t, int lx, int lz, Urban.Cursor out) {
        int x0 = t.lotMinX(lx);
        int x1 = t.lotMaxX(lx);
        int z0 = t.lotMinZ(lz);
        int z1 = t.lotMaxZ(lz);

        // 取街廓裡最大的正方形，置中。旋轉要在正方形上做才不會轉出界
        int n = Math.min(x1 - x0 + 1, z1 - z0 + 1);
        if (n < 20) return;                         // 太小的街廓蓋不出一間廟，留空地
        int u = x - (x0 + (x1 - x0 + 1 - n) / 2);
        int v = z - (z0 + (z1 - z0 + 1 - n) / 2);
        if (u < 0 || v < 0 || u >= n || v >= n) return;

        int salt = t.lotSalt(lx, lz);
        int rot = Math.floorMod(salt >>> 5, 4);
        int fu = canon(u, v, n, rot, true);
        int fv = canon(u, v, n, rot, false);

        int floorY = base + 1;
        int plazaDepth = Math.round(n * PLAZA);
        int c = n / 2;

        if (fv < plazaDepth) {
            plaza(fu, fv, n, c, plazaDepth, floorY, salt, out);
            return;
        }
        hall(fu, fv, n, c, plazaDepth, floorY, salt, FRONT[rot], out);
    }

    /**
     * 各旋轉下，「從廟身看向廟埕」是世界座標的哪個方向。
     *
     * <p>正規化座標裡正面永遠朝 {@code fv} 減少的方向；這張表就是那個方向轉回世界座標的結果。
     * 廟額（廟名匾）要朝這個方向掛，不然人站在廟埕上看到的是背面。
     */
    private static final Direction[] FRONT = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
    };

    /**
     * 把街廓內座標轉成「正面朝 {@code fv = 0}」的座標。
     *
     * @param wantU true 取 fu，false 取 fv
     */
    private static int canon(int u, int v, int n, int rot, boolean wantU) {
        return switch (rot) {
            case 0 -> wantU ? u : v;
            case 1 -> wantU ? v : n - 1 - u;
            case 2 -> wantU ? n - 1 - u : n - 1 - v;
            default -> wantU ? n - 1 - v : u;
        };
    }

    // ------------------------------------------------------------------ 廟埕

    private static void plaza(int fu, int fv, int n, int c, int plazaDepth,
                              int floorY, int salt, Urban.Cursor out) {
        out.set(floorY, Palette.TEMPLE_FLOOR);

        // 金爐：燒金紙的塔，放在廟埕角落。它是廟埕上唯一有高度的東西，
        // 所以它同時也是「這個街廓是廟」的遠距離線索
        if (fu >= 2 && fu <= 4 && fv >= 2 && fv <= 4) {
            boolean core = fu == 3 && fv == 3;
            for (int i = 1; i <= 4; i++) {
                out.set(floorY + i, Palette.FURNACE_STACK);
            }
            if (core) {
                out.set(floorY + 4, Palette.CAMPFIRE);
                out.set(floorY + 5, Palette.FURNACE_STACK);
            }
            return;
        }

        // 香爐：正對中門，在廟埕靠廟身那一端。人是先過香爐才進廟的
        if (fu == c && fv == plazaDepth - 3) {
            out.set(floorY + 1, Palette.INCENSE);
            return;
        }

        // 擲筊區：香爐兩側的兩張供桌，配幾張塑膠椅
        if (fv == plazaDepth - 3 && (fu == c - 3 || fu == c + 3)) {
            out.set(floorY + 1, Palette.STALL_TABLE);
            return;
        }
        if (fv == plazaDepth - 4 && (fu == c - 3 || fu == c + 3)) {
            out.set(floorY + 1, Palette.PLASTIC_STOOL);
            return;
        }

        // 廟埕邊上的燈籠柱。只在最前緣，等距四根
        if (fv == 1 && Math.floorMod(fu - 2, 7) == 0 && fu > 1 && fu < n - 2) {
            for (int i = 1; i <= 3; i++) out.set(floorY + i, Palette.POLE);
            out.set(floorY + 4, Palette.LANTERN);
        }
        // 台階：廟埕與廟身之間差半格
        if (fv == plazaDepth - 1) {
            out.set(floorY + 1, Palette.TEMPLE_STEP);
        }
        if (fv == 0 && (salt & 1) == 0 && fu == c) {
            out.set(floorY + 1, Palette.INCENSE);   // 廟口那個小香爐
        }
    }

    // ------------------------------------------------------------------ 廟身

    private static void hall(int fu, int fv, int n, int c, int plazaDepth, int floorY,
                             int salt, Direction front, Urban.Cursor out) {
        int platTop = floorY + 1;                   // 廟身抬高一格
        int uMin = MARGIN;
        int uMax = n - 1 - MARGIN;
        int vMin = plazaDepth + 1;
        int vMax = n - 1 - MARGIN;

        // 月台：廟身前面那一圈鋪面，範圍比廟身大
        out.set(floorY, Palette.TEMPLE_FLOOR);
        if (fv >= plazaDepth) out.set(platTop, Palette.TEMPLE_FLOOR);

        // 龍柱：站在廟身前緣，夾著中門。用整根雕花石英，柱頭鑲金
        if (fv == vMin - 1 && (fu == c - 5 || fu == c + 5)) {
            for (int i = 1; i <= WALL; i++) out.set(platTop + i, Palette.TEMPLE_COLUMN);
            out.set(platTop + WALL + 1, Palette.TEMPLE_GOLD);
            return;
        }
        // 廟額：中門上方那塊匾。廟名是唯一能把一間廟跟隔壁那間分開的東西
        if (fv == vMin - 1 && fu == c) {
            out.sign(platTop + 4, Palette.wallSign(front), DyeColor.YELLOW,
                    ShopName.temple(salt >>> 3));
        }
        if (fu < uMin || fu > uMax || fv < vMin || fv > vMax) {
            roof(fu, fv, c, uMin, uMax, vMin, vMax, platTop, out);
            return;
        }

        boolean perimeter = fu == uMin || fu == uMax || fv == vMin || fv == vMax;
        if (perimeter) {
            for (int i = 1; i <= WALL; i++) {
                out.set(platTop + i, i == WALL ? Palette.TEMPLE_TRIM : Palette.TEMPLE_WALL);
            }
            // 三川殿：正面三個門洞。中門最寬，兩側的偏門各一格——這是三川殿的定義
            if (fv == vMin && door(fu, c)) {
                for (int i = 1; i <= 3; i++) out.set(platTop + i, Palette.AIR);
            }
        } else {
            // 神龕：正對中門的後牆前，一塊金磚。進門第一眼看到的就是它
            if (fv == vMax - 1 && fu >= c - 1 && fu <= c + 1) {
                out.set(platTop + 1, Palette.TEMPLE_GOLD);
            }
            if (fv == vMin + 2 && fu == c) out.set(platTop + 1, Palette.INCENSE);
            if ((fu == c - 3 || fu == c + 3) && fv == vMax - 2) out.set(platTop + 3, Palette.LANTERN);
        }

        roof(fu, fv, c, uMin, uMax, vMin, vMax, platTop, out);
    }

    /** 中門三格、兩側偏門各一格。 */
    private static boolean door(int fu, int c) {
        int d = Math.abs(fu - c);
        return d <= 1 || d == 4;
    }

    /**
     * 屋頂：兩坡，屋脊平行於正面，出簷一格。
     *
     * <p>出簷是必要的：屋頂跟牆切齊的話，整棟會讀成一個加了帽子的盒子。挑出去一格，
     * 牆面才會落在陰影裡。
     */
    private static void roof(int fu, int fv, int c, int uMin, int uMax, int vMin, int vMax,
                             int platTop, Urban.Cursor out) {
        if (fu < uMin - 1 || fu > uMax + 1 || fv < vMin - 1 || fv > vMax + 1) return;

        int mid = (vMin + vMax) / 2;
        int span = (vMax - vMin) / 2 + 1;
        int dist = Math.abs(fv - mid);
        int rise = Math.max(0, (span - dist + 1) / 2);
        int y = platTop + WALL + rise;

        out.set(y, Palette.TEMPLE_ROOF);

        // 補下面一格：只鋪一層的話，坡度變化處會露出天空。
        //
        // 但**最外圈的出簷不補**：那一圈的坡度沒有變化（它跟往內一格之間的落差由內側那格
        // 自己補掉），而補下去剛好會蓋到中門上方的廟額。屋簷贏過招牌的話，招牌會變成一塊
        // 帶著 block entity 的屋瓦，載入時就是一行 "Invalid block entity" 然後字整個消失
        boolean eave = fu == uMin - 1 || fu == uMax + 1 || fv == vMin - 1 || fv == vMax + 1;
        if (!eave) out.set(y - 1, Palette.TEMPLE_ROOF_SLAB);

        // 燕尾脊：屋脊兩端往上翹兩格。廟宇的輪廓就是靠這兩下認出來的
        if (dist == 0) {
            out.set(y + 1, Palette.TEMPLE_ROOF);
            if (fu == uMin - 1 || fu == uMax + 1) {
                out.set(y + 2, Palette.TEMPLE_ROOF);
                out.set(y + 3, Palette.TEMPLE_GOLD);
            } else if (fu == uMin || fu == uMax) {
                out.set(y + 2, Palette.TEMPLE_ROOF);
            }
        }
    }
}
