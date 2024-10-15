package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import espada.spacex.aurora.modules.autocrystal.AutoCrystal;
import espada.spacex.aurora.modules.autocrystal.AutoCrystalType;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.awt.*;

public class Render {
    public static Setting<Boolean> placeSwing(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Place-Swing")
            .description("Renders swing animation when placing a crystal.")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Boolean> attackSwing(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Attack-Swing")
            .description("Renders swing animation when placing a crystal.")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<SettingColor> lineColor(SettingGroup group) {
        return group.add(new ColorSetting.Builder()
            .name("Line Color")
            .description("Line color of rendered boxes")
            .defaultValue(new SettingColor(255, 0, 0, 255))
            .build()
        );
    }
    public static Setting<SettingColor> color(SettingGroup group) {
        return group.add(new ColorSetting.Builder()
            .name("Side Color")
            .description("Side color of rendered boxes")
            .defaultValue(new SettingColor(255, 0, 0, 50))
            .build()
        );
    }
    public static Setting<Boolean> Render(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Rendere")
            .description("Render RenderRenderRenderRenderRender")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Boolean> renderTargetEsp(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Render Target")
            .description("Render on target.")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<SettingColor> color2(SettingGroup group) {
        return group.add(new ColorSetting.Builder()
            .name("RenderTargetEsp Color")
            .description("Color")
            .defaultValue(new SettingColor(149, 149, 149, 170))
            .build()
        );
    }
    public static Setting<Boolean> renderDmg(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Render Text Damage")
            .description("2D rendering of player and enemy damage.")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Double> scale(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("Scale")
            .defaultValue(1.0)
            .sliderRange(0.1, 2.0)
            .build()
        );
    }
    public static Setting<Integer> decimal(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("Decimal")
            .defaultValue(1)
            .min(1)
            .sliderRange(1, 10)
            .build()
        );
    }
    public static Setting<SettingColor> damageColor(SettingGroup group) {
        return group.add(new ColorSetting.Builder()
            .name("damageColor")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
        );
    }
}
