package com.xinbow99.taiwan.client.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
 * 低多邊形網格幾何，含骨架與動作。由 {@code tools/bake-lambo.mjs} 從 Blockbench 烘出來。
 *
 * <h2>為什麼不用 ModelPart</h2>
 * <p>原版的 {@code ModelPart} 只畫得了長方體，而且一根零件只有一組旋轉角——那台超跑的
 * 1,653 顆方塊裡有 1,590 顆各自有三軸旋轉。做成零件樹會超出 JVM 的 64KB 方法位元組上限。
 *
 * <p>26.2 的 {@code SubmitNodeCollector.submitCustomGeometry} 讓我們可以直接送頂點，
 * 所以繞開 {@code Model}——{@code Model.renderToBuffer} 是 final 而且只吃 {@code root}
 * 那棵零件樹，沒有地方塞得進任意網格。
 *
 * <h2>骨架是有階層的</h2>
 * <p>一層攤平做不到兩件事：轉向節轉的時候前輪要跟著轉（而前輪自己還在滾），
 * 懸吊上下震的時候剪刀門要跟著車身走。所以每根骨骼記一個父索引，
 * 而且**父一定排在子前面**——烘的時候是深度優先走 outliner，順序天然成立，
 * 算繪時一趟迴圈就能把整棵樹算完，不用遞迴也不用排序。
 *
 * <h2>資料長相</h2>
 * <p>骨骼 → 四邊形陣列。每個頂點 5 個 float（xyz + uv），每個四邊形 20 個 float，
 * 另外每面一組法線。長度單位是**格**（烘的時候已經除以 16），座標已經換成 ModelPart
 * 的慣例（Y 朝下、地面在 24），這樣它跟方塊車可以共用 {@link VehicleRenderer} 的翻正。
 *
 * <p>動作是一組關鍵影格通道，值已經是弧度與格。播放狀態不存在這裡——幾何是**共用且
 * 快取**的，同一份會同時被好幾台車拿去畫。每一次算繪自己開一個 {@link Pose} 當畫布。
 */
public final class MeshGeometry {

    private static final Logger LOG = LoggerFactory.getLogger("taiwan/mesh");
    private static final Map<Identifier, Optional<MeshGeometry>> CACHE = new HashMap<>();

    /**
     * 一根骨骼。
     *
     * @param parent 父骨骼的索引，根是 -1。父一定小於自己。
     * @param px     樞紐，相對於父骨骼的樞紐（格）
     */
    public record Bone(String name, int parent, float px, float py, float pz,
                       float[] quads, float[] normals) {
        public int faces() {
            return this.quads.length / 20;
        }
    }

    /**
     * 一條通道的關鍵影格。
     *
     * <p>攤平成一個 float 陣列而不是物件陣列：每一格四個值（時間、x、y、z），
     * 取值是每一幀每一根骨骼都要做的事，少一層指標就少一次快取未命中。
     */
    private record Channel(float[] keys) {
        int count() {
            return this.keys.length / 4;
        }

        /** 在 {@code time} 取值，寫進 {@code out} 的 {@code base} 起三格，乘上權重後累加。 */
        void sample(float time, float weight, float[] out, int base) {
            int n = count();
            int i = 0;
            while (i < n - 1 && this.keys[(i + 1) * 4] <= time) i++;
            float t0 = this.keys[i * 4];
            int j = Math.min(i + 1, n - 1);
            float t1 = this.keys[j * 4];
            // 兩格同時間（或只有一格）時 span 是 0，直接取前一格，不要除以零
            float span = t1 - t0;
            float f = span > 1.0e-5f ? Math.min(Math.max((time - t0) / span, 0.0f), 1.0f) : 0.0f;
            for (int axis = 0; axis < 3; axis++) {
                float a = this.keys[i * 4 + 1 + axis];
                float b = this.keys[j * 4 + 1 + axis];
                out[base + axis] += (a + (b - a) * f) * weight;
            }
        }
    }

    /** 一根骨骼在一段動作裡動到的通道。{@code null} 代表這段動作沒碰這個通道。 */
    private record BoneTrack(int bone, Channel rotation, Channel position) {
    }

    /** 一段動作。 */
    private record Animation(float length, boolean loop, List<BoneTrack> tracks) {
    }

    /**
     * 一次算繪的姿勢：每根骨骼的旋轉與位移**偏移量**。
     *
     * <p>偏移而不是絕對值，所以好幾段動作可以直接相加——轉向、開門、怠速震動動到的是
     * 不同骨骼，而引擎抖與路面顛簸動到的是同一根，兩者本來就該疊起來。
     */
    public static final class Pose {
        final float[] rotation;
        final float[] position;

        Pose(int bones) {
            this.rotation = new float[bones * 3];
            this.position = new float[bones * 3];
        }

        void clear() {
            java.util.Arrays.fill(this.rotation, 0.0f);
            java.util.Arrays.fill(this.position, 0.0f);
        }
    }

    private final List<Bone> bones;
    private final Map<String, Animation> animations;

    private MeshGeometry(List<Bone> bones, Map<String, Animation> animations) {
        this.bones = bones;
        this.animations = animations;
    }

    public List<Bone> bones() {
        return this.bones;
    }

    /** 開一張空白的姿勢畫布。每一次算繪一張——幾何是共用的，姿勢不是。 */
    public Pose newPose() {
        return new Pose(this.bones.size());
    }

    /**
     * 把一段動作在 {@code time}（秒）的取樣疊進姿勢。
     *
     * <p>{@code weight} 是 0 到 1 的權重，0 就整段不生效。循環的動作會自己把時間取模；
     * 不循環的夾在兩端——尾端維持最後一格，這正好是「開著的門停在開著的角度」。
     *
     * <p>找不到這段動作就當作沒發生。資源檔可能比程式舊，少播一段動畫只是車不會動，
     * 丟例外會讓整個算繪執行緒掛掉。
     */
    public void apply(Pose pose, String name, float time, float weight) {
        Animation animation = this.animations.get(name);
        if (animation == null || weight <= 0.0f) return;
        float t = animation.loop()
                ? time - animation.length() * (float) Math.floor(time / animation.length())
                : Math.min(Math.max(time, 0.0f), animation.length());
        for (BoneTrack track : animation.tracks()) {
            if (track.rotation() != null) {
                track.rotation().sample(t, weight, pose.rotation, track.bone() * 3);
            }
            if (track.position() != null) {
                track.position().sample(t, weight, pose.position, track.bone() * 3);
            }
        }
    }

    /**
     * 整台送進 buffer。
     *
     * <p>吃的是 {@link PoseStack.Pose} 而不是 {@code PoseStack}：{@code submitCustomGeometry}
     * 的回呼只給一個 Pose，沒有堆疊可以 push/pop。所以自己開一個陣列存每根骨骼算好的矩陣，
     * 父的算完才輪到子——骨骼順序保證了這一點。
     *
     * <p>先平移到樞紐再轉，不然輪子會繞著車子的原點公轉而不是自轉。旋轉的合成順序是
     * Z→Y→X，跟 {@code ModelPart} 一樣，也跟烘焙時假設的一樣。
     */
    public void render(Pose pose, PoseStack.Pose base, VertexConsumer buffer,
                       int light, int overlay, int color) {
        PoseStack.Pose[] matrices = new PoseStack.Pose[this.bones.size()];
        for (int i = 0; i < this.bones.size(); i++) {
            Bone bone = this.bones.get(i);
            PoseStack.Pose p = (bone.parent() < 0 ? base : matrices[bone.parent()]).copy();
            int o = i * 3;
            p.translate(bone.px() + pose.position[o],
                    bone.py() + pose.position[o + 1],
                    bone.pz() + pose.position[o + 2]);
            float rx = pose.rotation[o], ry = pose.rotation[o + 1], rz = pose.rotation[o + 2];
            if (rz != 0.0f) p.rotate(Axis.ZP.rotation(rz));
            if (ry != 0.0f) p.rotate(Axis.YP.rotation(ry));
            if (rx != 0.0f) p.rotate(Axis.XP.rotation(rx));
            matrices[i] = p;
            emit(bone, p, buffer, light, overlay, color);
        }
    }

    private static void emit(Bone bone, PoseStack.Pose pose, VertexConsumer buffer,
                             int light, int overlay, int color) {
        float[] q = bone.quads();
        float[] n = bone.normals();
        for (int f = 0, at = 0; at < q.length; f++, at += 20) {
            float nx = n[f * 3], ny = n[f * 3 + 1], nz = n[f * 3 + 2];
            for (int v = 0; v < 4; v++) {
                int o = at + v * 5;
                buffer.addVertex(pose, q[o], q[o + 1], q[o + 2])
                        .setColor(color)
                        .setUv(q[o + 3], q[o + 4])
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(pose, nx, ny, nz);
            }
        }
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
            Map<String, Integer> index = new HashMap<>();
            for (var e : root.getAsJsonArray("bones")) {
                JsonObject b = e.getAsJsonObject();
                String name = b.get("name").getAsString();
                var parentField = b.get("parent");
                int parent = parentField == null || parentField.isJsonNull()
                        ? -1 : index.getOrDefault(parentField.getAsString(), -1);
                JsonArray pivot = b.getAsJsonArray("pivot");
                index.put(name, bones.size());
                bones.add(new Bone(name, parent,
                        pivot.get(0).getAsFloat(), pivot.get(1).getAsFloat(), pivot.get(2).getAsFloat(),
                        floats(b.getAsJsonArray("quads")), floats(b.getAsJsonArray("normals"))));
            }

            Map<String, Animation> animations = new HashMap<>();
            JsonObject animated = root.getAsJsonObject("animations");
            if (animated != null) {
                for (var entry : animated.entrySet()) {
                    JsonObject a = entry.getValue().getAsJsonObject();
                    List<BoneTrack> tracks = new ArrayList<>();
                    for (var boneEntry : a.getAsJsonObject("bones").entrySet()) {
                        Integer at = index.get(boneEntry.getKey());
                        if (at == null) continue;
                        JsonObject channels = boneEntry.getValue().getAsJsonObject();
                        tracks.add(new BoneTrack(at,
                                channel(channels, "rotation"), channel(channels, "position")));
                    }
                    animations.put(entry.getKey(), new Animation(
                            a.get("length").getAsFloat(), a.get("loop").getAsBoolean(), tracks));
                }
            }
            return Optional.of(new MeshGeometry(bones, animations));
        } catch (Exception ex) {
            LOG.error("讀不到網格幾何 {}：{}", id, ex.toString());
            return Optional.empty();
        }
    }

    /** 把 {@code [[時間, x, y, z], ...]} 攤成一條 float 陣列。 */
    private static Channel channel(JsonObject channels, String name) {
        JsonArray keys = channels.getAsJsonArray(name);
        if (keys == null || keys.isEmpty()) return null;
        float[] flat = new float[keys.size() * 4];
        for (int i = 0; i < keys.size(); i++) {
            JsonArray key = keys.get(i).getAsJsonArray();
            for (int v = 0; v < 4; v++) flat[i * 4 + v] = key.get(v).getAsFloat();
        }
        return new Channel(flat);
    }

    private static float[] floats(JsonArray a) {
        float[] out = new float[a.size()];
        for (int i = 0; i < out.length; i++) out[i] = a.get(i).getAsFloat();
        return out;
    }
}
