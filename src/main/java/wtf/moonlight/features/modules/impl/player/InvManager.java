package wtf.moonlight.features.modules.impl.player;

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
import wtf.moonlight.events.annotations.EventTarget;
import wtf.moonlight.events.impl.packet.PacketEvent;
import wtf.moonlight.events.impl.player.AttackEvent;
import wtf.moonlight.events.impl.player.MotionEvent;
import wtf.moonlight.features.modules.Module;
import wtf.moonlight.features.modules.ModuleCategory;
import wtf.moonlight.features.modules.ModuleInfo;
import wtf.moonlight.features.modules.impl.movement.InvMove;
import wtf.moonlight.features.modules.impl.movement.Scaffold;
import wtf.moonlight.features.values.impl.BoolValue;
import wtf.moonlight.features.values.impl.ModeValue;
import wtf.moonlight.features.values.impl.SliderValue;
import wtf.moonlight.utils.math.MathUtils;
import wtf.moonlight.utils.math.TimerUtils;
import wtf.moonlight.utils.packet.PacketUtils;
import wtf.moonlight.utils.player.InventoryUtils;
import wtf.moonlight.utils.player.PlayerUtils;
import wtf.moonlight.utils.player.SelectorDetectionComponent;

@ModuleInfo(name = "InvManager", category = ModuleCategory.Player)
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
    private final SliderValue delay = new SliderValue("Delay", 150, 0, 500, 50, this);

    private final ModeValue modeValue = new ModeValue("Mode", new String[]{"Basic", "OpenInv"},  "Basic", this);

    public final TimerUtils timerUtil = new TimerUtils();
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
    public void onPreMotionEvent(MotionEvent event) {
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

            int helmet = -1;
            int chestplate = -1;
            int leggings = -1;
            int boots = -1;

            int sword = -1;
            int pickaxe = -1;
            int axe = -1;
            int block = -1;
            int potion = -1;
            int food = -1;

            int ARMOR_SLOTS = 4;
            int INVENTORY_ROWS = 4;
            int INVENTORY_COLUMNS = 9;
            int INVENTORY_SLOTS = (INVENTORY_ROWS * INVENTORY_COLUMNS) + ARMOR_SLOTS;

            for (int i = 0; i < INVENTORY_SLOTS; i++) {
                final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

                if (stack == null) {
                    continue;
                }

                final Item item = stack.getItem();

                if (item instanceof ItemFood) {
                    if (item != Item.getItemById(322) && item != Item.getItemById(466)) {
                        this.throwItem(i);
                        continue;
                    }
                }

                if (!InventoryUtils.isValid(stack)) {
                    this.throwItem(i);
                    continue;
                }

                if (autoArmor.get() && item instanceof ItemArmor armor) {
                    final int reduction = this.armorReduction(stack);
                    int currentBestSlot;
                    int currentBestReduction;

                    switch (armor.armorType) {
                        case 0: // 头盔
                            currentBestSlot = helmet;
                            currentBestReduction = helmet == -1 ? -1 : armorReduction(mc.thePlayer.inventory.getStackInSlot(helmet));
                            if (currentBestSlot == -1 || reduction > currentBestReduction) {
                                helmet = i;
                            }
                            break;

                        case 1: // 胸甲
                            currentBestSlot = chestplate;
                            currentBestReduction = chestplate == -1 ? -1 : armorReduction(mc.thePlayer.inventory.getStackInSlot(chestplate));
                            if (currentBestSlot == -1 || reduction > currentBestReduction) {
                                chestplate = i;
                            }
                            break;

                        case 2: // 护腿
                            currentBestSlot = leggings;
                            currentBestReduction = leggings == -1 ? -1 : armorReduction(mc.thePlayer.inventory.getStackInSlot(leggings));
                            if (currentBestSlot == -1 || reduction > currentBestReduction) {
                                leggings = i;
                            }
                            break;

                        case 3: // 靴子
                            currentBestSlot = boots;
                            currentBestReduction = boots == -1 ? -1 : armorReduction(mc.thePlayer.inventory.getStackInSlot(boots));
                            if (currentBestSlot == -1 || reduction > currentBestReduction) {
                                boots = i;
                            }
                            break;
                    }
                    continue;
                }

                if (item instanceof ItemSpade) {
                    this.throwItem(i);
                    continue;
                }

                if (item instanceof ItemSword) {
                    boolean hasFireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) > 0;
                    int durability = stack.getMaxDamage() - stack.getItemDamage();

                    if (sword == -1) {
                        sword = i;
                    } else {
                        ItemStack currentBest = mc.thePlayer.inventory.getStackInSlot(sword);
                        boolean currentHasFireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, currentBest) > 0;
                        int currentDurability = currentBest.getMaxDamage() - currentBest.getItemDamage();

                        if (currentDurability <= 10 && durability > currentDurability) {
                            sword = i;
                        } else if (hasFireAspect && !currentHasFireAspect) {
                            sword = i;
                        } else if (hasFireAspect == currentHasFireAspect && damage(stack) > damage(currentBest)) {
                            sword = i;
                        }
                    }
                    continue;
                }

                if (item instanceof ItemPickaxe) {
                    if (pickaxe == -1) {
                        pickaxe = i;
                    } else if (mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(pickaxe))) {
                        this.throwItem(pickaxe);
                        pickaxe = i;
                    } else {
                        this.throwItem(i);
                    }
                }

                if (item instanceof ItemAxe) {
                    if (axe == -1) {
                        axe = i;
                    } else if (mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(axe))) {
                        this.throwItem(axe);
                        axe = i;
                    } else {
                        this.throwItem(i);
                    }
                }

                if (item instanceof ItemBlock) {
                    if (block == -1) {
                        final ItemStack blockStack = mc.thePlayer.inventory.getStackInSlot((int) (this.blockSlot.get() - 1));

                        if (blockStack == null || !(blockStack.getItem() instanceof ItemBlock)) {
                            block = i;
                        } else {
                            block = (int) (this.blockSlot.get() - 1);
                        }
                    }

                    final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(block);
                    if (currentStack != null && stack.stackSize > currentStack.stackSize) {
                        block = i;
                    }
                    continue;
                }

                if (item instanceof ItemPotion itemPotion) {
                    if (potion == -1) {
                        ItemStack potionStack = mc.thePlayer.inventory.getStackInSlot((int) (this.potionSlot.get() - 1));
                        potion = (potionStack == null || !(potionStack.getItem() instanceof ItemPotion)) ? i : (int) (this.potionSlot.get() - 1);
                    }

                    ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(potion);
                    if (currentStack != null) {
                        ItemPotion currentItemPotion = (ItemPotion) currentStack.getItem();
                        int currentRank = PlayerUtils.potionRanking(currentItemPotion.getEffects(currentStack).get(0).getPotionID());
                        int newRank = PlayerUtils.potionRanking(itemPotion.getEffects(stack).get(0).getPotionID());

                        if (newRank > currentRank) {
                            potion = i;
                        }
                    }
                    continue;
                }

                if (item instanceof ItemFood itemFood) {
                    if (food == -1) {
                        ItemStack foodStack = mc.thePlayer.inventory.getStackInSlot((int) (this.gappleSlot.get() - 1));
                        food = (foodStack == null || !(foodStack.getItem() instanceof ItemFood)) ? i : (int) (this.gappleSlot.get() - 1);
                    }

                    ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(food);
                    if (currentStack != null) {
                        ItemFood currentItemFood = (ItemFood) currentStack.getItem();
                        if (itemFood.getSaturationModifier(stack) > currentItemFood.getSaturationModifier(currentStack)) {
                            food = i;
                        }
                    }
                }
            }

            if (autoArmor.get()) {
                for (int i = 0; i < INVENTORY_SLOTS; i++) {
                    final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack == null || !(stack.getItem() instanceof ItemArmor armor)) continue;

                    switch (armor.armorType) {
                        case 0: if (i != helmet) this.throwItem(i); break;
                        case 1: if (i != chestplate) this.throwItem(i); break;
                        case 2: if (i != leggings) this.throwItem(i); break;
                        case 3: if (i != boots) this.throwItem(i); break;
                    }
                }
            }

            if (autoArmor.get()) {
                if (helmet != -1 && helmet != 39) this.equipItem(helmet);
                if (chestplate != -1 && chestplate != 38) this.equipItem(chestplate);
                if (leggings != -1 && leggings != 37) this.equipItem(leggings);
                if (boots != -1 && boots != 36) this.equipItem(boots);
            }

            if (sword != -1) {
                ItemStack currentSword = mc.thePlayer.inventory.getStackInSlot(sword);
                int currentDurability = currentSword.getMaxDamage() - currentSword.getItemDamage();

                if (currentDurability <= 3) {
                    for (int i = 0; i < 36; i++) {
                        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                        if (stack != null && stack.getItem() instanceof ItemSword && i != sword) {
                            this.moveItem(i, (int) (this.swordSlot.get() - 37));
                            break;
                        }
                    }
                }

                if (sword != this.swordSlot.get() - 1) {
                    this.moveItem(sword, (int) (this.swordSlot.get() - 37));
                }
            }

            if (pickaxe != -1 && pickaxe != this.pickaxeSlot.get() - 1) {
                this.moveItem(pickaxe, (int) (this.pickaxeSlot.get() - 37));
            }

            if (axe != -1 && axe != this.axeSlot.get() - 1) {
                this.moveItem(axe, (int) (this.axeSlot.get() - 37));
            }

            if (block != -1 && block != this.blockSlot.get() - 1 && !isEnabled(Scaffold.class))
                this.moveItem(block, (int) (this.blockSlot.get() - 37));

            if (potion != -1 && potion != this.potionSlot.get() - 1) {
                this.moveItem(potion, (int) (this.potionSlot.get() - 37));
            }

            if (food != -1 && food != this.gappleSlot.get() - 1) {
                this.moveItem(food, (int) (this.gappleSlot.get() - 37));
            }

            if (this.canOpenInventory() && !this.moved) {
                this.closeInventory();
            }
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
        this.nextClick = Math.round((float) MathUtils.getRandom((int) this.delay.get(), (int) this.delay.get()));
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