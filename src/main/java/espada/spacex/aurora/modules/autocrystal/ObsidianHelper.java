package espada.spacex.aurora.modules.autocrystal;

public class ObsidianHelper {

  // protected final ObsidianHelper
    protected final AutoCrystal obi;

    public ObsidianHelper(AutoCrystal obsidian) {
        this.obi = obsidian;
    }

    public enum Mode {
        fast,
        smart,
        none
    }
}
