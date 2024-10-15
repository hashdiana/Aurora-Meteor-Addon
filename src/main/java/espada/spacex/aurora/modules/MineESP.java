package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.BOBlockUtil;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public class MineESP
    extends Modules {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Double> range;
    private final Setting<Double> maxTime;
    private final Setting<Boolean> renderName;
    private final Setting<SettingColor> nameColor;
    private final Setting<Boolean> renderProgess;
    private final Setting<Double> scale;
    private final Setting<ShapeMode> shapeMode;
    public final Setting<SettingColor> lineColor;
    public final Setting<SettingColor> sideColor;
    public final Setting<SettingColor> FlineColor;
    public final Setting<SettingColor> FsideColor;
    private final List<Render> renders;
    Render render;
        //skidded2 RV377.mg680

    public MineESP() {
        super(Aurora.Extendcombat, "Mine ESP", "Renders a box at blocks being mined by other players.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("Range")).description("Only renders inside this range.")).defaultValue(10.0).min(0.0).sliderRange(0.0, 50.0).build());
        this.maxTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("Max Time")).description("Removes rendered box after this time.")).defaultValue(10.0).min(0.0).sliderRange(0.0, 50.0).build());
        this.renderName = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("Render Name")).defaultValue(false)).build());
        this.nameColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("Name Text Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255)).visible(this.renderName::get)).build());
        this.renderProgess = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("Render Progess")).defaultValue(true)).build());
        this.scale = this.sgRender.add(((DoubleSetting.Builder)new DoubleSetting.Builder().name("Scale")).defaultValue(1.0).sliderRange(0.1, 2.0).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("Shape Mode")).description("Which parts of boxes should be rendered.")).defaultValue(ShapeMode.Both)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("Line Color")).description("Color of the outline.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("Side Color")).description("Color of the sides.")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
        this.FlineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("Friend Line Color")).description("Color of the outline.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
        this.FsideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("Friend Side Color")).description("Color of the sides.")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
        this.renders = new ArrayList<Render>();
        this.render = null;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.render != null && this.contains()) {
            this.render = null;
        }
        this.renders.removeIf(r -> System.currentTimeMillis() > r.time + Math.round((Double)this.maxTime.get() * 1000.0) || this.render != null && r.id == this.render.id);
        if (this.render != null) {
            this.renders.add(this.render);
            this.render = null;
        }
        this.renders.forEach(r -> {
            PlayerEntity player;
            if (!PlayerUtils.isWithin(r.pos, (double)((Double)this.range.get()))) {
                return;
            }
            double delta = Math.min((double)(System.currentTimeMillis() - r.time) / ((Double)this.maxTime.get() * 1000.0), 1.0);
            Entity entity = this.mc.world.getEntityById(r.id);
            PlayerEntity playerEntity = player = entity == null ? null : (PlayerEntity)entity;
            if (Friends.get().isFriend(player)) {
                event.renderer.box(this.getBox(r.pos, this.getProgress(Math.min(delta * 4.0, 1.0))), this.getColor((Color)this.FsideColor.get(), 1.0 - delta), this.getColor((Color)this.FlineColor.get(), 1.0 - delta), (ShapeMode)this.shapeMode.get(), 0);
            } else {
                event.renderer.box(this.getBox(r.pos, this.getProgress(Math.min(delta * 4.0, 1.0))), this.getColor((Color)this.sideColor.get(), 1.0 - delta), this.getColor((Color)this.lineColor.get(), 1.0 - delta), (ShapeMode)this.shapeMode.get(), 0);
            }
        });
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        this.renders.forEach(info -> {
            PlayerEntity player;
            BlockPos pos = info.pos;
            Entity entity = this.mc.world.getEntityById(info.id);
            PlayerEntity playerEntity = player = entity == null ? null : (PlayerEntity)entity;
            if (player != null) {
                if (!PlayerUtils.isWithin(info.pos, (double)((Double)this.range.get()))) {
                    return;
                }
                Vec3d rPos = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.7, (double)pos.getZ() + 0.5);
                Vector3d p1 = new Vector3d(rPos.x, rPos.y - 0.3, rPos.z);
                if (!NametagUtils.to2D(p1, (Double)this.scale.get(), true)) {
                    return;
                }
                NametagUtils.begin(p1);
                TextRenderer font = TextRenderer.get();
                font.begin((Double)this.scale.get());
                String text = BOBlockUtil.isAir(pos) ? "Romper!" : "rotura..";
                String name = player.getGameProfile().getName();
                Color breaking = new Color(255, 0, 0);
                Color broke = new Color(0, 255, 0);
                if (((Boolean)this.renderProgess.get()).booleanValue()) {
                    font.render(text, -(font.getWidth(text) / 2.0), -(font.getHeight() / 2.0) + 2.0 + font.getHeight(), BOBlockUtil.isAir(pos) ? broke : breaking, false);
                }
                if (((Boolean)this.renderName.get()).booleanValue()) {
                    font.render(name, -(font.getWidth(name) / 2.0), -font.getHeight(), (Color)this.nameColor.get(), false);
                }
                font.end();
                NametagUtils.end();
            }
        });
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        Packet packet = event.packet;
        if (packet instanceof BlockBreakingProgressS2CPacket) {
            BlockBreakingProgressS2CPacket packet2 = (BlockBreakingProgressS2CPacket)((Object)packet);
            this.render = new Render(packet2.getPos(), packet2.getEntityId(), System.currentTimeMillis());
        }
    }

    private boolean contains() {
        for (Render r : this.renders) {
            if (r.id != this.render.id || !r.pos.equals(this.render.pos)) continue;
            return true;
        }
        return false;
    }

    private Color getColor(Color color, double delta) {
        return new Color(color.r, color.g, color.b, (int)Math.floor((double)color.a * delta));
    }

    private double getProgress(double delta) {
        return 1.0 - Math.pow(1.0 - delta, 5.0);
    }

    private Box getBox(BlockPos pos, double progress) {
        return new Box((double)pos.getX() + 0.5 - progress / 2.0, (double)pos.getY() + 0.5 - progress / 2.0, (double)pos.getZ() + 0.5 - progress / 2.0, (double)pos.getX() + 0.5 + progress / 2.0, (double)pos.getY() + 0.5 + progress / 2.0, (double)pos.getZ() + 0.5 + progress / 2.0);
    }

    private record Render(BlockPos pos, int id, long time) {
    }
}
