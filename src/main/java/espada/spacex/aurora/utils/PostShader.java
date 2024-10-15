package espada.spacex.aurora.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.mixins.IPostEffectProcessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.Uniform;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;

public class PostShader {
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected PostEffectProcessor shader;
    public Consumer<PostShader> initCallback;
    private final Identifier location;


    public PostShader(Identifier id, Consumer<PostShader> initCallback) {
        this.initCallback = initCallback;
        location = id;
        initShader();
    }


    public ShaderUniform set(String name) {
        return findUniform(name);
    }

    public void render(float tickDelta) {
        PostEffectProcessor sg = this.getShader();
        if (sg != null) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            sg.render(tickDelta);
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.disableBlend();
            RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // restore blending
            RenderSystem.enableDepthTest();
        }
    }


    protected ShaderUniform findUniform(String name) {
        if (shader == null) {
            initShader();
        }
        final List<Uniform> uniforms = new ArrayList<>();
        for (PostEffectPass pass : ((IPostEffectProcessor) shader).getPasses()) {
            JsonEffectShaderProgram program = pass.getProgram();
            uniforms.add(program.getUniformByNameOrDummy(name));
        }
        return new ShaderUniform(uniforms);
    }

    public PostEffectProcessor getShader() {
        if (shader == null) {
            initShader();
        }

        return shader;
    }

    protected PostEffectProcessor parseShader(MinecraftClient mc, Identifier location) throws IOException {
        return new PostEffectProcessor(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), location);
    }

    private void initShader() {
        try {
            this.shader = parseShader(mc, location);
            shader.setupDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());

            if (initCallback != null)
                initCallback.accept(this);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialized post shader program", e);
        }
    }

    public void set(String name, int value) {
        this.set(name).set(value);
    }

    public void set(String name, float value) {
        this.set(name).set(value);
    }

    public void set(String name, float v0, float v1) {
        this.set(name).set(v0, v1);
    }

    public void set(String name, float v0, float v1, float v2, float v3) {
        this.set(name).set(v0, v1, v2, v3);
    }

    public void set(String name, float v0, float v1, float v2) {
        this.set(name).set(v0, v1, v2);
    }
}
