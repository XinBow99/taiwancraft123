package com.xinbow99.taiwan.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 世界的尺度參數，寫在 world_preset 的 JSON 裡，改完開新世界就生效，不用重編。
 *
 * <h2>為什麼參數在 world_preset 而不在 config</h2>
 * <p>地形是**一個世界的性質**，不是一台伺服器的偏好。放在 config 的話，同一個存檔換一台
 * 機器開就會長出對不上的地形——新生成的區塊跟舊的接不起來，接縫是一道垂直的斷崖。
 * 放在 world_preset 裡，參數就跟著存檔走（原版會把生成器連同參數整份存進 {@code level.dat}）。
 *
 * <p>代價是「改完要開新世界」。這是對的代價：地形參數本來就不該中途改。
 *
 * <h2>{@code minY} 與 {@code worldHeight} 必須跟 dimension_type 對得上</h2>
 * <p>對不上的話生成器會算出超出世界高度的方塊，那些寫入會被**安靜丟掉**——症狀是山被削平，
 * 而不是報錯。
 *
 * @param minY          世界底部，要等於 dimension_type 的 min_y
 * @param worldHeight   世界總高，要等於 dimension_type 的 height
 * @param seaLevel      海平面
 * @param landScale     海陸分布的波長（格）。大 = 大陸塊、少海岸線
 * @param mountainScale 山脈稜線的波長（格）
 * @param mountainPeak  山脈最高比海平面高幾格
 * @param hillPeak      丘陵最高比海平面高幾格
 * @param riverWidth    河的半寬（格），下游會自動變寬
 * @param cell          聚落網格的邊長（格）。一格最多一座聚落
 * @param townDensity   有多少比例的格子真的長出聚落（0～1）
 * @param blockSize     聚落內一個街廓的邊長（格）
 * @param roadWidth     街道寬（格）。含騎樓的話人走起來要至少 7
 * @param buildings     各建築類型的權重，0 ＝ 關掉
 */
public record Settings(
        int minY, int worldHeight, int seaLevel,
        float landScale, float mountainScale, int mountainPeak, int hillPeak,
        float riverWidth,
        int cell, float townDensity, int blockSize, int roadWidth,
        Buildings buildings) {

    public static final Settings DEFAULT = new Settings(
            -64, 384, 63,
            1500f, 760f, 168, 52,
            3.2f,
            384, 0.42f, 30, 9,
            Buildings.DEFAULT);

    public static final Codec<Settings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("min_y", DEFAULT.minY()).forGetter(Settings::minY),
            Codec.INT.optionalFieldOf("world_height", DEFAULT.worldHeight()).forGetter(Settings::worldHeight),
            Codec.INT.optionalFieldOf("sea_level", DEFAULT.seaLevel()).forGetter(Settings::seaLevel),
            Codec.FLOAT.optionalFieldOf("land_scale", DEFAULT.landScale()).forGetter(Settings::landScale),
            Codec.FLOAT.optionalFieldOf("mountain_scale", DEFAULT.mountainScale()).forGetter(Settings::mountainScale),
            Codec.INT.optionalFieldOf("mountain_peak", DEFAULT.mountainPeak()).forGetter(Settings::mountainPeak),
            Codec.INT.optionalFieldOf("hill_peak", DEFAULT.hillPeak()).forGetter(Settings::hillPeak),
            Codec.FLOAT.optionalFieldOf("river_width", DEFAULT.riverWidth()).forGetter(Settings::riverWidth),
            Codec.INT.optionalFieldOf("cell", DEFAULT.cell()).forGetter(Settings::cell),
            Codec.FLOAT.optionalFieldOf("town_density", DEFAULT.townDensity()).forGetter(Settings::townDensity),
            Codec.INT.optionalFieldOf("block_size", DEFAULT.blockSize()).forGetter(Settings::blockSize),
            Codec.INT.optionalFieldOf("road_width", DEFAULT.roadWidth()).forGetter(Settings::roadWidth),
            Buildings.CODEC.optionalFieldOf("buildings", DEFAULT.buildings()).forGetter(Settings::buildings)
    ).apply(i, Settings::new));

    /** 方塊可以寫到的最高一格，超過就會被世界高度安靜截掉。 */
    public int ceiling() {
        return minY + worldHeight - 1;
    }

    /**
     * 各建築類型的權重。**0 就是關掉那一類**——這是「設定檔開關」那條需求的落點。
     *
     * <p>用權重而不是布林值：關掉只是權重為 0 的特例，但權重還能調「我要更多宮廟」，
     * 而布林值只能全有全無。
     *
     * @param drinkShop   手搖飲料店
     * @param temple      宮廟
     * @param market      傳統市場／夜市攤位
     * @param tenement    透天厝與公寓
     * @param convenience 便利商店
     */
    public record Buildings(int drinkShop, int temple, int market, int tenement, int convenience) {

        /** 透天厝權重最高：台灣街景的底色是連棟街屋，其他都是點綴。 */
        public static final Buildings DEFAULT = new Buildings(3, 1, 2, 8, 2);

        public static final Codec<Buildings> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("drink_shop", DEFAULT.drinkShop()).forGetter(Buildings::drinkShop),
                Codec.INT.optionalFieldOf("temple", DEFAULT.temple()).forGetter(Buildings::temple),
                Codec.INT.optionalFieldOf("market", DEFAULT.market()).forGetter(Buildings::market),
                Codec.INT.optionalFieldOf("tenement", DEFAULT.tenement()).forGetter(Buildings::tenement),
                Codec.INT.optionalFieldOf("convenience", DEFAULT.convenience()).forGetter(Buildings::convenience)
        ).apply(i, Buildings::new));

        public int total() {
            return drinkShop + temple + market + tenement + convenience;
        }
    }
}
