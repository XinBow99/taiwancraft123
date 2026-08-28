package com.xinbow99.taiwan.worldgen;

/**
 * 梯度雜訊（Perlin）與它的三個變奏：多八度疊加、定義域扭曲、脊狀。
 *
 * <h2>為什麼不是值雜訊</h2>
 * <p>值雜訊在晶格點上擲一個值、中間內插，所以**每個晶格點都是一個極值**——拿來做地形的話
 * 起伏會變成一格一格的圓丘，等高線是橢圓，而且橢圓的中心排成網格。看久了會發現地形有週期。
 *
 * <p>梯度雜訊在晶格點上擲的是**方向**，晶格點本身的值恆為零。極值落在格子中間、位置不規則，
 * 等高線因此不對齊網格。
 *
 * <h2>八度</h2>
 * <p>單一波長不管多平滑都只有一種尺度的細節，遠看是丘陵、近看還是同一組丘陵。疊上頻率加倍、
 * 振幅減半的幾層（fBm），大尺度的走勢跟小尺度的皺褶才會同時存在。
 *
 * <p>頻率倍率用 2.03 不是 2：剛好加倍的話每一層的晶格線會疊在同一批座標上，那些線會隱隱約約
 * 看得出來。
 *
 * <h2>脊狀雜訊是山脈的關鍵</h2>
 * <p>{@link #ridged} 把雜訊摺一次（{@code 1 - |2n-1|}）。摺痕落在原本的零交越線上，而那條線
 * 是**連續、細長、會分岔**的——那正是山脈的形狀。直接用 fbm 當高程只會得到一堆饅頭，
 * 中央山脈那種「一條連續的稜線」是摺出來的，不是疊出來的。
 */
public final class Noise {

    /** 八個方向的單位向量。查表比 sin/cos 便宜，八個方向對這個尺度的地形已經足夠。 */
    private static final float[] GRAD_X = {1f, -1f, 0f, 0f, 0.7071f, -0.7071f, 0.7071f, -0.7071f};
    private static final float[] GRAD_Z = {0f, 0f, 1f, -1f, 0.7071f, 0.7071f, -0.7071f, -0.7071f};

    /** 單位梯度的 2D Perlin 值域是 ±1/√2，換算回 0～1 要乘這個。 */
    private static final float NORM = 0.7071f;

    private Noise() {
    }

    /** 一格 Perlin，晶格邊長為 1。回傳大約 -0.71～0.71。 */
    public static float perlin(float x, float z, int salt) {
        int gx = (int) Math.floor(x);
        int gz = (int) Math.floor(z);
        float fx = x - gx;
        float fz = z - gz;

        float u = fade(fx);
        float v = fade(fz);

        float n00 = dot(gx, gz, fx, fz, salt);
        float n10 = dot(gx + 1, gz, fx - 1f, fz, salt);
        float n01 = dot(gx, gz + 1, fx, fz - 1f, salt);
        float n11 = dot(gx + 1, gz + 1, fx - 1f, fz - 1f, salt);

        float a = n00 + u * (n10 - n00);
        float b = n01 + u * (n11 - n01);
        return a + v * (b - a);
    }

    /**
     * 多八度疊加，回傳大約 0～1，中央密集兩端稀疏。
     *
     * @param scale 最大那一層的波長（格）
     */
    public static float fbm(float x, float z, float scale, int octaves, int salt) {
        float sum = 0f;
        float amp = 1f;
        float norm = 0f;
        float freq = 1f / scale;

        for (int i = 0; i < octaves; i++) {
            // 每一層再平移一段：光靠 salt 分開的話，各層的晶格原點仍然重合在 (0,0)，
            // 而原點附近的地形會因此少掉細節
            sum += amp * perlin(x * freq + i * 137.13f, z * freq - i * 91.7f, salt ^ (i * 0x9E37));
            norm += amp;
            amp *= 0.5f;
            freq *= 2.03f;
        }
        return 0.5f + (sum / norm) * NORM;
    }

    /**
     * 先把取樣點推開，再取樣。最便宜也最有效的一招：等高線會被拉成不對稱、有拐彎的形狀，
     * 而不是一團一團的圓。
     *
     * @param amount 最多推開幾格。太小看不出來，太大會把地形攪成麵條
     */
    public static float warped(float x, float z, float scale, int octaves, int salt, float amount) {
        // 推移量本身用大一號的波長：跟被推的那份同尺度的話，兩者會互相抵銷成雜訊
        float dx = fbm(x, z, scale * 2f, 2, salt ^ 0x7A11) - 0.5f;
        float dz = fbm(x, z, scale * 2f, 2, salt ^ 0x1B93) - 0.5f;
        return fbm(x + dx * amount, z + dz * amount, scale, octaves, salt);
    }

    /**
     * 脊狀雜訊：0～1，高值落在一條連續會分岔的細線上。山脈與河谷都用它。
     *
     * <p>平方是為了把稜線收窄。不平方的話摺出來的是一片緩坡，山脈會胖成高原。
     */
    public static float ridged(float x, float z, float scale, int octaves, int salt, float warp) {
        float n = warped(x, z, scale, octaves, salt, warp);
        float fold = 1f - Math.abs(n * 2f - 1f);
        return fold * fold;
    }

    private static float dot(int gx, int gz, float dx, float dz, int salt) {
        int h = hash(gx, salt, gz) & 7;
        return GRAD_X[h] * dx + GRAD_Z[h] * dz;
    }

    /** 原版用的五次曲線。三次的 smoothstep 二階不連續，疊了幾層之後晶格線會浮出來。 */
    private static float fade(float t) {
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }

    /**
     * 位置雜湊。
     *
     * <p>自己寫而不是配一個 {@code RandomSource}：這是逐格呼叫的最內層，配物件太貴。
     * 用 splitmix64 的攪拌常數，位元散得夠開。
     */
    public static int hash(int x, int y, int z) {
        long h = x * 0x9E3779B97F4A7C15L ^ y * 0xC2B2AE3D27D4EB4FL ^ z * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        return (int) h;
    }

    /** 把雜湊壓成 0～1。 */
    public static float unit(int x, int y, int z) {
        return (hash(x, y, z) >>> 8) / (float) (1 << 24);
    }

    /** 線性內插。 */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /** 兩道門檻之間的平滑過渡，門檻外夾在 0 / 1。 */
    public static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.clamp((x - edge0) / (edge1 - edge0), 0f, 1f);
        return t * t * (3f - 2f * t);
    }
}
