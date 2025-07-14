package wtf.moonlight.module.impl.player;

import com.cubk.EventTarget;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.lwjglx.input.Keyboard;
import wtf.moonlight.Client;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.player.InventoryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ModuleInfo(name = "ChestStealer", category = Categor.Player, key = Keyboard.KEY_L)
public final class ChestStealer extends Module {
    private final SliderValue delay = new SliderValue("Delay", 80, 0, 300, 10, this);
    public final BoolValue titleCheck = new BoolValue("Title Check", true, this);
    public final BoolValue freeLook = new BoolValue("Free Look", true, this);
    private final BoolValue reverse = new BoolValue("Reverse", false, this);
    public final BoolValue silent = new BoolValue("Silent", false, this);
    public final BoolValue stealingIndicator = new BoolValue("Stealing Indicator", false, this, silent::get);
    private final BoolValue ignoreTrash = new BoolValue("Ignore Trash", true, this);

    private final List<Item> items = new ArrayList<>();
    private final TimerUtil timer = new TimerUtil();
    public boolean stealing;
    private InvManager invManager;
    private boolean clear;


    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (stealingIndicator.get() && stealing) {
            ScaledResolution sr = new ScaledResolution(mc);
            Interface hudMod = Client.INSTANCE.getModuleManager().getModule(Interface.class);
            if (hudMod.cFont.get()) {
                hudMod.getFr().drawStringWithShadow("§lStealing...", sr.getScaledWidth() / 2.0F -
                        hudMod.getFr().getStringWidth("§lStealing...") / 2.0F, sr.getScaledHeight() / 2.0F + 10, hudMod.color(0));
            } else {
                mc.fontRendererObj.drawStringWithShadow("§lStealing...", sr.getScaledWidth() / 2.0F -
                        mc.fontRendererObj.getStringWidth("§lStealing...") / 2.0F, sr.getScaledHeight() / 2.0F + 10, hudMod.color(0));
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.isPre()) {
            if (invManager == null) invManager = Client.INSTANCE.getModuleManager().getModule(InvManager.class);

            if (mc.thePlayer.openContainer instanceof ContainerChest chest) {
                IInventory chestInv = chest.getLowerChestInventory();
                if (titleCheck.get() && (!(chestInv instanceof ContainerLocalMenu) || !((ContainerLocalMenu) chestInv).realChest))
                    return;
                clear = true;

                List<Integer> slots = new ArrayList<>();
                for (int i = 0; i < chestInv.getSizeInventory(); i++) {
                    ItemStack is = chestInv.getStackInSlot(i);
                    if (!ignoreTrash.get() || (InventoryUtil.isValid(is) && !items.contains(is.getItem()))) {
                        slots.add(i);
                    }
                }

                if (reverse.get()) {
                    Collections.reverse(slots);
                }

                slots.forEach(s -> {
                    ItemStack is = chestInv.getStackInSlot(s);
                    Item item = is != null ? is.getItem() : null;
                    if (item != null && !items.contains(item) && (delay.getValue() == 0 || timer.hasTimeElapsed(delay.getValue().longValue(), true))) {
                        if (ignoreTrash.get() && !(item instanceof ItemBlock)) {
                            items.add(is.getItem());
                        }
                        mc.playerController.windowClick(chest.windowId, s, 0, 1, mc.thePlayer);
                    }
                });

                if (slots.isEmpty() || isInventoryFull()) {
                    items.clear();
                    clear = false;
                    stealing = false;
                    mc.thePlayer.closeScreen();
                }
            } else if (clear) {
                items.clear();
                clear = false;
            }
        }
    }

    private boolean isInventoryFull() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getStack() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean canSteal() {
        if (Client.INSTANCE.getModuleManager().getModule(ChestStealer.class).isEnabled() && mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            IInventory chestInv = chest.getLowerChestInventory();
            return !titleCheck.get() || (chestInv instanceof ContainerLocalMenu && ((ContainerLocalMenu) chestInv).realChest);
        }
        return false;
    }
}
