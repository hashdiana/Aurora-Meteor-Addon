package espada.spacex.aurora.events;


import net.minecraft.client.gui.DrawContext;

public class Render2DEvent {
    public DrawContext context;
    private static final Render2DEvent INSTANCE = new Render2DEvent();

    public static Render2DEvent Render2DEvent(DrawContext context) {
        INSTANCE.context = context;
        return INSTANCE;
    }
}

