package com.xinbow99.taiwan.client.entity;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * 巴嘎囧的四段動作：站、走、打招呼、抽菸。
 *
 * <p><b>這個檔案是產生出來的，不要手改。</b>在 Blockbench 裡調完
 * {@code models/bargarjung.bbmodel} 的時間軸之後跑 {@code node tools/bake-bargarjung.mjs}。
 *
 * <p>旋轉的 x、z 在烘的時候變過號了——Blockbench 的 Y 朝上，ModelPart 的 Y 朝下，
 * 兩個座標系差一個鏡射，鏡射會把繞 X 與繞 Z 的轉向翻過來。繞 Y 的不用動。
 */
public final class BargarjungAnimations {

    private BargarjungAnimations() {
    }

    /** {@code animation.idle}，4 秒、循環。 */
    public static final AnimationDefinition IDLE =
            AnimationDefinition.Builder.withLength(4.0F)
                    .looping()
                .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.12F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 3.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(1.0F, -3.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 3.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(2.0F, 0.0F, 3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.0F, 0.0F, -2.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(4.0F, KeyframeAnimations.degreeVec(2.0F, 0.0F, -2.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .build();

    /** {@code animation.walk}，1.2 秒、循環。 */
    public static final AnimationDefinition WALK =
            AnimationDefinition.Builder.withLength(1.2F)
                    .looping()
                .addAnimation("bargarjung", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.1F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, -1.1F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -1.1F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.2F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.2F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.2F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 3.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, -3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, -3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.2F, KeyframeAnimations.degreeVec(18.0F, 0.0F, -3.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.2F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.2F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .build();

    /** {@code animation.greet}，3 秒。 */
    public static final AnimationDefinition GREET =
            AnimationDefinition.Builder.withLength(3.0F)
                .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(-5.0F, 8.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.2F, KeyframeAnimations.degreeVec(-5.0F, 8.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 65.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.2F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 65.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, 18.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.3F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, -18.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.6F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, 18.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.9F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, -18.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.2F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .build();

    /** {@code animation.smoke}，8 秒、循環。 */
    public static final AnimationDefinition SMOKE =
            AnimationDefinition.Builder.withLength(8.0F)
                    .looping()
                .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.2F, KeyframeAnimations.degreeVec(-50.24F, -65.4F, -10.04F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(3.6F, KeyframeAnimations.degreeVec(-50.24F, -65.4F, -10.04F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(4.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 3.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.2F, KeyframeAnimations.degreeVec(-83.8F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(3.6F, KeyframeAnimations.degreeVec(-83.8F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(4.8F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(8.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.2F, KeyframeAnimations.degreeVec(6.0F, 8.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(3.6F, KeyframeAnimations.degreeVec(6.0F, 8.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(4.8F, KeyframeAnimations.degreeVec(-5.0F, -5.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(6.0F, KeyframeAnimations.degreeVec(-5.0F, -5.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .addAnimation("cigarette", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.2F, KeyframeAnimations.degreeVec(115.83607F, -48.3922F, 51.8806F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(3.6F, KeyframeAnimations.degreeVec(115.83607F, -48.3922F, 51.8806F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(4.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)))
                .build();
}
