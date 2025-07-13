package wtf.moonlight.module.impl.combat;

import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import com.cubk.EventTarget;
import org.lwjglx.input.Keyboard;
import wtf.moonlight.Client;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.*;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.packet.PacketUtils;
import wtf.moonlight.util.player.MovementUtil;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RotationUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Velocity", category = Categor.Combat)
public class Velocity extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Grim", "Delay", "Legit", "Boost", "Jump Reset"}, "Delay", this);
    private final ListValue delayMode = new ListValue("Delay Mode", new String[]{"Packet", "Ping Spoof"}, "Packet", this, () -> mode.is("Delay"));

    private final SliderValue hurtDelay = new SliderValue("Hurt Delay", 1.0f, 0.0f, 20.0f, 0.1f, this, () -> mode.is("Grim") || (mode.is("Delay") && delayMode.is("Ping Spoof")));
    private final SliderValue pingDelay = new SliderValue("Ping Delay", 1.0f, 0.0f, 20.0f, 0.1f, this, () -> mode.is("Grim") || (mode.is("Delay") && delayMode.is("Ping Spoof")));
    public final BoolValue jumpValue = new BoolValue("Jump Rest", true, this, () -> mode.is("Grim") || mode.is("Delay") && delayMode.is("Ping Spoof"));

    private final SliderValue reverseTick = new SliderValue("Boost Tick", 1, 1, 5, 1, this, () -> mode.is("Boost"));
    private final SliderValue reverseStrength = new SliderValue("Boost Strength", 1, 0.1f, 1, 0.01f, this, () -> mode.is("Boost"));

    private final ListValue jumpResetMode = new ListValue("Jump Reset Mode", new String[]{"Legit", "Packet", "Advanced", "Hurt Time"}, "Packet", this, () -> mode.is("Jump Reset"));
    private final SliderValue jumpResetHurtTime = new SliderValue("Jump Reset Hurt Time", 9, 1, 10, 1,
            this, () -> mode.is("Jump Reset") && (jumpResetMode.is("Hurt Time") || jumpResetMode.is("Advanced")));
    private final SliderValue jumpResetChance = new SliderValue("Jump Reset Chance", 100, 0, 100, 1, this, () -> mode.is("Jump Reset") &&
            (jumpResetMode.is("Legit") || jumpResetMode.is("Advanced")));
    private final SliderValue hitsUntilJump = new SliderValue("Hits Until Jump", 2, 1, 10, 1, this, () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"));
    private final SliderValue ticksUntilJump = new SliderValue("Ticks Until Jump", 2, 1, 20, 1, this, () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"));

    private int idk = 0;
    private int velocityTicks;
    private int hitsCount = 0;
    private int ticksCount = 0;

    boolean enable;
    private boolean delay;
    private boolean isFallDamage;
    public static boolean send = false;
    private boolean veloPacket = false;
    public static boolean jump = false;
    private static boolean lastResult = false;

    private final TimerUtil timerUtil = new TimerUtil();

    private final Random random = new Random();
    private final ArrayList<Packet<?>> delayedPackets = new ArrayList<>();
    public static List<Packet<INetHandler>> storedPackets= new CopyOnWriteArrayList<>();

    @Override
    public void onEnable() {
        timerUtil.reset();
        ticksCount = 0;
        velocityTicks = 0;
        lastResult = false;
        veloPacket = false;

        storedPackets.clear();
        delayedPackets.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1f;
        ticksCount = 0;
        velocityTicks = 0;
        timerUtil.reset();
        lastResult = false;
        veloPacket = false;

        if (mode.is("Jump Reset") && jumpResetMode.is("Legit")) {
            mc.gameSettings.keyBindJump.setPressed(false);
            mc.gameSettings.keyBindForward.setPressed(false);
        }

        storedPackets.clear();
        delayedPackets.clear();
        super.onDisable();
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        if (mode.is("Jump Reset") && jumpResetMode.is("Legit")) {
            mc.gameSettings.keyBindJump.setPressed(false);
            mc.gameSettings.keyBindForward.setPressed(false);
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.getValue());

        // @Author：haogemc
        if (!send) {
            if (!storedPackets.isEmpty()) {
                if (mode.is("Grim")) {
                    for (Packet<?> p : storedPackets) {
                        if (p instanceof S12PacketEntityVelocity) {
                            storedPackets.remove(p);
                            storedPackets.add((Packet<INetHandler>) p);
                            send = timerUtil.hasTimeElapsed(hurtDelay.getValue().longValue() * 100L);
                        } else {
                            send = timerUtil.hasTimeElapsed(pingDelay.getValue().longValue() * 100L) && (mc.thePlayer.onGround || mc.thePlayer.offGroundTicks >= 12);
                        }
                    }
                }

                if (mode.is("Delay") && delayMode.is("Ping Spoof")) {
                    boolean velocity = false;
                    for (Packet<?> p : storedPackets) {
                        if (p instanceof S12PacketEntityVelocity) {
                            velocity = true;
                            storedPackets.remove(p);
                            storedPackets.add((Packet<INetHandler>) p);
                        }
                    }

                    if (velocity) {
                        send = timerUtil.hasTimeElapsed(hurtDelay.getValue().longValue() * 100L);
                    } else {
                        send = timerUtil.hasTimeElapsed(pingDelay.getValue().longValue() * 100L);
                    }

                }
            } else {
                timerUtil.reset();
            }
        }
        if (mode.is("Jump Reset")) {
            if (jumpResetMode.is("Advanced")) {
                if (mc.thePlayer.hurtTime == 9) {
                    hitsCount++;
                }
                ticksCount++;
            }

            if (jumpResetMode.is("Legit")) {
                if (mc.currentScreen == null && Client.INSTANCE.getModuleManager().getModule(KillAura.class).target != null) {
                    if (mc.thePlayer.hurtTime == 10) {
                        this.enable = MathHelper.getRandomDoubleInRange(new Random(), 0.0d, 1.0d) <= jumpResetChance.getValue().doubleValue();
                    }

                    if (this.enable) {
                        if (mc.thePlayer.hurtTime >= 8) {
                            mc.gameSettings.keyBindJump.setPressed(true);
                        }
                        if (mc.thePlayer.hurtTime >= 7) {
                            mc.gameSettings.keyBindForward.setPressed(true);
                            return;
                        }
                        if (mc.thePlayer.hurtTime >= 4) {
                            mc.gameSettings.keyBindJump.setPressed(false);
                            mc.gameSettings.keyBindForward.setPressed(false);
                        } else if (mc.thePlayer.hurtTime > 1) {
                            mc.gameSettings.keyBindForward.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
                            mc.gameSettings.keyBindJump.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
                        }
                    }
                }
            }
        }

        if (mode.is("Delay") && delayMode.is("Packet") && delay) {
            velocityTicks++;

            if (mc.thePlayer.onGround && isValid()) {
                for (Packet<?> packet : delayedPackets) {
                    PacketUtils.sendPacketNoEvent(packet);
                }
                delayedPackets.clear();
                mc.thePlayer.jump();
                delay = false;
                velocityTicks = 0;
            } else if (!isValid() || velocityTicks > 40) {
                for (Packet<?> packet : delayedPackets) {
                    PacketUtils.sendPacketNoEvent(packet);
                }
                delayedPackets.clear();
                delay = false;
                velocityTicks = 0;
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mode.is("Grim") || (delayMode.is("Ping Spoof") && mode.is("Delay"))) {
            if (mc.thePlayer.motionX == 0 && mc.thePlayer.onGround && mc.thePlayer.motionZ == 0 || mc.thePlayer.fallDistance > 1.8f || this.mc.currentScreen != null || mc.timer.timerSpeed != 1.0f) {
                processPackets();
                send = false;
            }

            if (jump) {
                if (!mc.thePlayer.onGround) {
                    int sprintKey = mc.gameSettings.keyBindJump.getKeyCode();
                    KeyBinding.setKeyBindState(sprintKey, Keyboard.isKeyDown(sprintKey));
                    jump = false;
                }
            }

            if (send && !jump) {
                timerUtil.reset();
                processPackets();
                send = false;
            } else {
                if (!storedPackets.isEmpty() && jumpValue.get()) {
                    boolean velocity = false;
                    for (Packet<?> p : storedPackets) {
                        if (p instanceof S12PacketEntityVelocity) {

                            velocity = true;
                            break;
                        }
                    }
                    if (velocity) {
                        if (mc.thePlayer.onGround && !jump) {
                            if (!mc.thePlayer.isBurning() && !jump) {
                                int sprintKey = mc.gameSettings.keyBindJump.getKeyCode();
                                KeyBinding.setKeyBindState(sprintKey, true);
                                jump = true;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (mode.is("Delay") && delayMode.is("Packet")) {
            if (event.getState() == PacketEvent.State.OUTGOING) {
                if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart ||
                        packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing ||
                        packet instanceof C01PacketEncryptionResponse || packet instanceof C00PacketKeepAlive ||
                        packet instanceof C02PacketUseEntity || packet instanceof C0APacketAnimation ||
                        packet instanceof C0BPacketEntityAction) {
                    return;
                }
            }
        }

        if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId()) {
            switch (mode.getValue()) {
                case "Boost": {
                    if (mc.thePlayer.onGround) {
                        s12.motionX = (int) (mc.thePlayer.motionX * 8000);
                        s12.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    } else {
                        veloPacket = true;
                    }
                    break;
                }
                case "Delay": {
                    if (delayMode.is("Packet")) {
                        if (isValid()) {
                            velocityTicks = 0;
                            if (!delay) {
                                delayedPackets.clear();
                                delay = true;
                            }
                        }

                        if (event.getState() == PacketEvent.State.OUTGOING && delay) {
                            event.setCancelled(true);
                            delayedPackets.add(event.getPacket());
                        }
                    }
                    break;
                }
                case "Jump Reset": {
                    if (jumpResetMode.is("Packet")) {
                        veloPacket = true;
                    } else if (jumpResetMode.is("Advanced")) {
                        double velocityX = s12.getMotionX() / 8000.0;
                        double velocityY = s12.getMotionY() / 8000.0;
                        double velocityZ = s12.getMotionZ() / 8000.0;

                        isFallDamage = velocityX == 0.0 && velocityZ == 0.0 && velocityY < 0;
                    }
                    break;
                }
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(mode.getValue());
        if (mode.is("Boost")) {
            if (veloPacket) {
                idk++;
            }
            if (idk == reverseTick.getValue()) {
                MovementUtil.strafe(MovementUtil.getSpeed() * reverseStrength.getValue(), RotationUtil.currentRotation != null ? RotationUtil.currentRotation[0] : MovementUtil.getDirection());
                veloPacket = false;
                idk = 0;
            }
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
    public void onMoveInput(MoveInputEvent event) {
        if (mode.is("Legit") && getModule(KillAura.class).target != null && mc.thePlayer.hurtTime > 0) {
            ArrayList<Vec3> vec3s = new ArrayList<>();
            HashMap<Vec3, Integer> map = new HashMap<>();
            Vec3 playerPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            Vec3 onlyForward = PlayerUtil.getPredictedPos(1.0F, 0.0F).add(playerPos);
            Vec3 strafeLeft = PlayerUtil.getPredictedPos(1.0F, 1.0F).add(playerPos);
            Vec3 strafeRight = PlayerUtil.getPredictedPos(1.0F, -1.0F).add(playerPos);
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

    private boolean isValid() {
        return mc.thePlayer != null &&
                !mc.thePlayer.isDead &&
                !mc.thePlayer.isRiding() &&
                mc.thePlayer.hurtResistantTime <= 10 && (isEnabled(KillAura.class) && Client.INSTANCE.getModuleManager().getModule(KillAura.class).target != null);
    }

    public static boolean getGrimPost() {
        boolean result = Client.INSTANCE.getModuleManager().getModule(Velocity.class).isEnabled() &&
                (Client.INSTANCE.getModuleManager().getModule(Velocity.class).delayMode.is("Ping Spoof") && Client.INSTANCE.getModuleManager().getModule(Velocity.class).mode.is("Delay")) &&
                mc.thePlayer != null && mc.thePlayer.isEntityAlive() && mc.thePlayer.ticksExisted >= 10 && !(mc.currentScreen instanceof GuiDownloadTerrain);
        if (lastResult && !result) {
            lastResult = false;
            mc.addScheduledTask(Velocity::processPackets);
        }

        return lastResult = result;
    }

    public static boolean grimPostDelay(final Packet<?> packet) {
        if (mc.thePlayer == null) return false;

        if (mc.currentScreen instanceof GuiDownloadTerrain) return false;

        if (packet instanceof S12PacketEntityVelocity sPacketEntityVelocity) {
            return sPacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId();
        }

        return packet instanceof S32PacketConfirmTransaction || packet instanceof S00PacketKeepAlive;
    }

    public static void processPackets() {
        if (!storedPackets.isEmpty()) {
            for (final Packet<INetHandler> packet : storedPackets) {
                PacketEvent event = new PacketEvent(packet, PacketEvent.State.INCOMING);
                Client.INSTANCE.getEventManager().call(event);

                if (event.isCancelled()) continue;

                packet.processPacket(mc.getNetHandler());
            }
            storedPackets.clear();
        }
    }
}
