package wtf.moonlight.component;

import com.cubk.EventTarget;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import wtf.moonlight.events.player.SyncCurrentItemEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.util.misc.InstanceAccess;

public final class SlotComponent implements InstanceAccess {
    public static boolean finished = true;

    /**
     * Set the slot silently, optionally with rendering effects.
     */
    public static void setSlot(int slot) {
        if (slot < 0 || slot >= 9) return;

        InventoryPlayer inventory = mc.thePlayer.inventory;
        inventory.alternativeCurrentItem = slot;
        inventory.alternativeSlot = true;
        finished = false;

        // Sync with server (as in ModernInterface's setSlot)
        mc.playerController.syncCurrentPlayItem();
    }

    /**
     * Delayed slot change logic. Similar approach as in your SlotComponent code,
     * now integrated here. Uses random checks as before.
     */
    public static void setSlotDelayed(final int slot, boolean force) {
        // Mimic delay logic
        if (Math.random() * Math.random() > 0.25 || force) {
            setSlot(mc.playerController.currentPlayerItem);
        } else {
            setSlot(slot);
        }
    }

    /**
     * On sync event, tell the server which slot is being used.
     */
    @EventTarget
    public void onSyncCurrentItem(SyncCurrentItemEvent event) {
        InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        event.setSlot(inventoryPlayer.alternativeSlot ? inventoryPlayer.alternativeCurrentItem : inventoryPlayer.currentItem);
    }

    /**
     * Each update, revert back to normal slot usage to keep client and server in sync.
     */
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        inventoryPlayer.alternativeSlot = false;
        inventoryPlayer.alternativeCurrentItem = inventoryPlayer.currentItem;
    }

    /**
     * Get the current item stack from the active slot.
     */
    public static ItemStack getItemStack() {
        return (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null
                ? null
                : mc.thePlayer.inventoryContainer.getSlot(getItemIndex() + 36).getStack());
    }

    /**
     * Returns the currently active slot index (alternative if active, else current).
     */
    public static int getItemIndex() {
        InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        return inventoryPlayer.alternativeSlot ? inventoryPlayer.alternativeCurrentItem : inventoryPlayer.currentItem;
    }
}
