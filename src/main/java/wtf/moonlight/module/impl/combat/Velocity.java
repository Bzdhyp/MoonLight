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
package wtf.moonlight.module.impl.combat;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import com.cubk.EventTarget;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.*;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.movement.LongJump;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.utils.packet.PacketUtils;
import wtf.moonlight.utils.player.MovementUtils;
import wtf.moonlight.utils.player.PlayerUtils;
import wtf.moonlight.utils.player.RotationUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

@ModuleInfo(name = "Velocity", category = ModuleCategory.Combat)
public class Velocity extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Cancel", "Air","Horizontal","Watchdog", "Boost", "Jump Reset", "GrimAC","Reduce","Legit"}, "Air", this);
    private final ListValue grimMode = new ListValue("Grim Mode", new String[]{"Reduce", "1.17"}, "Reduce", this, () -> mode.is("GrimAC"));
    private final SliderValue reverseTick = new SliderValue("Boost Tick", 1, 1, 5, 1, this, () -> mode.is("Boost"));
    private final SliderValue reverseStrength = new SliderValue("Boost Strength", 1, 0.1f, 1, 0.01f, this, () -> mode.is("Boost"));
    private final ListValue jumpResetMode = new ListValue("Jump Reset Mode", new String[]{"Hurt Time", "Packet", "Advanced"}, "Packet", this, () -> mode.is("Jump Reset"));
    private final SliderValue jumpResetHurtTime = new SliderValue("Jump Reset Hurt Time", 9, 1, 10, 1, this, () -> mode.is("Jump Reset") && (jumpResetMode.is("Hurt Time") || jumpResetMode.is("Advanced")));
    private final SliderValue jumpResetChance = new SliderValue("Jump Reset Chance", 100, 0, 100, 1, this, () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"));
    private final SliderValue hitsUntilJump = new SliderValue("Hits Until Jump", 2, 1, 10, 1, this, () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"));
    private final SliderValue ticksUntilJump = new SliderValue("Ticks Until Jump", 2, 1, 20, 1, this, () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"));
    private final SliderValue reduceHurtTime = new SliderValue("Reduce Hurt Time", 9, 1, 10, 1, this, () -> mode.is("Reduce"));
    private final SliderValue reduceFactor = new SliderValue("Reduce Factor", 0.6f, 0, 1, 0.05f, this, () -> mode.is("Reduce"));

    private int lastSprint = -1;
    private boolean veloPacket = false;
    private boolean canSpoof, canCancel;
    private int idk = 0;
    private int reduceTick, reduceDamageTick;
    private long lastAttackTime;
    private boolean absorbedVelocity;
    private boolean isFallDamage;
    private int hitsCount = 0;
    private int ticksCount = 0;
    private final Random random = new Random();

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.getValue());

        switch (mode.getValue()) {
            case "GrimAC":
                if (grimMode.is("1.17")) {
                    if (canSpoof) {
                        sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
                        sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer).down(), EnumFacing.DOWN));
                        canSpoof = false;
                    }
                }
                break;

            case "Reduce":
                if (!veloPacket) return;
                reduceTick++;

                if (mc.thePlayer.hurtTime == 2) {
                    reduceDamageTick++;
                    if (mc.thePlayer.onGround && reduceTick % 2 == 0 && reduceDamageTick <= 10) {
                        mc.thePlayer.jump();
                        reduceTick = 0;
                    }
                    veloPacket = false;
                }
                break;

            case "Watchdog":
                if (mc.thePlayer.onGround) {
                    absorbedVelocity = false;
                }
                break;

            case "Jump Reset":
                if (jumpResetMode.is("Advanced")) {
                    if (mc.thePlayer.hurtTime == 9) {
                        hitsCount++;
                    }
                    ticksCount++;
                }
                break;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId()) {
            switch (mode.getValue()) {
                case "Cancel":
                    event.setCancelled(true);
                    break;

                case "Air":
                    if (!isEnabled(LongJump.class)) {
                        event.setCancelled(true);
                        if (mc.thePlayer.onGround)
                            mc.thePlayer.motionY = (double) s12.getMotionY() / 8000;
                    }
                    break;

                case "Horizontal":
                    if (!isEnabled(LongJump.class)) {
                        s12.motionX = (int) (mc.thePlayer.motionX * 8000);
                        s12.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    }
                    break;

                case "Boost":
                    if (mc.thePlayer.onGround) {
                        s12.motionX = (int) (mc.thePlayer.motionX * 8000);
                        s12.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    } else {
                        veloPacket = true;
                    }
                    break;

                case "Watchdog":
                    if (!isEnabled(LongJump.class)) {
                        if (!mc.thePlayer.onGround) {
                            if (!absorbedVelocity) {
                                event.setCancelled(true);
                                absorbedVelocity = true;
                                return;
                            }
                        }
                        s12.motionX = (int) (mc.thePlayer.motionX * 8000);
                        s12.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    }
                    break;

                case "GrimAC":
                    switch (grimMode.getValue()) {
                        case "Reduce":
                            if (getModule(KillAura.class).target != null && !isEnabled(Scaffold.class)) {
                                event.setCancelled(true);

                                if (!mc.thePlayer.serverSprintState) {
                                    PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                                }

                                for (int i = 0; i < 8; i++) {
                                    sendPacketNoEvent(new C02PacketUseEntity(getModule(KillAura.class).target, C02PacketUseEntity.Action.ATTACK));
                                    sendPacketNoEvent(new C0APacketAnimation());
                                }

                                double velocityX = s12.getMotionX() / 8000.0;
                                double velocityZ = s12.getMotionZ() / 8000.0;

                                if (MathHelper.sqrt_double(velocityX * velocityX * velocityZ * velocityZ) <= 3F) {
                                    mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                                } else {
                                    mc.thePlayer.motionX = velocityX * 0.078;
                                    mc.thePlayer.motionZ = velocityZ * 0.078;
                                }

                                mc.thePlayer.motionY = s12.getMotionY() / 8000.0;
                            }
                            break;

                        case "1.17":
                            if (canCancel) {
                                canCancel = false;
                                canSpoof = true;
                                event.setCancelled(true);
                            }
                            break;
                    }
                    break;

                case "Jump Reset":
                    if (jumpResetMode.is("Packet")) {
                    veloPacket = true;
                } else if (jumpResetMode.is("Advanced")) {
                    double velocityX = s12.getMotionX() / 8000.0;
                    double velocityY = s12.getMotionY() / 8000.0;
                    double velocityZ = s12.getMotionZ() / 8000.0;

                    isFallDamage = velocityX == 0.0 && velocityZ == 0.0 && velocityY < 0;
                }
                break;

                case "Reduce":
                    veloPacket = true;
                    break;
            }
        }

        if (mode.is("GrimAC") && grimMode.is("1.17")) {
            if (event.getPacket() instanceof S19PacketEntityStatus s19PacketEntityStatus) {
                if (s19PacketEntityStatus.getEntity(mc.theWorld) == mc.thePlayer) {
                    canCancel = true;
                }
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(mode.getValue());
        switch (mode.getValue()) {
            case "GrimAC": {
                if (grimMode.getValue().equals("Reduce")) {
                    if (event.isPre()) {
                        if (lastSprint == 0) {
                            lastSprint--;
                            if (!MovementUtils.canSprint(true))
                                sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                        } else if (lastSprint > 0) {
                            lastSprint--;
                            if (mc.thePlayer.onGround && !MovementUtils.canSprint(true)) {
                                lastSprint = -1;
                                sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                            }
                        }
                    }
                }
            }
            case "Boost":
                if (veloPacket) {
                    idk++;
                }
                if (idk == reverseTick.getValue()) {
                    MovementUtils.strafe(MovementUtils.getSpeed() * reverseStrength.getValue(), RotationUtils.currentRotation != null ? RotationUtils.currentRotation[0] : MovementUtils.getDirection());
                    veloPacket = false;
                    idk = 0;
                }
                break;
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (mode.is("Jump Reset")) {
            boolean shouldJump = false;

            if (jumpResetMode.is("Packet") && veloPacket) {
                shouldJump = true;
            } else if (jumpResetMode.is("Hurt Time") && mc.thePlayer.hurtTime >= jumpResetHurtTime.getValue()) {
                shouldJump = true;
            } else if (jumpResetMode.is("Advanced")) {
                if (random.nextInt(100) > jumpResetChance.getValue()) return;

                boolean hitsCondition = hitsCount >= hitsUntilJump.getValue();
                boolean ticksCondition = ticksCount >= ticksUntilJump.getValue();

                shouldJump = mc.thePlayer.hurtTime == 9 && mc.thePlayer.isSprinting() &&
                        !isFallDamage && (hitsCondition || ticksCondition);
            }

            if (shouldJump && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !checks()) {
                mc.thePlayer.jump();
                veloPacket = false;
                hitsCount = 0;
                ticksCount = 0;
            }
        }
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if(mode.is("Reduce")) {
            if (mc.thePlayer.hurtTime == reduceHurtTime.getValue() && System.currentTimeMillis() - lastAttackTime <= 8000) {
                mc.thePlayer.motionX *= reduceFactor.getValue();
                mc.thePlayer.motionZ *= reduceFactor.getValue();
            }

            lastAttackTime = System.currentTimeMillis();
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (mode.is("Legit") && getModule(KillAura.class).target != null && mc.thePlayer.hurtTime > 0) {
            ArrayList<Vec3> vec3s = new ArrayList<>();
            HashMap<Vec3, Integer> map = new HashMap<>();
            Vec3 playerPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            Vec3 onlyForward = PlayerUtils.getPredictedPos(1.0F, 0.0F).add(playerPos);
            Vec3 strafeLeft = PlayerUtils.getPredictedPos(1.0F, 1.0F).add(playerPos);
            Vec3 strafeRight = PlayerUtils.getPredictedPos(1.0F, -1.0F).add(playerPos);
            map.put(onlyForward, 0);
            map.put(strafeLeft, 1);
            map.put(strafeRight, -1);
            vec3s.add(onlyForward);
            vec3s.add(strafeLeft);
            vec3s.add(strafeRight);
            Vec3 targetVec = new Vec3(getModule(KillAura.class).target.posX, getModule(KillAura.class).target.posY, getModule(KillAura.class).target.posZ);
            vec3s.sort(Comparator.comparingDouble(targetVec::distanceXZTo));
            if (!mc.thePlayer.movementInput.sneak) {
                System.out.println(map.get(vec3s.get(0)));
                mc.thePlayer.movementInput.moveStrafe = map.get(vec3s.get(0));
            }
        }
    }

    private boolean checks() {
        return mc.thePlayer.isInWeb || mc.thePlayer.isInLava() || mc.thePlayer.isBurning() || mc.thePlayer.isInWater();
    }
}