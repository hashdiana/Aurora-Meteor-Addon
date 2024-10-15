package espada.spacex.aurora.globalsettings;


import espada.spacex.aurora.Aurora;
import meteordevelopment.meteorclient.events.entity.player.PlaceBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.meteorclient.settings.ColorSetting;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class PlaceRender extends Module {
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<render_type> type = sgRender.add(new EnumSetting.Builder<render_type>()
        .name("Render Type")
        .description("Render type.")
        .defaultValue(render_type.Fade)
        .build()
    );
    public enum render_type {
        Fade,
        Smooth
    }
    private final Setting<Integer> renderTime = sgRender.add(new IntSetting.Builder()
        .name("render-time")
        .description("How long to render placements.")
        .defaultValue(10)
        .min(0)
        .sliderMax(20)
        .visible(() -> type.get() == render_type.Smooth)
        .build()
    );
    private final Setting<Integer> smoothness = sgRender.add(new IntSetting.Builder()
        .name("Smoothness")
        .description("How smoothly the render should move around.")
        .defaultValue(10)
        .min(0)
        .sliderMax(20)
        .visible(() -> type.get() == render_type.Smooth)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(Aurora.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(Aurora.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    private final Setting<Integer> fade = sgRender.add(new IntSetting.Builder()
        .name("Fade time")
        .description("time")
        .defaultValue(1000).max(5000).min(500)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private Box renderBoxOne, renderBoxTwo;


    public static final List<Render> render = new ArrayList<>();
    //private final List<BlockPos> blocks = new ArrayList<>();

    public PlaceRender() {
        super(Aurora.SETTINGS, "PlaceRender", "Render Places");
    }
    @EventHandler
    public void onEvent(PlaceBlockEvent event){
        render.add(new Render(event.blockPos, System.currentTimeMillis()));

    }

    @EventHandler
    public void onRender(Render3DEvent event){

        if(type.get()== render_type.Fade){
            //surroundBlocks.stream().filter(isolationUtils::replaceable).forEach(block -> event.renderer.box(block, sideColor.get(), lineColor.get(), shapeMode.get(), 0));
            //blocks.forEach(block -> event.renderer.box(block, supportSideColor.get(), supportLineColor.get(), supportShapeMode.get(), 0));
            render.removeIf(r -> System.currentTimeMillis() - r.time > fade.get());

            render.forEach(r -> {
                double progress = 1 - Math.min(System.currentTimeMillis() - r.time, 500) / 500d;

                event.renderer.box(r.pos, new Color(sideColor.get().r, sideColor.get().g, sideColor.get().b, (int) Math.round(sideColor.get().a * progress)), new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
            });
        }
        if(type.get()==render_type.Smooth){
            render.removeIf(r -> System.currentTimeMillis() - r.time > fade.get());
            if(render.size()==0){
                return;
            }
            for(Render renderp:render){
                BlockPos renderPos=renderp.pos;
                if (renderTime.get() <= 0) return;

                if (renderBoxOne == null) renderBoxOne = new Box(renderPos);
                if (renderBoxTwo == null) {
                    renderBoxTwo = new Box(renderPos);
                } else {
                    ((IBox) renderBoxTwo).set(renderPos);
                }

                double offsetX = (renderBoxTwo.minX - renderBoxOne.minX) / smoothness.get();
                double offsetY = (renderBoxTwo.minY - renderBoxOne.minY) / smoothness.get();
                double offsetZ = (renderBoxTwo.minZ - renderBoxOne.minZ) / smoothness.get();

                ((IBox) renderBoxOne).set(
                    renderBoxOne.minX + offsetX,
                    renderBoxOne.minY + offsetY,
                    renderBoxOne.minZ + offsetZ,
                    renderBoxOne.maxX + offsetX,
                    renderBoxOne.maxY + offsetY,
                    renderBoxOne.maxZ + offsetZ
                );
                event.renderer.box(renderBoxOne, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }
        }


    }
    public record Render(BlockPos pos, long time) {}
}

