package espada.spacex.aurora.modules.autocrystal;

public class AutoCrystalType {

    public enum SwitchMode {
        Disabled,
        Simple,
        Gapple,
        Silent,
        InvSilent,
        PickSilent
    }
    public enum calcMode {
        HyperCard,
        earthhack,
        Normal
    }
    public enum ExplodeMode {
        Crystal,
        Calc,
        Always
    }
    public enum SequentialMode {
        Disabled(0),
        Strict(2),
        Strong(1);

        public final int ticks;

        SequentialMode(int ticks) {
            this.ticks = ticks;
        }
    }


    public enum DelayMode {
        Seconds,
        Ticks
    }
    public enum RenderMode {
        MotionOut,
        Smooth,
        Future,
        Earthhack,
        Romb
    }

    public enum EarthFadeMode {
        Normal,
        Up,
        Down,
        Shrink
    }
    public enum MotionOutMode{
        blockbox,
        None
    }

    public enum FadeMode {
        Up,
        Down,
        Normal,
    }
    public enum AutoMineBrokenMode {
        Near(true, false, false),
        Broken(true, true, false),
        Never(false, false, false),
        Always(true, true, true);

        public final boolean normal;
        public final boolean near;
        public final boolean broken;

        AutoMineBrokenMode(boolean normal, boolean near, boolean broken) {
            this.normal = normal;
            this.near = near;
            this.broken = broken;
        }
    }
}
