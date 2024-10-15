package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.autocrystal.AutoCrystal;
import espada.spacex.aurora.modules.automine.AuroraMine;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.Predicate;

/**
 * @author OLEPOSSU
 */

public class OffHandPlus extends Modules {
    public OffHandPlus() {
        super(Aurora.Extendcombat, "SmartOffhand", "Better offhand.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgHealth = settings.createGroup("Health");

    //--------------------General--------------------//
    private final Setting<Boolean> onlyInInv = sgGeneral.add(new BoolSetting.Builder()
        .name("Only in inventory")
        .description("Will only switch if you are in your inventory.")
        .defaultValue(false)
        .build()
    );
    private final Setting<ItemMode> itemMode = sgGeneral.add(new EnumSetting.Builder<ItemMode>()
        .name("Item Mode")
        .description("Which item should be held in offhand.")
        .defaultValue(ItemMode.Totem)
        .build()
    );
    private final Setting<GapMode> gapMode = sgGeneral.add(new EnumSetting.Builder<GapMode>()
        .name("Gapple Mode")
        .description("When should we hold golden apples.")
        .defaultValue(GapMode.Combat)
        .build()
    );
    private final Setting<SwordMode> swordMode = sgGeneral.add(new EnumSetting.Builder<SwordMode>()
        .name("Sword Mode")
        .description("When should we hold sword.")
        .defaultValue(SwordMode.Pressed)
        .visible(gapMode.get()::isSword)
        .build()
    );
    private final Setting<Boolean> safeSword = sgGeneral.add(new BoolSetting.Builder()
        .name("Safe Sword")
        .description("Only sword gaps if you have enough health.")
        .defaultValue(false)
        .visible(gapMode.get()::isSword)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between switches.")
        .defaultValue(0.1)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Boolean> pickswitch = sgGeneral.add(new BoolSetting.Builder()
        .name("pickswitch")
        .description("Uses pick silent and swap with offhand packets to bypass ncp inventory checks.")
        .defaultValue(false)
        .build()
    );

    //--------------------Health--------------------//
    private final Setting<Integer> hp = sgHealth.add(new IntSetting.Builder()
        .name("Health")
        .description("Switches to totem when health is under this value.")
        .defaultValue(14)
        .range(0, 36)
        .sliderMax(36)
        .build()
    );
    private final Setting<Boolean> safety = sgHealth.add(new BoolSetting.Builder()
        .name("Safety")
        .description("Tries to prevent offhand fails by switching while in danger.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> safetyHealth = sgHealth.add(new IntSetting.Builder()
        .name("Safety Health")
        .description("Holds totem if you would have under this health after possible damages.")
        .defaultValue(0)
        .range(0, 36)
        .sliderMax(36)
        .visible(safety::get)
        .build()
    );

    private double timer = 0;
    private Item item = null;
    private Suicide suicide = null;
    private AutoCrystal autoCrystalRewrite = null;
    private CrystalAura crystalAura = null;
    private AuroraMine autoMine = null;
    private long lastTime = 0;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        timer -= (System.currentTimeMillis() - lastTime) / 1000d;
        lastTime = System.currentTimeMillis();

        if (suicide == null) suicide = meteordevelopment.meteorclient.systems.modules.Modules.get().get(Suicide.class);
        if (autoCrystalRewrite == null) autoCrystalRewrite = meteordevelopment.meteorclient.systems.modules.Modules.get().get(AutoCrystal.class);
        if (crystalAura == null) crystalAura = meteordevelopment.meteorclient.systems.modules.Modules.get().get(CrystalAura.class);
        if (autoMine == null) autoMine = meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);

        item = getItem();
        if (item != null) update();
    }

    private void update() {
        if (timer > 0) return;
        if (getPredicate(item).test(mc.player.getOffHandStack().getItem())) return;
        if (onlyInInv.get() && !(mc.currentScreen instanceof InventoryScreen)) return;

        int slot = getSlot(getPredicate(item));

        move(slot);

        timer = delay.get();
    }

    private void move(int slot) {
        if (pickswitch.get()) {
            BOInvUtils.pickSwitch(slot);
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN, 0));
            BOInvUtils.pickSwapBack();
            InvUtils.swap(Managers.HOLDING.slot, false);
            return;
        }
        InvUtils.move().from(slot).toOffhand();
    }

    private Predicate<Item> getPredicate(Item item) {
        if (item == Items.GOLDEN_APPLE) return OLEPOSSUtils::isGapple;
        if (item == Items.RED_BED) return BedItem.class::isInstance;
        return item::equals;
    }

    private Item getItem() {
        if (mc.player.getMainHandStack().getItem() instanceof PickaxeItem && (!safeSword.get() || !inDanger())) {
            if (gapMode.get().sword) {
                switch (swordMode.get()) {
                    case Always -> {
                        return Items.GOLDEN_APPLE;
                    }
                    case Pressed -> {
                        if (mc.options.useKey.isPressed())
                            return Items.GOLDEN_APPLE;
                    }
                }
            }
        }
            if (mc.player.getMainHandStack().getItem() instanceof SwordItem && (!safeSword.get() || !inDanger())) {
                if (gapMode.get().sword) {
                    switch (swordMode.get()) {
                        case Always -> {
                            return Items.GOLDEN_APPLE;
                        }
                        case Pressed -> {
                            if (mc.options.useKey.isPressed())
                                return Items.GOLDEN_APPLE;
                        }
                    }
                }
            }

        if (inDanger() && !suicide.isActive() && itemAvailable(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING)) {
            return Items.TOTEM_OF_UNDYING;
        }

        switch (itemMode.get()) {
            case Totem -> {
                if (!suicide.isActive() && itemAvailable(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING)) {
                    return Items.TOTEM_OF_UNDYING;
                }
            }
            case Crystal -> {
                if (itemAvailable(itemStack -> itemStack.getItem() == Items.END_CRYSTAL)) {
                    return Items.END_CRYSTAL;
                }
            }
            case Gapple -> {
                if (itemAvailable(OLEPOSSUtils::isGapple)) {
                    return Items.GOLDEN_APPLE;
                }
            }
            case Bed -> {
                if (itemAvailable(itemStack -> itemStack.getItem() instanceof BedItem)) {
                    return Items.RED_BED;
                }
            }
        }

        return itemAvailable(OLEPOSSUtils::isGapple) && (gapMode.get() == GapMode.LastOption) ? Items.GOLDEN_APPLE : null;
    }

    private boolean inDanger() {
        double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        return health <= hp.get() || (safety.get() && health - PlayerUtils.possibleHealthReductions() <= safetyHealth.get());
    }

    private int getSlot(Predicate<Item> predicate) {
        double amount = -1;
        int slot = -1;

        ItemStack s;
        for (int i = 9; i < mc.player.getInventory().size() + 1; i++) {
            s = mc.player.getInventory().getStack(i);

            if (!predicate.test(s.getItem())) continue;

            if (s.getCount() > amount) {
                slot = i;
                amount = s.getCount();
            }
        }
        if (slot >= 0) return slot;

        for (int i = 0; i < 9; i++) {
            s = mc.player.getInventory().getStack(i);

            if (!predicate.test(s.getItem())) continue;

            if (s.getCount() > amount) {
                slot = i;
                amount = s.getCount();
            }
        }

        return slot;
    }

    private boolean itemAvailable(Predicate<ItemStack> predicate) {
        return InvUtils.find(predicate).found();
    }

    public enum ItemMode {
        Totem,
        Crystal,
        Gapple,
        Bed
    }

    public enum GapMode {
        LastOption(false),
        Combat(true),
        Never(false);

        public final boolean sword;

        GapMode(boolean sword) {
            this.sword = sword;
        }

        public boolean isSword() {
            return sword;
        }
    }

    public enum SwordMode {
        Pressed,
        Always
    }
}
