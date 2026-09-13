package com.xinbow99.taiwan.client.entity;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 巴嘎囧的零件樹。
 *
 * <p><b>這個檔案是產生出來的，不要手改。</b>改 {@code models/bargarjung.bbmodel}
 * 之後跑 {@code node tools/bake-bargarjung.mjs} 重出一份。
 */
public final class BargarjungGeometry {

    private BargarjungGeometry() {
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bargarjung = root.addOrReplaceChild("bargarjung",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = bargarjung.addOrReplaceChild("body",
                CubeListBuilder.create()
        .texOffs(2, 2).addBox(-5.0F, -1.0F, -2.0F, 10.0F, 11.0F, 5.0F)
        .texOffs(34, 2).addBox(-5.0F, 9.0F, -2.0F, 10.0F, 1.0F, 5.0F, new CubeDeformation(0.08F))
        .texOffs(30, 58).addBox(-2.42F, -0.92F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(36, 58).addBox(-2.22F, -0.32F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(42, 58).addBox(-1.82F, 0.18F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(48, 58).addBox(-1.32F, 0.58F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(54, 58).addBox(-0.77F, 0.83F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(60, 58).addBox(-0.22F, 0.93F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(66, 58).addBox(0.33F, 0.83F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(72, 58).addBox(0.88F, 0.58F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(78, 58).addBox(1.38F, 0.18F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(84, 58).addBox(1.78F, -0.32F, -2.42F, 0.44F, 0.44F, 0.34F)
        .texOffs(90, 58).addBox(1.98F, -0.92F, -2.42F, 0.44F, 0.44F, 0.34F),
                PartPose.offset(0.0F, -24.0F, 0.0F));

        PartDefinition head = bargarjung.addOrReplaceChild("head",
                CubeListBuilder.create()
        .texOffs(66, 2).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 3.0F, 3.0F)
        .texOffs(82, 2).addBox(-4.0F, -8.0F, -3.0F, 8.0F, 7.0F, 6.0F)
        .texOffs(112, 2).addBox(-5.0F, -4.0F, -1.0F, 1.0F, 2.0F, 2.0F)
        .texOffs(120, 2).addBox(4.0F, -4.0F, -1.0F, 1.0F, 2.0F, 2.0F)
        .texOffs(128, 2).addBox(-0.5F, -4.0F, -3.55F, 1.0F, 1.0F, 0.55F)
        .texOffs(134, 2).addBox(-4.0F, -8.0F, -2.0F, 8.0F, 3.0F, 5.0F, new CubeDeformation(0.04F))
        .texOffs(162, 2).addBox(-3.0F, -9.0F, -2.0F, 6.0F, 2.0F, 5.0F)
        .texOffs(186, 2).addBox(-4.0F, -8.0F, -3.3F, 1.0F, 2.0F, 1.0F)
        .texOffs(192, 2).addBox(-3.0F, -9.0F, -3.3F, 1.0F, 4.0F, 1.0F)
        .texOffs(198, 2).addBox(-2.0F, -8.0F, -3.3F, 1.0F, 2.0F, 1.0F)
        .texOffs(204, 2).addBox(-1.0F, -8.0F, -3.3F, 1.0F, 3.0F, 1.0F)
        .texOffs(210, 2).addBox(0.0F, -9.0F, -3.3F, 1.0F, 3.0F, 1.0F)
        .texOffs(216, 2).addBox(1.0F, -8.0F, -3.3F, 1.0F, 2.0F, 1.0F)
        .texOffs(222, 2).addBox(2.0F, -8.0F, -3.3F, 1.0F, 3.0F, 1.0F)
        .texOffs(228, 2).addBox(3.0F, -9.0F, -3.3F, 1.0F, 3.0F, 1.0F)
        .texOffs(234, 2).addBox(-4.0F, -10.0F, -2.0F, 2.0F, 3.0F, 2.0F)
        .texOffs(244, 2).addBox(-2.0F, -9.0F, -2.0F, 2.0F, 2.0F, 2.0F)
        .texOffs(2, 20).addBox(0.0F, -9.0F, -2.0F, 2.0F, 2.0F, 2.0F)
        .texOffs(12, 20).addBox(2.0F, -10.0F, -2.0F, 2.0F, 3.0F, 2.0F)
        .texOffs(22, 20).addBox(-3.75F, -9.0F, 0.0F, 2.0F, 2.0F, 2.0F)
        .texOffs(32, 20).addBox(-1.75F, -9.0F, 0.0F, 2.0F, 2.0F, 2.0F)
        .texOffs(42, 20).addBox(0.25F, -10.0F, 0.0F, 2.0F, 3.0F, 2.0F)
        .texOffs(52, 20).addBox(2.25F, -9.0F, 0.0F, 2.0F, 2.0F, 2.0F)
        .texOffs(62, 20).addBox(-4.0F, -9.0F, 2.0F, 2.0F, 2.0F, 2.0F)
        .texOffs(72, 20).addBox(-2.0F, -10.0F, 2.0F, 2.0F, 3.0F, 2.0F)
        .texOffs(82, 20).addBox(0.0F, -9.0F, 2.0F, 2.0F, 2.0F, 2.0F)
        .texOffs(92, 20).addBox(2.0F, -9.0F, 2.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -25.0F, 0.0F));

        PartDefinition right_arm = bargarjung.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
        .texOffs(102, 20).addBox(-4.0F, -1.0F, -2.0F, 4.0F, 6.0F, 5.0F)
        .texOffs(122, 20).addBox(-4.0F, 4.0F, -2.0F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.07F)),
                PartPose.offset(-5.0F, -24.0F, 0.0F));

        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm",
                CubeListBuilder.create()
        .texOffs(142, 20).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F)
        .texOffs(158, 20).addBox(-1.5F, 5.0F, -2.0F, 3.0F, 2.0F, 4.0F)
        .texOffs(174, 20).addBox(1.1F, 4.7F, -2.3F, 1.0F, 1.8F, 1.5F),
                PartPose.offset(-2.0F, 5.0F, 0.5F));

        PartDefinition cigarette = right_forearm.addOrReplaceChild("cigarette",
                CubeListBuilder.create()
        .texOffs(200, 200).addBox(-0.5F, -0.5F, -3.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.32F))
        .texOffs(215, 200).addBox(-0.5F, -0.5F, -0.2F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.32F))
        .texOffs(225, 200).addBox(-0.5F, -0.5F, -3.85F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.32F)),
                PartPose.offset(0.0F, 5.5F, -2.0F));

        PartDefinition left_arm = bargarjung.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
        .texOffs(74, 39).addBox(0.0F, -1.0F, -2.0F, 4.0F, 6.0F, 5.0F)
        .texOffs(94, 39).addBox(0.0F, 4.0F, -2.0F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.07F)),
                PartPose.offset(5.0F, -24.0F, 0.0F));

        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm",
                CubeListBuilder.create()
        .texOffs(114, 39).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F)
        .texOffs(130, 39).addBox(-1.5F, 5.0F, -2.0F, 3.0F, 2.0F, 4.0F)
        .texOffs(146, 39).addBox(-2.1F, 4.7F, -2.3F, 1.0F, 1.8F, 1.5F),
                PartPose.offset(2.0F, 5.0F, 0.5F));

        PartDefinition right_leg = bargarjung.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
        .texOffs(180, 20).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 12.0F, 5.0F)
        .texOffs(202, 20).addBox(-2.5F, 11.0F, -2.0F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.1F))
        .texOffs(224, 20).addBox(-2.3F, 11.3F, -2.15F, 4.6F, 0.7F, 0.25F)
        .texOffs(2, 39).addBox(-2.5F, 13.0F, -4.0F, 5.0F, 1.0F, 7.0F)
        .texOffs(28, 39).addBox(-2.15F, 12.0F, -3.7F, 4.3F, 1.0F, 4.7F)
        .texOffs(46, 39).addBox(-2.4F, 11.35F, -2.3F, 4.8F, 0.85F, 2.5F)
        .texOffs(60, 39).addBox(-2.5F, 11.5F, -2.6F, 5.0F, 0.8F, 0.5F),
                PartPose.offset(-2.5F, -14.0F, 0.0F));

        PartDefinition left_leg = bargarjung.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
        .texOffs(152, 39).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 12.0F, 5.0F)
        .texOffs(174, 39).addBox(-2.5F, 11.0F, -2.0F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.1F))
        .texOffs(196, 39).addBox(-2.3F, 11.3F, -2.15F, 4.6F, 0.7F, 0.25F)
        .texOffs(208, 39).addBox(-2.5F, 13.0F, -4.0F, 5.0F, 1.0F, 7.0F)
        .texOffs(234, 39).addBox(-2.15F, 12.0F, -3.7F, 4.3F, 1.0F, 4.7F)
        .texOffs(2, 58).addBox(-2.4F, 11.35F, -2.3F, 4.8F, 0.85F, 2.5F)
        .texOffs(16, 58).addBox(-2.5F, 11.5F, -2.6F, 5.0F, 0.8F, 0.5F),
                PartPose.offset(2.5F, -14.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }
}
