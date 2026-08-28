package com.xinbow99.taiwan.worldgen;

/**
 * 一座聚落：一塊被整平的地、一張街道網格、以及網格切出來的街廓。
 *
 * <h2>為什麼要先整平</h2>
 * <p>連棟街屋是**共用牆**的：隔壁的地板高度跟我不一樣，中間那道牆就會出現一階一階的鋸齒，
 * 而騎樓的柱子會有的高有的矮。真實的台灣聚落也是這樣解決的——蓋房子之前先把地整成一片。
 *
 * <p>整平不是把地形改掉，而是在地形高度與聚落高度之間做內插（{@link #pad}）：核心完全平，
 * 邊緣平滑接回原本的地形。硬切的話聚落周圍會是一圈垂直的擋土牆。
 *
 * <h2>座標系：一切都從「離最近的街緣幾格」長出來</h2>
 * <p>建築不是「填滿一個街廓」，而是**沿街排**：{@link #roadDepth} 是 0 的那一圈是騎樓的外緣，
 * 往內數 13 格是店面的進深，再往內是後巷。用這個量而不是用街廓的邊界，主幹道變寬的時候
 * 沿街的建築會自己退後，不用另外處理——而如果用街廓邊界，主幹道旁的騎樓會被馬路吃掉兩格。
 *
 * <h2>純函數</h2>
 * <p>一座聚落橫跨十幾個區塊，而區塊是平行、亂序生成的。所以「這一格屬於哪座聚落、
 * 那座聚落長什麼樣」必須是座標的函數，不能有生成期的狀態。所有隨機性都來自
 * {@link Noise#hash}，它只吃座標與世界的 salt。
 */
public final class Town {

    /** 網格中心的抖動幅度，佔一格的幾分之一。不抖的話聚落會排成一個看得出來的棋盤。 */
    private static final float JITTER = 0.12f;

    private final int centerX;
    private final int centerZ;
    private final int radius;
    private final int baseY;
    private final int salt;
    private final Settings settings;

    /** 主幹道比一般街道寬幾格。 */
    private final int trunkExtra;

    private Town(int centerX, int centerZ, int radius, int baseY, int salt, Settings settings, int trunkExtra) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.baseY = baseY;
        this.salt = salt;
        this.settings = settings;
        this.trunkExtra = trunkExtra;
    }

    /**
     * 這一格網格上的聚落，沒有就回 {@code null}。
     *
     * <h3>選址的四個條件</h3>
     * <p>密度擲得過、地夠平、海拔在可住的範圍、而且不在河心。前兩個是**必要**的：
     * 坡上的連棟街屋會變成一排懸空的盒子，而那是最醜的失敗模式。
     *
     * <p>「不在河心」比「離河遠」好：台灣的聚落幾乎都貼著河，把河排除掉就會得到一堆
     * 內陸的孤島。這裡只擋掉會把整個聚落泡在水裡的那種。
     */
    public static Town at(int cellX, int cellZ, Settings s, int worldSalt) {
        int cell = s.cell();
        int salt = Noise.hash(cellX, worldSalt ^ 0x70E0, cellZ);

        if (Noise.unit(cellX, worldSalt ^ 0x11, cellZ) >= s.townDensity()) return null;

        int jitter = (int) (cell * JITTER);
        int cx = cellX * cell + cell / 2 + (Math.floorMod(salt >> 3, jitter * 2 + 1) - jitter);
        int cz = cellZ * cell + cell / 2 + (Math.floorMod(salt >> 13, jitter * 2 + 1) - jitter);

        int ground = Terrain.height(cx, cz, s, worldSalt);
        int sea = s.seaLevel();
        // 太高就沒有聚落了：台灣的城鎮幾乎全部在三十公尺以下的平原與河階上
        if (ground < sea + 2 || ground > sea + 34) return null;
        if (Terrain.riverCarve(cx, cz, s, worldSalt) > 0.35f) return null;
        if (Terrain.roughness(cx, cz, s, worldSalt) > 7) return null;

        // 半徑上限訂在網格的四分之一多一點：加上抖動之後，相鄰兩格的聚落最遠也碰不到，
        // 所以完全不需要做重疊檢查
        int radius = 52 + Math.floorMod(salt >> 7, 68);
        int baseY = Math.max(ground, sea + 2);
        int trunk = 2 + Math.floorMod(salt >> 19, 3);

        return new Town(cx, cz, radius, baseY, salt, s, trunk);
    }

    public int centerX() {
        return centerX;
    }

    public int centerZ() {
        return centerZ;
    }

    /** 街道與地坪的高度。整座聚落只有這一個高度，共用牆才不會出現鋸齒。 */
    public int baseY() {
        return baseY;
    }

    public int salt() {
        return salt;
    }

    /** 最外圍：超出這個距離一定碰不到這座聚落，掃描時用它剪枝。 */
    public int reach() {
        return (int) (radius * 1.20f) + 4;
    }

    /**
     * 整平權重：1 ＝ 完全是聚落的地坪，0 ＝ 完全是原本的地形。
     *
     * <p>邊界用雜訊推開，不然聚落會是一個正圓——正圓在自然地形裡讀起來像飛碟降落過。
     */
    public float pad(int x, int z) {
        float dx = x - centerX;
        float dz = z - centerZ;
        float d = (float) Math.sqrt(dx * dx + dz * dz);
        float edge = radius * (0.84f + 0.34f * Noise.fbm(x, z, 110f, 2, salt ^ 0x2E11));
        return Noise.smoothstep(edge, edge * 0.74f, d);
    }

    /** 這一格是不是完全在地坪上。只有這種地方才能蓋房子——半平的地會讓共用牆出現鋸齒。 */
    public boolean paved(int x, int z) {
        return pad(x, z) > 0.999f;
    }

    // ------------------------------------------------------------------ 街道網格

    /** 街廓網格的間距：一個街廓加一條街。 */
    public int pitch() {
        return settings.blockSize() + settings.roadWidth();
    }

    /**
     * 第 {@code line} 條街道中線的半寬（含中線那一格）。
     *
     * <p>街寬一律取成奇數：街道是以一條**中線**為中心往兩邊長的，偶數寬不可能對稱。
     * 硬要偶數的話，一邊的騎樓會比另一邊窄一格，而那一格剛好是柱子的位置。
     */
    private int roadHalf(int line) {
        int w = settings.roadWidth() + (line == 0 ? trunkExtra : 0);
        return (w + 1) / 2;
    }

    /**
     * 這一格離最近的南北向街道有多遠（負數 ＝ 在街上，0 ＝ 緊貼街緣的第一格）。
     *
     * <p>主幹道（通過中心的第 0 條）比較寬：全部一樣寬的話，聚落會讀成一張方格紙，
     * 沒有中心也沒有方向。一條寬的十字路就足以讓人知道「這裡是鎮上」。
     */
    public int depthX(int x) {
        return axisDepth(x - centerX);
    }

    /** 這一格離最近的東西向街道有多遠。 */
    public int depthZ(int z) {
        return axisDepth(z - centerZ);
    }

    private int axisDepth(int offset) {
        int pitch = pitch();
        int line = Math.round(offset / (float) pitch);
        int d = Math.abs(offset - line * pitch);
        return d - roadHalf(line);
    }

    /** 離最近的街緣幾格（兩軸取小）。負數 ＝ 在街上。 */
    public int roadDepth(int x, int z) {
        return Math.min(depthX(x), depthZ(z));
    }

    /** 是不是柏油路面。 */
    public boolean road(int x, int z) {
        return roadDepth(x, z) < 0;
    }

    /**
     * 最近的那條街是不是南北向的（也就是建築要面東或面西）。
     *
     * <p>相等時偏南北向：轉角的兩面都是店面，選哪一面都對，重點是**兩次問要得到同一個答案**。
     */
    public boolean facesXAxis(int x, int z) {
        return depthX(x) <= depthZ(z);
    }

    /** 這條街的線號。同一條街上的店面共用一組寬度與樣式，靠它認得出來。 */
    public int lineX(int x) {
        return Math.round((x - centerX) / (float) pitch());
    }

    public int lineZ(int z) {
        return Math.round((z - centerZ) / (float) pitch());
    }

    /** 在街的哪一側：+1 ＝ 座標較大的那側。街緣那一格也算得出來（|偏移| 不可能是 0）。 */
    public int sideX(int x) {
        return (x - centerX) - lineX(x) * pitch() >= 0 ? 1 : -1;
    }

    public int sideZ(int z) {
        return (z - centerZ) - lineZ(z) * pitch() >= 0 ? 1 : -1;
    }

    /** 離最近的南北向街道**中線**幾格。畫路面標線、放電線桿用。 */
    public int lineDistX(int x) {
        return Math.abs((x - centerX) - lineX(x) * pitch());
    }

    public int lineDistZ(int z) {
        return Math.abs((z - centerZ) - lineZ(z) * pitch());
    }

    // ------------------------------------------------------------------ 街廓

    /**
     * 這一格屬於哪一個街廓。
     *
     * <p>街道中線落在 {@code pitch} 的整數倍上，所以「除以 pitch 取下界」剛好就是街廓編號，
     * 而且**街道兩側的格子會落到不同的街廓**——這正是要的。編號是聚落內部座標，
     * 所以同一個街廓在哪個區塊問都得到同一個答案。
     */
    public int lotX(int x) {
        return Math.floorDiv(x - centerX, pitch());
    }

    public int lotZ(int z) {
        return Math.floorDiv(z - centerZ, pitch());
    }

    /** 街廓的邊界（含）。兩側的街可能不一樣寬，所以要分別問。 */
    public int lotMinX(int lx) {
        return centerX + lx * pitch() + roadHalf(lx);
    }

    public int lotMaxX(int lx) {
        return centerX + (lx + 1) * pitch() - roadHalf(lx + 1);
    }

    public int lotMinZ(int lz) {
        return centerZ + lz * pitch() + roadHalf(lz);
    }

    public int lotMaxZ(int lz) {
        return centerZ + (lz + 1) * pitch() - roadHalf(lz + 1);
    }

    /**
     * 這個街廓在不在聚落裡。
     *
     * <p>問四個角而不是問中心：只問中心的話，邊緣那些一半在地坪上、一半掛在坡上的街廓
     * 會被判定成合格，然後蓋出半懸空的房子。
     */
    public boolean lotPaved(int lx, int lz) {
        int x0 = lotMinX(lx);
        int x1 = lotMaxX(lx);
        int z0 = lotMinZ(lz);
        int z1 = lotMaxZ(lz);
        return paved(x0, z0) && paved(x1, z0) && paved(x0, z1) && paved(x1, z1);
    }

    /** 這個街廓的亂數種子。整棟宮廟、整個市場都從它長出來。 */
    public int lotSalt(int lx, int lz) {
        return Noise.hash(lx, salt ^ 0x5B17, lz);
    }
}
