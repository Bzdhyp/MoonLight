/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package wtf.moonlight.util.player;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import wtf.moonlight.module.impl.combat.AutoProjectile;
import wtf.moonlight.module.impl.combat.AutoRod;
import wtf.moonlight.util.misc.InstanceAccess;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class InventoryUtil implements InstanceAccess {
    public static final int INCLUDE_ARMOR_BEGIN = 5;
    public static final int EXCLUDE_ARMOR_BEGIN = 9;
    public static final int END = 45;

    public static void forEachInventorySlot(final int begin, final int end, final SlotConsumer consumer) {
        for (int i = begin; i < end; ++i) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null) {
                consumer.accept(i, stack);
            }
        }
    }

    private static final IntSet BAD_EFFECTS_IDS = IntSet.of(
            Potion.poison.id, Potion.weakness.id, Potion.wither.id, Potion.blindness.id, Potion.digSlowdown.id, Potion.harm.id
    );

    public static boolean isBuffPotion(final ItemStack stack) {
        final ItemPotion potion = (ItemPotion) stack.getItem();
        final List<PotionEffect> effects = potion.getEffects(stack);
        if (effects.isEmpty()) {
            return false;
        }
        for (final PotionEffect effect : effects) {
            if (BAD_EFFECTS_IDS.contains(effect.getPotionID())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isGoodFood(final ItemStack stack) {
        final ItemFood food = (ItemFood) stack.getItem();
        return food instanceof ItemAppleGold || (food.getHealAmount(stack) >= 4 && food.getSaturationModifier(stack) >= 0.3f);
    }

    public static boolean isBestSword(ItemStack itemStack) {
        AtomicDouble damage = new AtomicDouble(0.0);
        AtomicReference<ItemStack> bestStack = new AtomicReference<>(null);

        forEachInventorySlot(EXCLUDE_ARMOR_BEGIN, END, (slot, stack) -> {
            if (stack.getItem() instanceof ItemSword) {
                double newDamage = getItemDamage(stack);

                if (newDamage > damage.get()) {
                    damage.set(newDamage);
                    bestStack.set(stack);
                }
            }
        });

        return bestStack.get() == itemStack || damage.get() < getItemDamage(itemStack);
    }

    public static int getToolType(final ItemStack stack) {
        final ItemTool tool = (ItemTool) stack.getItem();
        if (tool instanceof ItemPickaxe) {
            return 0;
        }
        if (tool instanceof ItemAxe) {
            return 1;
        }
        if (tool instanceof ItemSpade) {
            return 2;
        }
        return -1;
    }

    public static float getToolEfficiency(ItemStack itemStack) {
        float efficiency = 4;

        int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);

        if (lvl > 0)
            efficiency += lvl * lvl + 1;

        return efficiency;
    }

    public static double getItemDamage(final ItemStack stack) {
        double damage = 0.0;
        final Multimap<String, AttributeModifier> attributeModifierMap = stack.getAttributeModifiers();
        for (final String attributeName : attributeModifierMap.keySet()) {
            if (attributeName.equals("generic.attackDamage")) {
                final Iterator<AttributeModifier> attributeModifiers = attributeModifierMap.get(attributeName).iterator();
                if (attributeModifiers.hasNext()) {
                    damage += attributeModifiers.next().getAmount();
                    break;
                }
                break;
            }
        }
        if (stack.isItemEnchanted()) {
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25;
        }
        return damage;
    }

    public static double getDamageReduction(ItemStack stack) {
        double reduction = 0.0;

        ItemArmor armor = (ItemArmor) stack.getItem();
        reduction += armor.damageReduceAmount;
        if (stack.isItemEnchanted()) {
            reduction += EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.25D;
        }
        return reduction;
    }

    public static boolean isGoodBlockStack(final ItemStack stack) {
        return stack.stackSize >= 1 && isValidBlock(Block.getBlockFromItem(stack.getItem()), true);
    }

    public static boolean isValidBlock(final Block block, final boolean toPlace) {
        if (block instanceof BlockContainer || block instanceof BlockTNT || !block.isFullBlock() || !block.isFullCube() || (toPlace && block instanceof BlockFalling)) {
            return false;
        }
        final Material material = block.getMaterial();
        return !material.isLiquid() && material.isSolid();
    }

    public static boolean isBestArmor(ItemStack itemStack) {
        ItemArmor itemArmor = (ItemArmor) itemStack.getItem();
        AtomicDouble reduction = new AtomicDouble(0.0);
        AtomicReference<ItemStack> bestStack = new AtomicReference<>(null);
        forEachInventorySlot(InventoryUtil.INCLUDE_ARMOR_BEGIN, InventoryUtil.END, ((slot, stack) -> {
            if (stack.getItem() instanceof ItemArmor stackArmor) {
                if (stackArmor.armorType == itemArmor.armorType) {
                    double newReduction = getDamageReduction(stack);

                    if (newReduction > reduction.get()) {
                        reduction.set(newReduction);
                        bestStack.set(stack);
                    }
                }
            }
        }));

        return bestStack.get() == itemStack ||
                reduction.get() < getDamageReduction(itemStack);
    }

    public static boolean isBestTool(ItemStack itemStack) {
        final int type = getToolType(itemStack);

        AtomicReference<Tool> bestTool = new AtomicReference<>(new Tool(-1, -1, null));

        forEachInventorySlot(InventoryUtil.EXCLUDE_ARMOR_BEGIN, InventoryUtil.END, ((slot, stack) -> {
            if (stack.getItem() instanceof ItemTool && type == getToolType(stack)) {
                double efficiency = getToolEfficiency(stack);
                if (efficiency > bestTool.get().efficiency())
                    bestTool.set(new Tool(slot, efficiency, stack));
            }
        }));

        return bestTool.get().stack() == itemStack ||
                bestTool.get().efficiency() < getToolEfficiency(itemStack);
    }

    public static boolean isGoodItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBucket)
            if (((ItemBucket) item).isFull != Blocks.flowing_water)
                return false;

        return !(item instanceof ItemExpBottle) &&
                (!(item instanceof ItemEgg) && !(item instanceof ItemSnowball) && !INSTANCE.getModuleManager().getModule(AutoProjectile.class).isEnabled() ||
                        INSTANCE.getModuleManager().getModule(AutoProjectile.class).isEnabled()) &&
                (!(item instanceof ItemFishingRod) && !INSTANCE.getModuleManager().getModule(AutoRod.class).isEnabled() ||
                        INSTANCE.getModuleManager().getModule(AutoRod.class).isEnabled()) &&
                !(item instanceof ItemSkull) && !(item instanceof ItemBucket);
    }

    public static boolean isValid(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getItem() instanceof ItemBlock) {
            return isGoodBlockStack(stack);
        } else if (stack.getItem() instanceof ItemSword) {
            return isBestSword(stack);
        } else if (stack.getItem() instanceof ItemTool) {
            return isBestTool(stack);
        } else if (stack.getItem() instanceof ItemArmor) {
            return isBestArmor(stack);
        } else if (stack.getItem() instanceof ItemPotion) {
            return isBuffPotion(stack);
        } else if (stack.getItem() instanceof ItemFood) {
            return isGoodFood(stack);
        } else if (stack.getItem() instanceof ItemEnderPearl) {
            return true;
        } else return isGoodItem(stack);
    }

    public static boolean isInventoryEmpty(IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (InventoryUtil.isValid(inventory.getStackInSlot(i)))
                return false;
        }
        return true;
    }

    public static boolean isInventoryFull() {
        for (int i = 9; i < 45; i++) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack())
                return false;
        }
        return true;
    }

    public static int getEnchantment(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack == null || itemStack.getEnchantmentTagList() == null || itemStack.getEnchantmentTagList().hasNoTags())
            return 0;

        for (int i = 0; i < itemStack.getEnchantmentTagList().tagCount(); i++) {
            final NBTTagCompound tagCompound = itemStack.getEnchantmentTagList().getCompoundTagAt(i);

            if ((tagCompound.hasKey("ench") && tagCompound.getShort("ench") == enchantment.effectId) || (tagCompound.hasKey("id") && tagCompound.getShort("id") == enchantment.effectId))
                return tagCompound.getShort("lvl");
        }

        return 0;
    }

    @FunctionalInterface
    public interface SlotConsumer {
        void accept(final int p0, final ItemStack p1);
    }

    public record Tool(int slot, double efficiency, ItemStack stack) {
    }
}
