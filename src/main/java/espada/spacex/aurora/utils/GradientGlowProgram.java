package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.WindowResizedEvent;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL30;

public class GradientGlowProgram extends GlProgram {

    private GlUniform uSize;
    private GlUniform uLocation;
    private GlUniform radius;
    private GlUniform softness;
    private GlUniform color1;
    private GlUniform color2;
    private GlUniform color3;
    private GlUniform color4;

    private Framebuffer input;

    public GradientGlowProgram() {
        super("gradientglow", VertexFormats.POSITION);
        MeteorClient.EVENT_BUS.subscribe(new ConsumerListener(WindowResizedEvent.class, (event) -> {
            if (this.input == null) return;
            this.input.resize(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        }));
    }


    @Override
    public void use() {
        var buffer = MinecraftClient.getInstance().getFramebuffer();
        this.input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);
        super.use();
    }

    @Override
    protected void setup() {
        this.uSize = this.findUniform("uSize");
        this.uLocation = this.findUniform("uLocation");
        this.softness = this.findUniform("softness");
        this.radius = this.findUniform("radius");
        this.color1 = this.findUniform("color1");
        this.color2 = this.findUniform("color2");
        this.color3 = this.findUniform("color3");
        this.color4 = this.findUniform("color4");
        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }

}
