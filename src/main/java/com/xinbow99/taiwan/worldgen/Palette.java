package com.xinbow99.taiwan.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * 台灣街景的建材表。**26.2 的染色方塊 API 只在這一個檔案裡出現。**
 *
 * <h2>為什麼要有這一層</h2>
 * <p>26.2 把所有染色方塊合併成了 {@code ColorCollection}：沒有 {@code RED_WOOL} 這個欄位了，
 * 只有 {@code Blocks.WOOL.red()}；銅則是 {@code WeatheringCopperCollection}，
 * 要 {@code Blocks.CUT_COPPER.waxed().exposed()}。這種形狀的 API 每個版本都可能再動一次。
 *
 * <p>把它們全部關在這裡，之後版本升級只要改這一個檔案，五種建築的程式碼一行都不用碰。
 *
 * <h2>顏色的選法</h2>
 * <p>台灣街屋的顏色不是隨機的：**牆面是低彩度的磁磚**（白、米、淺灰、褐），
 * **招牌是高彩度的四五個顏色**（紅、黃、藍、綠），**鐵皮是藍綠或鏽紅**。
 * 三組各自成套、彼此不混，街景才會有「底色 ＋ 重音」的層次。全部隨機的話會變成一鍋彩色雜訊。
 */
public final class Palette {

    private Palette() {
    }

    // ------------------------------------------------------------------ 基本材料

    public static final BlockState AIR = Blocks.AIR.defaultBlockState();
    public static final BlockState CONCRETE_RAW = Blocks.SMOOTH_STONE.defaultBlockState();
    public static final BlockState CONCRETE_ROUGH = Blocks.STONE.defaultBlockState();
    public static final BlockState CONCRETE_POLISHED = Blocks.POLISHED_ANDESITE.defaultBlockState();
    public static final BlockState ASPHALT = Blocks.CONCRETE.black().defaultBlockState();
    public static final BlockState PAVEMENT = Blocks.CONCRETE.gray().defaultBlockState();
    /** 路面標線。白色的分向線，虛線畫法見 {@code Urban.surface}。 */
    public static final BlockState ROAD_LINE = Blocks.CONCRETE.white().defaultBlockState();
    /** 黃色的禁停線，畫在街緣。台灣的路緣不是紅就是黃。 */
    public static final BlockState CURB_LINE = Blocks.CONCRETE.yellow().defaultBlockState();
    public static final BlockState SIDEWALK_BRICK = Blocks.BRICKS.defaultBlockState();
    public static final BlockState CURB = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
    public static final BlockState IRON_GRILLE = Blocks.IRON_BARS.defaultBlockState();
    public static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
    public static final BlockState GLASS_PANE = Blocks.GLASS_PANE.defaultBlockState();
    public static final BlockState LANTERN = Blocks.LANTERN.defaultBlockState();
    public static final BlockState LADDER = Blocks.LADDER.defaultBlockState();
    public static final BlockState WATER = Blocks.WATER.defaultBlockState();

    /** 鐵捲門：拉下來的那種。銅的鏽色比鐵塊像用了二十年的門。 */
    public static final BlockState SHUTTER = Blocks.CUT_COPPER.waxed().exposed().defaultBlockState();
    /** 比較新的鐵捲門。 */
    public static final BlockState SHUTTER_NEW = Blocks.IRON_BLOCK.defaultBlockState();
    /** 捲門收起來的箱子。 */
    public static final BlockState SHUTTER_BOX = Blocks.CUT_COPPER_SLAB.waxed().exposed()
            .defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);

    /**
     * 磁磚外牆的四種底色。
     *
     * <p>都是低彩度：這是**背景**。街屋的顏色記憶點在招牌跟鐵皮上，牆面一旦搶戲，
     * 整條街就會讀成遊樂園。
     */
    private static final BlockState[] WALL_TILE = {
            Blocks.DYED_TERRACOTTA.white().defaultBlockState(),
            Blocks.DYED_TERRACOTTA.lightGray().defaultBlockState(),
            Blocks.DYED_TERRACOTTA.brown().defaultBlockState(),
            Blocks.QUARTZ_BRICKS.defaultBlockState(),
            Blocks.SMOOTH_QUARTZ.defaultBlockState(),
            Blocks.DYED_TERRACOTTA.cyan().defaultBlockState(),
    };

    /**
     * 鐵皮加蓋的顏色。
     *
     * <p>藍綠與鏽紅——這兩個是台灣屋頂從空中看下去的顏色。加第三個顏色進來就不對了。
     */
    private static final BlockState[] TIN_ROOF = {
            Blocks.DYED_TERRACOTTA.lightBlue().defaultBlockState(),
            Blocks.DYED_TERRACOTTA.cyan().defaultBlockState(),
            Blocks.CONCRETE.lightBlue().defaultBlockState(),
            Blocks.CUT_COPPER.waxed().weathered().defaultBlockState(),
            Blocks.DYED_TERRACOTTA.red().defaultBlockState(),
    };

    /** 招牌的底色。高彩度、只有這幾個，招牌才會讀成同一種東西。 */
    private static final DyeColor[] SIGN_COLOR = {
            DyeColor.RED, DyeColor.YELLOW, DyeColor.BLUE, DyeColor.GREEN,
            DyeColor.WHITE, DyeColor.ORANGE,
    };

    /** 這一棟的牆面磁磚。同一棟要一致，所以吃的是建築的 salt 不是座標。 */
    public static BlockState wall(int salt) {
        return WALL_TILE[Math.floorMod(salt, WALL_TILE.length)];
    }

    public static BlockState tin(int salt) {
        return TIN_ROOF[Math.floorMod(salt, TIN_ROOF.length)];
    }

    public static DyeColor signColor(int salt) {
        return SIGN_COLOR[Math.floorMod(salt, SIGN_COLOR.length)];
    }

    /** 招牌本體。 */
    public static BlockState signBoard(DyeColor color) {
        return Blocks.CONCRETE.pick(color).defaultBlockState();
    }

    /**
     * 會亮的招牌面。
     *
     * <p>台灣的招牌到晚上是**發光的**，這是整個街景的一半。用彩色玻璃包住螢石：
     * 直接用彩色方塊的話，入夜之後整條街會全黑，白天看起來對、晚上卻像廢墟。
     */
    public static BlockState signGlass(DyeColor color) {
        return Blocks.STAINED_GLASS.pick(color).defaultBlockState();
    }

    public static final BlockState SIGN_LAMP = Blocks.GLOWSTONE.defaultBlockState();

    /** 遮陽棚的布：紅白藍那種。 */
    public static BlockState awning(int salt) {
        return switch (Math.floorMod(salt, 3)) {
            case 0 -> Blocks.WOOL.red().defaultBlockState();
            case 1 -> Blocks.WOOL.blue().defaultBlockState();
            default -> Blocks.WOOL.white().defaultBlockState();
        };
    }

    /** 塑膠椅。紅色的那種，一疊十張的那種。 */
    public static final BlockState PLASTIC_STOOL = Blocks.CARPET.red().defaultBlockState();

    // ------------------------------------------------------------------ 店內設備

    /** 封膜機。織布機的形狀跟高度剛好，而且它有一塊面板。 */
    public static final BlockState SEALER = Blocks.LOOM.defaultBlockState();
    /** 杯架。 */
    public static final BlockState DRINK_SHELF = Blocks.CHISELED_BOOKSHELF.defaultBlockState();
    /** 超商貨架。 */
    public static final BlockState SHELF = Blocks.BARREL.defaultBlockState();
    /** 關東煮台：裝了水的鍋。滿的那一格才看得出來是湯。 */
    public static final BlockState ODEN = Blocks.WATER_CAULDRON.defaultBlockState()
            .setValue(BlockStateProperties.LEVEL_CAULDRON, 3);

    // ------------------------------------------------------------------ 宮廟

    public static final BlockState TEMPLE_WALL = Blocks.DYED_TERRACOTTA.red().defaultBlockState();
    public static final BlockState TEMPLE_TRIM = Blocks.DYED_TERRACOTTA.yellow().defaultBlockState();
    public static final BlockState TEMPLE_ROOF = Blocks.RED_NETHER_BRICKS.defaultBlockState();
    public static final BlockState TEMPLE_ROOF_STAIR = Blocks.RED_NETHER_BRICK_STAIRS.defaultBlockState();
    public static final BlockState TEMPLE_ROOF_SLAB = Blocks.RED_NETHER_BRICK_SLAB.defaultBlockState();
    public static final BlockState TEMPLE_COLUMN = Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
    public static final BlockState TEMPLE_GOLD = Blocks.GOLD_BLOCK.defaultBlockState();
    public static final BlockState TEMPLE_FLOOR = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    /** 廟埕與月台之間的半格台階。 */
    public static final BlockState TEMPLE_STEP = Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState();
    public static final BlockState INCENSE = Blocks.CAULDRON.defaultBlockState();
    public static final BlockState FURNACE_STACK = Blocks.BLACKSTONE.defaultBlockState();
    public static final BlockState CAMPFIRE = Blocks.CAMPFIRE.defaultBlockState();

    // ------------------------------------------------------------------ 攤販

    public static final BlockState FRYER = Blocks.CAULDRON.defaultBlockState();
    public static final BlockState STALL_TABLE = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    public static final BlockState CRATE = Blocks.BARREL.defaultBlockState();
    public static final BlockState SMOKER = Blocks.SMOKER.defaultBlockState();
    public static final BlockState COUNTER = Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState();
    /** 遊戲攤的標靶。射氣球那種攤子唯一需要的道具。 */
    public static final BlockState GAME_TARGET = Blocks.TARGET.defaultBlockState();
    /** 攤子的骨架。細，而且它會自己接成一個棚架。 */
    public static final BlockState STALL_POST = Blocks.IRON_BARS.defaultBlockState();

    // ------------------------------------------------------------------ 街道設施

    /** 電線桿。細的，所以用牆而不是方塊——街上塞不下一根 1×1 的實心柱。 */
    public static final BlockState POLE = Blocks.STONE_BRICK_WALL.defaultBlockState();
    public static final BlockState WIRE = Blocks.IRON_CHAIN.defaultBlockState();
    public static final BlockState POLE_TOP = Blocks.LIGHTNING_ROD.waxed().unaffected().defaultBlockState();

    /** 頂樓水塔：不鏽鋼的跟橘色的。 */
    public static final BlockState TANK_STEEL = Blocks.IRON_BLOCK.defaultBlockState();
    public static final BlockState TANK_ORANGE = Blocks.CONCRETE.orange().defaultBlockState();
    public static final BlockState TANK_LEG = Blocks.IRON_BARS.defaultBlockState();

    // ------------------------------------------------------------------ 方向工具

    /**
     * 掛在牆上、有字的招牌。
     *
     * <p>{@code facing} 是招牌正面朝的方向，**背後那一格必須是實心的**——不然它會在
     * 第一次方塊更新時掉下來。用原木材質是因為它最不搶色：招牌的顏色靠字的顏色，
     * 不靠板子。
     */
    public static BlockState wallSign(Direction facing) {
        return Blocks.OAK_WALL_SIGN.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    /** 水平方向的樓梯，{@code facing} 是**往上走的方向**。 */
    public static BlockState stairs(Block block, Direction facing, boolean upsideDown) {
        return block.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.HALF, upsideDown ? Half.TOP : Half.BOTTOM);
    }

    public static BlockState slab(Block block, boolean top) {
        return block.defaultBlockState()
                .setValue(BlockStateProperties.SLAB_TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
    }

    /** 橫躺的鏈條當電線。原版的鏈條有 {@code AXIS}，所以它可以是水平的。 */
    public static BlockState wire(Direction.Axis axis) {
        return WIRE.setValue(BlockStateProperties.AXIS, axis);
    }

    public static BlockState facing(BlockState state, Direction facing) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }
}
