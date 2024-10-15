package espada.spacex.aurora.modules.maojunqing;

public class MaoJunQingType {
    public enum LogicMode {
        PlaceBreak,
        BreakPlace
    }

    public enum SwitchMode {
        Silent,
        Normal,
        PickSilent,
        InvSwitch,
        Disabled
    }

    public enum AnchorState {
        Air,
        Anchor,
        Loaded
    }
    public enum FadeMode {
        Up,
        Down,
        Normal,
        Test,
        Test2
    }

}
