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
package wtf.moonlight.module.impl.movement;
import lombok.AllArgsConstructor;
import net.minecraft.block.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjglx.input.Keyboard;
import com.cubk.EventTarget;
import wtf.moonlight.component.BlinkComponent;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.*;
import wtf.moonlight.events.render.Render3DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.combat.KillAura;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.MultiBoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MathUtil;
import wtf.moonlight.component.SpoofSlotComponent;
import wtf.moonlight.util.MovementCorrection;
import wtf.moonlight.util.player.*;
import wtf.moonlight.util.render.RenderUtil;

import java.util.*;

@ModuleInfo(name = "Scaffold", category = Categor.Movement)
public class Scaffold extends Module {
    private final ListValue switchBlock = new ListValue("Switch Block", new String[]{"Silent", "Switch", "Spoof"}, "Spoof", this);
    public final BoolValue biggestStack = new BoolValue("Biggest Stack", false, this);
    private final ListValue mode = new ListValue("Mode", new String[]{"Normal", "Telly", "Snap", "Watchdog"}, "Normal", this);
    private final SliderValue minTellyTicks = new SliderValue("Min Telly Ticks", 2, 1, 5, this, () -> mode.is("Telly"));
    private final SliderValue maxTellyTicks = new SliderValue("Max Telly Ticks", 4, 1, 5, this, () -> mode.is("Telly"));
    private final ListValue rotations = new ListValue("Rotations", new String[]{"Normal", "Normal 2","Normal 3", "Back", "Strict", "God Bridge", "Reverse", "Custom", "Unfair Pitch", "Hypixel", "Derp"}, "Normal", this);
    private final SliderValue customYaw = new SliderValue("Custom Yaw", 180, 0, 180, 1, this, () -> rotations.is("Custom"));
    private final SliderValue minPitch = new SliderValue("Min Pitch Range", 55, 50, 90, .1f, this, () -> rotations.is("Custom") || rotations.is("God Bridge"));
    public final SliderValue maxPitch = new SliderValue("Max Pitch Range", 75, 50, 90, .1f, this, () -> rotations.is("Custom") || rotations.is("God Bridge"));
    private final BoolValue customRotationSetting = new BoolValue("Custom Rotation Setting", false, this);
    private final ListValue smoothMode = new ListValue("Rotations Smooth", RotationUtil.smoothModes, RotationUtil.smoothModes[0], this, customRotationSetting::get);
    private final SliderValue minYawRotSpeed = new SliderValue("Min Yaw Rotation Speed", 45, 1, 180, 1, this, customRotationSetting::get);
    private final SliderValue minPitchRotSpeed = new SliderValue("Min Pitch Rotation Speed", 45, 1, 180, 1, this, customRotationSetting::get);
    private final SliderValue maxYawRotSpeed = new SliderValue("Max Yaw Rotation Speed", 90, 1, 180, 1, this, customRotationSetting::get);
    private final SliderValue maxPitchRotSpeed = new SliderValue("Max Pitch Rotation Speed", 90, 1, 180, 1, this, customRotationSetting::get);
    private final SliderValue bezierP0 = new SliderValue("Bezier P0", 0f, 0f, 1f, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP1 = new SliderValue("Bezier P1", 0.05f, 0f, 1f, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP2 = new SliderValue("Bezier P2", 0.2f, 0f, 1f, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP3 = new SliderValue("Bezier P3", 0.4f, 0f, 1f, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP4 = new SliderValue("Bezier P4", 0.6f, 0f, 1f, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP5 = new SliderValue("Bezier P5", 0.8f, 0f, 1f, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP6 = new SliderValue("Bezier P6", 0.95f, 0f, 1f, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP7 = new SliderValue("Bezier P7", 0.1f, 0f, 1f, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue elasticity = new SliderValue("Elasticity", 0.3f, 0.1f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && smoothMode.is(RotationUtil.smoothModes[7]));
    private final SliderValue dampingFactor = new SliderValue("Damping Factor", 0.5f, 0.1f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && smoothMode.is(RotationUtil.smoothModes[7]));
    public final BoolValue smoothlyResetRotation = new BoolValue("Smoothly Reset Rotation", true, this, customRotationSetting::get);
    private final MultiBoolValue addons = new MultiBoolValue("Addons", Arrays.asList(
            new BoolValue("Sprint", true),
            new BoolValue("Swing", true),
            new BoolValue("Movement Fix", true),
            new BoolValue("Ray Trace", true),
            new BoolValue("Keep Y", false),
            new BoolValue("Speed Keep Y", false),
            new BoolValue("Safe Walk", false),
            new BoolValue("Safe Walk When No Data", false),
            new BoolValue("AD Strafe", false),
            new BoolValue("Hover", false),
            new BoolValue("Sneak", false),
            new BoolValue("Jump", false),
            new BoolValue("Target Block ESP", false)
    ), this);
    private final SliderValue blocksToJump = new SliderValue("Blocks To Jump", 7, 1, 8, this, () -> addons.isEnabled("Jump"));
    private final SliderValue blocksToSneak = new SliderValue("Blocks To Sneak", 7, 1, 8, this, () -> addons.isEnabled("Sneak"));
    private final SliderValue sneakDistance = new SliderValue("Sneak Distance", 0, 0, 0.5f, 0.01f, this, () -> addons.isEnabled("Sneak"));
    private final ListValue tower = new ListValue("Tower", new String[]{"Jump", "Vanilla", "Watchdog"}, "Jump", this, () -> !mode.is("Telly"));
    private final BoolValue towerStop = new BoolValue("Tower Stop",true,this,() -> tower.is("Watchdog"));
    private final SliderValue towerStopTick = new SliderValue("Tower Stop Tick",7,4,20,this,() -> towerStop.canDisplay() && towerStop.get());
    private final ListValue towerMove = new ListValue("Tower Move", new String[]{"Jump", "Vanilla", "Watchdog", "Low"}, "Jump", this, () -> !mode.is("Telly"));
    private final BoolValue towerMoveStop = new BoolValue("Tower Move Stop",true,this,() -> tower.is("Watchdog"));
    private final SliderValue towerMoveStopTick = new SliderValue("Tower Stop Tick",7,4,20,this,() -> towerMoveStop.canDisplay() && towerMoveStop.get());
    private final ListValue wdSprint = new ListValue("WD Sprint Mode", new String[]{"Offset"}, "Bottom", this, () -> mode.is("Watchdog") && addons.isEnabled("Sprint") && !addons.isEnabled("Keep Y"));
    private final BoolValue sprintBoost = new BoolValue("Sprint Boost Test", true, this, () -> mode.is("Watchdog") && addons.isEnabled("Sprint") && !addons.isEnabled("Keep Y"));
    private final ListValue wdKeepY = new ListValue("WD Keep Y Mode", new String[]{"Extra", "Vanilla"}, "Extra", this, () -> mode.is("Watchdog") && addons.isEnabled("Sprint") && (addons.isEnabled("Keep Y") || addons.isEnabled("Speed Keep Y")));
    private final ListValue wdLowhop = new ListValue("WD Fast Fall Mode", new String[]{"8 Tick", "7 Tick", "Disabled"}, "Disabled", this, () -> mode.is("Watchdog") && addons.isEnabled("Sprint") && addons.isEnabled("Keep Y"));
    private final BoolValue unFlagged = new BoolValue("Un Flagged Test", true, this, () -> mode.is("Watchdog") && addons.isEnabled("Sprint"));
    private final BoolValue unPatch = new BoolValue("Un Patch Test", true, this, () -> mode.is("Watchdog") && addons.isEnabled("Sprint") && (addons.isEnabled("Keep Y") || addons.isEnabled("Speed Keep Y")));
    private final SliderValue straightSpeed = new SliderValue("Keep Y Straight Speed", 1, 0.5f, 1f, 0.01f, this, () -> mode.is("Watchdog") && addons.isEnabled("Sprint") && (addons.isEnabled("Keep Y") || addons.isEnabled("Speed Keep Y")));
    private final SliderValue diagonalSpeed = new SliderValue("Keep Y Diagonal Speed", 0.95f, 0.5f, 1f, 0.01f, this, () -> mode.is("Watchdog") && addons.isEnabled("Sprint") && (addons.isEnabled("Keep Y") || addons.isEnabled("Speed Keep Y")));
    public final ListValue counter = new ListValue("Counter", new String[]{"None", "Simple", "Normal", "Exhibition", "Adjust", "Novo", "Augustus"}, "Normal", this);

    public PlaceData data;
    BlockPos targetBlock;
    public BlockPos previousBlock;

    private boolean placed;
    private boolean flagged;
    private boolean placing;
    private boolean isOnRightSide;
    private boolean canPlace = true;

    private double onGroundY;

    private int tellyTicks;
    private int oloSlot = -1;
    private int blocksPlaced;

    private float derpYaw;
    private float[] rotation;
    private float[] previousRotation;

    private HoverState hoverState = HoverState.DONE;

    @Override
    public void onEnable() {
        if (addons.isEnabled("Hover") && mc.thePlayer.onGround && !isEnabled(Speed.class)) {
            hoverState = HoverState.JUMP;
        } else {
            hoverState = HoverState.DONE;
        }

        oloSlot = mc.thePlayer.inventory.currentItem;
        onGroundY = mc.thePlayer.getEntityBoundingBox().minY;
        previousRotation = new float[]{mc.thePlayer.rotationYaw + 180, 82};

        if (wdSprint.canDisplay() && wdSprint.is("Offset") && !(PlayerUtil.getBlock(mc.thePlayer.getPosition()) instanceof BlockLiquid) && !addons.isEnabled("Hover")) {
            if (mc.thePlayer.onGround) {
                hoverState = HoverState.JUMP;
            } else {
                hoverState = HoverState.DONE;
            }
        }

        flagged = false;

        canPlace = true;
    }

    enum HoverState {
        JUMP,
        FALL,
        DONE
    }

    @Override
    public void onDisable() {
        switch (switchBlock.getValue()) {
            case "Silent":
                sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                break;
            case "Switch":
                mc.thePlayer.inventory.currentItem = oloSlot;
                break;
            case "Spoof":
                mc.thePlayer.inventory.currentItem = oloSlot;
                SpoofSlotComponent.stopSpoofing();
                break;
        }
        previousRotation = rotation = null;
        blocksPlaced = 0;
        placed = placing = false;
        tellyTicks = 0;
        data = null;
        targetBlock = null;

        if (wdSprint.canDisplay() && sprintBoost.get()) {
            mc.thePlayer.motionX *= .8;
            mc.thePlayer.motionZ *= .8;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.getValue());

        data = null;

        if (ScaffoldUtil.getBlockSlot() == -1)
            return;

        // must be on here so the yaw is updated properly
        if (rotations.is("Derp")) {
            derpYaw += 30;
        }

        switch (switchBlock.getValue()) {
            case "Silent":
                sendPacketNoEvent(new C09PacketHeldItemChange(ScaffoldUtil.getBlockSlot()));
                break;
            case "Switch":
                mc.thePlayer.inventory.currentItem = ScaffoldUtil.getBlockSlot();
                break;
            case "Spoof":
                mc.thePlayer.inventory.currentItem = ScaffoldUtil.getBlockSlot();
                SpoofSlotComponent.startSpoofing(oloSlot);
                break;
        }

        if (mc.thePlayer.onGround) {
            onGroundY = mc.thePlayer.getEntityBoundingBox().minY;
        }

        double posY = mc.thePlayer.getEntityBoundingBox().minY;

        if ((hoverState != HoverState.DONE || addons.isEnabled("Keep Y") && !isEnabled(Speed.class) || addons.isEnabled("Speed Keep Y") && isEnabled(Speed.class)) && !mc.gameSettings.keyBindJump.isKeyDown()) {
            posY = onGroundY;
        }

        if (wdKeepY.canDisplay() && wdKeepY.is("Extra") && !towering() && (addons.isEnabled("Keep Y") && !isEnabled(Speed.class) || addons.isEnabled("Speed Keep Y") && isEnabled(Speed.class)) && !mc.gameSettings.keyBindJump.isKeyDown()) {
            posY = mc.thePlayer.ticksExisted % 6 != 0 ? onGroundY : mc.thePlayer.getEntityBoundingBox().minY;
        }

        if (towerMoving() || towering()) {
            onGroundY = posY = mc.thePlayer.getEntityBoundingBox().minY;
        }

        if (mode.is("Telly") && mc.thePlayer.onGround) {
            tellyTicks = MathUtil.randomizeInt(minTellyTicks.getValue().intValue(), maxTellyTicks.getValue().intValue());
        }

        double posX = mc.thePlayer.posX;
        double posZ = mc.thePlayer.posZ;

        targetBlock = new BlockPos(posX, posY, posZ).offset(EnumFacing.DOWN);

        data = ScaffoldUtil.getPlaceData(targetBlock);

        if (tower.canDisplay() && towering() && !isEnabled(Speed.class) && tower.is("Watchdog") && !placing) {
            BlockPos xPos = data.blockPos.add(-1, 0, 0);
            if (ScaffoldUtil.canBePlacedOn(xPos)) {
                data.blockPos = xPos;
            }
        }

        placing = false;

        if (isEnabled(KillAura.class) && !getModule(KillAura.class).noScaffold.get() && getModule(KillAura.class).target != null && getModule(KillAura.class).shouldAttack() && data == null) {
            return;
        }

        if (mode.is("Telly") && mc.thePlayer.onGround) {
            tellyTicks = MathUtil.randomizeInt(minTellyTicks.getValue().intValue(), maxTellyTicks.getValue().intValue());
        }

        if (addons.isEnabled("Sprint")) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            mc.thePlayer.setSprinting(false);
        }

        if (tower.canDisplay() && (!tower.is("Jump") && towering() && !isEnabled(Speed.class) || !towerMove.is("Jump") && towerMoving())) {
            hoverState = HoverState.JUMP;
            blocksPlaced = 0;
        }

        switch (hoverState) {
            case JUMP:
                if (mc.thePlayer.onGround && !isEnabled(Speed.class) && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.thePlayer.jump();
                }
                hoverState = HoverState.FALL;
                break;
            case FALL:
                if (mc.thePlayer.onGround)
                    hoverState = HoverState.DONE;
                break;
        }

        if (!placed) {
            rotation = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        }

        canPlace = (mode.is("Telly") && mc.thePlayer.offGroundTicks >= tellyTicks || mode.is("Snap") && data != null || !mode.is("Telly") && !mode.is("Snap"));

        getRotations();

        place(data.blockPos, data.facing, getVec3(data));
    }

    @EventTarget
    public void onRotationUpdate(UpdateEvent event) {
        if (canPlace) {
            if (customRotationSetting.get()) {
                RotationUtil.setRotation(rotation, smoothMode.getValue(), addons.isEnabled("Movement Fix") ? MovementCorrection.SILENT : MovementCorrection.OFF, minYawRotSpeed.getValue(), maxYawRotSpeed.getValue(), minPitchRotSpeed.getValue(), maxPitchRotSpeed.getValue(),
                        bezierP0.getValue(),
                        bezierP1.getValue(),
                        bezierP2.getValue(),
                        bezierP3.getValue(),
                        bezierP4.getValue(),
                        bezierP5.getValue(),
                        bezierP6.getValue(),
                        bezierP7.getValue(),
                        elasticity.getValue(),
                        dampingFactor.getValue(), smoothlyResetRotation.get());
            } else {
                RotationUtil.setRotation(rotation, addons.isEnabled("Movement Fix") ? MovementCorrection.SILENT : MovementCorrection.OFF);
            }
        }
    }

    @EventTarget
    public void onSafeWalk(SafeWalkEvent event) {
        if (addons.isEnabled("Safe Walk") && mc.thePlayer.onGround || addons.isEnabled("Safe Walk When No Data") && data == null) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onMovementInput(MoveInputEvent event) {

        if (isEnabled(KillAura.class) && !getModule(KillAura.class).noScaffold.get() && getModule(KillAura.class).target != null && getModule(KillAura.class).shouldAttack() && data == null) {
            return;
        }

        if (mode.is("Watchdog") && towerMoving() && !isEnabled(Speed.class) && towerMove.is("Jump")) {
            event.setJumping(placing);
        }

        if (addons.isEnabled("AD Strafe") && MovementUtil.isMoving() && MovementUtil.isMovingStraight() && mc.currentScreen == null && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCodeDefault()) && mc.thePlayer.onGround) {
            final BlockPos b = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
            if (mc.thePlayer.getHorizontalFacing(mc.thePlayer.rotationYaw + 180) == EnumFacing.EAST) {
                if (b.getZ() + 0.5 > mc.thePlayer.posZ) {
                    event.setStrafe(1.0f);
                } else {
                    event.setStrafe(-1.0f);
                }
            } else if (mc.thePlayer.getHorizontalFacing(mc.thePlayer.rotationYaw + 180) == EnumFacing.WEST) {
                if (b.getZ() + 0.5 < mc.thePlayer.posZ) {
                    event.setStrafe(1.0f);
                } else {
                    event.setStrafe(-1.0f);
                }
            } else if (mc.thePlayer.getHorizontalFacing(mc.thePlayer.rotationYaw + 180) == EnumFacing.SOUTH) {
                if (b.getX() + 0.5 < mc.thePlayer.posX) {
                    event.setStrafe(1.0f);
                } else {
                    event.setStrafe(-1.0f);
                }
            } else if (b.getX() + 0.5 > mc.thePlayer.posX) {
                event.setStrafe(1.0f);
            } else {
                event.setStrafe(-1.0f);
            }
        }

        if (addons.isEnabled("Sneak")) {

            double dif = 0.5;
            BlockPos blockPos = new BlockPos(mc.thePlayer).down();

            for (EnumFacing side : EnumFacing.values()) {
                if (side.getAxis() == EnumFacing.Axis.Y) {
                    continue;
                }

                BlockPos neighbor = blockPos.offset(side);

                if (PlayerUtil.isReplaceable(neighbor)) {
                    double calcDif = (side.getAxis() == EnumFacing.Axis.Z) ?
                            Math.abs(neighbor.getZ() + 0.5 - mc.thePlayer.posZ) :
                            Math.abs(neighbor.getX() + 0.5 - mc.thePlayer.posX) - 0.5;

                    if (calcDif < dif) {
                        dif = calcDif;
                    }
                }
            }

            if (mc.thePlayer.onGround && (PlayerUtil.isReplaceable(blockPos) || dif < sneakDistance.getValue()) && blocksPlaced == blocksToSneak.getValue()) {
                event.setSneaking(true);
            }
            if (blocksPlaced > blocksToSneak.getValue())
                blocksPlaced = 0;
        }

        if (!(PlayerUtil.getBlock(mc.thePlayer.getPosition()) instanceof BlockLiquid) && !towering() && !isEnabled(Speed.class)) {

            if (unFlagged.get()) {
                if (flagged) {
                    event.setSneaking(true);
                    if (blocksPlaced > 1)
                        flagged = false;
                }
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {

        if (isEnabled(KillAura.class) && !getModule(KillAura.class).noScaffold.get() && getModule(KillAura.class).target != null && getModule(KillAura.class).shouldAttack() && data == null) {
            return;
        }

        if (wdLowhop.canDisplay() && wdLowhop.is("7 Tick") && placed && !towering() && !isEnabled(Speed.class) && !towerMoving()) {
            switch (mc.thePlayer.offGroundTicks) {
                case 1:
                    mc.thePlayer.motionY += 0.057f;
                    break;
                case 3:
                    mc.thePlayer.motionY -= 0.1309f;
                    break;
                case 4:
                    mc.thePlayer.motionY -= 0.2;
                    break;
            }
        }

        if (towerMove.is("Low")) {
            if (towerMoving()) {
                if (mc.thePlayer.offGroundTicks == 1) {
                    mc.thePlayer.motionY += 0.057f;
                    MovementUtil.strafe(Math.max(MovementUtil.getSpeed(), 0.33f + MovementUtil.getSpeedEffect() * 0.075));
                }

                if (mc.thePlayer.offGroundTicks == 3) {
                    mc.thePlayer.motionY -= 0.1309f;
                }

                if (mc.thePlayer.offGroundTicks == 4) {
                    mc.thePlayer.motionY -= 0.2;
                }
            }
        }

        if (mc.thePlayer.onGround) {
            if (((mode.is("Telly") || wdKeepY.canDisplay())) && MovementUtil.isMoving() && !towering() && !towerMoving()) {
                mc.thePlayer.jump();
            }
        }

        if (addons.isEnabled("Jump")) {
            if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && MovementUtil.isMoving() && MovementUtil.isMovingStraight()
                    && !mc.thePlayer.isSneaking()) {
                if (blocksPlaced >= blocksToJump.getValue()) {
                    mc.thePlayer.jump();
                    blocksPlaced = 0;
                }
            } else {
                blocksPlaced = 0;
            }
        }
    }

    @EventTarget
    public void onMove(MoveEvent event) {

        if (isEnabled(KillAura.class) && !getModule(KillAura.class).noScaffold.get() && getModule(KillAura.class).target != null && getModule(KillAura.class).shouldAttack() && data == null) {
            return;
        }

        if (tower.canDisplay()) {
            if (tower.getValue().equals("Vanilla")) {
                if (!mc.thePlayer.isPotionActive(Potion.jump)) {
                    if (towering()) {
                        event.setY(mc.thePlayer.motionY = 0.42);
                    }
                }
            }
        }

        if (towerMove.canDisplay()) {
            if (towerMove.getValue().equals("Vanilla")) {
                if (MovementUtil.isMoving() && MovementUtil.getSpeed() > 0.1 && !mc.thePlayer.isPotionActive(Potion.jump)) {
                    if (towerMoving()) {
                        mc.thePlayer.motionY = 0.42f;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onAfterJump(AfterJumpEvent event) {

        if (isEnabled(KillAura.class) && !getModule(KillAura.class).noScaffold.get() && getModule(KillAura.class).target != null && getModule(KillAura.class).shouldAttack() && data == null) {
            return;
        }

        if (mode.is("Watchdog") && mc.gameSettings.keyBindJump.isKeyDown() && towerMove.is("Jump") && placing) {
            MovementUtil.strafe(0.4);
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {

        if (event.isPost())
            return;

        if (isEnabled(KillAura.class) && !getModule(KillAura.class).noScaffold.get() && getModule(KillAura.class).target != null && getModule(KillAura.class).shouldAttack() && data == null) {
            return;
        }

        if (!(PlayerUtil.getBlock(mc.thePlayer.getPosition()) instanceof BlockLiquid) && !towering() && !isEnabled(Speed.class) && !towerMoving()) {
            if (wdSprint.canDisplay() && !(addons.isEnabled("Speed Keep Y") & isEnabled(Speed.class) || addons.isEnabled("Keep Y"))) {
                if (wdSprint.is("Offset")) {
                    if (mc.thePlayer.onGround) {
                        event.setY(event.getY() + 1E-13F);
                    }
                }

                if (!isEnabled(Speed.class) && mc.thePlayer.onGround && sprintBoost.get() && (unFlagged.get() && !flagged || !unFlagged.get())) {
                    mc.thePlayer.motionX *= 1.14 - MovementUtil.getSpeedEffect() * .01;
                    mc.thePlayer.motionZ *= 1.14 - MovementUtil.getSpeedEffect() * .01;
                }
            }
        }

        if (wdKeepY.canDisplay() && (addons.isEnabled("Speed Keep Y") && isEnabled(Speed.class) || !addons.isEnabled("Speed Keep Y")) && !towering() && !isEnabled(Speed.class) && !towerMoving() && !isEnabled(Speed.class) && addons.isEnabled("Sprint") && mc.thePlayer.onGround && MovementUtil.isMoving()) {
            MovementUtil.strafe(MovementUtil.getSpeed() * (MovementUtil.isMovingStraight() ? straightSpeed.getValue() : diagonalSpeed.getValue()));
        }

        if (wdLowhop.canDisplay() && wdLowhop.is("8 Tick") && placed && !towering() && !isEnabled(Speed.class) && !towerMoving()) {
            int simpleY = (int) Math.round((event.y % 1) * 10000);

            if (simpleY == 13) {
                mc.thePlayer.motionY = mc.thePlayer.motionY - 0.02483;
            }

            if (simpleY == 2000) {
                mc.thePlayer.motionY = mc.thePlayer.motionY - 0.1913;
            }
        }

        if (tower.canDisplay()) {
            if (tower.getValue().equals("Watchdog")) {
                if (!mc.thePlayer.isPotionActive(Potion.jump) && (!towerStop.get() || towerStop.get() && mc.thePlayer.offGroundTicks < towerStopTick.getValue())) {
                    if (towering()) {
                        MovementUtil.stopXZ();

                        if (mc.thePlayer.posY % 1 == 0.5) {
                            mc.thePlayer.motionY = 0.42F;
                        }

                        if (mc.thePlayer.posX % 1.0 != 0.0 && !MovementUtil.isMoving()) {
                            double velocityXAdjustment = (Math.round(mc.thePlayer.posX) - mc.thePlayer.posX);
                            mc.thePlayer.motionX = Math.min(velocityXAdjustment, 0.281);
                        }

                        int valY = (int) Math.round((event.y % 1) * 10000);

                        if (valY == 0) {
                            mc.thePlayer.motionY = 0.42F;
                        } else if (valY > 4000 && valY < 4300) {
                            mc.thePlayer.motionY = 0.33;
                        } else if (valY > 7000) {
                            mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                        }

                    }
                }
            }
        }

        if (towerMove.canDisplay()) {
            if (towerMove.getValue().equals("Watchdog")) {
                if (MovementUtil.isMoving() && MovementUtil.getSpeed() > 0.1 && !mc.thePlayer.isPotionActive(Potion.jump) && (!towerMoveStop.get() || towerMoveStop.get() && mc.thePlayer.offGroundTicks < towerMoveStopTick.getValue())) {
                    if (towerMoving()) {
                        int valY = (int) Math.round((event.y % 1) * 10000);
                        if (valY == 0) {
                            mc.thePlayer.motionY = 0.42F;
                            MovementUtil.strafe((float) 0.28 + MovementUtil.getSpeedEffect() * 0.04);
                        } else if (valY > 4000 && valY < 4300) {
                            mc.thePlayer.motionY = 0.33;
                            MovementUtil.strafe((float) 0.28 + MovementUtil.getSpeedEffect() * 0.04);
                        } else if (valY > 7000) {
                            mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (isEnabled(KillAura.class) && !getModule(KillAura.class).noScaffold.get() && getModule(KillAura.class).target != null && getModule(KillAura.class).shouldAttack() && data == null) {
            return;
        }

        if (addons.isEnabled("Target Block ESP")) {
            RenderUtil.renderBlock(data.blockPos, getModule(Interface.class).color(0, 100), false, true);
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            if (wdSprint.canDisplay() && !(PlayerUtil.getBlock(mc.thePlayer.getPosition()) instanceof BlockLiquid) && wdSprint.is("Offset")) {
                blocksPlaced = 0;
                flagged = true;
            }
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        setEnabled(false);
    }

    public void getRotations() {

        switch (rotations.getValue()) {
            case "Normal": {
                if (data != null)
                    rotation = RotationUtil.getRotations(getVec3(data));
            }
            break;
            case "Normal 2": {
                if (data != null)
                    rotation = RotationUtil.getRotations(data.blockPos);
            }
            break;
            case "Normal 3": {
                if (data != null)
                    rotation = getBestRotation(data.blockPos,data.facing);
            }
            break;
            case "Strict": {
                if (data != null)
                    rotation = RotationUtil.getRotations(ScaffoldUtil.getHitVecOptimized(data.blockPos, data.facing));
            }
            case "Back": {
                if (data != null) {
                    // @Author：zyyzs
                    rotation = new float[]{RotationUtil.oppositeYaw(MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw)), (float) (75 + Math.random() + mc.thePlayer.offGroundTicks * 0.2)};
                }
            }
            break;

            case "God Bridge": {
                float movingYaw = MovementUtil.getRawDirection() + 180;

                if (mc.thePlayer.onGround) {
                    isOnRightSide = Math.floor(mc.thePlayer.posX + Math.cos(Math.toRadians(movingYaw)) * 0.5) != Math.floor(mc.thePlayer.posX) ||
                            Math.floor(mc.thePlayer.posZ + Math.sin(Math.toRadians(movingYaw)) * 0.5) != Math.floor(mc.thePlayer.posZ);

                    BlockPos posInDirection = mc.thePlayer.getPosition().offset(EnumFacing.fromAngle(movingYaw), 1);

                    boolean isLeaningOffBlock = mc.theWorld.getBlockState(mc.thePlayer.getPosition().down()) instanceof BlockAir;
                    boolean nextBlockIsAir = mc.theWorld.getBlockState(posInDirection.down()).getBlock() instanceof BlockAir;

                    if (isLeaningOffBlock && nextBlockIsAir) {
                        isOnRightSide = !isOnRightSide;
                    }
                }

                float yaw = MovementUtil.isMovingStraight() ? (movingYaw + (isOnRightSide ? 45 : -45)) : movingYaw;

                float finalYaw = Math.round(yaw / 45f) * 45f;

                for (float i = minPitch.getValue(); i < maxPitch.getValue(); i += 0.01f) {
                    float[] rot = new float[]{finalYaw, i};
                    MovingObjectPosition ray = RotationUtil.rayTrace(rot, mc.playerController.getBlockReachDistance(), 1);

                    if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && ray.getBlockPos().equalsBlockPos(data.blockPos)) {
                        rotation = rot;
                    }
                }
            }
            break;
            case "Custom": {
                float yaw = MovementUtil.getRawDirection() + customYaw.getValue();

                for (float i = minPitch.getValue(); i < maxPitch.getValue(); i += 0.01f) {
                    float[] rot = new float[]{yaw, i};
                    MovingObjectPosition ray = RotationUtil.rayTrace(rot, mc.playerController.getBlockReachDistance(), 1);

                    if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && ray.getBlockPos().equalsBlockPos(data.blockPos)) {
                        rotation = rot;
                    }
                }
            }
            break;
            case "Hypixel": {
                float yaw = MovementUtil.getRawDirection();
                if (MovementUtil.isMovingStraight()) {
                    if (Math.abs(MathHelper.wrapAngleTo180_double(RotationUtil.getRotations(targetBlock)[0] - MovementUtil.getRawDirection() - 118)) < Math.abs(MathHelper.wrapAngleTo180_double(RotationUtil.getRotations(targetBlock)[0] - MovementUtil.getRawDirection() + 118))) {
                        yaw += 118;
                    } else {
                        yaw -= 118;
                    }
                } else {
                    yaw += 132;
                }

                rotation[0] = yaw;

                if (data != null) {
                    rotation[1] = getBestRotation(data.blockPos, data.facing)[1];
                } else {
                    rotation = new float[]{yaw, previousRotation[1]};
                }
            }
            break;
            case "Unfair Pitch": {
                rotation = new float[]{mc.thePlayer.rotationYaw, 93};
            }
            break;
            case "Derp":
                rotation = new float[]{derpYaw, 85};
                break;
        }

        if (tower.canDisplay() && tower.is("Watchdog") && towering()) {
            rotation = getBestRotation(data.blockPos, data.facing);
        }

        if (unPatch.canDisplay() && (addons.isEnabled("Speed Keep Y") && isEnabled(Speed.class) || !addons.isEnabled("Speed Keep Y")) && unPatch.get() && (mc.thePlayer.offGroundTicks == 1 || mc.thePlayer.onGround) && !towering() && !towerMoving()) {
            rotation = new float[]{mc.thePlayer.rotationYaw, previousRotation[1]};
            canPlace = false;
        }

        previousRotation = rotation;
    }

    public boolean towering() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && !MovementUtil.isMoving();
    }

    public boolean towerMoving() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && MovementUtil.isMoving();
    }

    private void place(BlockPos pos, EnumFacing facing, Vec3 hitVec) {
        if (canPlace && data != null) {

            if (!addons.isEnabled("Ray Trace")) {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), pos, facing, hitVec)) {
                    if (addons.isEnabled("Swing")) {
                        mc.thePlayer.swingItem();
                        mc.getItemRenderer().resetEquippedProgress();
                    } else
                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                    placing = true;
                    blocksPlaced += 1;
                    placed = true;
                }
            } else {
                MovingObjectPosition ray = RotationUtil.rayTrace(mc.playerController.getBlockReachDistance(), 1);
                if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), ray.getBlockPos(), ray.sideHit, ray.hitVec)) {
                    if (addons.isEnabled("Swing")) {
                        mc.thePlayer.swingItem();
                        mc.getItemRenderer().resetEquippedProgress();
                    } else
                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                    placing = true;
                    blocksPlaced += 1;
                    placed = true;
                }
            }
        }
    }

    public float[] getBestRotation(BlockPos blockPos, EnumFacing face) {
        Vec3i faceVec = face.getDirectionVec();

        float minX, maxX, minY, maxY, minZ, maxZ;

        if (faceVec.getX() == 0) {
            minX = 0.1f;
            maxX = 0.9f;
        } else if (faceVec.getX() == 1) {
            minX = maxX = 1.0f;
        } else if (faceVec.getX() == -1) {
            minX = maxX = 0.0f;
        } else {
            minX = 0.1f;
            maxX = 0.9f;
        }

        if (faceVec.getY() == 0) {
            minY = 0.1f;
            maxY = 0.9f;
        } else if (faceVec.getY() == 1) {
            minY = maxY = 1.0f;
        } else if (faceVec.getY() == -1) {
            minY = maxY = 0.0f;
        } else {
            minY = 0.1f;
            maxY = 0.9f;
        }

        if (faceVec.getZ() == 0) {
            minZ = 0.1f;
            maxZ = 0.9f;
        } else if (faceVec.getZ() == 1) {
            minZ = maxZ = 1.0f;
        } else if (faceVec.getZ() == -1) {
            minZ = maxZ = 0.0f;
        } else {
            minZ = 0.1f;
            maxZ = 0.9f;
        }

        float[] bestRot = RotationUtil.getRotations(blockPos);
        double bestDist = RotationUtil.getRotationDifference(bestRot);

        for (float x = minX; x <= maxX; x += 0.1f) {
            for (float y = minY; y <= maxY; y += 0.1f) {
                for (float z = minZ; z <= maxZ; z += 0.1f) {
                    Vec3 candidateLocal = new Vec3(x, y, z);
                    Vec3 candidateWorld = candidateLocal.add(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));

                    double diff = RotationUtil.getRotationDifference(candidateWorld);
                    if (diff < bestDist) {
                        bestDist = diff;
                        bestRot = RotationUtil.getRotations(candidateWorld);
                    }
                }
            }
        }

        return bestRot;
    }

    private Vec3 getVec3(PlaceData data) {
        BlockPos pos = data.blockPos;
        EnumFacing face = data.facing;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        x += face.getFrontOffsetX() / 2.0D;
        z += face.getFrontOffsetZ() / 2.0D;
        y += face.getFrontOffsetY() / 2.0D;

        return new Vec3(x, y, z);
    }

    @AllArgsConstructor
    public static class PlaceData {
        public BlockPos blockPos;
        public EnumFacing facing;
    }
}
