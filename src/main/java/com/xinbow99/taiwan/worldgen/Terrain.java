package com.xinbow99.taiwan.worldgen;

/**
 * 地形高程。**整個模組唯一一個「這一柱的地面在哪」的來源。**
 *
 * <h2>為什麼只能有一份</h2>
 * <p>區塊填充、高度圖（{@code getBaseHeight}）、柱體取樣（{@code getBaseColumn}）、
 * 聚落選址、生態系判定，全部要問同一個問題。各寫一份的話它們會慢慢對不上，而那種錯誤的
 * 症狀不是報錯，是**玩家掉進地板**或站在半空中——因為伺服器認為地面在 A，方塊卻在 B。
 *
 * <h2>台灣的地形是有結構的，不是隨機起伏</h2>
 * <p>一條中央山脈、山脈往西降成丘陵、丘陵降成沖積平原、河從山上一路切到海。這個順序
 * 決定了這裡的算法：先擲一份**大陸性**（{@link #continent}）決定海陸與海拔帶，再把
 * 脊狀雜訊**只加在高海拔帶上**當山脈，最後用另一份脊狀雜訊切河谷。
 *
 * <p>反過來做——先擲高度再分帶——會得到一鍋均勻的丘陵，因為 fbm 的值集中在中間，
 * 高山跟平原都會被稀釋掉。
 *
 * <h2>純函數</h2>
 * <p>每一格區塊各自平行、亂序生成，所以這裡不能有任何狀態。所有隨機性只來自座標與
 * {@code salt}（由世界種子導出）。
 */
public final class Terrain {

    /** 低於這個大陸性就是海。 */
    public static final float SEA_EDGE = 0.44f;
    /** 高於這個大陸性開始長山。 */
    public static final float MOUNTAIN_EDGE = 0.575f;

    /**
     * 陸地度的實測上緣（第 98 百分位）。
     *
     * <p>{@link #continent} 是多八度 fbm，值近似常態分布（實測 mean 0.503、sd 0.093），
     * 所以 0.44～1.0 這個區間裡有一大半**永遠擲不到**。拿 1.0 當上緣去算海拔的話，
     * 丘陵與高山的那幾段等於不存在——整個世界會平成一片近海的低地。
     *
     * <p>這個數字是量回來的，不是算出來的。改了 {@code landScale} 或八度數就要重量一次。
     */
    public static final float LAND_TOP = 0.72f;

    private Terrain() {
    }

    /**
     * 大陸性：0 ＝ 深海，1 ＝ 山脈核心。海陸分布、海拔帶、聚落選址、生態系全部從它導出。
     *
     * <p>用扭曲過的 fbm 而不是單純的 fbm：少了扭曲，海岸線會是一團團的圓，看起來像用圓規
     * 畫的；有了扭曲才會有半島、灣、夾在中間的細長海峽。
     */
    public static float continent(int x, int z, Settings s, int salt) {
        return Noise.warped(x, z, s.landScale(), 4, salt ^ 0x1A17, s.landScale() * 0.28f);
    }

    /**
     * 這一柱的地面高度，**含河谷切下去的部分**。
     *
     * <p>這是對外唯一的入口。想知道「沒有河的話有多高」請用 {@link #bedrockRelief}，
     * 但除了畫河本身以外沒有人該問那個。
     */
    public static int height(int x, int z, Settings s, int salt) {
        float dry = bedrockRelief(x, z, s, salt);
        float carve = riverCarve(x, z, s, salt);
        if (carve <= 0f) return Math.round(dry);

        // 往下拉到「河床」為止。河床不是絕對高度：山上的溪谷只切下去二十幾格，
        // 沒切到海平面——不然一條河會在山裡變成一道兩百格深的峽谷
        float bed = Math.max(s.seaLevel() - 2f, dry - 26f);
        return Math.round(Noise.lerp(dry, bed, carve));
    }

    /**
     * 沒有河的地面高度。分三段：海底、平原丘陵、山脈。
     *
     * <p>回傳 float 而不是 int：河谷要在它上面做內插，先取整的話河床會變成一階一階的梯田。
     */
    public static float bedrockRelief(int x, int z, Settings s, int salt) {
        float c = continent(x, z, s, salt);
        int sea = s.seaLevel();
        float h;

        if (c < SEA_EDGE) {
            // 海底。近岸緩、遠洋深，用平方讓深海不要一下子就掉下去
            float t = Math.clamp(c / SEA_EDGE, 0f, 1f);
            h = Noise.lerp(sea - 42f, sea - 1f, t * t);
        } else {
            // 陸地。用實測的上緣（見 LAND_TOP）而不是 1.0 當分母，前段是平原（幾乎平），
            // 後段抬成丘陵
            float t = (c - SEA_EDGE) / (LAND_TOP - SEA_EDGE);
            float plain = Noise.smoothstep(0f, 0.14f, t);          // 海岸 → 平原
            float hill = Noise.smoothstep(0.20f, 0.74f, t);        // 平原 → 丘陵
            h = sea + 2f + plain * 7f + hill * s.hillPeak();
        }

        // 山脈：脊狀雜訊只加在高大陸性的地方，所以山會連成一條帶狀的稜線，
        // 而不是撒在平原上的孤峰
        float belt = Noise.smoothstep(MOUNTAIN_EDGE, 0.68f, c);
        if (belt > 0f) {
            float ridge = Noise.ridged(x, z, s.mountainScale(), 4, salt ^ 0x5E17, s.mountainScale() * 0.3f);
            h += belt * ridge * s.mountainPeak();
        }

        // 細節。振幅跟著海拔走：平原上只有一兩格的田埂起伏，山上才有大塊的岩壁。
        // 給定值的話，平原會變成搓衣板，或者山會變得太平滑——兩件事不能用同一個振幅
        float rough = 1.5f + Noise.smoothstep(sea + 6f, sea + 90f, h) * 13f;
        h += (Noise.fbm(x, z, 46f, 3, salt ^ 0x33D1) - 0.5f) * 2f * rough;

        return Math.clamp(h, s.minY() + 2f, s.ceiling() - 8f);
    }

    /**
     * 河：0 ＝ 沒有，1 ＝ 河心。
     *
     * <p>用 {@code |n - 0.5|} 取零交越線而不是用脊狀雜訊：脊狀的值在稜線附近變化很慢，
     * 切出來的河會是一條寬而淺的凹槽；零交越線兩側的梯度是滿的，河岸才有形。
     *
     * <p>下游變寬：大陸性愈低（愈靠海）門檻放愈大。這是免費的真實感——河出山之後本來就
     * 該散開，而且它剛好讓河口寬得足以當港。
     */
    public static float riverCarve(int x, int z, Settings s, int salt) {
        float c = continent(x, z, s, salt);
        // 海裡不切河，深山的稜線頂端也不切（山頂的河看起來像被鋸開）
        if (c < SEA_EDGE + 0.01f) return 0f;

        float v = Noise.warped(x, z, 900f, 3, salt ^ 0x5217, 380f);
        float d = Math.abs(v - 0.5f);

        // 河寬換算成雜訊值的門檻。0.0035 大約是一格，所以 riverWidth 是「半寬幾格」
        float wide = 1f + Noise.smoothstep(0.70f, 0.46f, c) * 1.8f;
        float core = 0.0035f * s.riverWidth() * wide;
        float bank = core * 3.2f;

        // 河心到河岸之間平滑收尾，不然河會有一道垂直的側壁
        return Noise.smoothstep(bank, core, d);
    }

    /**
     * 這一柱有多平：回傳周圍取樣點的最大高差（格）。聚落選址用。
     *
     * <p>只取四個方向、間距 12 格：真正逐格算坡度太貴，而選址只需要知道「這裡大致上平不平」。
     * 間距比建築尺度小的話量到的是細節雜訊，比聚落尺度大的話又會漏掉一整片斜坡。
     */
    public static int roughness(int x, int z, Settings s, int salt) {
        int c = height(x, z, s, salt);
        int lo = c;
        int hi = c;
        for (int i = 0; i < 4; i++) {
            int dx = (i == 0 ? 12 : i == 1 ? -12 : 0);
            int dz = (i == 2 ? 12 : i == 3 ? -12 : 0);
            int h = height(x + dx, z + dz, s, salt);
            lo = Math.min(lo, h);
            hi = Math.max(hi, h);
        }
        return hi - lo;
    }
}
