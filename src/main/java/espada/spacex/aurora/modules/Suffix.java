
package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class Suffix
    extends Modules {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;

    public Suffix() {
        super(Aurora.AURORA, "Suffix", "Suffix.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("Mode")).description("4")).defaultValue((Object)Mode.Aurora)).build());
    }

    private String getSuffix() {
        return switch ((Mode)((Object)this.mode.get())) {
            default -> throw new IncompatibleClassChangeError();
            case Aurora -> " A̷u̷r̷o̷r̷a̷";
            case Hyperion -> " ʜʏᴘᴇʀɪᴏɴ";
            case kamiblue -> " 上にカミブルー ";
            case Espada -> " ʍօօռ օf Sաօʀɖ†";
            case Jack -> " \uD835\uDE4F\uD835\uDE5D\uD835\uDE5A \uD835\uDE45\uD835\uDE56\uD835\uDE58\uD835\uDE60";
            case earthhack -> " 3ᵃʳᵗʰʰ4ᶜᵏ";
            case Zori -> " ᶻᵒʳⁱᴴᵃᶜᵏ";case Trollhack -> " ＴＲＯＬＬ ＨＡＣＫ";
            case Rebirth -> " ✷ℜ\uD835\uDD22\uD835\uDD1F\uD835\uDD26\uD835\uDD2F\uD835\uDD31\uD835\uDD25";
            case mio -> " \uD835\uDDE0\uD835\uDDF6\uD835\uDDFC";
            case shit -> " \uD835\uDD10\uD835\uDD22\uD835\uDD29\uD835\uDD2C\uD835\uDD2B\uD835\uDD05\uD835\uDD22\uD835\uDD31\uD835\uDD1E";



        };
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        Object message = event.message;
        if (((String)message).startsWith(".") || ((String)message).startsWith("/") || ((String)message).startsWith("+")) {
            return;
        }
        event.message = (String) (message = (String)message + this.getSuffix());
    }

    public static enum Mode {
        Aurora,
        Hyperion,
        kamiblue,
        Espada,
        Jack,
        earthhack,
        Zori,
        Trollhack,
        Rebirth,
        mio,
        shit

    }
}
