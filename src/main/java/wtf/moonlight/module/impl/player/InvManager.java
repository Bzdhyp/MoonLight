package wtf.moonlight.module.impl.player;

import lombok.Getter;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.DamageSource;
import com.cubk.EventTarget;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.AttackEvent;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.movement.InvMove;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MathUtil;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.packet.PacketUtils;
import wtf.moonlight.util.player.InventoryUtil;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.component.SelectorDetectionComponent;

import java.util.HashSet;
import java.util.Set;

@ModuleInfo(name = "InvManager", category = Categor.Player)
public class InvManager extends Module {
    private final BoolValue autoArmor = new BoolValue("AutoArmor", true, this);
    private final BoolValue keepSync = new BoolValue("Keep Sync", false, this);
    private final BoolValue dropItems = new BoolValue("Drop Items", true, this);

    private final SliderValue swordSlot = new SliderValue("Sword Slot", 1, 1, 9, this);
    private final SliderValue gappleSlot = new SliderValue("Gapple Slot", 2, 1, 9, this);
    private final SliderValue pickaxeSlot = new SliderValue("Pickaxe Slot", 4, 1, 9, this);
    private final SliderValue axeSlot = new SliderValue("Axe Slot", 5, 1, 9, this);
    private final SliderValue blockSlot = new SliderValue("Block Slot", 6, 1, 9, this);
    private final SliderValue potionSlot = new SliderValue("Potion Slot", 8, 1, 9, this);
    private final SliderValue delay = new SliderValue("Delay", 150, 0, 500, 10, this);

    private final ListValue modeValue = new ListValue("Mode", new String[]{"Basic", "OpenInv"},  "Basic", this);

    public final TimerUtil timerUtil = new TimerUtil();
    private int chestTicks, attackTicks, placeTicks;
    @Getter
    private boolean moved, open;
    private long nextClick;
    public short action;

    @EventTarget
    public void onPacketSend(PacketEvent event) {
        if (event.getState() == PacketEvent.State.OUTGOING) {
            if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                this.placeTicks = 0;
            }
        }
    }

    @EventTarget
    public void onPacketReceive(PacketEvent event) {
        if (event.getState() == PacketEvent.State.INCOMING) {
            if (keepSync.get()) {
                Packet<?> packet = event.getPacket();

                if (packet instanceof S32PacketConfirmTransaction wrapper) {
                    Container inventory = mc.thePlayer.inventoryContainer;

                    if (wrapper.getWindowId() == inventory.windowId) {
                        this.action = wrapper.getActionNumber();

                        if (this.action > 0 && this.action < inventory.transactionID) {
                            inventory.transactionID = (short) (this.action + 1);
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.isPre()) {
            if (mc.thePlayer.ticksExisted <= 40) return;

            if (mc.currentScreen instanceof GuiChest) {
                this.chestTicks = 0;
            } else {
                this.chestTicks++;
            }

            this.attackTicks++;
            this.placeTicks++;

            if (!this.timerUtil.hasTimeElapsed(this.nextClick) || this.chestTicks < 10 || this.attackTicks < 10 || this.placeTicks < 10) {
                this.closeInventory();
                return;
            }

            if (modeValue.is("OpenInv") && !(mc.currentScreen instanceof GuiInventory)) {
                return;
            }

            this.moved = false;

            int helmet = -1, chestplate = -1, leggings = -1, boots = -1;
            int sword = -1, pickaxe = -1, axe = -1, block = -1, potion = -1, food = -1;
            Set<Integer> keepSlots = new HashSet<>();

            int INVENTORY_SLOTS = 4 * 9 + 4;

            for (int i = 0; i < INVENTORY_SLOTS; i++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack == null) continue;

                Item item = stack.getItem();

                if (item instanceof ItemFood && item != Item.getItemById(322) && item != Item.getItemById(466)) continue;
                if (!InventoryUtil.isValid(stack)) continue;

                if (autoArmor.get() && item instanceof ItemArmor armor) {
                    int reduction = armorReduction(stack);
                    switch (armor.armorType) {
                        case 0:
                            if (helmet == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(helmet))) helmet = i;
                            break;
                        case 1:
                            if (chestplate == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(chestplate))) chestplate = i;
                            break;
                        case 2:
                            if (leggings == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(leggings))) leggings = i;
                            break;
                        case 3:
                            if (boots == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(boots))) boots = i;
                            break;
                    }
                    continue;
                }

                if (item instanceof ItemSpade) continue;

                if (item instanceof ItemSword) {
                    int durability = stack.getMaxDamage() - stack.getItemDamage();
                    if (sword == -1) sword = i;
                    else {
                        ItemStack best = mc.thePlayer.inventory.getStackInSlot(sword);
                        int bestDurability = best.getMaxDamage() - best.getItemDamage();
                        boolean fire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) > 0;
                        boolean bestFire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, best) > 0;

                        if (bestDurability <= 10 && durability > bestDurability) sword = i;
                        else if (fire && !bestFire) sword = i;
                        else if (fire == bestFire && damage(stack) > damage(best)) sword = i;
                    }
                    continue;
                }

                if (item instanceof ItemPickaxe) {
                    if (pickaxe == -1 || mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(pickaxe))) pickaxe = i;
                    continue;
                }

                if (item instanceof ItemAxe) {
                    if (axe == -1 || mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(axe))) axe = i;
                    continue;
                }

                if (item instanceof ItemBlock) {
                    keepSlots.add(i);

                    if (block == -1) {
                        block = i;
                    } else {
                        ItemStack currentBlock = mc.thePlayer.inventory.getStackInSlot(block);
                        if (stack.stackSize > currentBlock.stackSize) {
                            block = i;
                        }
                    }
                    continue;
                }

                if (item instanceof ItemPotion potionItem) {
                    if (potion == -1) potion = i;
                    else {
                        int curRank = PlayerUtil.potionRanking(((ItemPotion) mc.thePlayer.inventory.getStackInSlot(potion).getItem()).getEffects(mc.thePlayer.inventory.getStackInSlot(potion)).get(0).getPotionID());
                        int newRank = PlayerUtil.potionRanking(potionItem.getEffects(stack).get(0).getPotionID());
                        if (newRank > curRank) potion = i;
                    }
                    continue;
                }

                if (item instanceof ItemFood foodItem) {
                    if (food == -1) food = i;
                    else {
                        float curSat = ((ItemFood) mc.thePlayer.inventory.getStackInSlot(food).getItem()).getSaturationModifier(mc.thePlayer.inventory.getStackInSlot(food));
                        float newSat = foodItem.getSaturationModifier(stack);
                        if (newSat > curSat) food = i;
                    }
                }
            }

            if (helmet != -1) keepSlots.add(helmet);
            if (chestplate != -1) keepSlots.add(chestplate);
            if (leggings != -1) keepSlots.add(leggings);
            if (boots != -1) keepSlots.add(boots);
            if (sword != -1) keepSlots.add(sword);
            if (pickaxe != -1) keepSlots.add(pickaxe);
            if (axe != -1) keepSlots.add(axe);
            if (block != -1) keepSlots.add(block);
            if (potion != -1) keepSlots.add(potion);
            if (food != -1) keepSlots.add(food);

            for (int i = 0; i < INVENTORY_SLOTS; i++) {
                if (!keepSlots.contains(i)) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack == null) continue;
                    Item item = stack.getItem();
                    if (item instanceof ItemSpade || !InventoryUtil.isValid(stack)) {
                        throwItem(i);
                    } else if (item instanceof ItemFood && item != Item.getItemById(322) && item != Item.getItemById(466)) {
                        throwItem(i);
                    }
                }
            }

            if (autoArmor.get()) {
                if (helmet != -1 && helmet != 39) equipItem(helmet);
                if (chestplate != -1 && chestplate != 38) equipItem(chestplate);
                if (leggings != -1 && leggings != 37) equipItem(leggings);
                if (boots != -1 && boots != 36) equipItem(boots);
            }

            if (sword != -1 && sword != swordSlot.getValue() - 1) moveItem(sword, (int) (swordSlot.getValue() - 37));
            if (pickaxe != -1 && pickaxe != pickaxeSlot.getValue() - 1) moveItem(pickaxe, (int) (pickaxeSlot.getValue() - 37));
            if (axe != -1 && axe != axeSlot.getValue() - 1) moveItem(axe, (int) (axeSlot.getValue() - 37));
            if (block != -1 && block != blockSlot.getValue() - 1 && !isEnabled(Scaffold.class)) moveItem(block, (int) (blockSlot.getValue() - 37));
            if (potion != -1 && potion != potionSlot.getValue() - 1) moveItem(potion, (int) (potionSlot.getValue() - 37));
            if (food != -1 && food != gappleSlot.getValue() - 1) moveItem(food, (int) (gappleSlot.getValue() - 37));

            if (canOpenInventory() && !moved) closeInventory();
        }
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        this.attackTicks = 0;
    }

    @Override
    public void onDisable() {
        if (this.canOpenInventory()) {
            this.closeInventory();
        }
        super.onDisable();
    }

    private void openInventory() {
        if (!this.open) {
            PacketUtils.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.open = true;
        }
    }

    private void closeInventory() {
        if (this.open) {
            PacketUtils.sendPacket(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            this.open = false;
        }
    }

    private boolean canOpenInventory() {
        return isEnabled(InvMove.class) && !(mc.currentScreen instanceof GuiInventory);
    }

    private void throwItem(final int slot) {
        if ((!moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot) && dropItems.get()) {
            if (this.canOpenInventory()) openInventory();
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 1, 4, mc.thePlayer);
            updateNextClick();
        }
    }

    private void moveItem(int slot, int destination) {
        if ((!moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot)) {
            if (this.canOpenInventory()) openInventory();
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), this.slot(destination), 2, mc.thePlayer);
            updateNextClick();
        }
    }

    private void equipItem(int slot) {
        if ((!moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot) && autoArmor.get()) {
            if (this.canOpenInventory()) openInventory();
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 0, 1, mc.thePlayer);
            updateNextClick();
        }
    }

    private void updateNextClick() {
        this.nextClick = Math.round((float) MathUtil.getRandom(this.delay.getValue().intValue(), this.delay.getValue().intValue()));
        this.timerUtil.reset();
        moved = true;
    }

    private float damage(final ItemStack stack) {
        final ItemSword sword = (ItemSword) stack.getItem();
        final int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
        return (float) (sword.getDamageVsEntity() + level * 1.25);
    }

    private float mineSpeed(final ItemStack stack) {
        final Item item = stack.getItem();
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);

        level = switch (level) {
            case 1 -> 30;
            case 2 -> 69;
            case 3 -> 120;
            case 4 -> 186;
            case 5 -> 271;
            default -> 0;
        };

        if (item instanceof ItemPickaxe pickaxe) {
            return pickaxe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemSpade shovel) {
            return shovel.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemAxe axe) {
            return axe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        }

        return 0;
    }

    private int armorReduction(final ItemStack stack) {
        final ItemArmor armor = (ItemArmor) stack.getItem();
        return armor.damageReduceAmount + EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[]{stack}, DamageSource.generic);
    }

    private int slot(final int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        }

        if (slot < 9) {
            return slot + 36;
        }

        return slot;
    }
}