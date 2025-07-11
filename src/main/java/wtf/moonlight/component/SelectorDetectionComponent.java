package wtf.moonlight.component;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.utils.InstanceAccess;

public final class SelectorDetectionComponent implements InstanceAccess {

    private static boolean selector;

    public static boolean selector() {
        return selector;
    }

    public static boolean selector(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        } else if (itemStack == mc.thePlayer.inventory.getItemStack()) {
            return selector();
        } else {
            return !trueName(itemStack).contains(itemStack.getDisplayName());
        }
    }

    public static boolean selector(int itemSlot) {
        return selector(mc.thePlayer.inventory.getStackInSlot(itemSlot));
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (getItemStack() != null) {
            ItemStack itemStack = getItemStack();

            selector = !trueName(itemStack).contains(itemStack.getDisplayName());
        } else {
            selector = false;
        }
    }

    public ItemStack getItemStack() {
        return (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null ? null : mc.thePlayer.inventoryContainer.getSlot(getItemIndex() + 36).getStack());
    }

    public int getItemIndex() {
        final InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        return inventoryPlayer.alternativeSlot && !inventoryPlayer.breakNotNative ? inventoryPlayer.alternativeCurrentItem : inventoryPlayer.currentItem;
    }

    public static String trueName(ItemStack itemStack) {
        String name = (StatCollector.translateToLocal(itemStack.getUnlocalizedName() + ".name")).trim();
        final String s1 = EntityList.getStringFromID(itemStack.getMetadata());

        if (s1 != null) {
            name = name + " " + StatCollector.translateToLocal("entity." + s1 + ".name");
        }

        return name;
    }
}
