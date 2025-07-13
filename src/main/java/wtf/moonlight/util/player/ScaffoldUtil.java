package wtf.moonlight.util.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import wtf.moonlight.Client;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.util.MathUtil;
import wtf.moonlight.util.misc.InstanceAccess;

import java.util.Arrays;
import java.util.List;

public class ScaffoldUtil implements InstanceAccess {
    public static boolean canBePlacedOn(final BlockPos blockPos) {
        final Material material = mc.theWorld.getBlockState(blockPos).getBlock().getMaterial();
        return (material.blocksMovement() && material.isSolid() && !(PlayerUtil.getBlock(blockPos) instanceof BlockAir));
    }

    public static Vec3 getHitVecOptimized(BlockPos blockPos, EnumFacing facing) {
        Vec3 eyes = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        return MathUtil.closestPointOnFace(new AxisAlignedBB(blockPos, blockPos.add(1, 1, 1)), facing, eyes);
    }

    private static boolean isBlockValid(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock)) return false;
        Block block = ((ItemBlock) stack.getItem()).getBlock();
        return !blacklistedBlocks.contains(block);
    }

    public static int getBlockCount() {
        int blockCount = 0;

        for (int i = 36; i < 45; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue;

            ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (!(is.getItem() instanceof ItemBlock && !blacklistedBlocks.contains(((ItemBlock) is.getItem()).getBlock()))) {
                continue;
            }

            blockCount += is.stackSize;
        }

        return blockCount;
    }

    private static int lastSelectedSlot = -1;
    private static long lastSwitchTime = 0;

    public static int getBlockSlot() {
        int slot = -1;
        int size = 0;
        final int minSwitchThreshold = 4;

        if (ScaffoldUtil.getBlockCount() == 0) {
            return -1;
        }

        if (!Client.INSTANCE.getModuleManager().getModule(Scaffold.class).biggestStack.get()) {
            for (int i = 36; i < 45; i++) {
                Slot s = mc.thePlayer.inventoryContainer.getSlot(i);
                if (s.getHasStack() && ScaffoldUtil.isBlockValid(s.getStack())) {
                    return i - 36;
                }
            }
            return -1;
        }

        for (int i = 36; i < 45; i++) {
            Slot s = mc.thePlayer.inventoryContainer.getSlot(i);
            if (!s.getHasStack()) continue;

            ItemStack stack = s.getStack();
            if (!ScaffoldUtil.isBlockValid(stack)) continue;

            int stackSize = stack.stackSize;

            if (i - 36 == lastSelectedSlot && stackSize > minSwitchThreshold) {
                return lastSelectedSlot;
            }

            if (stackSize > size && (size <= minSwitchThreshold || lastSelectedSlot == -1)) {
                size = stackSize;
                slot = i;
            }
        }

        long currentTime = System.currentTimeMillis();
        if (slot != -1 && (currentTime - lastSwitchTime > 200 || lastSelectedSlot == -1)) {
            lastSelectedSlot = slot - 36;
            lastSwitchTime = currentTime;
            return lastSelectedSlot;
        } else {
            return lastSelectedSlot != -1 ? lastSelectedSlot : 0;
        }
    }

    public static Scaffold.PlaceData getPlaceData(final BlockPos pos) {
        EnumFacing[] horizontalFacings = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH};
        EnumFacing[] allFacings = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP};

        for (EnumFacing facing : horizontalFacings) {
            final BlockPos blockPos = pos.add(facing.getOpposite().getDirectionVec());
            if (canBePlacedOn(blockPos)) {
                return new Scaffold.PlaceData(blockPos, facing);
            }
        }

        for (EnumFacing facing : allFacings) {
            final BlockPos blockPos = pos.add(facing.getOpposite().getDirectionVec());
            for (EnumFacing facing1 : allFacings) {
                final BlockPos blockPos1 = blockPos.add(facing1.getOpposite().getDirectionVec());
                if (canBePlacedOn(blockPos1)) {
                    return new Scaffold.PlaceData(blockPos1, facing1);
                }
            }
        }

        final BlockPos posBelow = pos.add(0, -1, 0);
        if (canBePlacedOn(posBelow)) {
            return new Scaffold.PlaceData(posBelow, EnumFacing.UP);
        }

        return null;
    }

    static final List<Block> blacklistedBlocks = Arrays.asList(Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.wooden_slab, Blocks.chest, Blocks.flowing_lava,
            Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.skull, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice,
            Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.trapped_chest, Blocks.torch, Blocks.anvil,
            Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore,
            Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate,
            Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower,
            Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand, Blocks.cactus,
            Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall,
            Blocks.oak_fence, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.redstone_torch, Blocks.acacia_stairs,
            Blocks.birch_stairs, Blocks.brick_stairs, Blocks.dark_oak_stairs, Blocks.jungle_stairs, Blocks.nether_brick_stairs, Blocks.oak_stairs,
            Blocks.quartz_stairs, Blocks.red_sandstone_stairs, Blocks.sandstone_stairs, Blocks.spruce_stairs, Blocks.stone_brick_stairs, Blocks.stone_stairs,
            Blocks.double_wooden_slab, Blocks.stone_slab, Blocks.double_stone_slab, Blocks.stone_slab2, Blocks.double_stone_slab2,
            Blocks.web, Blocks.gravel, Blocks.daylight_detector_inverted, Blocks.daylight_detector, Blocks.soul_sand, Blocks.piston, Blocks.piston_extension,
            Blocks.piston_head, Blocks.sticky_piston, Blocks.iron_trapdoor, Blocks.ender_chest, Blocks.end_portal, Blocks.end_portal_frame, Blocks.standing_banner,
            Blocks.wall_banner, Blocks.deadbush, Blocks.slime_block, Blocks.acacia_fence_gate, Blocks.birch_fence_gate, Blocks.dark_oak_fence_gate,
            Blocks.jungle_fence_gate, Blocks.spruce_fence_gate, Blocks.oak_fence_gate);
}
