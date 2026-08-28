package com.xinbow99.taiwan.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 連棟街屋：騎樓、鐵捲門、鐵窗、突出招牌、頂樓加蓋、水塔。**台灣街景的底色。**
 *
 * <h2>單元沿街排，不是填滿街廓</h2>
 * <p>這裡的座標不是「街廓內的第幾格」，而是 {@link Town#roadDepth}——離街緣幾格。
 * 用街廓邊界的話，主幹道變寬時騎樓會被馬路吃掉兩格；用離街距離，建築會自己退後。
 *
 * <p>一個「單元」是一戶：沿街寬 5～7 格、進深 13 格、樓高 3～5 層。整條街的單元寬度一樣
 * （同一個 {@code rowSalt}），因為連棟街屋本來就是一次蓋一整排的。樓高與顏色各戶不同，
 * 因為那是各自後來改的。這個「骨架一致、表皮各異」的分配方式，就是連棟街屋看起來的樣子。
 *
 * <h2>騎樓是這裡唯一不能省的東西</h2>
 * <p>二樓以上壓在人行道上方、一樓退進去三格、退進去那段每隔一戶一根柱子。少了它，
 * 同一排房子會讀成任何一個亞洲城市；有了它才是台灣。
 *
 * <h2>店名一律原創惡搞</h2>
 * <p>見 {@link ShopName}。不用任何真實商標。
 */
public final class Shophouse {

    /** 騎樓進深（含柱子那一格）。7 格寬的街配 3 格騎樓，兩邊加起來人走得過去。 */
    private static final int ARCADE = 3;
    /** 店面總進深。13 格 ＝ 騎樓 3 ＋ 門面 1 ＋ 室內 8 ＋ 後牆 1。 */
    private static final int BAND = 13;
    /** 一層樓高。3 格淨空 ＋ 1 格樓板。 */
    private static final int STOREY = 4;

    /** 一樓做什麼生意。 */
    public enum Kind {
        /** 純住家／車庫，鐵捲門拉下來的那種。 */
        TENEMENT,
        /** 手搖飲料店。 */
        DRINK,
        /** 便利商店。 */
        CONVENIENCE
    }

    private Shophouse() {
    }

    public static void build(int x, int z, int base, Town t, Settings s, Urban.Cursor out) {
        int dx = t.depthX(x);
        int dz = t.depthZ(z);
        // 路口不蓋東西也不掛招牌：那裡要留給轉彎的視線，而且兩條街的單元編號在這裡會打架
        if (dx < 0 && dz < 0) return;

        boolean alongZ = t.facesXAxis(x, z);
        int d = alongZ ? dx : dz;
        if (d < -1 || d >= BAND) return;

        int alongOff = alongZ ? z - t.centerZ() : x - t.centerX();
        int side = alongZ ? t.sideX(x) : t.sideZ(z);
        int line = alongZ ? t.lineX(x) : t.lineZ(z);

        // 整排共用的骨架：單元寬度。同一條街的同一側才是同一排
        int rowSalt = Noise.hash(line * 2 + (alongZ ? 0 : 1), t.salt() ^ 0x2C71, side);
        int width = 5 + Math.floorMod(rowSalt >>> 5, 3);
        int unit = Math.floorDiv(alongOff, width);
        int a = Math.floorMod(alongOff, width);
        int unitSalt = Noise.hash(unit, rowSalt, side * 31 + line);

        int floors = 3 + Math.floorMod(unitSalt >>> 3, 3);
        int floorY = base + 1;
        int roofY = floorY + floors * STOREY;

        Kind kind = kind(unitSalt, s);
        DyeColor signColor = Palette.signColor(unitSalt >>> 17);

        // 街上那一格：只可能有突出招牌
        if (d < 0) {
            protrudingSign(a, alongZ ? z : x, floorY, unitSalt, kind, signColor,
                    Urban.faceToStreet(alongZ, side), out);
            return;
        }

        ground(x, z, d, a, width, floorY, unitSalt, kind, out);
        upper(d, a, width, floors, floorY, unitSalt, out);
        fascia(d, a, width, floorY, unitSalt, kind, signColor, out);
        roof(d, a, width, roofY, unitSalt, out);
    }

    /**
     * 一樓做什麼生意。權重來自設定檔，**設成 0 就是關掉那一類**。
     *
     * <p>飲料店與超商是「一樓的用途」而不是「一整棟建築」，因為它們在真實世界裡就是這樣：
     * 一間手搖店樓上還是住人的。把它們做成獨立的建築物，街景會變成一排各自獨立的盒子。
     */
    private static Kind kind(int unitSalt, Settings s) {
        Settings.Buildings b = s.buildings();
        int total = b.tenement() + b.drinkShop() + b.convenience();
        if (total <= 0) return Kind.TENEMENT;
        int roll = Math.floorMod(unitSalt >>> 23, total);
        if (roll < b.drinkShop()) return Kind.DRINK;
        roll -= b.drinkShop();
        if (roll < b.convenience()) return Kind.CONVENIENCE;
        return Kind.TENEMENT;
    }

    // ------------------------------------------------------------------ 一樓

    private static void ground(int x, int z, int d, int a, int width, int floorY,
                               int unitSalt, Kind kind, Urban.Cursor out) {
        // 地板。騎樓鋪磚、室內磨石子——腳下的材質換掉，人就知道自己從街上走進店裡了
        out.set(floorY, d < ARCADE ? Palette.SIDEWALK_BRICK : Palette.CONCRETE_POLISHED);

        boolean party = a == 0;
        for (int i = 1; i < STOREY; i++) {
            int y = floorY + i;

            if (d < ARCADE) {
                // 騎樓：柱子落在單元交界的最外緣，其餘全空。這一段是連續的，
                // 整個街廓可以從頭走到尾不淋雨
                if (party && d == 0) out.set(y, Palette.CONCRETE_ROUGH);
                continue;
            }
            if (party) {
                out.set(y, Palette.CONCRETE_ROUGH);
                continue;
            }
            if (d == ARCADE) {
                out.set(y, front(a, width, i, unitSalt, kind));
                continue;
            }
            if (d == BAND - 1) {
                out.set(y, Palette.wall(unitSalt));
            }
            // 室內留空，讓 interior() 決定要不要擺東西
        }

        if (d >= ARCADE && d < BAND - 1 && !party) {
            interior(x, z, d, a, width, floorY, unitSalt, kind, out);
        }
        // 騎樓下的排隊人龍空間：飲料店門口擺兩張塑膠椅
        if (kind == Kind.DRINK && d == 1 && (a == 2 || a == 3)) {
            out.set(floorY + 1, Palette.PLASTIC_STOOL);
        }
    }

    /**
     * 門面那一格。三種生意的門面差別，是走在街上唯一分得出它們的線索。
     *
     * @param i 離地板幾格（1～3）
     */
    private static BlockState front(int a, int width, int i, int unitSalt, Kind kind) {
        return switch (kind) {
            // 鐵捲門拉下來，留一道側門進得去。捲門箱在最上面那格
            case TENEMENT -> {
                if (a == 1) yield Palette.AIR;
                if (i == STOREY - 1) yield Palette.SHUTTER_BOX;
                yield (unitSalt & 1) == 0 ? Palette.SHUTTER : Palette.SHUTTER_NEW;
            }
            // 點餐窗口：檯面高一格，上面留空，再上面是玻璃
            case DRINK -> {
                if (a >= 1 && a <= 2) {
                    if (i == 1) yield Palette.COUNTER;
                    if (i == 2) yield Palette.AIR;
                    yield Palette.GLASS;
                }
                if (a == 3) yield Palette.AIR;                       // 側邊的門
                yield i == STOREY - 1 ? Palette.SHUTTER_BOX : Palette.GLASS;
            }
            // 自動門：正中央兩格全開，兩側落地玻璃
            case CONVENIENCE -> {
                int mid = width / 2;
                if (a == mid || a == mid - 1) yield Palette.AIR;
                yield i == STOREY - 1 ? Palette.SHUTTER_BOX : Palette.GLASS;
            }
        };
    }

    /** 店裡的東西。只擺在幾個固定位置，擺滿的話從騎樓看進去會是一團雜物。 */
    private static void interior(int x, int z, int d, int a, int width, int floorY,
                                 int unitSalt, Kind kind, Urban.Cursor out) {
        int y = floorY + 1;
        switch (kind) {
            case DRINK -> {
                // 封膜機與杯架靠著門面後方那一排
                if (d == ARCADE + 1) {
                    if (a == 1) out.set(y, Palette.SEALER);
                    if (a == 2) out.set(y, Palette.DRINK_SHELF);
                    if (a == width - 2) out.set(y, Palette.CRATE);
                }
                // 菜單看板掛在店內後牆上，從點餐窗口看得到
                if (d == ARCADE + 2 && a >= 1 && a <= 3) {
                    out.set(floorY + 3, Palette.signBoard(Palette.signColor(unitSalt >>> 17)));
                }
            }
            case CONVENIENCE -> {
                // 關東煮台就在櫃檯旁邊，進門左手邊第一個
                if (d == ARCADE + 1 && a == 1) out.set(y, Palette.ODEN);
                if (d == ARCADE + 1 && a >= 2 && a <= 3) out.set(y, Palette.COUNTER);
                // 貨架，兩排
                if ((d == ARCADE + 4 || d == ARCADE + 6) && a >= 1 && a <= width - 2) {
                    out.set(y, Palette.SHELF);
                }
                if (d == ARCADE + 3 && a == width - 2) out.set(y, Palette.LANTERN);
            }
            case TENEMENT -> {
                // 車庫兼客廳，只放個樓梯上去
                if (d == BAND - 2 && a == 1) out.set(y, Palette.LADDER);
            }
        }
        // 一樓天花板的燈。沒有它，入夜之後從騎樓往裡看是全黑的
        if (d == ARCADE + 2 && a == width / 2) {
            out.set(floorY + STOREY - 1, Palette.SIGN_LAMP);
        }
    }

    // ------------------------------------------------------------------ 二樓以上

    private static void upper(int d, int a, int width, int floors, int floorY,
                              int unitSalt, Urban.Cursor out) {
        BlockState wall = Palette.wall(unitSalt);

        for (int k = 1; k < floors; k++) {
            int slab = floorY + k * STOREY;
            // 樓板鋪滿整個進深，包含騎樓上方——**二樓壓在人行道上，這就是騎樓**
            out.set(slab, Palette.CONCRETE_RAW);

            for (int i = 1; i < STOREY; i++) {
                int y = slab + i;
                if (a == 0) {
                    out.set(y, Palette.CONCRETE_ROUGH);
                } else if (d == 0) {
                    out.set(y, window(a, width, i) ? Palette.IRON_GRILLE : wall);
                } else if (d == BAND - 1) {
                    out.set(y, wall);
                }
            }
        }
    }

    /**
     * 鐵窗的位置。
     *
     * <p>整面開窗會讓建築讀成辦公大樓；隔一格開一扇、只開中間兩格高，才是住宅的比例。
     * 而且開口用鐵窗而不是玻璃：台灣的公寓外牆上就是一格一格的鐵窗。
     */
    private static boolean window(int a, int width, int i) {
        return i >= 1 && i <= 2 && a >= 1 && a <= width - 2 && a % 2 == 1;
    }

    // ------------------------------------------------------------------ 招牌

    /**
     * 一樓與二樓之間那一條橫的招牌。
     *
     * <p>它蓋在二樓樓板的外緣上，所以從騎樓下抬頭就看得到——那正是台灣招牌掛的位置。
     * 燈藏在後面一格：招牌到晚上會亮，是這個街景的一半。
     */
    private static void fascia(int d, int a, int width, int floorY, int unitSalt,
                               Kind kind, DyeColor color, Urban.Cursor out) {
        int y = floorY + STOREY;
        if (kind == Kind.TENEMENT && (unitSalt & 2) == 0) return;   // 純住家不一定有招牌

        if (d == 0) {
            // a == 2 那一格用實心板：突出招牌就掛在它外面，而彩色玻璃撐不住告示牌
            // ——招牌會在第一次方塊更新時掉下來
            out.set(y, a == 0 || a == 2 || a == width - 1
                    ? Palette.signBoard(color)
                    : Palette.signGlass(color));
        } else if (d == 1 && a >= 1 && a <= width - 2) {
            out.set(y, Palette.SIGN_LAMP);
        }
    }

    /**
     * 突出招牌：伸出騎樓外緣、掛在馬路上空的那一塊，上面有店名。
     *
     * <p>一戶只掛一塊（{@code a == 2}），不然整條街的上空會被招牌封死，而且從騎樓下往前看
     * 會看不到下一家。掛的高度剛好在二樓樓板外緣下面——那正是台灣招牌掛的位置。
     *
     * @param alongWorld 沿街的**世界座標**，用來避開電線桿
     */
    private static void protrudingSign(int a, int alongWorld, int floorY, int unitSalt,
                                       Kind kind, DyeColor color, Direction face,
                                       Urban.Cursor out) {
        if (a != 2) return;
        // 電線桿那一格不掛：招牌會插進桿子裡，而桿子先蓋、招牌後蓋，結果是桿子斷一截
        if (Math.floorMod(alongWorld, 13) == 0) return;

        String[] name = switch (kind) {
            case DRINK -> ShopName.drink(unitSalt >>> 11);
            case CONVENIENCE -> ShopName.convenience(unitSalt >>> 11);
            // 純住家只有一半掛東西，而且掛的是出租廣告或鐵工廠的招牌，不是店招
            case TENEMENT -> (unitSalt & 2) == 0 ? ShopName.tenement(unitSalt >>> 11) : null;
        };
        if (name == null) return;

        out.sign(floorY + STOREY, Palette.wallSign(face), color, name);
    }

    // ------------------------------------------------------------------ 屋頂

    /**
     * 屋頂板、女兒牆、鐵皮加蓋、水塔。
     *
     * <p>頂樓加蓋不是裝飾，是**台灣從空中看下去的樣子**：一片藍色與鏽紅的鐵皮。
     * 四分之三的戶數有，剩下的留空當曬衣場——全部都有的話反而假。
     */
    private static void roof(int d, int a, int width, int roofY, int unitSalt, Urban.Cursor out) {
        out.set(roofY, Palette.CONCRETE_RAW);

        // 女兒牆。屋頂沒有邊界的話，整排房子從遠處看是一塊平的板子
        if (d == 0 || d == BAND - 1) out.set(roofY + 1, Palette.CONCRETE_ROUGH);

        boolean shed = (unitSalt >>> 9 & 3) != 0;
        int front = ARCADE + 1;
        int back = BAND - 3;
        if (shed && d >= front && d <= back) {
            BlockState tin = Palette.tin(unitSalt >>> 13);
            boolean edge = a == 0 || d == front || d == back;
            for (int i = 1; i <= 3; i++) {
                if (edge) out.set(roofY + i, tin);
            }
            out.set(roofY + 4, tin);
        }

        // 水塔。放在加蓋後面那一格，兩者不會打架
        if (d == BAND - 2 && a == 1) {
            out.set(roofY + 1, Palette.TANK_LEG);
            out.set(roofY + 2, Palette.TANK_LEG);
            out.set(roofY + 3, (unitSalt & 4) == 0 ? Palette.TANK_STEEL : Palette.TANK_ORANGE);
        }
    }
}
