package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.*;

public class IDPreidct {
    public static Setting<Boolean> idPredict(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("ID Predict")
            .description("Hits the crystal before it spawns.")
            .defaultValue(false)
            .build()
        );
    }
    public static Setting<Integer> idStartOffset(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("Id Start Offset")
            .description("How many id's ahead should we attack.")
            .defaultValue(1)
            .min(0)
            .sliderMax(10)
            .build()
        );
    }
    public static Setting<Integer> idOffset(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("Id Packet Offset")
            .description("How many id's ahead should we attack between id packets.")
            .defaultValue(1)
            .min(1)
            .sliderMax(10)
            .build()
        );
    }
    public static Setting<Integer> idPackets(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("Id Packets")
            .description("How many packets to send.")
            .defaultValue(1)
            .min(1)
            .sliderMax(10)
            .build()
        );
    }
    public static Setting<Double> idDelay(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("ID Start Delay")
            .description("Starts sending id predict packets after this many seconds.")
            .defaultValue(0.05)
            .min(0)
            .sliderRange(0, 1)
            .build()
        );
    }
    public static Setting<Double> idPacketDelay(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("ID Packet Delay")
            .description("Waits this many seconds between sending ID packets.")
            .defaultValue(0.05)
            .min(0)
            .sliderRange(0, 1)
            .build()
        );
    }
}
