package espada.spacex.aurora.modules.maojunqing;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.automine.AuroraMine;
import espada.spacex.aurora.utils.*;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.*;

/**
 * @author OLEPOSSU
 */

public class MaoJunQingAura extends Modules {
    public MaoJunQingAura() {
        super(Aurora.Extendcombat, "MaoJunQing Aura", "Automatically destroys people using anchors.");
    }

    private double renderProgress = 0;
    private long lastMillis = System.currentTimeMillis();
    double dmg;
    double self;
    Vector3d vec;

    private final List<Render> renderBlocks = new ArrayList<>();
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgDev = settings.createGroup("Dev");

    //--------------------General--------------------//
    private final Setting<Boolean> toggleModules = sgGeneral.add(new BoolSetting.Builder()
        .name("Toggle Modules")
        .description("Turn off other modules when Cev Breaker is activated.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> toggleBack = sgGeneral.add(new BoolSetting.Builder()
        .name("Toggle Back On")
        .description("Turn the modules back on when Cev Breaker is deactivated.")
        .defaultValue(false)
        .visible(toggleModules::get)
        .build()
    );
    private final Setting<List<Module>> modules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("modules")
        .description("Which modules to toggle.")
        .visible(toggleModules::get)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating.")
        .defaultValue(true)
        .build()
    );
    private final Setting<MaoJunQingType.SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<MaoJunQingType.SwitchMode>()
        .name("Switch Mode")
        .description("Switching method. Silent is the most reliable but doesn't work everywhere.")
        .defaultValue(MaoJunQingType.SwitchMode.Silent)
        .build()
    );
    private final Setting<MaoJunQingType.LogicMode> logicMode = sgGeneral.add(new EnumSetting.Builder<MaoJunQingType.LogicMode>()
        .name("Logic Mode")
        .description("Logic for bullying kids.")
        .defaultValue(MaoJunQingType.LogicMode.BreakPlace)
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("How many anchors should be blown every second.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Damage--------------------//
    private final Setting<Double> minDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Damage")
        .description("Minimum damage required to place.")
        .defaultValue(8)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Damage")
        .description("Maximum damage to self.")
        .defaultValue(6)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> minRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Damage Ratio")
        .description("Damage ratio between enemy damage and self damage (enemy / self).")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Place Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Place Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> interactSwing = sgRender.add(new BoolSetting.Builder()
        .name("Interact Swing")
        .description("Renders swing animation when interacting with a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> interactHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Interact Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(interactSwing::get)
        .build()
    );
    private final Setting<Boolean> damage = sgRender.add(new BoolSetting.Builder()
        .name("Render Damage")
        .description("Renders Damage.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> damageScale = sgRender.add(new DoubleSetting.Builder()
        .name("damage-scale")
        .description("How big the damage text should be.")
        .defaultValue(1.25)
        .min(1)
        .sliderMax(4)
        .visible(damage::get)
        .build()
    );
    private final Setting<SettingColor> damageColor = sgRender.add(new ColorSetting.Builder()
        .name("Render Damage")
        .description("Renders Damage.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(damage::get)
        .build()
    );
    private final Setting<MaoJunQingType.FadeMode> fadeMode = sgRender.add(new EnumSetting.Builder<MaoJunQingType.FadeMode>()
        .name("Fade Mode")
        .description("How long the fading should take.")
        .defaultValue(MaoJunQingType.FadeMode.Normal)
        .build()
    );
    private final Setting<Double> animationSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Move Speed")
        .description("How fast should aurora mode box move.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> animationMoveExponent = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Move Exponent")
        .description("Moves faster when longer away from the target.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> animationExponent = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Exponent")
        .description("How fast should aurora mode box grow.")
        .defaultValue(3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of render should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Line color of rendered boxes")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered boxes")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    //--------------------Dev--------------------//
    private final Setting<Integer> Predict = sgDev.add(new IntSetting.Builder()
        .name("PredictTicks")
        .description("PredictTicks.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    private final Setting<Integer> Radius = sgDev.add(new IntSetting.Builder()
        .name("Radius")
        .description("Radius.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );


    private BlockPos[] blocks = new BlockPos[]{};
    private int lastIndex = 0;
    private int length = 0;
    private long tickTime = -1;
    private double bestDmg = -1;
    private long lastTime = 0;
    private Vec3d renderTarget = null;

    private BlockPos placePos = null;
    private PlaceData placeData = null;
    private BlockPos calcPos = null;
    private PlaceData calcData = null;
    private Vec3d renderPos = null;
    private List<PlayerEntity> targets = new ArrayList<>();
    private final Map<BlockPos, Anchor> anchors = new HashMap<>();
    private final ArrayList<Module> toActivate = new ArrayList<Module>();

    double timer = 0;
    //type
    private MaoJunQingType.LogicMode LogicMode;
    private MaoJunQingType.AnchorState AnchorState;
    private MaoJunQingType.FadeMode FadeMode;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTickPre(TickEvent.Post event) {
        calculate(length - 1);
        placePos = calcPos;
        placeData = calcData;

        blocks = getBlocks(mc.player.getEyePos(), Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));

        // Reset stuff
        tickTime = System.currentTimeMillis();
        length = blocks.length;
        lastIndex = 0;
        bestDmg = -1;
        calcPos = null;
        calcData = null;

        updateTargets();
    }


    @Override
    public void onActivate() {
        if (toggleModules.get() && !modules.get().isEmpty() && mc.world != null && mc.player != null) {
            for (Module module : modules.get()) {
                if (module.isActive()) {
                    module.toggle();
                    toActivate.add(module);
                }
            }
        }
        super.onActivate();
        renderPos = null;
        renderProgress = 0;
        lastMillis = System.currentTimeMillis();
    }
    @Override
    public void onDeactivate() {
        if (toggleBack.get() && !toActivate.isEmpty() && mc.world != null && mc.player != null) {
            for (Module module : toActivate) {
                if (!module.isActive()) {
                    module.toggle();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender(Render3DEvent event) {
        renderBlocks.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

        renderBlocks.forEach(r -> {
            double progress = 1 - Math.min(System.currentTimeMillis() - r.time + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);

            event.renderer.box(r.blockPos, RenderUtils.injectAlpha(color.get(), (int) Math.round(color.get().a * progress)), RenderUtils.injectAlpha(lineColor.get(), (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
        });
        double delta = (System.currentTimeMillis() - lastMillis) / 1000f;
        timer += delta;
        lastMillis = System.currentTimeMillis();
        if (tickTime < 0 || mc.player == null || mc.world == null) {
            return;
        }

        if (pauseCheck()) {
            update();
        }
        if (placePos != null && pauseCheck()) {
            renderProgress = Math.min(1, renderProgress + delta);
            renderTarget = new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()).add(0, 1, 0);
        } else {
            renderProgress = Math.max(0, renderProgress - delta);
        }

        if (renderTarget != null) {
            renderPos = smoothMove(renderPos, renderTarget, delta * animationSpeed.get() * 5);
        }

        if (renderPos != null) {
            double r = 0.5 - Math.pow(1 - renderProgress, animationExponent.get()) / 2f;

            if (r >= 0.001 && fadeMode.get() != MaoJunQingType.FadeMode.Test2 ) {
                double down = -0.5;
                double up = -0.5;
                double width = 0.5;
                int a = 0;

                switch (fadeMode.get()) {
                    case Up -> {
                        up = 0;
                        down = -(r * 2);
                    }
                    case Down -> {
                        up = -1 + r * 2;
                        down = -1;
                    }
                    case Normal -> {
                        up = -0.5 + r;
                        down = -0.5 - r;
                        width = r;
                    }
                    case Test -> {
                        up = 0;
                        down = -1;
                        a= (int)  ((-r) * 100);
                    }
                }
                Box box = new Box(renderPos.getX() + 0.5 - width, renderPos.getY() + down, renderPos.getZ() + 0.5 - width,
                    renderPos.getX() + 0.5 + width, renderPos.getY() + up, renderPos.getZ() + 0.5 + width);

                event.renderer.box(box, new Color(color.get().r, color.get().g, color.get().b, color.get().a - a), lineColor.get(), shapeMode.get(), 0);
            }
        }

    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {

        if (tickTime < 0 || mc.player == null || mc.world == null) {
            return;
        }

        if (placePos != null && pauseCheck()) {
            vec = new Vector3d(renderPos.getX() + 0.5, renderPos.getY() - 0.5, renderPos.getZ() + 0.5);
        }

        if (vec != null) {
            if (NametagUtils.to2D(vec, damageScale.get())) {
                NametagUtils.begin(vec);
                TextRenderer.get().begin(1, false, true);

                String text = String.format("%.1f", dmg) + "/" + String.format("%.1f", self);
                double w = TextRenderer.get().getWidth(text) * 0.5;
                TextRenderer.get().render(text, -w, 0, damageColor.get(), true);

                TextRenderer.get().end();
                NametagUtils.end();
            }
        }
    }

    private boolean pauseCheck() {
        return !pauseEat.get() || !mc.player.isUsingItem();
    }

    private void calculate(int index) {
        BlockPos pos;
        for (int i = lastIndex; i < index; i++) {
            pos = blocks[i];

            dmg = getDmg(pos);
            self = BODamageUtils.anchorDamage(mc.player, mc.player.getBoundingBox().offset(CrystalUtil.getMotionVec(mc.player, Predict.get(), true)), pos, pos.toCenterPos());

            if (!dmgCheck(dmg, self)) {
                continue;
            }

            PlaceData data = SettingUtils.getPlaceData(pos);

            if (!data.valid()) {
                continue;
            }

            if (pos.equals(getMinePos())) {
                continue;
            }

            if (EntityUtils.intersectsWithEntity(new Box(pos), entity -> !(entity instanceof ItemEntity))) {
                continue;
            }

            calcData = data;
            calcPos = pos;
            bestDmg = dmg;
        }
        lastIndex = index;
    }

    private BlockPos getMinePos(){
        return meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class).targetPos();
    }

    private void updateTargets() {
        List<PlayerEntity> players = new ArrayList<>();
        double closestDist = 1000;
        PlayerEntity closest;
        double dist;
        for (int i = 3; i > 0; i--) {

            closest = null;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (players.contains(player) || Friends.get().isFriend(player) || player == mc.player) {
                    continue;
                }

                dist = player.distanceTo(mc.player);

                if (dist > 15) {
                    continue;
                }

                if (closest == null || dist < closestDist) {
                    closestDist = dist;
                    closest = player;
                }
            }
            if (closest != null) {
                players.add(closest);
            }
        }
        targets = players;
    }

    private BlockPos[] getBlocks(Vec3d middle, double radius) {
        ArrayList<BlockPos> result = new ArrayList<>();
        int i = (int) Math.ceil(radius);
        BlockPos pos;

        for (int x = -i; x <= i; x++) {
            for (int y = -i; y <= i; y++) {
                for (int z = -i; z <= i; z++) {
                    pos = new BlockPos((int) (Math.floor(middle.x) + x), (int) (Math.floor(middle.y) + y), (int) (Math.floor(middle.z) + z));

                    if (!OLEPOSSUtils.replaceable(pos) && !(mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR)) {
                        continue;
                    }

                    if (!inRangeToTargets(pos)) {
                        continue;
                    }

                    if (!SettingUtils.inPlaceRange(pos)) {
                        continue;
                    }

                    result.add(pos);
                }
            }
        }
        return result.toArray(new BlockPos[0]);
    }

    private boolean inRangeToTargets(BlockPos pos) {
        for (PlayerEntity target : targets) {
            if (target.getPos().add(0, 1, 0).distanceTo(Vec3d.ofCenter(pos)) < 3.5) return true;
        }
        return false;
    }

    private void update() {
        if (placePos == null || placeData == null || !placeData.valid()) return;

        Anchor anchor = getAnchor(placePos);

        if (logicMode.get() == LogicMode.PlaceBreak) {
            switch (anchor.state) {
                case Anchor -> {
                    if (chargeUpdate(placePos)) {
                        Anchor a = new Anchor(AnchorState.Loaded, anchor.charges + 1, System.currentTimeMillis());

                        anchors.remove(placePos);
                        anchors.put(placePos, a);
                    }
                }
                case Loaded -> {
                    if (explodeUpdate(placePos)) {
                        anchors.remove(placePos);
                        anchors.put(placePos, new Anchor(AnchorState.Air, 0, System.currentTimeMillis()));
                    }
                }
                case Air -> {
                    if (timer <= 1 / speed.get()) {
                        return;
                    }

                    if (placeUpdate()) {
                        anchors.remove(placePos);
                        anchors.put(placePos, new Anchor(AnchorState.Anchor, 0, System.currentTimeMillis()));
                        timer = 0;
                    }
                }
            }
        } else {
            switch (anchor.state) {
                case Air -> {
                    if (placeUpdate()) {
                        anchors.remove(placePos);
                        anchors.put(placePos, new Anchor(AnchorState.Anchor, 0, System.currentTimeMillis()));
                    }
                }
                case Anchor -> {
                    if (chargeUpdate(placePos)) {
                        Anchor a = new Anchor(AnchorState.Loaded, anchor.charges + 1, System.currentTimeMillis());

                        anchors.remove(placePos);
                        anchors.put(placePos, a);
                    }
                }
                case Loaded -> {
                    if (timer <= 1 / speed.get()) {
                        return;
                    }

                    if (explodeUpdate(placePos)) {
                        anchors.remove(placePos);
                        anchors.put(placePos, new Anchor(AnchorState.Air, 0, System.currentTimeMillis()));
                        timer = 0;
                    }
                }
            }
        }
    }

    public boolean Exploding() {
        return isActive() && targets.stream().allMatch(target -> target != null && placePos != null);
    }

    private void place(Hand hand) {
        placeBlock(hand, placeData.pos().toCenterPos(), placeData.dir(), placeData.pos());
        if (placeSwing.get()) clientSwing(placeHand.get(), hand);
    }

    private Anchor getAnchor(BlockPos pos) {
        if (anchors.containsKey(pos)) {
            return anchors.get(pos);
        }
        BlockState state = mc.world.getBlockState(pos);
        return new Anchor(state.getBlock() == Blocks.RESPAWN_ANCHOR ? state.get(Properties.CHARGES) < 1 ? AnchorState.Anchor : AnchorState.Loaded : AnchorState.Air, state.getBlock() == Blocks.RESPAWN_ANCHOR ? state.get(Properties.CHARGES) : 0, System.currentTimeMillis());
    }

    private boolean placeUpdate() {
        Hand hand = Managers.HOLDING.isHolding(Items.RESPAWN_ANCHOR) ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() == Items.RESPAWN_ANCHOR ? Hand.OFF_HAND : null;

        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(Items.RESPAWN_ANCHOR);
                    switched = result.found();
                }
                case PickSilent, InvSwitch -> {
                    FindItemResult result = InvUtils.find(Items.RESPAWN_ANCHOR);
                    switched = result.found();
                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(placeData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return false;
        }


        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(Items.RESPAWN_ANCHOR);
                    InvUtils.swap(result.slot(), true);
                }
                case PickSilent -> {
                    FindItemResult result = InvUtils.find(Items.RESPAWN_ANCHOR);
                    switched = BOInvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    FindItemResult result = InvUtils.find(Items.RESPAWN_ANCHOR);
                    switched = BOInvUtils.invSwitch(result.slot());
                }
            }
        }

        if (!switched) {
            return false;
        }

        place(hand == null ? Hand.MAIN_HAND : hand);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
        return true;
    }

    private boolean chargeUpdate(BlockPos pos) {
        Hand hand = Managers.HOLDING.isHolding(Items.GLOWSTONE) ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() == Items.GLOWSTONE ? Hand.OFF_HAND : null;
        Direction dir = SettingUtils.getPlaceOnDirection(pos);

        if (dir == null) {
            return false;
        }

        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(Items.GLOWSTONE);
                    switched = result.found();
                }
                case PickSilent, InvSwitch -> {
                    FindItemResult result = InvUtils.find(Items.GLOWSTONE);
                    switched = result.found();
                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(pos, priority, RotationType.Interact, Objects.hash(name + "interact"))) {
            return false;
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(Items.GLOWSTONE);
                    InvUtils.swap(result.slot(), true);
                }
                case PickSilent -> {
                    FindItemResult result = InvUtils.find(Items.GLOWSTONE);
                    switched = BOInvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    FindItemResult result = InvUtils.find(Items.GLOWSTONE);
                    switched = BOInvUtils.invSwitch(result.slot());
                }

            }
        }

        if (!switched) {
            return false;
        }

        interact(pos, dir, hand == null ? Hand.MAIN_HAND : hand);

        if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end(Objects.hash(name + "interact"));
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
        return true;
    }

    private boolean explodeUpdate(BlockPos pos) {
        Hand hand = !Managers.HOLDING.isHolding(Items.GLOWSTONE) ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() != Items.GLOWSTONE ? Hand.OFF_HAND : null;
        Direction dir = SettingUtils.getPlaceOnDirection(pos);

        if (dir == null) {
            return false;
        }

        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(stack -> stack.getItem() != Items.GLOWSTONE);
                    switched = result.found();
                }
                case PickSilent, InvSwitch -> {
                    FindItemResult result = InvUtils.find(stack -> stack.getItem() != Items.GLOWSTONE);
                    switched = result.found();
                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(pos, priority, RotationType.Interact, Objects.hash(name + "explode"))) {
            return false;
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(item -> item.getItem() != Items.GLOWSTONE);
                    InvUtils.swap(result.slot(), true);
                }
                case PickSilent -> {
                    FindItemResult result = InvUtils.find(item -> item.getItem() != Items.GLOWSTONE);
                    switched = BOInvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    FindItemResult result = InvUtils.find(item -> item.getItem() != Items.GLOWSTONE);
                    switched = BOInvUtils.invSwitch(result.slot());
                }
            }
        }

        if (!switched) {
            return false;
        }

        interact(pos, dir, hand == null ? Hand.MAIN_HAND : hand);

        if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end(Objects.hash(name + "explode"));
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
        return true;
    }

    private void interact(BlockPos pos, Direction dir, Hand hand) {
        interactBlock(hand, pos.toCenterPos(), dir, pos);
        if(fadeMode.get() == FadeMode.Test2){
            renderBlocks.add(new Render(pos, System.currentTimeMillis()));
        }
        if (interactSwing.get()) clientSwing(interactHand.get(), hand);
    }

    private boolean dmgCheck(double dmg, double self) {
        if (dmg < bestDmg) return false;
        if (dmg < minDmg.get()) return false;
        if (self > maxDmg.get()) return false;
        return dmg / self >= minRatio.get();
    }

    private double getDmg(BlockPos pos) {
        double highest = -1;
        for (PlayerEntity target : targets) {
            highest = Math.max(highest, BODamageUtils.anchorDamage(target, target.getBoundingBox().offset(CrystalUtil.getMotionVec(target, Predict.get(), true)), pos, pos.toCenterPos()));
        }
        return highest;
    }

    private Vec3d calcPredict(final Entity e, final int ticks) {
        if (ticks == 0) {
            return e.getPos();
        }
        return new Vec3d(e.getX() + (e.getX() - e.lastRenderX) * ticks, e.getY() + (e.getY() - e.lastRenderY) * ticks, e.getZ() + (e.getZ() - e.lastRenderZ) * ticks);
    }

    private Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
        if (current == null) return target;

        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);

        double x = (absX + Math.pow(absX, animationMoveExponent.get() - 1)) * delta;
        double y = (absX + Math.pow(absY, animationMoveExponent.get() - 1)) * delta;
        double z = (absX + Math.pow(absZ, animationMoveExponent.get() - 1)) * delta;

        return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x),
            current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y),
            current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
    }

    private record Anchor(MaoJunQingType.AnchorState state, int charges, long time) {
    }
    public record Render(BlockPos blockPos, long time) {
    }
}
