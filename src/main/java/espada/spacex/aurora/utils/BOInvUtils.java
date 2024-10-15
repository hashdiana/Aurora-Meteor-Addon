package espada.spacex.aurora.utils;

import espada.spacex.aurora.managers.Managers;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

@SuppressWarnings("DataFlowIssue")
public class BOInvUtils {
    private static int[] slots;
    public static int pickSlot = -1;

    public static boolean pickSwitch(int slot) {
        if (slot >= 0) {
            Managers.HOLDING.modifyStartTime = System.currentTimeMillis();
            pickSlot = slot;
            mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(slot));

            return true;
        }
        return false;
    }
    public static void pickSwapBack() {
        if (pickSlot >= 0) {
            mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(pickSlot));
            pickSlot = -1;
        }
    }

    // Credits to rickyracuun
    public static boolean invSwitch(int slot) {
        if (slot >= 0) {
            ScreenHandler handler = mc.player.currentScreenHandler;
            Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap<>();
            stack.put(slot, handler.getSlot(slot).getStack());

            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId,
                handler.getRevision(), PlayerInventory.MAIN_SIZE + Managers.HOLDING.slot,
                slot, SlotActionType.SWAP, handler.getSlot(slot).getStack(), stack)
            );
            ((IClientPlayerInteractionManager) mc.interactionManager).syncSelected();
            slots = new int[]{slot, Managers.HOLDING.slot};
            return true;
        }
        return false;
    }

    public static void swapBack() {
        ScreenHandler handler = mc.player.currentScreenHandler;
        Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap<>();
        stack.put(slots[0], handler.getSlot(slots[0]).getStack());

        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId,
            handler.getRevision(), PlayerInventory.MAIN_SIZE + slots[1],
            slots[0], SlotActionType.SWAP, handler.getSlot(slots[0]).getStack().copy(), stack)
        );
        ((IClientPlayerInteractionManager) mc.interactionManager).syncSelected();
    }

    public static void invSwapBack() {
        ScreenHandler handler = mc.player.currentScreenHandler;
        Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap<>();
        stack.put(slots[0], handler.getSlot(slots[0]).getStack());

        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId,
            handler.getRevision(), PlayerInventory.MAIN_SIZE + slots[1],
            slots[0], SlotActionType.SWAP, handler.getSlot(slots[0]).getStack().copy(), stack)
        );
        ((IClientPlayerInteractionManager) mc.interactionManager).syncSelected();
    }

    public static int findHotbarBlock(Block blockIn) {
        for(int i = 0; i < 9; ++i) {
            ItemStack stack = MeteorClient.mc.player.getInventory().getStack(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == blockIn) {
                return i;
            }
        }

        return -1;
    }

    public static void doSwap(int slot) {
        mc.player.getInventory().selectedSlot = slot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        ((IClientPlayerInteractionManager) mc.interactionManager).syncSelected();
    }
}
