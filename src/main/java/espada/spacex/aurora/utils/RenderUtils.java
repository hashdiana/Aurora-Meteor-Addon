package espada.spacex.aurora.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class RenderUtils {

    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
    public static TextureColorProgram TEXTURE_COLOR_PROGRAM;
    public static GradientGlowProgram GRADIENT_GLOW_PROGRAM;
    private static float prevCircleStep;
    private static float circleStep;

    public static void initShaders() {
        if (GRADIENT_GLOW_PROGRAM == null)
            GRADIENT_GLOW_PROGRAM = new GradientGlowProgram();
        if (TEXTURE_COLOR_PROGRAM == null)
            TEXTURE_COLOR_PROGRAM = new TextureColorProgram();
    }



    public static Color injectAlpha(Color color, int alpha) {
        return new Color(color.r, color.g, color.b, MathHelper.clamp(alpha, 0, 255));
    }

    public static void drawTexture(DrawContext context, Identifier icon, int x, int y, int width, int height) {
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.enableBlend();
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        context.drawTexture(icon, x, y, 0, 0, width, height, width, height);
    }

    private static final VertexConsumerProvider.Immediate vertex = VertexConsumerProvider.immediate(new BufferBuilder(2048));

    public static void rounded(MatrixStack stack, float x, float y, float w, float h, float radius, int p, int color) {

        Matrix4f matrix4f = stack.peek().getPositionMatrix();

        float a = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float r = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float b = (float) ColorHelper.Argb.getBlue(color) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        corner(x + w, y, radius, 360, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x, y, radius, 270, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x, y + h, radius, 180, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x + w, y + h, radius, 90, p, r, g, b, a, bufferBuilder, matrix4f);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void corner(float x, float y, float radius, int angle, float p, float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
        for (float i = angle; i > angle - 90; i -= 90 / p) {
            bufferBuilder.vertex(matrix4f, (float) (x + Math.cos(Math.toRadians(i)) * radius), (float) (y + Math.sin(Math.toRadians(i)) * radius), 0).color(r, g, b, a).next();
        }
    }

    public static void text(String text, MatrixStack stack, float x, float y, int color) {
        mc.textRenderer.draw(text, x, y, color, false, stack.peek().getPositionMatrix(), vertex, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        vertex.draw();
    }

    public static void quad(MatrixStack stack, float x, float y, float w, float h, int color) {
        Matrix4f matrix4f = stack.peek().getPositionMatrix();

        float a = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float r = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float b = (float) ColorHelper.Argb.getBlue(color) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x + w, y, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix4f, x, y, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix4f, x, y + h, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix4f, x + w, y + h, 0).color(r, g, b, a).next();


        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static Vec3d interpolatePos(float prevposX, float prevposY, float prevposZ, float posX, float posY, float posZ) {
        double x = prevposX + ((posX - prevposX) * mc.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = prevposY + ((posY - prevposY) * mc.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = prevposZ + ((posZ - prevposZ) * mc.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        return new Vec3d(x, y, z);
    }


    public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
        Vec3d camera = mc.entityRenderDispatcher.camera.getPos();
        int displayHeight = mc.window.getHeight();
        int viewport[] = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();
        float deltaX = (float) (pos.x - camera.x);
        float deltaY = (float) (pos.y - camera.y);
        float deltaZ = (float) (pos.z - camera.z);
        Vector4f transformedCoordinates = new Vector4f(deltaX, deltaY, deltaZ, 1f).mul(lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);
        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
        return new Vec3d(
            target.x / mc.window.getScaleFactor(),
            (displayHeight - target.y) / mc.window.getScaleFactor(),
            target.z);
    }

    public static void updateJello() {
        prevCircleStep = circleStep;
        circleStep += 0.15f;
    }


    public static void drawJello(MatrixStack matrix, Entity target, Color color) {
        double cs = prevCircleStep + (circleStep - prevCircleStep) * mc.getTickDelta();
        double prevSinAnim = absSinAnimation(cs - 0.45f);
        double sinAnim = absSinAnimation(cs);
        double x = target.prevX + (target.getX() - target.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + prevSinAnim * target.getHeight();
        double z = target.prevZ + (target.getZ() - target.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double nextY = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + sinAnim * target.getHeight();

        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        float cos;
        float sin;
        for (int i = 0; i <= 30; i++) {
            cos = (float) (x + Math.cos(i * 6.28 / 30) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            sin = (float) (z + Math.sin(i * 6.28 / 30) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) nextY, sin).color(color.getPacked()).next();
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) y, sin).color(RenderUtils.injectAlpha(color, 0).getPacked()).next();
        }

        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrix.pop();
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, int color) {
        float f = (color >> 24 & 0xFF) / 255.0F;
        float g = (color >> 16 & 0xFF) / 255.0F;
        float h = (color  >> 8 & 0xFF) / 255.0F;
        float k = (color & 0xFF) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y + height, 0.0f).color(g, h, k, f).next();
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y + height, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y, 0.0F).color(g, h, k, f).next();
        tessellator.draw();
    }

    private static double absSinAnimation(double input) {
        return Math.abs(1 + Math.sin(input)) / 2;
    }

}
