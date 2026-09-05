package com.xinbow99.taiwan.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * 勁戰四代 125 的模型。數字跟 {@code tools/models.js} 的 {@code cygnus} 一字不差。
 *
 * <h2>它要跟通用速克達一眼分得出來</h2>
 * <p>靠四個實車的辨識點，不是靠顏色——就算兩台都塗成黑的也要認得出來：
 * <ol>
 *   <li><b>往前突出的楔形燈殼</b>。通用款的臉是一顆圓燈；勁戰是一塊往前戳出去的殼，
 *       車評形容成「像四輪超跑的進氣口」。正面最強的識別。
 *   <li><b>階梯式坐墊</b>。騎士座低、後座高一階。側面最強的識別。
 *   <li><b>一體式後扶手</b>。四代把左右分開的扶手改成相連的一圈，取代通用款的貨架。
 *   <li><b>LED 燈條尾燈</b>。尾巴上一條橫的紅燈。
 * </ol>
 *
 * <h2>白車殼藏不住重疊</h2>
 * <p>通用款是消光深灰，零件互相穿插時陰影會把破綻蓋掉；白色不會。所以那個
 * 「護板與座墊之間的缺口」在這台車上必須**真的挖出來**：踏板到 z=4 為止、車尾從 z=3
 * 才開始、中間 y 15~20 完全淨空。第一版沒挖，整台在算圖上讀成一塊白磚。
 *
 * <p>同理，暗色的那一格色票（{@link #DARK}）在這張貼圖裡是真的暗（#3b4248），
 * 不是通用款那種深灰——不然大燈、飾板、後照鏡在白車殼上全部看不見。
 */
public class CygnusModel extends EntityModel<VehicleRenderState> {

    // 三十二塊色票的左上角（32×32 一格，8×4 排在 **256×256** 上）。
    //
    // 為什麼從 128×128 換到 256×256：不是因為解析度限制細節——這是「純色色票」，
    // 每個零件指到一格純色，取樣到什麼顏色跟盒子多大無關，細節來自零件數而不是貼圖。
    // 換大是為了**格子數**：紅黑配色要有明暗階（同一個紅的亮面／暗面／陰影面）才有立體感，
    // 只有一階的話所有面一樣亮，整台看起來是平的。十六格不夠放三階，三十二格夠。
    //
    // 順序必須跟 tools/swatch-texture.mjs 的 cygnus 調色盤一致。
    private static final int RED       =   0, RED_V       =   0;
    private static final int RED_DK    =  32, RED_DK_V    =   0;
    private static final int RED_LT    =  64, RED_LT_V    =   0;
    private static final int BODY      =  96, BODY_V      =   0;
    private static final int BODY_DK   = 128, BODY_DK_V   =   0;
    private static final int BODY_LT   = 160, BODY_LT_V   =   0;
    private static final int GREY      = 192, GREY_V      =   0;
    private static final int GREY_LT   = 224, GREY_LT_V   =   0;
    private static final int SEAT      =   0, SEAT_V      =  32;
    private static final int TIRE      =  32, TIRE_V      =  32;
    private static final int RIM       =  64, RIM_V       =  32;
    private static final int RIM_DK    =  96, RIM_DK_V    =  32;
    private static final int CHROME    = 128, CHROME_V    =  32;
    private static final int SILVER    = 160, SILVER_V    =  32;
    private static final int WHITE     = 192, WHITE_V     =  32;
    private static final int AMBER     = 224, AMBER_V     =  32;
    private static final int BRAKE     =   0, BRAKE_V     =  64;
    private static final int SHADOW    =  32, SHADOW_V    =  64;
    private static final int SLATE     =  64, SLATE_V     =  64;
    private static final int STEEL     =  96, STEEL_V     =  64;
    private static final int RED_MID   = 128, RED_MID_V   =  64;
    private static final int RED_DEEP  = 160, RED_DEEP_V  =  64;
    private static final int PURE      = 192, PURE_V      =  64;
    private static final int ASH       = 224, ASH_V       =  64;
    private static final int INK       =   0, INK_V       =  96;
    private static final int GUN       =  32, GUN_V       =  96;
    private static final int PEWTER    =  64, PEWTER_V    =  96;
    private static final int COAL      =  96, COAL_V      =  96;
    private static final int ROSE      = 128, ROSE_V      =  96;
    private static final int AMBER_LT  = 160, AMBER_LT_V  =  96;
    private static final int ORCHID    = 192, ORCHID_V    =  96;
    private static final int GRAPHITE  = 224, GRAPHITE_V  =  96;

    private final ModelPart steer;
    private final ModelPart wheelFront;
    private final ModelPart wheelRear;

    public CygnusModel(ModelPart root) {
        // 理由同 ScooterModel：預設的 cutout-no-cull 會讓車殼內側跟外側一起畫、互相穿插，
        // 看起來像整台車是半透明的
        super(root, RenderTypes::entitySolid);
        this.steer = root.getChild("steer");
        this.wheelFront = this.steer.getChild("wheel_front");
        this.wheelRear = root.getChild("wheel_rear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("shield0",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.16f, 0.0f, -0.39f, 8.32f, 14.04f, 0.78f),
                PartPose.offset(0.0f, 5.54f, -3.12f));
        root.addOrReplaceChild("shield1",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.42f, 0.0f, -0.39f, 8.84f, 14.82f, 0.78f),
                PartPose.offset(0.0f, 4.89f, -3.9f));
        root.addOrReplaceChild("shield2",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.55f, 0.0f, -0.39f, 9.1f, 15.47f, 0.78f),
                PartPose.offset(0.0f, 4.37f, -4.68f));
        root.addOrReplaceChild("shield3",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 15.86f, 0.78f),
                PartPose.offset(0.0f, 3.98f, -5.46f));
        root.addOrReplaceChild("shield4",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 15.99f, 0.78f),
                PartPose.offset(0.0f, 3.85f, -6.24f));
        root.addOrReplaceChild("shield5",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 15.86f, 0.78f),
                PartPose.offset(0.0f, 3.98f, -7.02f));
        root.addOrReplaceChild("shield6",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.55f, 0.0f, -0.39f, 9.1f, 15.34f, 0.78f),
                PartPose.offset(0.0f, 4.5f, -7.8f));
        root.addOrReplaceChild("shield7",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.42f, 0.0f, -0.39f, 8.84f, 14.3f, 0.78f),
                PartPose.offset(0.0f, 5.54f, -8.58f));
        root.addOrReplaceChild("nose0",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.29f, 0.0f, -0.39f, 8.58f, 13.13f, 0.78f),
                PartPose.offset(0.0f, 6.58f, -9.36f));
        root.addOrReplaceChild("nose1",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.16f, 0.0f, -0.39f, 8.32f, 11.96f, 0.78f),
                PartPose.offset(0.0f, 7.62f, -10.14f));
        root.addOrReplaceChild("nose2",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.03f, 0.0f, -0.39f, 8.06f, 10.53f, 0.78f),
                PartPose.offset(0.0f, 8.66f, -10.92f));
        root.addOrReplaceChild("nose3",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-3.835f, 0.0f, -0.39f, 7.67f, 9.1f, 0.78f),
                PartPose.offset(0.0f, 9.7f, -11.7f));
        root.addOrReplaceChild("nose4",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-3.64f, 0.0f, -0.39f, 7.28f, 7.41f, 0.78f),
                PartPose.offset(0.0f, 10.87f, -12.48f));
        root.addOrReplaceChild("nose5",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-3.38f, 0.0f, -0.39f, 6.76f, 5.59f, 0.78f),
                PartPose.offset(0.0f, 12.17f, -13.26f));
        root.addOrReplaceChild("nose6",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-2.99f, 0.0f, -0.39f, 5.98f, 3.64f, 0.78f),
                PartPose.offset(0.0f, 13.6f, -14.04f));
        root.addOrReplaceChild("nose7",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-2.47f, 0.0f, -0.39f, 4.94f, 1.82f, 0.78f),
                PartPose.offset(0.0f, 14.9f, -14.69f));
        root.addOrReplaceChild("wingl0",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 7.02f, 1.04f),
                PartPose.offset(5.2f, 12.3f, -5.46f));
        root.addOrReplaceChild("wingl1",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 7.8f, 1.04f),
                PartPose.offset(5.2f, 11.52f, -6.5f));
        root.addOrReplaceChild("wingl2",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 7.8f, 1.04f),
                PartPose.offset(5.2f, 11.26f, -7.54f));
        root.addOrReplaceChild("wingl3",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 6.76f, 1.04f),
                PartPose.offset(5.2f, 11.78f, -8.58f));
        root.addOrReplaceChild("wingl4",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 5.46f, 1.04f),
                PartPose.offset(5.2f, 12.56f, -9.62f));
        root.addOrReplaceChild("wingl5",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 3.64f, 1.04f),
                PartPose.offset(5.2f, 13.6f, -10.66f));
        root.addOrReplaceChild("wingr0",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 7.02f, 1.04f),
                PartPose.offset(-5.2f, 12.3f, -5.46f));
        root.addOrReplaceChild("wingr1",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 7.8f, 1.04f),
                PartPose.offset(-5.2f, 11.52f, -6.5f));
        root.addOrReplaceChild("wingr2",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 7.8f, 1.04f),
                PartPose.offset(-5.2f, 11.26f, -7.54f));
        root.addOrReplaceChild("wingr3",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 6.76f, 1.04f),
                PartPose.offset(-5.2f, 11.78f, -8.58f));
        root.addOrReplaceChild("wingr4",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 5.46f, 1.04f),
                PartPose.offset(-5.2f, 12.56f, -9.62f));
        root.addOrReplaceChild("wingr5",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.65f, 0.0f, -0.52f, 1.3f, 3.64f, 1.04f),
                PartPose.offset(-5.2f, 13.6f, -10.66f));
        root.addOrReplaceChild("deck0",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-4.42f, 0.0f, -0.39f, 8.84f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 18.02f, -2.34f));
        root.addOrReplaceChild("deck1",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-4.55f, 0.0f, -0.39f, 9.1f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 18.02f, -1.56f));
        root.addOrReplaceChild("deck2",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 18.02f, -0.78f));
        root.addOrReplaceChild("tail0",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.29f, 0.0f, -0.39f, 8.58f, 7.02f, 0.78f),
                PartPose.offset(0.0f, 12.82f, 0.0f));
        root.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.485f, 0.0f, -0.39f, 8.97f, 8.32f, 0.78f),
                PartPose.offset(0.0f, 11.52f, 0.78f));
        root.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 9.36f, 0.78f),
                PartPose.offset(0.0f, 10.48f, 1.56f));
        root.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.81f, 0.0f, -0.39f, 9.62f, 10.14f, 0.78f),
                PartPose.offset(0.0f, 9.7f, 2.34f));
        root.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.94f, 0.0f, -0.39f, 9.88f, 10.53f, 0.78f),
                PartPose.offset(0.0f, 9.18f, 3.12f));
        root.addOrReplaceChild("tail5",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.07f, 0.0f, -0.39f, 10.14f, 10.92f, 0.78f),
                PartPose.offset(0.0f, 8.66f, 3.9f));
        root.addOrReplaceChild("tail6",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.135f, 0.0f, -0.39f, 10.27f, 11.18f, 0.78f),
                PartPose.offset(0.0f, 8.27f, 4.68f));
        root.addOrReplaceChild("tail7",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.2f, 0.0f, -0.39f, 10.4f, 11.31f, 0.78f),
                PartPose.offset(0.0f, 8.01f, 5.46f));
        root.addOrReplaceChild("tail8",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.2f, 0.0f, -0.39f, 10.4f, 11.31f, 0.78f),
                PartPose.offset(0.0f, 7.88f, 6.24f));
        root.addOrReplaceChild("tail9",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.2f, 0.0f, -0.39f, 10.4f, 11.18f, 0.78f),
                PartPose.offset(0.0f, 7.88f, 7.02f));
        root.addOrReplaceChild("tail10",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.2f, 0.0f, -0.39f, 10.4f, 11.18f, 0.78f),
                PartPose.offset(0.0f, 7.75f, 7.8f));
        root.addOrReplaceChild("tail11",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.2f, 0.0f, -0.39f, 10.4f, 11.05f, 0.78f),
                PartPose.offset(0.0f, 7.75f, 8.58f));
        root.addOrReplaceChild("tail12",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.135f, 0.0f, -0.39f, 10.27f, 10.79f, 0.78f),
                PartPose.offset(0.0f, 7.88f, 9.36f));
        root.addOrReplaceChild("tail13",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.07f, 0.0f, -0.39f, 10.14f, 10.4f, 0.78f),
                PartPose.offset(0.0f, 8.01f, 10.14f));
        root.addOrReplaceChild("tail14",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-5.005f, 0.0f, -0.39f, 10.01f, 10.14f, 0.78f),
                PartPose.offset(0.0f, 8.14f, 10.92f));
        root.addOrReplaceChild("tail15",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.875f, 0.0f, -0.39f, 9.75f, 9.62f, 0.78f),
                PartPose.offset(0.0f, 8.4f, 11.7f));
        root.addOrReplaceChild("tail16",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 8.84f, 0.78f),
                PartPose.offset(0.0f, 8.79f, 12.48f));
        root.addOrReplaceChild("tail17",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.485f, 0.0f, -0.39f, 8.97f, 7.93f, 0.78f),
                PartPose.offset(0.0f, 9.31f, 13.26f));
        root.addOrReplaceChild("tail18",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-4.225f, 0.0f, -0.39f, 8.45f, 6.89f, 0.78f),
                PartPose.offset(0.0f, 9.83f, 14.04f));
        root.addOrReplaceChild("tail19",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-3.9f, 0.0f, -0.39f, 7.8f, 5.59f, 0.78f),
                PartPose.offset(0.0f, 10.61f, 14.82f));
        root.addOrReplaceChild("tail20",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-3.445f, 0.0f, -0.39f, 6.89f, 4.03f, 0.78f),
                PartPose.offset(0.0f, 11.52f, 15.6f));
        root.addOrReplaceChild("tail21",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-2.86f, 0.0f, -0.39f, 5.72f, 2.34f, 0.78f),
                PartPose.offset(0.0f, 12.56f, 16.25f));
        root.addOrReplaceChild("redl0",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 4.305f, -4.095f));
        root.addOrReplaceChild("redl1",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 3.915f, -5.005f));
        root.addOrReplaceChild("redl2",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 3.8067f, -5.8067f));
        root.addOrReplaceChild("redl3",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 3.98f, -6.5f));
        root.addOrReplaceChild("redl4",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 4.1533f, -7.1933f));
        root.addOrReplaceChild("redl5",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 4.5433f, -7.8433f));
        root.addOrReplaceChild("redl6",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 5.15f, -8.45f));
        root.addOrReplaceChild("redl7",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 5.7567f, -9.0567f));
        root.addOrReplaceChild("redl8",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 6.4175f, -9.5875f));
        root.addOrReplaceChild("redl9",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 7.1325f, -10.0425f));
        root.addOrReplaceChild("redl10",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 7.8475f, -10.4975f));
        root.addOrReplaceChild("redl11",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 8.5625f, -10.9525f));
        root.addOrReplaceChild("redl12",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 9.31f, -11.375f));
        root.addOrReplaceChild("redl13",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 10.09f, -11.765f));
        root.addOrReplaceChild("redl14",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 10.87f, -12.155f));
        root.addOrReplaceChild("redl15",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 11.65f, -12.545f));
        root.addOrReplaceChild("redl16",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 12.3975f, -12.9025f));
        root.addOrReplaceChild("redl17",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 13.1125f, -13.2275f));
        root.addOrReplaceChild("redl18",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 13.8275f, -13.5525f));
        root.addOrReplaceChild("redl19",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 14.5425f, -13.8775f));
        root.addOrReplaceChild("redl20",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 15.2467f, -14.1267f));
        root.addOrReplaceChild("redl21",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 15.94f, -14.3f));
        root.addOrReplaceChild("redl22",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 16.6333f, -14.4733f));
        root.addOrReplaceChild("redl23",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 17.4025f, -14.3f));
        root.addOrReplaceChild("redl24",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 18.2475f, -13.78f));
        root.addOrReplaceChild("redl25",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 18.865f, -13.0867f));
        root.addOrReplaceChild("redl26",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.255f, -12.22f));
        root.addOrReplaceChild("redl27",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.645f, -11.3533f));
        root.addOrReplaceChild("redl28",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.892f, -10.478f));
        root.addOrReplaceChild("redl29",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.996f, -9.594f));
        root.addOrReplaceChild("redl30",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.1f, -8.71f));
        root.addOrReplaceChild("redl31",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.204f, -7.826f));
        root.addOrReplaceChild("redl32",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.308f, -6.942f));
        root.addOrReplaceChild("redl33",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.373f, -6.058f));
        root.addOrReplaceChild("redl34",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.399f, -5.174f));
        root.addOrReplaceChild("redl35",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.425f, -4.29f));
        root.addOrReplaceChild("redl36",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.451f, -3.406f));
        root.addOrReplaceChild("redl37",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.477f, -2.522f));
        root.addOrReplaceChild("redl38",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.4413f, -1.69f));
        root.addOrReplaceChild("redl39",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.3438f, -0.91f));
        root.addOrReplaceChild("redl40",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.2463f, -0.13f));
        root.addOrReplaceChild("redl41",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 20.1488f, 0.65f));
        root.addOrReplaceChild("redl42",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.97f, 1.3867f));
        root.addOrReplaceChild("redl43",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.71f, 2.08f));
        root.addOrReplaceChild("redl44",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.45f, 2.7733f));
        root.addOrReplaceChild("redr0",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 4.305f, -4.095f));
        root.addOrReplaceChild("redr1",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 3.915f, -5.005f));
        root.addOrReplaceChild("redr2",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 3.8067f, -5.8067f));
        root.addOrReplaceChild("redr3",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 3.98f, -6.5f));
        root.addOrReplaceChild("redr4",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 4.1533f, -7.1933f));
        root.addOrReplaceChild("redr5",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 4.5433f, -7.8433f));
        root.addOrReplaceChild("redr6",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 5.15f, -8.45f));
        root.addOrReplaceChild("redr7",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 5.7567f, -9.0567f));
        root.addOrReplaceChild("redr8",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 6.4175f, -9.5875f));
        root.addOrReplaceChild("redr9",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 7.1325f, -10.0425f));
        root.addOrReplaceChild("redr10",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 7.8475f, -10.4975f));
        root.addOrReplaceChild("redr11",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 8.5625f, -10.9525f));
        root.addOrReplaceChild("redr12",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 9.31f, -11.375f));
        root.addOrReplaceChild("redr13",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 10.09f, -11.765f));
        root.addOrReplaceChild("redr14",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 10.87f, -12.155f));
        root.addOrReplaceChild("redr15",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 11.65f, -12.545f));
        root.addOrReplaceChild("redr16",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 12.3975f, -12.9025f));
        root.addOrReplaceChild("redr17",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 13.1125f, -13.2275f));
        root.addOrReplaceChild("redr18",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 13.8275f, -13.5525f));
        root.addOrReplaceChild("redr19",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 14.5425f, -13.8775f));
        root.addOrReplaceChild("redr20",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 15.2467f, -14.1267f));
        root.addOrReplaceChild("redr21",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 15.94f, -14.3f));
        root.addOrReplaceChild("redr22",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 16.6333f, -14.4733f));
        root.addOrReplaceChild("redr23",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 17.4025f, -14.3f));
        root.addOrReplaceChild("redr24",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 18.2475f, -13.78f));
        root.addOrReplaceChild("redr25",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 18.865f, -13.0867f));
        root.addOrReplaceChild("redr26",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.255f, -12.22f));
        root.addOrReplaceChild("redr27",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.645f, -11.3533f));
        root.addOrReplaceChild("redr28",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.892f, -10.478f));
        root.addOrReplaceChild("redr29",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.996f, -9.594f));
        root.addOrReplaceChild("redr30",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.1f, -8.71f));
        root.addOrReplaceChild("redr31",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.204f, -7.826f));
        root.addOrReplaceChild("redr32",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.308f, -6.942f));
        root.addOrReplaceChild("redr33",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.373f, -6.058f));
        root.addOrReplaceChild("redr34",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.399f, -5.174f));
        root.addOrReplaceChild("redr35",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.425f, -4.29f));
        root.addOrReplaceChild("redr36",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.451f, -3.406f));
        root.addOrReplaceChild("redr37",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.477f, -2.522f));
        root.addOrReplaceChild("redr38",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.4413f, -1.69f));
        root.addOrReplaceChild("redr39",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.3438f, -0.91f));
        root.addOrReplaceChild("redr40",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.2463f, -0.13f));
        root.addOrReplaceChild("redr41",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 20.1488f, 0.65f));
        root.addOrReplaceChild("redr42",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.97f, 1.3867f));
        root.addOrReplaceChild("redr43",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.71f, 2.08f));
        root.addOrReplaceChild("redr44",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.45f, 2.7733f));
        root.addOrReplaceChild("redl20",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 5.3667f, -5.5467f));
        root.addOrReplaceChild("redl21",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 5.54f, -6.24f));
        root.addOrReplaceChild("redl22",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 5.7133f, -6.9333f));
        root.addOrReplaceChild("redl23",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 6.1033f, -7.5833f));
        root.addOrReplaceChild("redl24",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 6.71f, -8.19f));
        root.addOrReplaceChild("redl25",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 7.3167f, -8.7967f));
        root.addOrReplaceChild("redl26",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 7.9775f, -9.3275f));
        root.addOrReplaceChild("redl27",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 8.6925f, -9.7825f));
        root.addOrReplaceChild("redl28",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 9.4075f, -10.2375f));
        root.addOrReplaceChild("redl29",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 10.1225f, -10.6925f));
        root.addOrReplaceChild("redl210",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 10.87f, -11.115f));
        root.addOrReplaceChild("redl211",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 11.65f, -11.505f));
        root.addOrReplaceChild("redl212",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 12.43f, -11.895f));
        root.addOrReplaceChild("redl213",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 13.21f, -12.285f));
        root.addOrReplaceChild("redl214",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 14.0333f, -12.6533f));
        root.addOrReplaceChild("redl215",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 14.9f, -13.0f));
        root.addOrReplaceChild("redl216",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 15.7667f, -13.3467f));
        root.addOrReplaceChild("redl217",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 16.5467f, -13.39f));
        root.addOrReplaceChild("redl218",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 17.24f, -13.13f));
        root.addOrReplaceChild("redl219",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 17.9333f, -12.87f));
        root.addOrReplaceChild("redl220",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 18.41f, -12.3825f));
        root.addOrReplaceChild("redl221",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 18.67f, -11.6675f));
        root.addOrReplaceChild("redl222",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 18.93f, -10.9525f));
        root.addOrReplaceChild("redl223",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.19f, -10.2375f));
        root.addOrReplaceChild("redl224",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.3525f, -9.49f));
        root.addOrReplaceChild("redl225",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.4175f, -8.71f));
        root.addOrReplaceChild("redl226",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.4825f, -7.93f));
        root.addOrReplaceChild("redl227",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.5475f, -7.15f));
        root.addOrReplaceChild("redl228",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.6125f, -6.37f));
        root.addOrReplaceChild("redl229",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.6775f, -5.59f));
        root.addOrReplaceChild("redl230",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.71f, -4.81f));
        root.addOrReplaceChild("redl231",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.71f, -4.03f));
        root.addOrReplaceChild("redl232",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.71f, -3.25f));
        root.addOrReplaceChild("redl233",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.71f, -2.47f));
        root.addOrReplaceChild("redl234",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.55f, 19.71f, -1.69f));
        root.addOrReplaceChild("redr20",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 5.3667f, -5.5467f));
        root.addOrReplaceChild("redr21",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 5.54f, -6.24f));
        root.addOrReplaceChild("redr22",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 5.7133f, -6.9333f));
        root.addOrReplaceChild("redr23",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 6.1033f, -7.5833f));
        root.addOrReplaceChild("redr24",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 6.71f, -8.19f));
        root.addOrReplaceChild("redr25",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 7.3167f, -8.7967f));
        root.addOrReplaceChild("redr26",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 7.9775f, -9.3275f));
        root.addOrReplaceChild("redr27",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 8.6925f, -9.7825f));
        root.addOrReplaceChild("redr28",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 9.4075f, -10.2375f));
        root.addOrReplaceChild("redr29",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 10.1225f, -10.6925f));
        root.addOrReplaceChild("redr210",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 10.87f, -11.115f));
        root.addOrReplaceChild("redr211",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 11.65f, -11.505f));
        root.addOrReplaceChild("redr212",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 12.43f, -11.895f));
        root.addOrReplaceChild("redr213",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 13.21f, -12.285f));
        root.addOrReplaceChild("redr214",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 14.0333f, -12.6533f));
        root.addOrReplaceChild("redr215",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 14.9f, -13.0f));
        root.addOrReplaceChild("redr216",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 15.7667f, -13.3467f));
        root.addOrReplaceChild("redr217",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 16.5467f, -13.39f));
        root.addOrReplaceChild("redr218",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 17.24f, -13.13f));
        root.addOrReplaceChild("redr219",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 17.9333f, -12.87f));
        root.addOrReplaceChild("redr220",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 18.41f, -12.3825f));
        root.addOrReplaceChild("redr221",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 18.67f, -11.6675f));
        root.addOrReplaceChild("redr222",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 18.93f, -10.9525f));
        root.addOrReplaceChild("redr223",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.19f, -10.2375f));
        root.addOrReplaceChild("redr224",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.3525f, -9.49f));
        root.addOrReplaceChild("redr225",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.4175f, -8.71f));
        root.addOrReplaceChild("redr226",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.4825f, -7.93f));
        root.addOrReplaceChild("redr227",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.5475f, -7.15f));
        root.addOrReplaceChild("redr228",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.6125f, -6.37f));
        root.addOrReplaceChild("redr229",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.6775f, -5.59f));
        root.addOrReplaceChild("redr230",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.71f, -4.81f));
        root.addOrReplaceChild("redr231",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.71f, -4.03f));
        root.addOrReplaceChild("redr232",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.71f, -3.25f));
        root.addOrReplaceChild("redr233",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.71f, -2.47f));
        root.addOrReplaceChild("redr234",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.55f, 19.71f, -1.69f));
        root.addOrReplaceChild("chevl00",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 10.9025f, 15.5025f));
        root.addOrReplaceChild("chevl01",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 10.7075f, 14.7875f));
        root.addOrReplaceChild("chevl02",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 10.5125f, 14.0725f));
        root.addOrReplaceChild("chevl03",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 10.3175f, 13.3575f));
        root.addOrReplaceChild("chevl04",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 10.2525f, 12.61f));
        root.addOrReplaceChild("chevl05",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 10.3175f, 11.83f));
        root.addOrReplaceChild("chevl06",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 10.3825f, 11.05f));
        root.addOrReplaceChild("chevl07",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 10.4475f, 10.27f));
        root.addOrReplaceChild("chevl08",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 10.6967f, 9.4467f));
        root.addOrReplaceChild("chevl09",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.13f, 8.58f));
        root.addOrReplaceChild("chevl010",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.5633f, 7.7133f));
        root.addOrReplaceChild("chevl011",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.0833f, 6.9333f));
        root.addOrReplaceChild("chevl012",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.69f, 6.24f));
        root.addOrReplaceChild("chevl013",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 13.2967f, 5.5467f));
        root.addOrReplaceChild("chevl014",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 13.9467f, 4.94f));
        root.addOrReplaceChild("chevl015",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 14.64f, 4.42f));
        root.addOrReplaceChild("chevl016",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 15.3333f, 3.9f));
        root.addOrReplaceChild("chevl10",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.0075f, 15.5025f));
        root.addOrReplaceChild("chevl11",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.8125f, 14.7875f));
        root.addOrReplaceChild("chevl12",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.6175f, 14.0725f));
        root.addOrReplaceChild("chevl13",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.4225f, 13.3575f));
        root.addOrReplaceChild("chevl14",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.3575f, 12.61f));
        root.addOrReplaceChild("chevl15",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.4225f, 11.83f));
        root.addOrReplaceChild("chevl16",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.4875f, 11.05f));
        root.addOrReplaceChild("chevl17",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.5525f, 10.27f));
        root.addOrReplaceChild("chevl18",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 11.8017f, 9.4467f));
        root.addOrReplaceChild("chevl19",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.235f, 8.58f));
        root.addOrReplaceChild("chevl110",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.6683f, 7.7133f));
        root.addOrReplaceChild("chevl111",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 13.1883f, 6.9333f));
        root.addOrReplaceChild("chevl112",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 13.795f, 6.24f));
        root.addOrReplaceChild("chevl113",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 14.4017f, 5.5467f));
        root.addOrReplaceChild("chevl114",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 15.0517f, 4.94f));
        root.addOrReplaceChild("chevl115",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 15.745f, 4.42f));
        root.addOrReplaceChild("chevl116",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 16.4383f, 3.9f));
        root.addOrReplaceChild("chevl20",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 13.1125f, 15.5025f));
        root.addOrReplaceChild("chevl21",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.9175f, 14.7875f));
        root.addOrReplaceChild("chevl22",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.7225f, 14.0725f));
        root.addOrReplaceChild("chevl23",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.5275f, 13.3575f));
        root.addOrReplaceChild("chevl24",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.4625f, 12.61f));
        root.addOrReplaceChild("chevl25",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.5275f, 11.83f));
        root.addOrReplaceChild("chevl26",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.5925f, 11.05f));
        root.addOrReplaceChild("chevl27",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.6575f, 10.27f));
        root.addOrReplaceChild("chevl28",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 12.9067f, 9.4467f));
        root.addOrReplaceChild("chevl29",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 13.34f, 8.58f));
        root.addOrReplaceChild("chevl210",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 13.7733f, 7.7133f));
        root.addOrReplaceChild("chevl211",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 14.2933f, 6.9333f));
        root.addOrReplaceChild("chevl212",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 14.9f, 6.24f));
        root.addOrReplaceChild("chevl213",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 15.5067f, 5.5467f));
        root.addOrReplaceChild("chevl214",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 16.1567f, 4.94f));
        root.addOrReplaceChild("chevl215",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 16.85f, 4.42f));
        root.addOrReplaceChild("chevl216",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(4.68f, 17.5433f, 3.9f));
        root.addOrReplaceChild("chevr00",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 10.9025f, 15.5025f));
        root.addOrReplaceChild("chevr01",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 10.7075f, 14.7875f));
        root.addOrReplaceChild("chevr02",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 10.5125f, 14.0725f));
        root.addOrReplaceChild("chevr03",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 10.3175f, 13.3575f));
        root.addOrReplaceChild("chevr04",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 10.2525f, 12.61f));
        root.addOrReplaceChild("chevr05",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 10.3175f, 11.83f));
        root.addOrReplaceChild("chevr06",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 10.3825f, 11.05f));
        root.addOrReplaceChild("chevr07",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 10.4475f, 10.27f));
        root.addOrReplaceChild("chevr08",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 10.6967f, 9.4467f));
        root.addOrReplaceChild("chevr09",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.13f, 8.58f));
        root.addOrReplaceChild("chevr010",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.5633f, 7.7133f));
        root.addOrReplaceChild("chevr011",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.0833f, 6.9333f));
        root.addOrReplaceChild("chevr012",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.69f, 6.24f));
        root.addOrReplaceChild("chevr013",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 13.2967f, 5.5467f));
        root.addOrReplaceChild("chevr014",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 13.9467f, 4.94f));
        root.addOrReplaceChild("chevr015",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 14.64f, 4.42f));
        root.addOrReplaceChild("chevr016",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 15.3333f, 3.9f));
        root.addOrReplaceChild("chevr10",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.0075f, 15.5025f));
        root.addOrReplaceChild("chevr11",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.8125f, 14.7875f));
        root.addOrReplaceChild("chevr12",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.6175f, 14.0725f));
        root.addOrReplaceChild("chevr13",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.4225f, 13.3575f));
        root.addOrReplaceChild("chevr14",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.3575f, 12.61f));
        root.addOrReplaceChild("chevr15",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.4225f, 11.83f));
        root.addOrReplaceChild("chevr16",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.4875f, 11.05f));
        root.addOrReplaceChild("chevr17",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.5525f, 10.27f));
        root.addOrReplaceChild("chevr18",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 11.8017f, 9.4467f));
        root.addOrReplaceChild("chevr19",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.235f, 8.58f));
        root.addOrReplaceChild("chevr110",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.6683f, 7.7133f));
        root.addOrReplaceChild("chevr111",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 13.1883f, 6.9333f));
        root.addOrReplaceChild("chevr112",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 13.795f, 6.24f));
        root.addOrReplaceChild("chevr113",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 14.4017f, 5.5467f));
        root.addOrReplaceChild("chevr114",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 15.0517f, 4.94f));
        root.addOrReplaceChild("chevr115",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 15.745f, 4.42f));
        root.addOrReplaceChild("chevr116",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 16.4383f, 3.9f));
        root.addOrReplaceChild("chevr20",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 13.1125f, 15.5025f));
        root.addOrReplaceChild("chevr21",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.9175f, 14.7875f));
        root.addOrReplaceChild("chevr22",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.7225f, 14.0725f));
        root.addOrReplaceChild("chevr23",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.5275f, 13.3575f));
        root.addOrReplaceChild("chevr24",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.4625f, 12.61f));
        root.addOrReplaceChild("chevr25",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.5275f, 11.83f));
        root.addOrReplaceChild("chevr26",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.5925f, 11.05f));
        root.addOrReplaceChild("chevr27",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.6575f, 10.27f));
        root.addOrReplaceChild("chevr28",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 12.9067f, 9.4467f));
        root.addOrReplaceChild("chevr29",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 13.34f, 8.58f));
        root.addOrReplaceChild("chevr210",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 13.7733f, 7.7133f));
        root.addOrReplaceChild("chevr211",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 14.2933f, 6.9333f));
        root.addOrReplaceChild("chevr212",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 14.9f, 6.24f));
        root.addOrReplaceChild("chevr213",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 15.5067f, 5.5467f));
        root.addOrReplaceChild("chevr214",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 16.1567f, 4.94f));
        root.addOrReplaceChild("chevr215",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 16.85f, 4.42f));
        root.addOrReplaceChild("chevr216",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-0.52f, -0.585f, -0.4859f, 1.04f, 1.17f, 0.9718f),
                PartPose.offset(-4.68f, 17.5433f, 3.9f));
        root.addOrReplaceChild("panl0",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(4.81f, 16.915f, 6.1425f));
        root.addOrReplaceChild("panl1",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(4.81f, 16.785f, 6.9875f));
        root.addOrReplaceChild("panl2",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(4.81f, 16.655f, 7.8325f));
        root.addOrReplaceChild("panl3",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(4.81f, 16.525f, 8.6775f));
        root.addOrReplaceChild("panl4",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(4.81f, 16.3625f, 9.49f));
        root.addOrReplaceChild("panl5",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(4.81f, 16.1675f, 10.27f));
        root.addOrReplaceChild("panl6",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(4.81f, 15.9725f, 11.05f));
        root.addOrReplaceChild("panl7",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(4.81f, 15.7775f, 11.83f));
        root.addOrReplaceChild("panr0",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(-4.81f, 16.915f, 6.1425f));
        root.addOrReplaceChild("panr1",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(-4.81f, 16.785f, 6.9875f));
        root.addOrReplaceChild("panr2",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(-4.81f, 16.655f, 7.8325f));
        root.addOrReplaceChild("panr3",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(-4.81f, 16.525f, 8.6775f));
        root.addOrReplaceChild("panr4",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(-4.81f, 16.3625f, 9.49f));
        root.addOrReplaceChild("panr5",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(-4.81f, 16.1675f, 10.27f));
        root.addOrReplaceChild("panr6",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(-4.81f, 15.9725f, 11.05f));
        root.addOrReplaceChild("panr7",
                CubeListBuilder.create().texOffs(STEEL, STEEL_V)
                        .addBox(-0.455f, -0.91f, -0.4859f, 0.91f, 1.82f, 0.9718f),
                PartPose.offset(-4.81f, 15.7775f, 11.83f));
        root.addOrReplaceChild("lowl0",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 19.411f, 4.836f));
        root.addOrReplaceChild("lowl1",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 19.333f, 5.668f));
        root.addOrReplaceChild("lowl2",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 19.255f, 6.5f));
        root.addOrReplaceChild("lowl3",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 19.177f, 7.332f));
        root.addOrReplaceChild("lowl4",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 19.099f, 8.164f));
        root.addOrReplaceChild("lowl5",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 18.9625f, 9.035f));
        root.addOrReplaceChild("lowl6",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 18.7675f, 9.945f));
        root.addOrReplaceChild("lowl7",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 18.5725f, 10.855f));
        root.addOrReplaceChild("lowl8",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 18.3775f, 11.765f));
        root.addOrReplaceChild("lowl9",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 18.1067f, 12.61f));
        root.addOrReplaceChild("lowl10",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 17.76f, 13.39f));
        root.addOrReplaceChild("lowl11",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(4.81f, 17.4133f, 14.17f));
        root.addOrReplaceChild("lowr0",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 19.411f, 4.836f));
        root.addOrReplaceChild("lowr1",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 19.333f, 5.668f));
        root.addOrReplaceChild("lowr2",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 19.255f, 6.5f));
        root.addOrReplaceChild("lowr3",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 19.177f, 7.332f));
        root.addOrReplaceChild("lowr4",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 19.099f, 8.164f));
        root.addOrReplaceChild("lowr5",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 18.9625f, 9.035f));
        root.addOrReplaceChild("lowr6",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 18.7675f, 9.945f));
        root.addOrReplaceChild("lowr7",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 18.5725f, 10.855f));
        root.addOrReplaceChild("lowr8",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 18.3775f, 11.765f));
        root.addOrReplaceChild("lowr9",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 18.1067f, 12.61f));
        root.addOrReplaceChild("lowr10",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 17.76f, 13.39f));
        root.addOrReplaceChild("lowr11",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-0.52f, -0.52f, -0.4859f, 1.04f, 1.04f, 0.9718f),
                PartPose.offset(-4.81f, 17.4133f, 14.17f));
        root.addOrReplaceChild("greyl0",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(4.42f, 10.545f, -11.635f));
        root.addOrReplaceChild("greyl1",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(4.42f, 11.195f, -12.025f));
        root.addOrReplaceChild("greyl2",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(4.42f, 11.845f, -12.415f));
        root.addOrReplaceChild("greyl3",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(4.42f, 12.495f, -12.805f));
        root.addOrReplaceChild("greyl4",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(4.42f, 13.2533f, -13.1733f));
        root.addOrReplaceChild("greyl5",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(4.42f, 14.12f, -13.52f));
        root.addOrReplaceChild("greyl6",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(4.42f, 14.9867f, -13.8667f));
        root.addOrReplaceChild("greyr0",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(-4.42f, 10.545f, -11.635f));
        root.addOrReplaceChild("greyr1",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(-4.42f, 11.195f, -12.025f));
        root.addOrReplaceChild("greyr2",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(-4.42f, 11.845f, -12.415f));
        root.addOrReplaceChild("greyr3",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(-4.42f, 12.495f, -12.805f));
        root.addOrReplaceChild("greyr4",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(-4.42f, 13.2533f, -13.1733f));
        root.addOrReplaceChild("greyr5",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(-4.42f, 14.12f, -13.52f));
        root.addOrReplaceChild("greyr6",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-0.455f, -0.52f, -0.4859f, 0.91f, 1.04f, 0.9718f),
                PartPose.offset(-4.42f, 14.9867f, -13.8667f));
        root.addOrReplaceChild("lampc",
                CubeListBuilder.create().texOffs(WHITE, WHITE_V)
                        .addBox(-2.08f, -1.82f, -0.65f, 4.16f, 3.64f, 1.04f),
                PartPose.offset(0.0f, 15.16f, -14.95f));
        root.addOrReplaceChild("lampl",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-1.17f, -1.43f, -0.52f, 2.34f, 2.86f, 0.91f),
                PartPose.offset(2.6f, 14.9f, -14.56f));
        root.addOrReplaceChild("lampr",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(-1.17f, -1.43f, -0.52f, 2.34f, 2.86f, 0.91f),
                PartPose.offset(-2.6f, 14.9f, -14.56f));
        root.addOrReplaceChild("drll",
                CubeListBuilder.create().texOffs(WHITE, WHITE_V)
                        .addBox(-0.52f, -1.82f, -0.52f, 1.04f, 3.64f, 0.91f),
                PartPose.offset(3.25f, 12.3f, -14.04f));
        root.addOrReplaceChild("drlr",
                CubeListBuilder.create().texOffs(WHITE, WHITE_V)
                        .addBox(-0.52f, -1.82f, -0.52f, 1.04f, 3.64f, 0.91f),
                PartPose.offset(-3.25f, 12.3f, -14.04f));
        root.addOrReplaceChild("blinkl",
                CubeListBuilder.create().texOffs(AMBER, AMBER_V)
                        .addBox(-0.91f, -0.91f, -0.91f, 1.82f, 1.82f, 1.82f),
                PartPose.offset(4.29f, 4.24f, -8.58f));
        root.addOrReplaceChild("blinkr",
                CubeListBuilder.create().texOffs(AMBER, AMBER_V)
                        .addBox(-0.91f, -0.91f, -0.91f, 1.82f, 1.82f, 1.82f),
                PartPose.offset(-4.29f, 4.24f, -8.58f));
        root.addOrReplaceChild("blinkbl",
                CubeListBuilder.create().texOffs(AMBER, AMBER_V)
                        .addBox(-0.91f, -0.91f, -0.91f, 1.82f, 1.82f, 1.82f),
                PartPose.offset(4.42f, 12.04f, 15.86f));
        root.addOrReplaceChild("blinkbr",
                CubeListBuilder.create().texOffs(AMBER, AMBER_V)
                        .addBox(-0.91f, -0.91f, -0.91f, 1.82f, 1.82f, 1.82f),
                PartPose.offset(-4.42f, 12.04f, 15.86f));
        root.addOrReplaceChild("taillamp",
                CubeListBuilder.create().texOffs(BRAKE, BRAKE_V)
                        .addBox(-2.86f, -0.65f, -0.52f, 5.72f, 1.3f, 0.78f),
                PartPose.offset(0.0f, 13.08f, 16.77f));
        root.addOrReplaceChild("plate",
                CubeListBuilder.create().texOffs(WHITE, WHITE_V)
                        .addBox(-2.47f, -1.56f, -0.39f, 4.94f, 3.12f, 0.65f),
                PartPose.offsetAndRotation(0.0f, 16.07f, 17.03f, -16f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("seat0",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-3.64f, 0.0f, -0.39f, 7.28f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 10.48f, 0.78f));
        root.addOrReplaceChild("seat1",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-3.965f, 0.0f, -0.39f, 7.93f, 1.95f, 0.78f),
                PartPose.offset(0.0f, 9.57f, 1.56f));
        root.addOrReplaceChild("seat2",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.225f, 0.0f, -0.39f, 8.45f, 1.95f, 0.78f),
                PartPose.offset(0.0f, 8.92f, 2.34f));
        root.addOrReplaceChild("seat3",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.42f, 0.0f, -0.39f, 8.84f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 8.4f, 3.12f));
        root.addOrReplaceChild("seat4",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.55f, 0.0f, -0.39f, 9.1f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 8.14f, 3.9f));
        root.addOrReplaceChild("seat5",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.615f, 0.0f, -0.39f, 9.23f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 7.88f, 4.68f));
        root.addOrReplaceChild("seat6",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 7.75f, 5.46f));
        root.addOrReplaceChild("seat7",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 7.62f, 6.24f));
        root.addOrReplaceChild("seat8",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 7.62f, 7.02f));
        root.addOrReplaceChild("seat9",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 2.08f, 0.78f),
                PartPose.offset(0.0f, 7.49f, 7.8f));
        root.addOrReplaceChild("seat10",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.68f, 0.0f, -0.39f, 9.36f, 2.34f, 0.78f),
                PartPose.offset(0.0f, 7.1f, 8.58f));
        root.addOrReplaceChild("seat11",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.615f, 0.0f, -0.39f, 9.23f, 2.47f, 0.78f),
                PartPose.offset(0.0f, 6.84f, 9.36f));
        root.addOrReplaceChild("seat12",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.55f, 0.0f, -0.39f, 9.1f, 2.47f, 0.78f),
                PartPose.offset(0.0f, 6.84f, 10.14f));
        root.addOrReplaceChild("seat13",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.485f, 0.0f, -0.39f, 8.97f, 2.47f, 0.78f),
                PartPose.offset(0.0f, 6.97f, 10.92f));
        root.addOrReplaceChild("seat14",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-4.29f, 0.0f, -0.39f, 8.58f, 2.47f, 0.78f),
                PartPose.offset(0.0f, 7.23f, 11.7f));
        root.addOrReplaceChild("seat15",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-3.965f, 0.0f, -0.39f, 7.93f, 2.47f, 0.78f),
                PartPose.offset(0.0f, 7.75f, 12.48f));
        root.addOrReplaceChild("raill",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-0.585f, -0.585f, -4.16f, 1.17f, 1.17f, 8.32f),
                PartPose.offsetAndRotation(4.42f, 8.66f, 12.74f, -5f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("railr",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-0.585f, -0.585f, -4.16f, 1.17f, 1.17f, 8.32f),
                PartPose.offsetAndRotation(-4.42f, 8.66f, 12.74f, -5f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("railbk",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-4.42f, -0.585f, -0.585f, 8.84f, 1.17f, 1.17f),
                PartPose.offset(0.0f, 8.01f, 16.64f));
        root.addOrReplaceChild("engine",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-3.9f, -2.99f, -3.9f, 7.8f, 5.98f, 7.8f),
                PartPose.offset(-0.78f, 19.32f, 8.58f));
        root.addOrReplaceChild("cvt",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(-1.69f, -3.25f, -4.42f, 1.69f, 6.5f, 8.84f),
                PartPose.offset(-4.81f, 19.19f, 7.8f));
        root.addOrReplaceChild("cvtcap",
                CubeListBuilder.create().texOffs(GREY, GREY_V)
                        .addBox(-0.65f, -1.43f, -1.43f, 0.65f, 2.86f, 2.86f),
                PartPose.offset(-5.85f, 19.45f, 8.06f));
        root.addOrReplaceChild("swing",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(0.0f, -0.715f, -3.9f, 2.86f, 1.43f, 7.8f),
                PartPose.offset(1.95f, 19.97f, 8.58f));
        root.addOrReplaceChild("pipe",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(0.0f, -0.715f, -5.46f, 1.43f, 1.43f, 10.92f),
                PartPose.offsetAndRotation(2.86f, 20.75f, 3.38f, -8f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("muffler",
                CubeListBuilder.create().texOffs(GREY, GREY_V)
                        .addBox(0.0f, -1.365f, 0.0f, 2.73f, 2.73f, 5.72f),
                PartPose.offset(3.25f, 19.97f, 9.36f));
        root.addOrReplaceChild("mufftip",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(0.0f, -0.975f, 0.0f, 1.95f, 1.95f, 1.04f),
                PartPose.offset(3.64f, 19.97f, 15.08f));
        root.addOrReplaceChild("spring0",
                CubeListBuilder.create().texOffs(GREY, GREY_V)
                        .addBox(-0.715f, -0.455f, -0.715f, 1.43f, 0.91f, 1.43f),
                PartPose.offset(3.9f, 13.08f, 8.06f));
        root.addOrReplaceChild("spring1",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(-0.715f, -0.455f, -0.715f, 1.43f, 0.91f, 1.43f),
                PartPose.offset(3.9f, 14.51f, 8.775f));
        root.addOrReplaceChild("spring2",
                CubeListBuilder.create().texOffs(GREY, GREY_V)
                        .addBox(-0.715f, -0.455f, -0.715f, 1.43f, 0.91f, 1.43f),
                PartPose.offset(3.9f, 15.94f, 9.49f));
        root.addOrReplaceChild("spring3",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(-0.715f, -0.455f, -0.715f, 1.43f, 0.91f, 1.43f),
                PartPose.offset(3.9f, 17.37f, 10.205f));
        root.addOrReplaceChild("spring4",
                CubeListBuilder.create().texOffs(GREY, GREY_V)
                        .addBox(-0.715f, -0.455f, -0.715f, 1.43f, 0.91f, 1.43f),
                PartPose.offset(3.9f, 18.8f, 10.92f));
        root.addOrReplaceChild("standl",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(-0.455f, 0.0f, -0.455f, 0.91f, 4.16f, 0.91f),
                PartPose.offsetAndRotation(-3.12f, 19.97f, 4.68f, -26f * Mth.DEG_TO_RAD, 0f, 0f));
        root.addOrReplaceChild("standft",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-0.65f, 0.0f, -1.17f, 1.3f, 0.78f, 2.34f),
                PartPose.offset(-4.94f, 23.48f, 2.86f));
        PartDefinition steer = root.addOrReplaceChild("steer",
                CubeListBuilder.create(),
                PartPose.offset(0.0f, 3.46f, -5.46f));
        PartDefinition bar = steer.addOrReplaceChild("bar",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-8.32f, -0.715f, -0.715f, 16.64f, 1.43f, 1.43f),
                PartPose.offset(0.0f, -0.78f, 0.0f));
        steer.addOrReplaceChild("barcowl",
                CubeListBuilder.create().texOffs(BODY, BODY_V)
                        .addBox(-4.16f, -2.73f, -2.21f, 8.32f, 2.73f, 4.42f),
                PartPose.offsetAndRotation(0.0f, -0.78f, 0.0f, -10f * Mth.DEG_TO_RAD, 0f, 0f));
        steer.addOrReplaceChild("bartop",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(-2.73f, -0.845f, -1.43f, 5.46f, 0.845f, 2.86f),
                PartPose.offsetAndRotation(0.0f, -3.25f, -0.52f, -10f * Mth.DEG_TO_RAD, 0f, 0f));
        bar.addOrReplaceChild("gripl",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(5.85f, -1.105f, -1.105f, 2.34f, 2.21f, 2.21f),
                PartPose.ZERO);
        bar.addOrReplaceChild("gripr",
                CubeListBuilder.create().texOffs(SEAT, SEAT_V)
                        .addBox(-8.19f, -1.105f, -1.105f, 2.34f, 2.21f, 2.21f),
                PartPose.ZERO);
        bar.addOrReplaceChild("grendl",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(8.19f, -0.78f, -0.78f, 1.17f, 1.56f, 1.56f),
                PartPose.ZERO);
        bar.addOrReplaceChild("grendr",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(-9.36f, -0.78f, -0.78f, 1.17f, 1.56f, 1.56f),
                PartPose.ZERO);
        PartDefinition steml = steer.addOrReplaceChild("steml",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-0.364f, -4.16f, -0.364f, 0.728f, 4.16f, 0.728f),
                PartPose.offsetAndRotation(5.2f, -1.56f, 0.0f, 0f, 0f, -14f * Mth.DEG_TO_RAD));
        PartDefinition stemr = steer.addOrReplaceChild("stemr",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-0.364f, -4.16f, -0.364f, 0.728f, 4.16f, 0.728f),
                PartPose.offsetAndRotation(-5.2f, -1.56f, 0.0f, 0f, 0f, 14f * Mth.DEG_TO_RAD));
        PartDefinition mirl = steml.addOrReplaceChild("mirl",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-1.43f, -1.43f, -0.455f, 2.86f, 2.86f, 0.91f),
                PartPose.offsetAndRotation(0.78f, -4.16f, 0.0f, 0f, 0f, -14f * Mth.DEG_TO_RAD));
        PartDefinition mirr = stemr.addOrReplaceChild("mirr",
                CubeListBuilder.create().texOffs(BODY_DK, BODY_DK_V)
                        .addBox(-1.43f, -1.43f, -0.455f, 2.86f, 2.86f, 0.91f),
                PartPose.offsetAndRotation(-0.78f, -4.16f, 0.0f, 0f, 0f, 14f * Mth.DEG_TO_RAD));
        mirl.addOrReplaceChild("mirfl",
                CubeListBuilder.create().texOffs(GREY_LT, GREY_LT_V)
                        .addBox(-1.04f, -1.04f, -0.26f, 2.08f, 2.08f, 0.39f),
                PartPose.offset(0.0f, 0.0f, -0.65f));
        mirr.addOrReplaceChild("mirfr",
                CubeListBuilder.create().texOffs(GREY_LT, GREY_LT_V)
                        .addBox(-1.04f, -1.04f, -0.26f, 2.08f, 2.08f, 0.39f),
                PartPose.offset(0.0f, 0.0f, -0.65f));
        steer.addOrReplaceChild("forkl",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(-0.65f, 0.0f, -0.65f, 1.3f, 17.42f, 1.3f),
                PartPose.offsetAndRotation(2.99f, 0.0f, 0.0f, -23f * Mth.DEG_TO_RAD, 0f, 0f));
        steer.addOrReplaceChild("forkr",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(-0.65f, 0.0f, -0.65f, 1.3f, 17.42f, 1.3f),
                PartPose.offsetAndRotation(-2.99f, 0.0f, 0.0f, -23f * Mth.DEG_TO_RAD, 0f, 0f));
        steer.addOrReplaceChild("fend0",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-2.86f, -0.78f, -1.95f, 5.72f, 1.56f, 3.9f),
                PartPose.offsetAndRotation(0.0f, 10.79f, -5.46f, -10f * Mth.DEG_TO_RAD, 0f, 0f));
        steer.addOrReplaceChild("fend1",
                CubeListBuilder.create().texOffs(RED, RED_V)
                        .addBox(-2.86f, -0.78f, -1.95f, 5.72f, 1.56f, 3.9f),
                PartPose.offsetAndRotation(0.0f, 10.4f, -8.97f, 6f * Mth.DEG_TO_RAD, 0f, 0f));
        steer.addOrReplaceChild("fend2",
                CubeListBuilder.create().texOffs(RED_DK, RED_DK_V)
                        .addBox(-2.73f, -0.78f, -1.82f, 5.46f, 1.56f, 3.64f),
                PartPose.offsetAndRotation(0.0f, 11.44f, -10.14f, 18f * Mth.DEG_TO_RAD, 0f, 0f));
        PartDefinition wheelFront = steer.addOrReplaceChild("wheel_front",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.69f, -4.42f, -4.42f, 3.38f, 8.84f, 8.84f),
                PartPose.offset(0.0f, 15.99f, -6.89f));
        wheelFront.addOrReplaceChild("rimf",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-1.95f, -3.25f, -3.25f, 3.9f, 6.5f, 6.5f),
                PartPose.ZERO);
        wheelFront.addOrReplaceChild("rimfd",
                CubeListBuilder.create().texOffs(RIM_DK, RIM_DK_V)
                        .addBox(-2.08f, -2.47f, -2.47f, 4.16f, 4.94f, 4.94f),
                PartPose.ZERO);
        wheelFront.addOrReplaceChild("hubf",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(-2.21f, -1.17f, -1.17f, 4.42f, 2.34f, 2.34f),
                PartPose.ZERO);
        wheelFront.addOrReplaceChild("discf",
                CubeListBuilder.create().texOffs(SILVER, SILVER_V)
                        .addBox(1.69f, -2.47f, -2.47f, 0.65f, 4.94f, 4.94f),
                PartPose.ZERO);
        wheelFront.addOrReplaceChild("discfc",
                CubeListBuilder.create().texOffs(CHROME, CHROME_V)
                        .addBox(1.82f, -1.17f, -1.17f, 0.65f, 2.34f, 2.34f),
                PartPose.ZERO);
        PartDefinition wheelRear = root.addOrReplaceChild("wheel_rear",
                CubeListBuilder.create().texOffs(TIRE, TIRE_V)
                        .addBox(-1.95f, -4.42f, -4.42f, 3.9f, 8.84f, 8.84f),
                PartPose.offset(0.0f, 19.58f, 11.18f));
        wheelRear.addOrReplaceChild("rimr",
                CubeListBuilder.create().texOffs(RIM, RIM_V)
                        .addBox(-2.21f, -3.25f, -3.25f, 4.42f, 6.5f, 6.5f),
                PartPose.ZERO);
        wheelRear.addOrReplaceChild("rimrd",
                CubeListBuilder.create().texOffs(RIM_DK, RIM_DK_V)
                        .addBox(-2.34f, -2.47f, -2.47f, 4.68f, 4.94f, 4.94f),
                PartPose.ZERO);
        wheelRear.addOrReplaceChild("hubr",
                CubeListBuilder.create().texOffs(BODY_LT, BODY_LT_V)
                        .addBox(-2.47f, -1.17f, -1.17f, 4.94f, 2.34f, 2.34f),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(VehicleRenderState state) {
        super.setupAnim(state);
        this.steer.yRot = Mth.clamp(state.steer, -45f, 45f) * Mth.DEG_TO_RAD;
        this.wheelFront.xRot = state.wheelSpin;
        this.wheelRear.xRot = state.wheelSpin;
    }
}
