package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.events.Render2DEvent;
import espada.spacex.aurora.utils.RenderUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


public class NewNameTags extends Modules {

    private final List<Entity> entityList = new ArrayList<>();

    private final Vector3d pos = new Vector3d();

    public NewNameTags() {
        super(Aurora.AURORA, "NewNameTags" , "1");
    }


    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select entities to draw nametags on.")
        .defaultValue(EntityType.PLAYER, EntityType.ITEM)
        .build()
    );
    private final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-self")
        .description("Ignore yourself when in third person or freecam.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Ignore rendering nametags for friends.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreBots = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-bots")
        .description("Only render non-bot nametags.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> culling = sgGeneral.add(new BoolSetting.Builder()
        .name("culling")
        .description("Only render a certain number of nametags at a certain distance.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> maxCullRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("culling-range")
        .description("Only render nametags within this distance of your player.")
        .defaultValue(20)
        .min(0)
        .sliderMax(200)
        .visible(culling::get)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        entityList.clear();

        boolean freecamNotActive = !meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(Freecam.class);
        boolean notThirdPerson = mc.options.getPerspective().isFirstPerson();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        for (Entity entity : mc.world.getEntities()) {
            EntityType<?> type = entity.getType();
            if (!entities.get().contains(type)) continue;

            if (type == EntityType.PLAYER) {
                if ((ignoreSelf.get() || (freecamNotActive && notThirdPerson)) && entity == mc.player) continue;
                if (EntityUtils.getGameMode((PlayerEntity) entity) == null && ignoreBots.get()) continue;
                if (Friends.get().isFriend((PlayerEntity) entity) && ignoreFriends.get()) continue;
            }

            if (!culling.get() || PlayerUtils.isWithinCamera(entity, maxCullRange.get())) {
                entityList.add(entity);
            }
        }

        entityList.sort(Comparator.comparing(e -> e.squaredDistanceTo(cameraPos)));
    }


    @EventHandler
    private void Render2d(Render2DEvent event){
        int count = getRenderCount();

        for (int i = count - 1; i > -1; i--) {
            Entity entity = entityList.get(i);
            pos.add(0, getHeight(entity), 0);

            EntityType<?> type = entity.getType();
            if (type == EntityType.PLAYER) drawText(event.context, "text", entity.getPos().add(new Vec3d(0.0, entity.getHeight() + 0.5, 0.0)));
        }

    }

    public boolean excludeBots() {
        return ignoreBots.get();
    }

    public boolean playerNametags() {
        return isActive() && entities.get().contains(EntityType.PLAYER);
    }

    private double getHeight(Entity entity) {
        double height = entity.getEyeHeight(entity.getPose());

        if (entity.getType() == EntityType.ITEM || entity.getType() == EntityType.ITEM_FRAME) height += 0.2;
        else height += 0.5;

        return height;
    }

    private int getRenderCount() {
        int count = entityList.size();
        count = MathHelper.clamp(count, 0, entityList.size());

        return count;
    }

    private  void  drawText(DrawContext context, String  text, Vec3d vec)  {
        Vec3d  vector  =  RenderUtils.worldSpaceToScreenSpace(new  Vec3d(vec.x,  vec.y,  vec.z));

        if  (vector.z  >  0  &&  vector.z  <  1)  {
            float  posX  = (float) vector.x;
            float  posY  = (float) vector.y;
            float  endPosX  = (float) Math.max(vector.x,  vector.z);

            float  scale  =  1.0f;
            float  diff  =  ((endPosX  -  posX)  /  2);
            float  textWidth  =  mc.textRenderer.getWidth(text)  *  scale;
            float  tagX  =  (posX  +  diff  -  textWidth  /  2);

            context.matrices.push();
            context.matrices.scale(scale,  scale,  scale);

            float  y  = (float) ((posY  -  11  +  mc.textRenderer.fontHeight  *  1.2)  /  scale);
            if (true) {
                RenderUtils.drawRect(context.matrices, ((tagX / scale) - 2),
                    y - 3, mc.textRenderer.getWidth(text) + 4.0f,
                    mc.textRenderer.fontHeight + 6.0f,new Color(0, 0, 0, 140).getPacked());
            }

            context.drawText(mc.textRenderer,  text,  (int)  (tagX / scale),  (int)  y,
                    Color.WHITE.getPacked(),  true);

            context.matrices.pop();
        }
    }

}
