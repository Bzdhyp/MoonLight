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

import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Keyboard;
import com.cubk.EventTarget;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.*;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MathUtil;
import wtf.moonlight.util.DebugUtil;
import wtf.moonlight.util.player.MovementUtil;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RotationUtil;

import java.util.Objects;

@ModuleInfo(name = "Speed", category = Categor.Movement, key = Keyboard.KEY_V)
public class Speed extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Vanilla","Watchdog", "EntityCollide", "BlocksMC", "Intave", "NCP", "Miniblox"}, "Watchdog", this);
    private final ListValue wdMode = new ListValue("Watchdog Mode", new String[]{"Fast", "Glide","Ground Test"}, "Basic", this, () -> mode.is("Watchdog"));
    private final BoolValue fastFall = new BoolValue("Fast Fall", true, this, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue hurtTimeCheck = new BoolValue("Hurt Time Check", false, this, () -> mode.is("Watchdog") && (wdMode.is("Fast") || fastFall.canDisplay() && fastFall.get()));
    private final ListValue wdFastFallMode = new ListValue("Fast Fall Mode", new String[]{"7 Tick","8 Tick Strafe","8 Tick Fast","9 Tick"}, "8 Tick", this, () -> mode.is("Watchdog") && fastFall.canDisplay() && fastFall.get());
    private final BoolValue disableWhileScaffold = new BoolValue("Disable While Scaffold", true, this, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue frictionOverride = new BoolValue("Friction Override", true, this, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue extraStrafe = new BoolValue("Extra Strafe", true, this, () -> mode.is("Watchdog") && wdMode.is("Fast"));
    private final BoolValue expand = new BoolValue("More Expand", false, this, () -> Objects.equals(mode.getValue(), "EntityCollide"));
    private final BoolValue ignoreDamage = new BoolValue("Ignore Damage", true, this, () -> Objects.equals(mode.getValue(), "EntityCollide"));
    private final BoolValue pullDown = new BoolValue("Pull Down", true, this, () -> Objects.equals(mode.getValue(), "NCP"));
    private final SliderValue onTick = new SliderValue("On Tick", 5, 1, 10, 1, this, () -> Objects.equals(mode.getValue(), "NCP") && pullDown.get());
    private final BoolValue onHurt = new BoolValue("On Hurt", true, this, () -> Objects.equals(mode.getValue(), "NCP") && pullDown.get());
    private final BoolValue airBoost = new BoolValue("Air Boost", true, this, () -> Objects.equals(mode.getValue(), "NCP"));
    private final BoolValue damageBoost = new BoolValue("Damage Boost", false, this, () -> Objects.equals(mode.getValue(), "NCP"));
    private final SliderValue mTicks = new SliderValue("Ticks", 5, 1, 6, 1, this, () -> Objects.equals(mode.getValue(), "Miniblox"));
    public final BoolValue noBob = new BoolValue("No Bob", true, this);
    private final BoolValue forceStop = new BoolValue("Force Stop", true, this);
    private final BoolValue lagBackCheck = new BoolValue("Lag Back Check", true, this);
    private final BoolValue liquidCheck = new BoolValue("Liquid Check", true, this);
    private final BoolValue guiCheck = new BoolValue("Gui Check", true, this);
    private final BoolValue printOffGroundTicks = new BoolValue("Print Off Ground Ticks", true, this);
    private final SliderValue vanilla = new SliderValue("Vanilla Speed", 0.5f,0.05f,2, 0.05f,this, () -> Objects.equals(mode.getValue(), "Vanilla"));
    private final BoolValue vanillaPullDown = new BoolValue("Pull Down", true, this, () -> mode.is("Vanilla"));
    private final SliderValue vanillaPullDownAmount = new SliderValue("Vanilla Pull Down", 0.5f,0.05f,2, 0.05f,this, () -> Objects.equals(mode.getValue(), "Vanilla") && vanillaPullDown.get());
    private boolean disable;
    private boolean disable3;
    private int boostTicks;
    private boolean recentlyCollided;
    private boolean slab;
    private boolean stopVelocity;
    public boolean couldStrafe;
    private double speed;
    private int ticksSinceTeleport;
    private boolean valued;

    @Override
    public void onEnable() {
        if (mode.is("Watchdog")) {
            if (wdMode.is("Glide")) {
                speed = 0.28;
            }
            slab = false;

            disable3 = false;
            if(mc.thePlayer.offGroundTicks > 2){
                disable = true;
            }
            valued = false;
        }
    }

    @Override
    public void onDisable() {
        disable = false;
        couldStrafe = false;
        if(forceStop.get()){
            MovementUtil.stopXZ();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.getValue());
        ticksSinceTeleport++;
        if(liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;

        if (printOffGroundTicks.get())
            DebugUtil.sendMessage(mc.thePlayer.offGroundTicks + "Tick");

        switch (mode.getValue()) {
            case "Miniblox": {
                if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
                    mc.thePlayer.jump();
                }

                switch (mc.thePlayer.offGroundTicks) {
                    case 1: {
                        switch (mTicks.getValue().intValue()) {
                            case 1:
                                mc.thePlayer.motionY -= 0.76;
                                break;
                            case 2:
                                mc.thePlayer.motionY -= 0.52;
                                break;
                            case 3:
                                mc.thePlayer.motionY -= 0.452335182447;
                                break;
                            case 4:
                                mc.thePlayer.motionY -= 0.322335182447;
                                break;
                            case 5:
                                mc.thePlayer.motionY -= 0.232335182447;
                                break;
                            case 6:
                                mc.thePlayer.motionY -= 0.162335182447;
                                break;
                        }
                    }
                    break;

                    case 3: {
                        mc.thePlayer.motionY -= 0.1523351824467155;
                    }
                    break;
                }
            }
            break;

            case "NCP": {
                if (mc.thePlayer.offGroundTicks == onTick.getValue() && pullDown.get()) {
                    MovementUtil.strafe();
                    mc.thePlayer.motionY -= 0.1523351824467155;
                }

                if (onHurt.get() && mc.thePlayer.hurtTime >= 5 && mc.thePlayer.motionY >= 0) {
                    mc.thePlayer.motionY -= 0.1;
                }

                if (airBoost.get() && MovementUtil.isMoving()) {
                    mc.thePlayer.motionX *= 1f + 0.00718;
                    mc.thePlayer.motionZ *= 1f + 0.00718;
                }
            }
            break;

            case "Vanilla": {
                MovementUtil.strafe(1 * vanilla.getValue());
                couldStrafe = true;

                if (vanillaPullDown.get()) {
                    mc.thePlayer.motionY = -vanillaPullDownAmount.getValue();
                }
            }
            break;

            case "Intave": {
                if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
                    mc.thePlayer.jump();
                }

                if (mc.thePlayer.motionY > 0.03 && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.motionX *= 1f + 0.003;
                    mc.thePlayer.motionZ *= 1f + 0.003;
                }
            }
            break;

            case "EntityCollide": {
                couldStrafe = false;
                if (mc.thePlayer.hurtTime <= 1) {
                    stopVelocity = false;
                }

                if (stopVelocity && !ignoreDamage.get()) {
                    return;
                }

                if (!MovementUtil.isMoving())
                    return;

                int collisions = 0;
                AxisAlignedBB box = expand.get() ? mc.thePlayer.getEntityBoundingBox().expand(1.0, 1.0, 1.0)
                        : mc.thePlayer.getEntityBoundingBox().expand(0.8, 0.8, 0.8);
                for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                    AxisAlignedBB entityBox = entity.getEntityBoundingBox();
                    if (canCauseSpeed(entity) && box.intersectsWith(entityBox)) {
                        collisions++;
                    }
                }

                double yaw = Math.toRadians(RotationUtil.shouldRotate() ? RotationUtil.currentRotation[0] : mc.thePlayer.rotationYaw);

                double boost = 0.078 * collisions;
                mc.thePlayer.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
            }
            break;

            case "Watchdog":
                if(wdMode.is("Fast")) {
                    if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
                        mc.thePlayer.jump();
                        if(!isEnabled(Scaffold.class))
                            MovementUtil.strafe(0.47 + MovementUtil.getSpeedEffect() * 0.042);
                        couldStrafe = true;
                    }
                }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {

        if (liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;

        if (isEnabled(Scaffold.class) && (getModule(Scaffold.class).towering() || getModule(Scaffold.class).towerMoving()))
            return;

        switch (mode.getValue()) {
            case "Miniblox": {
                if (MovementUtil.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        switch (mTicks.getValue().intValue()) {
                            case 1:
                                MovementUtil.strafe(0.07);
                                break;
                            case 2:
                                MovementUtil.strafe(0.08);
                                break;
                            case 3:
                                MovementUtil.strafe(0.09);
                                break;
                            case 4:
                                MovementUtil.strafe(0.1);
                                break;
                            case 5:
                                MovementUtil.strafe(0.115);
                                break;
                            case 6:
                                MovementUtil.strafe(0.13);
                                break;
                        }
                    } else {
                        MovementUtil.strafe(0.35f);
                    }
                }
            }
            break;

            case "NCP": {
                if (MovementUtil.isMoving()) {
                    couldStrafe = true;
                    MovementUtil.strafe();
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                        MovementUtil.strafe(0.48 + MovementUtil.getSpeedEffect() * 0.07);
                    }
                }

                if (damageBoost.get() && mc.thePlayer.hurtTime > 0) {
                    MovementUtil.strafe(Math.max(MovementUtil.getSpeed(), 0.5));
                }
            }
            break;

            case "Vanilla": {
                if (MovementUtil.isMoving()) {
                    couldStrafe = true;
                    MovementUtil.strafe();
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    }
                }
            }
            break;

            case "Watchdog":
                if (event.isPre()) {
                    if (fastFall.get()) {
                        if (mc.thePlayer.isCollidedHorizontally || ticksSinceTeleport < 2) {
                            recentlyCollided = true;
                            boostTicks = mc.thePlayer.ticksExisted + 9;
                        }
                        if (!mc.thePlayer.isCollidedHorizontally && (mc.thePlayer.ticksExisted > boostTicks)) {
                            recentlyCollided = false;
                        }

                        if (mc.thePlayer.onGround) {
                            disable3 = false;
                        }
                        if (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air) {
                            disable = false;
                        }

                        if (mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround && PlayerUtil.isBlockOver(2.0)) {
                            disable = true;
                        }

                        double posY = event.getY();
                        if (Math.abs(posY - Math.round(posY)) > 0.03 && mc.thePlayer.onGround) {
                            slab = true;
                        } else if (mc.thePlayer.onGround) {
                            slab = false;
                        }
                    }

                    if (fastFall.canDisplay() && fastFall.get()) {

                        if (mc.thePlayer.isInWater() ||
                                mc.thePlayer.isInWeb ||
                                mc.thePlayer.isInLava() ||
                                hurtTimeCheck.get() && mc.thePlayer.hurtTime > 0
                        ) {
                            disable = true;
                            return;
                        }

                        if (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air) {
                            disable = false;
                        }

                        if (mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround && PlayerUtil.isBlockOver(2.0)) {
                            disable = true;
                        }
                    }

                    if (wdMode.is("Ground Test")) {
                        if (valued) {
                            if (mc.thePlayer.onGround) {
                                event.setY(event.getY() + 1E-13F);
                                mc.thePlayer.motionX *= 1.14 - MovementUtil.getSpeedEffect() * .01;
                                mc.thePlayer.motionZ *= 1.14 - MovementUtil.getSpeedEffect() * .01;
                                MovementUtil.strafe();
                                couldStrafe = true;
                            }
                        }
                    }
                }

                break;
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {

        if (liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;

        if (mode.getValue().equals("Watchdog") && wdMode.getValue().equals("Fast") && (mc.thePlayer.isInWater() || mc.thePlayer.isInWeb || mc.thePlayer.isInLava())) {
            disable = true;
            return;
        }
        if (mode.getValue().equals("Watchdog")) {

            switch (wdMode.getValue()) {
                case "Glide":
                    if (MovementUtil.isMoving() && mc.thePlayer.onGround) {
                        MovementUtil.strafe(MovementUtil.getAllowedHorizontalDistance());
                        mc.thePlayer.jump();
                    }

                    if (mc.thePlayer.onGround) {
                        speed = 1.0F;
                    }

                    final int[] allowedAirTicks = new int[]{10, 11, 13, 14, 16, 17, 19, 20, 22, 23, 25, 26, 28, 29};

                    if (!(mc.theWorld.getBlockState(mc.thePlayer.getPosition().add(0, -0.25, 0)).getBlock() instanceof BlockAir)) {
                        for (final int allowedAirTick : allowedAirTicks) {
                            if (mc.thePlayer.offGroundTicks == allowedAirTick && allowedAirTick <= 11) {
                                mc.thePlayer.motionY = 0;
                                MovementUtil.strafe(MovementUtil.getAllowedHorizontalDistance() * speed);
                                couldStrafe = true;

                                speed *= 0.98F;

                            }
                        }
                    }
                    break;
                case "Fast":

                    if (!disable && fastFall.get() && (disableWhileScaffold.get() && !isEnabled(Scaffold.class) || !disableWhileScaffold.get())) {

                        switch (wdFastFallMode.getValue()) {
                            case "7 Tick", "8 Tick Strafe":
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
                                break;

                            case "8 Tick Fast":
                                switch (mc.thePlayer.offGroundTicks) {
                                    case 3:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.02483;
                                        break;
                                    case 5:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.1913;
                                        break;
                                }
                                break;
                            case "9 Tick":
                                switch (mc.thePlayer.offGroundTicks) {
                                    case 3:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.02483;
                                        break;
                                    case 5:
                                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.16874;
                                        break;
                                }
                                break;
                        }

                    }

                    if (mc.thePlayer.offGroundTicks == 1 && !disable) {
                        if (isEnabled(Scaffold.class)) {
                            if (getModule(Scaffold.class).towerMoving())
                                MovementUtil.strafe(0.3);
                        } else {
                            MovementUtil.strafe(Math.max(MovementUtil.getSpeed(), 0.33f + MovementUtil.getSpeedEffect() * 0.075));
                            couldStrafe = true;
                        }
                    }

                    if (mc.thePlayer.offGroundTicks == 2 && !disable && extraStrafe.get()) {
                        double motionX3 = mc.thePlayer.motionX;
                        double motionZ3 = mc.thePlayer.motionZ;
                        mc.thePlayer.motionZ = (mc.thePlayer.motionZ * 1 + motionZ3 * 2) / 3;
                        mc.thePlayer.motionX = (mc.thePlayer.motionX * 1 + motionX3 * 2) / 3;
                    }

                    if (mc.thePlayer.offGroundTicks == 6 && wdFastFallMode.is("8 Tick Strafe") && !disable && PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY * 3, 0) != Blocks.air && PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY * 3, 0).isFullBlock() && (disableWhileScaffold.get() && !isEnabled(Scaffold.class) || !disableWhileScaffold.get())) {
                        mc.thePlayer.motionY += 0.0754;
                        MovementUtil.strafe();
                        couldStrafe = true;
                    }

                    if ((mc.thePlayer.motionX == 0 || mc.thePlayer.motionZ == 0) && !disable && (!recentlyCollided && mc.thePlayer.isPotionActive(Potion.moveSpeed)) && !getModule(Scaffold.class).isEnabled()) {
                        MovementUtil.strafe();
                        couldStrafe = true;
                    }

                    if (mc.thePlayer.offGroundTicks < 7 && (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air) && mc.thePlayer.isPotionActive(Potion.moveSpeed) && !slab) {
                        boostTicks = mc.thePlayer.ticksExisted + 9;
                        recentlyCollided = true;
                    }

                    if (mc.thePlayer.offGroundTicks == 7 && !disable && (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY * 2, 0) != Blocks.air) && !getModule(Scaffold.class).isEnabled()) {
                        MovementUtil.strafe(MovementUtil.getSpeed());
                        couldStrafe = true;
                    }

                    if (PlayerUtil.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) != Blocks.air && mc.thePlayer.offGroundTicks > 5 && !disable3) {
                        MovementUtil.strafe();
                        couldStrafe = true;
                        disable3 = true;
                    }

                    double speed2 = Math.hypot((mc.thePlayer.motionX - (mc.thePlayer.lastTickPosX - mc.thePlayer.lastLastTickPosX)), (mc.thePlayer.motionZ - (mc.thePlayer.lastTickPosZ - mc.thePlayer.lastLastTickPosZ)));
                    if (speed2 < .0125 && frictionOverride.get()) {
                        MovementUtil.strafe();
                        couldStrafe = true;
                    }

                    break;

                case "Ground Test":
                    if(!valued && mc.thePlayer.onGround){
                        mc.thePlayer.jump();
                        valued = true;
                    }
                    break;
            }
        }
    }

    @EventTarget
    public void onPostStrafe(PostStrafeEvent event) {
        if (mode.is("Watchdog") && wdMode.is("Fast")) {
            if (extraStrafe.get()) {
                double attempt_angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MovementUtil.getDirection()));
                double movement_angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(Math.atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX)) - 90);
                if (MathUtil.wrappedDifference(attempt_angle, movement_angle) > 90) {
                    MovementUtil.strafe(MovementUtil.getSpeed(), (float) movement_angle - 180);
                }
            }
        }
    }

    @EventTarget
    public void onMove(MoveEvent event){

        if (!liquidCheck.get() || (!mc.thePlayer.isInWater() && !mc.thePlayer.isInLava())) {
            guiCheck.get();
        }

    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            ticksSinceTeleport = 0;
            if (lagBackCheck.get()) {
                toggle();
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event){
        if(!mode.is("EntityCollide"))
            if(mc.thePlayer.onGround)
                event.setJumping(false);
    }

    private boolean canCauseSpeed(Entity entity) {
        return entity != mc.thePlayer && entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand);
    }
}
