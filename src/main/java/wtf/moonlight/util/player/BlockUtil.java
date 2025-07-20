package wtf.moonlight.util.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.util.*;
import wtf.moonlight.util.misc.InstanceAccess;

public class BlockUtil implements InstanceAccess {
    public static boolean isReplaceable(BlockPos blockPos) {
        Material material = getMaterial(blockPos);
        return material != null && material.isReplaceable();
    }

    public static float[] getFaceRotation(EnumFacing face, BlockPos blockPos) {
        Vec3i faceVec = face.getDirectionVec();
        Vec3 blockFaceVec = new Vec3(faceVec.getX() * 0.5d, faceVec.getY() * 0.5d, faceVec.getZ() * 0.5d);
        return RotationUtil.getRotations(blockFaceVec.add(blockPos.toVec3()).addVector(0.5d, 0.5d, 0.5d));
    }

    public static Block getBlock(BlockPos blockPos) {
        if (mc.theWorld != null && blockPos != null) {
            return mc.theWorld.getBlockState(blockPos).getBlock();
        }
        return null;
    }

    public static boolean isAirOrLiquid(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();

        return block instanceof BlockAir || block instanceof BlockLiquid;
    }

    public static Material getMaterial(BlockPos blockPos) {
        Block block = getBlock(blockPos);
        if (block != null) {
            return block.getMaterial();
        }
        return null;
    }

    public static double getClutchPriority(BlockPos blockPos) {
        return getCenterDistance(blockPos) + (Math.abs(MathHelper.wrapAngleTo180_double(getCenterRotation(blockPos)[0] - mc.thePlayer.rotationYaw)) / 130.0d);
    }

    public static double getCenterDistance(BlockPos blockPos) {
        return mc.thePlayer.getDistance(blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d);
    }

    public static float[] getCenterRotation(BlockPos blockPos) {
        return RotationUtil.getRotations(blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d);
    }

    public static EnumFacing getHorizontalFacingEnum(BlockPos blockPos, double x, double z) {
        double dx = x - (blockPos.getX() + 0.5d);
        double dz = z - (blockPos.getZ() + 0.5d);
        if (dx > 0.0d) {
            if (dz > dx) {
                return EnumFacing.SOUTH;
            }
            if ((-dz) > dx) {
                return EnumFacing.NORTH;
            }
            return EnumFacing.EAST;
        }
        if (dz > (-dx)) {
            return EnumFacing.SOUTH;
        }
        if (dz < dx) {
            return EnumFacing.NORTH;
        }
        return EnumFacing.WEST;
    }
}
