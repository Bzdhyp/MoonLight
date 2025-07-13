package wtf.moonlight.module.impl.player;

import com.cubk.EventTarget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MovementCorrection;
import wtf.moonlight.util.player.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "Clutch", category = Categor.Player)
public class Clutch extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Manual", "Predictive", "Auto Disable"}, "Manual", this);
    public final SliderValue searchDist = new SliderValue("Search Distance", 4.0f, 1, 6f, .6f, this);
    private final BoolValue silent = new BoolValue("Silent", true, this);
    private final BoolValue movefix = new BoolValue("Movement Fix", true, this, silent::get);
    private final BoolValue forceRotation = new BoolValue("Force Rotation", true, this); // 新增强制旋转选项

    private static float[] currentTargetRotation = null;
    private static boolean isClutching = false;
    private boolean wasSprinting = false;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (isEnabled(Scaffold.class)) return;

        if (mc.thePlayer.onGround && this.mode.is("Auto Disable")) {
            toggle();
            return;
        }

        if ((!this.mode.is("Predictive") || (PlayerUtil.isOverVoid() && PlayerUtil.isOverVoid(
                mc.thePlayer.posX + (mc.thePlayer.motionX * 7.0d),
                mc.thePlayer.posY + MovementUtil.predictedSumMotion(mc.thePlayer.motionY, 7),
                mc.thePlayer.posZ + (mc.thePlayer.motionZ * 7.0d)) && !mc.thePlayer.onGround))) {

            wasSprinting = mc.thePlayer.isSprinting();

            if (startSearch() && pickBlock()) {
                if (wasSprinting) {
                    mc.thePlayer.setSprinting(false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
                }

                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
            } else if (wasSprinting) {
                mc.thePlayer.setSprinting(true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            }
        }
    }

    private boolean startSearch() {
        BlockPos below = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0d, mc.thePlayer.posZ);

        if (!BlockUtil.isReplaceable(below)) return false;

        List<BlockPos> searchQueue = new ArrayList<>();
        searchQueue.add(below.down());
        int dist = this.searchDist.getValue().intValue();

        for (int x = -dist; x <= dist; x++) {
            for (int z = -dist; z <= dist; z++) {
                searchQueue.add(below.add(x, 0, z));
            }
        }

        searchQueue.sort(Comparator.comparingDouble(BlockUtil::getClutchPriority));

        for (BlockPos blockPos : searchQueue) {
            if (searchBlock(blockPos)) {
                isClutching = true;
                return true;
            }
        }

        for (BlockPos blockPos : searchQueue) {
            if (searchBlock(blockPos.down())) {
                isClutching = true;
                return true;
            }
        }

        for (BlockPos blockPos : searchQueue) {
            if (searchBlock(blockPos.down().down())) {
                isClutching = true;
                return true;
            }
        }

        isClutching = false;
        return false;
    }

    private boolean searchBlock(BlockPos block) {
        if (!BlockUtil.isReplaceable(block)) {
            EnumFacing placeFace = BlockUtil.getHorizontalFacingEnum(block, mc.thePlayer.posX, mc.thePlayer.posZ);

            if (block.getY() <= mc.thePlayer.posY - 3.0d) placeFace = EnumFacing.UP;

            BlockPos blockPlacement = block.add(placeFace.getDirectionVec());

            if (!BlockUtil.isReplaceable(blockPlacement)) return false;

            mc.thePlayer.posX += mc.thePlayer.motionX;
            mc.thePlayer.posY += mc.thePlayer.motionY;
            mc.thePlayer.posZ += mc.thePlayer.motionZ;

            float[] targetRotation = BlockUtil.getFaceRotation(placeFace, block);

            // 强制旋转或需要更新旋转时执行
            if (forceRotation.get() || currentTargetRotation == null || !isRotationCloseEnough(targetRotation)) {
                currentTargetRotation = targetRotation;
                if (!silent.get()) {
                    setPlayerRotation(targetRotation);
                } else {
                    RotationUtil.setRotation(targetRotation, movefix.get() ? MovementCorrection.SILENT : MovementCorrection.OFF);
                }
            }

            mc.thePlayer.posX -= mc.thePlayer.motionX;
            mc.thePlayer.posY -= mc.thePlayer.motionY;
            mc.thePlayer.posZ -= mc.thePlayer.motionZ;
            return true;
        }
        currentTargetRotation = null;
        return false;
    }

    public static void setPlayerRotation(float[] targetRotation) {
        float[] targetRotation2 = applyGCD(targetRotation, new float[]{mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch});
        mc.thePlayer.rotationYaw = targetRotation2[0];
        mc.thePlayer.rotationPitch = targetRotation2[1];
    }

    public static float[] applyGCD(float[] rotations, float[] prevRots) {
        float mouseSensitivity = (float) ((mc.gameSettings.mouseSensitivity * (1.0d + (Math.random() / 1.0E7d)) * 0.6000000238418579d) + 0.20000000298023224d);
        double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0f * 0.15d;
        float yaw = prevRots[0] + ((float) (Math.round((rotations[0] - prevRots[0]) / multiplier) * multiplier));
        float pitch = prevRots[1] + ((float) (Math.round((rotations[1] - prevRots[1]) / multiplier) * multiplier));
        return new float[]{yaw, MathHelper.clamp_float(pitch, -90.0f, 90.0f)};
    }

    private boolean pickBlock() {
        int slot = InventoryUtil.pickHotarBlock(true);

        if (slot != -1) {
            mc.thePlayer.inventory.currentItem = slot;
            return true;
        }

        return false;
    }

    private boolean isRotationCloseEnough(float[] target) {
        if (currentTargetRotation == null) return false;
        float yawDiff = Math.abs(mc.thePlayer.rotationYaw - target[0]);
        float pitchDiff = Math.abs(mc.thePlayer.rotationPitch - target[1]);
        return yawDiff < 5.0f && pitchDiff < 5.0f;
    }

    public static boolean isClutching() {
        return isClutching;
    }

    public static void resetTargetRotation() {
        currentTargetRotation = null;
    }

    @Override
    public void onDisable() {
        resetTargetRotation();
        isClutching = false;
        if (wasSprinting) {
            mc.thePlayer.setSprinting(true);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }
}