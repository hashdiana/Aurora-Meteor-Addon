package espada.spacex.aurora.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import io.netty.util.Attribute;
import espada.spacex.aurora.timers.TimerList;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import meteordevelopment.meteorclient.settings.ColorSetting;

import static espada.spacex.aurora.utils.Util.mc;

public class CrystalESP extends Modules {
    public CrystalESP(){
        super(Aurora.SETTINGS,"CrystalESP","Test TorllHack Render");
    }
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<RenderMode>().name("Render Mode").description("The mode to render in.").defaultValue(RenderMode.Normal).build());

    private final Setting<Boolean> outline = sgRender.add(new BoolSetting.Builder().name("outline").description("").defaultValue(false).visible(() -> renderMode.get() == RenderMode.Normal).build());
    private final Setting<Boolean> box = sgRender.add(new BoolSetting.Builder().name("box").description("").defaultValue(false).visible(() -> renderMode.get() == RenderMode.Normal).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("Line Color").description("Line color of rendered stuff").defaultValue(new SettingColor(255, 0, 0, 255)).build());
    public final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("Side Color").description("Side color of rendered stuff").defaultValue(new SettingColor(255, 0, 0, 50)).build());

    private final Setting<Double> animationExponent = sgRender.add(new DoubleSetting.Builder().name("Animation Exponent").description("How fast should boze mode box grow.").defaultValue(3).min(0).sliderRange(0, 10).visible(() -> renderMode.get() == RenderMode.Test).build());
    private long lastTime = 0;
    private double renderProgress = 0;
    @EventHandler
    public void onRender3D(Render3DEvent event) {
        for (Entity crystal : mc.world.getEntities()) {
            if (crystal instanceof EndCrystalEntity) {
                EntityEsp(crystal,event);
                //CrystalESP(crystal);
            }
        }
    }

    private RenderMode sgGeneral;
    public void EntityEsp(Entity entity,Render3DEvent event) {
        double delta = (System.currentTimeMillis() - lastTime) / 1000f;
        lastTime = System.currentTimeMillis();
        BlockPos pos = new BlockPos(entity.getBlockX(), entity.getBlockY()-1, entity.getBlockZ());
        final BlockPos axisAlignedBB = new BlockPos(pos);
        renderProgress = Math.max(0, renderProgress - delta);
        double r = 0.5 - Math.pow(1 - renderProgress, animationExponent.get()) / 2f;

        Box box = new Box(axisAlignedBB.getX() + 0.5 - r, axisAlignedBB.getY() + -0.5 - r +1 , axisAlignedBB.getZ() + 0.5 - r,
            axisAlignedBB.getX() + 0.5 + r, axisAlignedBB.getY() + -0.5 + r + 1, axisAlignedBB.getZ() + 0.5 + r);
        if(renderMode.get() == RenderMode.Normal) {
            if (this.outline.get()) {
                event.renderer.box(axisAlignedBB, sideColor.get(), lineColor.get(), ShapeMode.Lines, 0);
            }
            if (this.box.get()) {
                event.renderer.box(axisAlignedBB, sideColor.get(), lineColor.get(), ShapeMode.Both, 0);
            }
        }else{
            if (this.outline.get()) {
                event.renderer.box(box, sideColor.get(), lineColor.get(), ShapeMode.Lines, 0);
            }
            if (this.box.get()) {
                event.renderer.box(box, sideColor.get(), lineColor.get(), ShapeMode.Both, 0);
            }
        }
    }

    public enum RenderMode {
        Normal,
        Test
    }
}
