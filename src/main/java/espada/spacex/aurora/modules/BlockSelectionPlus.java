/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.RenderUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class BlockSelectionPlus extends Modules {

    long time = 0;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private long lastMillis = System.currentTimeMillis();
    BlockPos bp2 = null;
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered boxes")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Double> animationMoveExponent = sgGeneral.add(new DoubleSetting.Builder()
        .name("Animation Move Exponent")
        .description("Moves faster when longer away from the target.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> animationSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Animation Move Speed")
        .description("How fast should aurora mode box move.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<FadeMode> fade = sgGeneral.add(new EnumSetting.Builder<FadeMode>()
        .name("FadeMode")
        .description("FadeMode")
        .defaultValue(FadeMode.Normal)
        .build()
    );
    private final Setting<Double> renderTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    public BlockSelectionPlus() {
        super(Aurora.AURORA, "block-selection-plus", "Modifies how your block selection is rendered.");
    }

    private Vec3d renderTarget = null;
    private double renderProgress = 0;
    private Vec3d renderPos = null;

    @Override
    public void onActivate() {
        super.onActivate();
        renderPos = null;
        renderProgress = 0;
        lastMillis = System.currentTimeMillis();
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.crosshairTarget == null || !(mc.crosshairTarget instanceof BlockHitResult result)) return;
        double progress = 0;
        if (result.isInsideBlock()){
            return;
        }
        double delta = (System.currentTimeMillis() - lastMillis) / 1000f;
        lastMillis = System.currentTimeMillis();
        BlockPos bp = result.getBlockPos();
        BlockState state = mc.world.getBlockState(bp);
        VoxelShape shape = state.getOutlineShape(mc.world, bp);

        if (shape.isEmpty()) {
            if(fade.get() == FadeMode.Normal){
                bp2 = bp;
            }
            if(System.currentTimeMillis() - time > 1000){
                return;
            }
            if(time != 0){
                progress = 1 - Math.min(System.currentTimeMillis() -  time + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);
            }
        }
        else {
            bp2 = bp;
            time = System.currentTimeMillis();
        }
        renderProgress = Math.min(1, renderProgress + delta);
        renderTarget = new Vec3d(bp2.getX(), bp2.getY(), bp2.getZ()).add(0, 1, 0);
        renderPos = smoothMove(renderPos, renderTarget, delta * animationSpeed.get() * 5);
        Box box = new Box(renderPos.getX(), renderPos.getY() -1, renderPos.getZ(),
            renderPos.getX() + 1, renderPos.getY(), renderPos.getZ() + 1);
        event.renderer.box(box, shape.isEmpty() ?  RenderUtils.injectAlpha(color.get(), (int) Math.round(color.get().a * progress)) : color.get() , shape.isEmpty() ? RenderUtils.injectAlpha(lineColor.get(), (int) Math.round(lineColor.get().a * progress)) : lineColor.get(), shapeMode.get(), 0);
    }

    private Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
        if (current == null) return target;

        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);

        double x = (absX + Math.pow(absX, animationMoveExponent.get() - 1)) * delta;
        double y = (absX + Math.pow(absY, animationMoveExponent.get() - 1)) * delta;
        double z = (absX + Math.pow(absZ, animationMoveExponent.get() - 1)) * delta;

        return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x),
            current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y),
            current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
    }

    public enum FadeMode{
        Normal,
        Disabled
    }

}
