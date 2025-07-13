package wtf.moonlight.module.impl.player;

import com.cubk.EventTarget;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.lwjglx.util.vector.Vector2f;
import wtf.moonlight.component.BadPacketsComponent;
import wtf.moonlight.component.SlotComponent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MathUtil;
import wtf.moonlight.util.packet.PacketUtils;
import wtf.moonlight.util.MovementCorrection;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RaytraceUtil;
import wtf.moonlight.util.player.RotationUtil;
import wtf.moonlight.util.vector.Vector3d;

@ModuleInfo(name = "Clutch", category = Categor.Player)
public class Clutch extends Module {
    private final BoolValue movefix = new BoolValue("Movement Fix", true, this);

    private final SliderValue minPlaceDelay = new SliderValue("Max Place Delay", 0, 0, 1, this);
    private final SliderValue maxPlaceDelay = new SliderValue("Max Place Delay", 0, 0, 1, this);

    private final SliderValue minRotationSpeed = new SliderValue("Min Rotation Speed", 5, 0, 10, this);
    private final SliderValue maxRotationSpeed = new SliderValue("Max Rotation Speed", 5, 0, 500, this);

    private Vec3 targetBlock;
    private EnumFacingOffset enumFacing;
    private BlockPos blockFace;
    private float targetYaw, targetPitch;
    private int ticksOnAir;
    private int toggle;

    @Getter
    public static class EnumFacingOffset {
        public EnumFacing enumFacing;
        private final Vec3 offset;

        public EnumFacingOffset(final EnumFacing enumFacing, final Vec3 offset) {
            this.enumFacing = enumFacing;
            this.offset = offset;
        }
    }

    @Override
    public void onEnable() {
        targetYaw = mc.thePlayer.rotationYaw - 180;
        targetPitch = 90;

        targetBlock = null;
    }

    public void calculateRotations() {
        if (ticksOnAir > 0 && !RaytraceUtil.overBlock(RotationUtil.currentRotation, enumFacing.getEnumFacing(), blockFace, true)) {
            getRotations(0);
        }

        /* Smoothing rotations */
        final double minRotationSpeed = this.minRotationSpeed.getValue().doubleValue();
        final double maxRotationSpeed = this.maxRotationSpeed.getValue().doubleValue();
        float rotationSpeed = (float) MathUtil.getRandom(minRotationSpeed, maxRotationSpeed);

        if (rotationSpeed != 0) {
            RotationUtil.setRotation(new float[]{targetYaw, targetPitch}, movefix.get() ? MovementCorrection.SILENT : MovementCorrection.OFF);
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event){
        if (mc.thePlayer.ticksSinceTeleport <= 15 || mc.thePlayer.ticksExisted <= 50 ||
                BadPacketsComponent.bad() || getModule(Scaffold.class).isEnabled() ||
                (!mc.gameSettings.keyBindSneak.isKeyDown())) return;

        if (mc.thePlayer.offGroundTicks > 3 && !PlayerUtil.isBlockUnder(10)) {
            toggle = 10;
        }

        if (toggle-- < 0) return;

        // Getting ItemSlot
        SlotComponent.setSlot(PlayerUtil.findBlock());

        final Vec3i offset = new Vec3i(0, 0, 0);

        //Used to detect when to place a block, if over air, allow placement of blocks
        if (PlayerUtil.blockRelativeToPlayer(offset.getX(), -1 + offset.getY(), offset.getZ()).isReplaceable(mc.theWorld, new BlockPos(mc.thePlayer).down())) {
            ticksOnAir++;
        } else {
            ticksOnAir = 0;
        }

        // Gets block to place
        targetBlock = PlayerUtil.getPlacePossibility(offset.getX(), offset.getY(), offset.getZ());

        if (targetBlock == null) {
            return;
        }

        //Gets EnumFacing
        enumFacing = PlayerUtil.getEnumFacing(targetBlock);

        if (enumFacing == null) {
            return;
        }

        final BlockPos position = new BlockPos(targetBlock.xCoord, targetBlock.yCoord, targetBlock.zCoord);

        blockFace = position.add(enumFacing.getOffset().xCoord, enumFacing.getOffset().yCoord, enumFacing.getOffset().zCoord);

        if (blockFace == null || enumFacing == null) {
            return;
        }

        this.calculateRotations();

        if (targetBlock == null || enumFacing == null || blockFace == null) {
            return;
        }

        if (mc.thePlayer.inventory.alternativeCurrentItem == SlotComponent.getItemIndex()) {
            if (!BadPacketsComponent.bad(false, true, false, false, true) &&
                    ticksOnAir > MathUtil.getRandom(minPlaceDelay.getValue().intValue(), maxPlaceDelay.getValue().intValue()) &&
                    (RaytraceUtil.overBlock(enumFacing.getEnumFacing(), blockFace, true))) {

                Vec3 hitVec = RaytraceUtil.rayCast(new Vector2f(RotationUtil.currentRotation[0], RotationUtil.currentRotation[1]), mc.playerController.getBlockReachDistance()).hitVec;

                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, SlotComponent.getItemStack(), blockFace, enumFacing.getEnumFacing(), hitVec)) {
                    PacketUtils.sendPacket(new C0APacketAnimation());
                }

                mc.rightClickDelayTimer = 0;
                ticksOnAir = 0;

                assert SlotComponent.getItemStack() != null;
                if (SlotComponent.getItemStack() != null && SlotComponent.getItemStack().stackSize == 0) {
                    mc.thePlayer.inventory.mainInventory[SlotComponent.getItemIndex()] = null;
                }
            } else if (Math.random() > 0.92 && mc.rightClickDelayTimer <= 0) {
                PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
                mc.rightClickDelayTimer = 0;
            }
        }
    }

    public void getRotations(final int yawOffset) {

        EntityPlayer entityPlayer = mc.thePlayer;
        double difference = entityPlayer.posY + entityPlayer.getEyeHeight() - targetBlock.yCoord - 0.1 - Math.random() * 0.8;

        MovingObjectPosition movingObjectPosition;

        for (int offset = -180 + yawOffset; offset <= 180; offset += 45) {
            entityPlayer.setPosition(entityPlayer.posX, entityPlayer.posY - difference, entityPlayer.posZ);
            movingObjectPosition = RaytraceUtil.rayCast(new Vector2f(entityPlayer.rotationYaw + offset, 0), 4.5);
            entityPlayer.setPosition(entityPlayer.posX, entityPlayer.posY + difference, entityPlayer.posZ);

            if (movingObjectPosition != null && new BlockPos(blockFace).equals(movingObjectPosition.getBlockPos()) &&
                    enumFacing.getEnumFacing() == movingObjectPosition.sideHit) {
                Vector2f rotations = RotationUtil.calculate(movingObjectPosition.hitVec);

                targetYaw = rotations.x;
                targetPitch = rotations.y;
                return;
            }
        }

        // Backup Rotations
        final Vector2f rotations = RotationUtil.calculate(
                new Vector3d(blockFace.getX(), blockFace.getY(), blockFace.getZ()), enumFacing.getEnumFacing());

        targetYaw = rotations.x;
        targetPitch = rotations.y;
    }
}