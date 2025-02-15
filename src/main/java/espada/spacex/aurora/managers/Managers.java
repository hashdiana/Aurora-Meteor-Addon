package espada.spacex.aurora.managers;

/**
 * @author OLEPOSSU
 */

public class Managers {

    public static final HoldingManager HOLDING = new HoldingManager();
    public static final OnGroundManager ON_GROUND = new OnGroundManager();
    public static final RotationManager ROTATION = new RotationManager();
    public static final BreakManager BREAK = new BreakManager();
    public static final RenderManager RENDER = new RenderManager();
    public static final PlayerManager PLAYER = new PlayerManager();

    public Managers() {
    }

}
