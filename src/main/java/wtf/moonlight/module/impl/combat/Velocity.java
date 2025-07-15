package wtf.moonlight.module.impl.combat;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import com.cubk.EventTarget;
import wtf.moonlight.Client;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.*;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.player.MovementUtil;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RotationUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Velocity", category = Categor.Combat)
public class Velocity extends Module {
    public final ListValue mode = new ListValue("Mode",
            new String[]{"Delay", "Legit", "Polar", "Intave", "Boost", "Skip Tick", "Jump Reset", "Matrix Semi", "Matrix Reverse", "Polar Under-Block"}, "Skip Tick", this);

    private final SliderValue reverseTick = new SliderValue("Boost Tick", 1, 1, 5, 1, this, () -> mode.is("Boost"));
    private final SliderValue reverseStrength = new SliderValue("Boost Strength", 1, 0.1f, 1, 0.01f, this, () -> mode.is("Boost"));

    private final SliderValue skipTicks = new SliderValue("Skip Ticks", 1, 1, 20, 1, this, () -> mode.is("Skip Tick"));
    private final SliderValue skipChance = new SliderValue("Skip Chance", 100, 0, 100, 1, this, () -> mode.is("Skip Tick"));

    private final ListValue jumpResetMode = new ListValue("Jump Reset Mode", new String[]{"Legit", "Packet", "Advanced", "Hurt Time"}, "Packet", this, () -> mode.is("Jump Reset"));
    private final SliderValue jumpResetHurtTime = new SliderValue("Jump Reset Hurt Time", 9, 1, 10, 1,
            this, () -> mode.is("Jump Reset") && (jumpResetMode.is("Hurt Time") || jumpResetMode.is("Advanced")));
    private final SliderValue jumpResetChance = new SliderValue("Jump Reset Chance", 100, 0, 100, 1, this, () -> mode.is("Jump Reset") &&
            (jumpResetMode.is("Legit") || jumpResetMode.is("Advanced")));
    private final SliderValue hitsUntilJump = new SliderValue("Hits Until Jump", 2, 1, 10, 1, this, () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"));
    private final SliderValue ticksUntilJump = new SliderValue("Ticks Until Jump", 2, 1, 20, 1, this, () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"));

    private int idk = 0;
    private int hitsCount = 0;
    private int ticksCount = 0;
    private int skipTickCounter = 0;

    boolean enable;
    private boolean attacked;
    private boolean isFallDamage;
    private boolean veloPacket = false;
    public static boolean jump = false;

    private final Random random = new Random();
    public static List<Packet<INetHandler>> storedPackets= new CopyOnWriteArrayList<>();

    @Override
    public void onEnable() {
        ticksCount = 0;
        veloPacket = false;
        skipTickCounter = 0;

        storedPackets.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ticksCount = 0;
        veloPacket = false;
        skipTickCounter = 0;

        if (mode.is("Jump Reset") && jumpResetMode.is("Legit")) {
            mc.gameSettings.keyBindJump.setPressed(false);
            mc.gameSettings.keyBindForward.setPressed(false);
        }

        storedPackets.clear();
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

        if (mode.is("Skip Tick")) {
            if (skipTickCounter > 0) {
                skipTickCounter--;
                return;
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

                    if (this.enable && getModule(KillAura.class).isEnabled()) {
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
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S12PacketEntityVelocity velocity && velocity.getEntityID() == mc.thePlayer.getEntityId()) {
            switch (mode.getValue()) {
                case "Skip Tick": {
                    if (random.nextInt(100) < skipChance.getValue()) {
                        skipTickCounter = skipTicks.getValue().intValue();
                    }
                    break;
                }

                case "Boost": {
                    if (mc.thePlayer.onGround) {
                        velocity.motionX = (int) (mc.thePlayer.motionX * 8000);
                        velocity.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    } else {
                        veloPacket = true;
                    }
                    break;
                }

                case "Polar Under-Block": {
                    AxisAlignedBB axisAlignedBB = mc.thePlayer.getEntityBoundingBox().offset(0.0, 1.0, 0.0);

                    if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, axisAlignedBB).isEmpty()) {
                        event.setCancelled(true);
                        mc.thePlayer.motionY = ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0;
                    }
                    break;
                }

                case "Matrix Reverse": {
                    if (mc.thePlayer.hurtTime > 0) {
                        mc.thePlayer.motionX *= -0.3;
                        mc.thePlayer.motionZ *= -0.3;
                    }
                    break;
                }
                case "Matrix Semi": {
                    if (mc.thePlayer.hurtTime > 0) {
                        mc.thePlayer.motionX *= 0.6;
                        mc.thePlayer.motionZ *= 0.6;
                    }
                    break;
                }

                case "Polar": {
                    if (mc.thePlayer.isSwingInProgress) {
                        attacked = true;
                    }

                    if (mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY) && mc.thePlayer.hurtTime > 0 && !attacked) {
                        mc.thePlayer.motionX *= 0.45D;
                        mc.thePlayer.motionZ *= 0.45D;
                        mc.thePlayer.setSprinting(false);
                    }

                    attacked = false;
                    break;
                }

                case "Intave": {
                    if (mc.thePlayer.isSwingInProgress) {
                        attacked = true;
                    }

                    if (mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY) && mc.thePlayer.hurtTime > 0 && !attacked) {
                        mc.thePlayer.motionX *= 0.6D;
                        mc.thePlayer.motionZ *= 0.6D;
                        mc.thePlayer.setSprinting(false);
                    }

                    attacked = false;
                    break;
                }

                case "Jump Reset": {
                    if (jumpResetMode.is("Packet")) {
                        veloPacket = true;
                    } else if (jumpResetMode.is("Advanced")) {
                        double velocityX = velocity.getMotionX() / 8000.0;
                        double velocityY = velocity.getMotionY() / 8000.0;
                        double velocityZ = velocity.getMotionZ() / 8000.0;

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
}
