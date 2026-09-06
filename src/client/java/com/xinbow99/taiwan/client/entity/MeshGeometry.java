package com.xinbow99.taiwan.client.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 低多邊形網格幾何。由 {@code tools/bbmesh.mjs} 從 Blockbench 的 mesh 模型烘出來。
 *
 * <h2>為什麼不用 ModelPart</h2>
 * <p>原版的 {@code ModelPart} 只畫得了長方體。體素化的車殼要靠幾百顆方塊去堆一個斜面，
 * 每一階都是六個面；低多邊形的同一個斜面只要**一個四邊形**。實測差距：那台聯結車
 * 整台 408 個面，我先前體素化的超跑光車身就 2172 個面，而且是階梯狀的。
 *
 * <p>26.2 的 {@code SubmitNodeCollector.submitCustomGeometry} 讓我們可以直接送頂點，
 * 所以繞開 {@code Model}——{@code Model.renderToBuffer} 是 final 而且只吃 {@code root}
 * 那棵零件樹，沒有地方塞得進任意網格。
 *
 * <h2>資料長相</h2>
 * <p>骨骼 → 四邊形陣列。每個頂點 5 個 float（xyz + uv），每個四邊形 20 個 float，
 * 另外每面一組法線。座標已經在烘的時候換成 ModelPart 的慣例（Y 朝下、地面在 24），
 * 這樣它跟另外兩台方塊車可以共用 {@link VehicleRenderer} 的翻正。
 */
public final class MeshGeometry {

    private static final Logger LOG = LoggerFactory.getLogger("taiwan/mesh");
    private static final Map<Identifier, Optional<MeshGeometry>> CACHE = new HashMap<>();

    /** 一根骨骼：一堆四邊形，加上一個可以繞著轉的樞紐。 */
    public record Bone(String name, float px, float py, float pz, float[] quads, float[] normals) {
        public int faces() {
            return this.quads.length / 20;
        }
    }

    private final List<Bone> bones;

    private MeshGeometry(List<Bone> bones) {
        this.bones = bones;
    }

    public List<Bone> bones() {
        return this.bones;
    }

    /**
     * 讀一份幾何，讀過就快取。
     *
     * <p>失敗時回 empty 而不是丟例外：算繪執行緒上丟例外會整個當掉，而少畫一台車
     * 只是看不到車。錯誤只記一次——這是每幀都會走到的路徑，記在迴圈裡會刷爆日誌。
     */
    public static Optional<MeshGeometry> get(Identifier id) {
        return CACHE.computeIfAbsent(id, MeshGeometry::load);
    }

    /** 資源重載時要清掉，不然改了模型還是畫舊的。 */
    public static void clearCache() {
        CACHE.clear();
    }

    private static Optional<MeshGeometry> load(Identifier id) {
        try (BufferedReader in = Minecraft.getInstance().getResourceManager().openAsReader(id)) {
            JsonObject root = JsonParser.parseReader(in).getAsJsonObject();
            List<Bone> bones = new ArrayList<>();
            for (var e : root.getAsJsonArray("bones")) {
                JsonObject b = e.getAsJsonObject();
                JsonArray pivot = b.getAsJsonArray("pivot");
                bones.add(new Bone(b.get("name").getAsString(),
                        pivot.get(0).getAsFloat(), pivot.get(1).getAsFloat(), pivot.get(2).getAsFloat(),
                        floats(b.getAsJsonArray("quads")), floats(b.getAsJsonArray("normals"))));
            }
            return Optional.of(new MeshGeometry(bones));
        } catch (Exception ex) {
            LOG.error("讀不到網格幾何 {}：{}", id, ex.toString());
            return Optional.empty();
        }
    }

    private static float[] floats(JsonArray a) {
        float[] out = new float[a.size()];
        for (int i = 0; i < out.length; i++) out[i] = a.get(i).getAsFloat();
        return out;
    }

    /**
     * 把一根骨骼送進 buffer。
     *
     * <p>吃的是 {@link PoseStack.Pose} 而不是 {@code PoseStack}：{@code submitCustomGeometry}
     * 的回呼只給一個 Pose，沒有堆疊可以 push/pop。所以每根骨骼自己 {@code copy()} 一份
     * 再變換——複製一個 4×4 加 3×3 比維護一個堆疊便宜，而且不會忘記 pop。
     *
     * <p>{@code xRot} 是繞著樞紐轉，給輪子用。先平移到樞紐再轉，不然輪子會繞著車子的
     * 原點公轉而不是自轉。
     */
    public static void render(Bone bone, PoseStack.Pose base, VertexConsumer buffer,
                              int light, int overlay, int color, float xRot) {
        PoseStack.Pose pose = base.copy();
        pose.translate(bone.px(), bone.py(), bone.pz());
        if (xRot != 0.0f) pose.rotate(com.mojang.math.Axis.XP.rotation(xRot));
        float[] q = bone.quads();
        float[] n = bone.normals();
        for (int f = 0, base2 = 0; base2 < q.length; f++, base2 += 20) {
            float nx = n[f * 3], ny = n[f * 3 + 1], nz = n[f * 3 + 2];
            for (int v = 0; v < 4; v++) {
                int o = base2 + v * 5;
                buffer.addVertex(pose, q[o], q[o + 1], q[o + 2])
                        .setColor(color)
                        .setUv(q[o + 3], q[o + 4])
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(pose, nx, ny, nz);
            }
        }
    }
}
