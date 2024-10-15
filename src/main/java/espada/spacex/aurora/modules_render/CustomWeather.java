package espada.spacex.aurora.modules_render;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.events.WeatherRenderEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

import static net.minecraft.client.render.WorldRenderer.getLightmapCoordinates;

public class CustomWeather extends Modules {
    private static final Identifier RAIN = new Identifier("textures/environment/rain.png");
    private static final Identifier SNOW = new Identifier("textures/environment/snow.png");

    public CustomWeather() {
        super(Aurora.AURORA, "Weather", "Custom Weather");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgValue = settings.createGroup("Value");
    private final Setting<PrecipitationType> precipitationSetting = sgGeneral.add(new EnumSetting.Builder<PrecipitationType>()
        .name("precipitation")
        .defaultValue(PrecipitationType.Snow)
        .build()
    );

    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("weather-height")
        .defaultValue(0)
        .min(0)
        .max(320)
        .build()
    );

    private final Setting<Double> strength =sgGeneral.add(new DoubleSetting.Builder()
        .name("weather-strength")
        .defaultValue(0.8)
        .range(0.1,2.0)
        .build()
    );

    private final Setting<SettingColor> weatherColor = sgGeneral.add(new ColorSetting.Builder()
        .name("weather-color")
        .defaultValue(SettingColor.WHITE)
        .build()
    );

    private final Setting<Integer> expandSize = sgValue.add(new IntSetting.Builder()
        .name("expand-size")
        .defaultValue(5)
        .range(1,10)
        .build()
    );

    private final Setting<Double> snowFallingSpeedMultiplier = sgValue.add(new DoubleSetting.Builder()
        .name("snow-falling-speed-multiplier")
        .defaultValue(1.0)
        .range(0.0,10.0)
        .build()
    );

    private int ticks = 0;
    private static final float[] weatherXCoords = new float[1024];
    private static final float[] weatherYCoords = new float[1024];

    static {
        for(int xRange = 0; xRange < 32; ++xRange) {
            for(int zRange = 0; zRange < 32; ++zRange) {
                float x = (float)(zRange - 16);
                float z = (float)(xRange - 16);
                float length = MathHelper.sqrt(x * x + z * z);
                weatherXCoords[xRange << 5 | zRange] = -z / length;
                weatherYCoords[xRange << 5 | zRange] = x / length;
            }
        }
    }

    public enum PrecipitationType {
        None,Rain,Snow,Both;

        public Biome.Precipitation toMC() {
            return switch (this) {
                case None -> Biome.Precipitation.NONE;
                case Rain -> Biome.Precipitation.RAIN;
                case Snow -> Biome.Precipitation.SNOW;
                case Both -> Biome.Precipitation.SNOW;
            };
        }
    }

    @EventHandler
    private void onWeather(WeatherRenderEvent event) {
        if (precipitationSetting.get().equals(PrecipitationType.Both)) {
            render(event,PrecipitationType.Rain);
            render(event,PrecipitationType.Snow);
            event.cancel();
            return;
        }

        render(event,precipitationSetting.get());

        event.cancel();
    }

    private void render(WeatherRenderEvent event,PrecipitationType precipitationType) {
        LightmapTextureManager manager = event.lightmapTextureManager;
        double cameraX = event.cameraX;
        double cameraY = event.cameraY;
        double cameraZ = event.cameraZ;
        float tickDelta = event.tickDelta;
        float f = strength.get().floatValue();
        float red = weatherColor.get().r / 255f;
        float blue = weatherColor.get().b / 255f;
        float green = weatherColor.get().g / 255f;

        // renderer
        manager.enable();
        int cameraIntX = MathHelper.floor(cameraX);
        int cameraIntY = MathHelper.floor(cameraY);
        int cameraIntZ = MathHelper.floor(cameraZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.setShader(GameRenderer::getParticleProgram);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int expand = expandSize.get();
        int tessPosition = -1;
        float fallingValue = (float)this.ticks + tickDelta;
        for(int zRange = cameraIntZ - expand; zRange <= cameraIntZ + expand; ++zRange) {
            for(int xRange = cameraIntX - expand; xRange <= cameraIntX + expand; ++xRange) {
                int coordPos = (zRange - cameraIntZ + 16) * 32 + xRange - cameraIntX + 16;

                if (coordPos < 0 || coordPos > 1023) continue;

                double xCoord = (double) weatherXCoords[coordPos] * 0.5;
                double zCoord = (double) weatherYCoords[coordPos] * 0.5;
                mutable.set(xRange, cameraY, zRange);

                int maxHeight = height.get();
                int minIntY = cameraIntY - expand;
                int expandedCameraY = cameraIntY + expand;
                if (minIntY < maxHeight) {
                    minIntY = maxHeight;
                }

                if (expandedCameraY < maxHeight) {
                    expandedCameraY = maxHeight;
                }

                int maxRenderY = Math.max(maxHeight, cameraIntY);

                if (minIntY != expandedCameraY) {
                    Random random = Random.create((long) xRange * xRange * 3121 + xRange * 45238971L ^ (long) zRange * zRange * 418711 + zRange * 13761L);
                    mutable.set(xRange, minIntY, zRange);
                    float texTextureV;
                    float weatherAlpha;
                    Biome.Precipitation precipitation = precipitationType.toMC();
                    if (precipitation == Biome.Precipitation.RAIN) {
                        if (tessPosition != 0) {
                            if (tessPosition >= 0) {
                                tessellator.draw();
                            }

                            tessPosition = 0;
                            RenderSystem.setShaderTexture(0, RAIN);
                            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }

                        int randomSeed = this.ticks + xRange * xRange * 3121 + xRange * 45238971 + zRange * zRange * 418711 + zRange * 13761 & 31;
                        texTextureV = -((float)randomSeed + tickDelta) / 32.0F * (3.0F + random.nextFloat());
                        double xOffset = (double)xRange + 0.5 - cameraX;
                        double yOffset = (double)zRange + 0.5 - cameraZ;
                        float dLength = (float)Math.sqrt(xOffset * xOffset + yOffset * yOffset) / (float)expand;
                        weatherAlpha = ((1.0F - dLength * dLength) * 0.5F + 0.5F) * f;
                        mutable.set(xRange, maxRenderY, zRange);
                        int lightmapCoord = getLightmapCoordinates(mc.world, mutable);

                        bufferBuilder.vertex(xRange - cameraX - xCoord + 0.5, expandedCameraY - cameraY, zRange - cameraZ - zCoord + 0.5).texture(0.0F, (float)minIntY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord).next();
                        bufferBuilder.vertex(xRange - cameraX + xCoord + 0.5, expandedCameraY - cameraY, zRange - cameraZ + zCoord + 0.5).texture(1.0F, (float)minIntY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord).next();
                        bufferBuilder.vertex(xRange - cameraX + xCoord + 0.5, minIntY - cameraY, zRange - cameraZ + zCoord + 0.5).texture(1.0F, (float)expandedCameraY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord).next();
                        bufferBuilder.vertex(xRange - cameraX - xCoord + 0.5, minIntY - cameraY, zRange - cameraZ - zCoord + 0.5).texture(0.0F, (float)expandedCameraY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord).next();
                    } else if (precipitation == Biome.Precipitation.SNOW) {
                        if (tessPosition != 1) {
                            if (tessPosition == 0) {
                                tessellator.draw();
                            }

                            tessPosition = 1;
                            RenderSystem.setShaderTexture(0, SNOW);
                            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }
                        float snowSmooth = -((float)(this.ticks & 511) + tickDelta) / 512.0F;
                        texTextureV = (float)(random.nextDouble() + (double)fallingValue * 0.01 * (double)((float)random.nextGaussian()));
                        float fallingSpeed = (float) ((float)(random.nextDouble() + (double)(fallingValue * (float)random.nextGaussian()) * 0.001) * snowFallingSpeedMultiplier.get());
                        double xOffset = (double)xRange + 0.5 - cameraX;
                        double yOffset = (double)zRange + 0.5 - cameraZ;
                        weatherAlpha = (float)Math.sqrt(xOffset * xOffset + yOffset * yOffset) / (float)expand;
                        float snowAlpha = ((1.0F - weatherAlpha * weatherAlpha) * 0.3F + 0.5F) * f;
                        mutable.set(xRange, maxRenderY, zRange);
                        int lightMapCoord = getLightmapCoordinates(mc.world, mutable);
                        int lightmapCalcV = lightMapCoord >> 16 & '\uffff';
                        int lightmapCalcU = lightMapCoord & '\uffff';
                        int lightmapV = (lightmapCalcV * 3 + 240) / 4;
                        int lightmapU = (lightmapCalcU * 3 + 240) / 4;
                        bufferBuilder.vertex(xRange - cameraX - xCoord + 0.5, expandedCameraY - cameraY, zRange - cameraZ - zCoord + 0.5).texture(0.0F + texTextureV, (float)minIntY * 0.25F + snowSmooth + fallingSpeed).color(red,green,blue, snowAlpha).light(lightmapU, lightmapV).next();
                        bufferBuilder.vertex(xRange - cameraX + xCoord + 0.5, expandedCameraY - cameraY, zRange - cameraZ + zCoord + 0.5).texture(1.0F + texTextureV, (float)minIntY * 0.25F + snowSmooth + fallingSpeed).color(red,green,blue, snowAlpha).light(lightmapU, lightmapV).next();
                        bufferBuilder.vertex(xRange - cameraX + xCoord + 0.5, minIntY - cameraY, zRange - cameraZ + zCoord + 0.5).texture(1.0F + texTextureV, (float)expandedCameraY * 0.25F + snowSmooth + fallingSpeed).color(red,green,blue, snowAlpha).light(lightmapU, lightmapV).next();
                        bufferBuilder.vertex(xRange - cameraX - xCoord + 0.5, minIntY - cameraY, zRange - cameraZ - zCoord + 0.5).texture(0.0F + texTextureV, (float)expandedCameraY * 0.25F + snowSmooth + fallingSpeed).color(red,green,blue, snowAlpha).light(lightmapU, lightmapV).next();
                    }
                }
            }
        }

        if (tessPosition >= 0) {
            tessellator.draw();
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        manager.disable();
    }

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        ++ticks;
    }
}
