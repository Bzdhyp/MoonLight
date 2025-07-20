package wtf.moonlight.module.impl.combat;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.util.*;
import com.cubk.EventTarget;
import org.lwjglx.input.Keyboard;
import wtf.moonlight.Client;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.*;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.movement.LongJump;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.DebugUtil;
import wtf.moonlight.util.packet.PacketUtils;
import wtf.moonlight.util.player.MovementUtil;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RotationUtil;
import wtf.moonlight.util.vector.Vector3d;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Velocity", category = Categor.Combat)
public class Velocity extends Module {
    public final ListValue mode = new ListValue("Mode",
            new String[]{"Grim", "Legit", "Legit 2", "Polar", "Intave", "Reduce", "Boost", "Skip Tick", "Jump Reset", "Matrix Semi", "Matrix Reverse", "Polar Under-Block"}, "Skip Tick", this);

    private final ListValue grimMode = new ListValue("Grim Mode", new String[]{"Reduce", "1.17"}, "Reduce", this, () -> mode.is("Grim"));
    private final BoolValue invalidEntity = new BoolValue("Attack Invalid Entity", true, this, () -> mode.is("Grim") && grimMode.is("Reduce"));

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

    private final SliderValue reduceHurtTime = new SliderValue("Reduce Hurt Time", 9, 1, 10, 1, this, () -> mode.is("Reduce"));
    private final SliderValue reduceFactor = new SliderValue("Reduce Factor", 0.6f, 0, 1, 0.05f, this, () -> mode.is("Reduce"));

    private Entity target;
    private long lastAttackTime;

    private int idk = 0;
    private int hitsCount = 0;
    private int ticksCount = 0;
    private int skipTickCounter = 0;
    private int reduceTick, reduceDamageTick;

    boolean enable;
    private boolean attacked;
    private boolean reducing;
    private boolean isFallDamage;
    public boolean shouldVelocity;
    private boolean veloPacket = false;
    public static boolean jump = false;
    private boolean canSpoof, canCancel;

    private final Random random = new Random();
    public static List<Packet<INetHandler>> storedPackets = new CopyOnWriteArrayList<>();

    @Override
    public void onEnable() {
        ticksCount = 0;
        reducing = false;
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
        if (mode.is("Grim") && grimMode.is("Reduce")) {
            this.reset();
        }

        if (mode.is("Jump Reset") && jumpResetMode.is("Legit")) {
            mc.gameSettings.keyBindJump.setPressed(false);
            mc.gameSettings.keyBindForward.setPressed(false);
        }
    }

    private void reset() {
        this.shouldVelocity = false;
        this.target = null;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.getValue());

        if (getModule(LongJump.class).isEnabled()) return;

        if (mode.is("Skip Tick")) {
            if (skipTickCounter > 0) {
                skipTickCounter--;
                return;
            }
        }

        if (mode.is("Grim")) {
            if (grimMode.is("1.17")) {
                if (canSpoof) {
                    sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
                    sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer).down(), EnumFacing.DOWN));
                    canSpoof = false;
                }
            }
        }

        if (mode.is("Reduce")) {
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

        if (getModule(LongJump.class).isEnabled()) return;

        if (packet instanceof S12PacketEntityVelocity velocity && velocity.getEntityID() == mc.thePlayer.getEntityId()) {
            switch (mode.getValue()) {
                case "Reduce": {
                    veloPacket = true;
                    break;
                }

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

                case "Legit": {
                    if (mc.currentScreen == null) {
                        mc.gameSettings.keyBindSprint.setPressed(true);
                        mc.gameSettings.keyBindForward.setPressed(true);
                        mc.gameSettings.keyBindJump.setPressed(true);
                        mc.gameSettings.keyBindBack.setPressed(false);

                        reducing = true;
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

                case "Grim": {
                    switch (grimMode.getValue()) {
                        case "Reduce": {
                            double strength = new Vector3d(velocity.getMotionX(), velocity.getMotionY(), velocity.getMotionZ()).length();
                            if (velocity.getEntityID() == mc.thePlayer.getEntityId() && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWeb) {
                                target = getNearTarget();
                                if (target == null) return;

                                if (mc.thePlayer.getDistanceToEntity(target) > 3.3F) {
                                    reset();
                                    return;
                                }

                                shouldVelocity = true;
                                DebugUtil.print("[M]" + strength + " " + (mc.thePlayer.onGround ? "on Ground" : "on Air") + (target != null ? " - Distance: " + mc.thePlayer.getClosestDistanceToEntity(target) : ""));
                            }
                            break;
                        }

                        case "1.17": {
                            if (canCancel) {
                                canCancel = false;
                                canSpoof = true;
                                event.setCancelled(true);
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (mode.is("Grim") && grimMode.is("1.17")) {
            if (event.getPacket() instanceof S19PacketEntityStatus s19PacketEntityStatus) {

                if (s19PacketEntityStatus.getEntity(mc.theWorld) == mc.thePlayer) {
                    canCancel = true;
                }
            }
        }
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (mode.is("Reduce")) {
            if (mc.thePlayer.hurtTime == reduceHurtTime.getValue() && System.currentTimeMillis() - lastAttackTime <= 8000) {
                mc.thePlayer.motionX *= reduceFactor.getValue();
                mc.thePlayer.motionZ *= reduceFactor.getValue();
            }

            lastAttackTime = System.currentTimeMillis();
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (getModule(LongJump.class).isEnabled()) return;

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

        if (mode.is("Legit 2")) {
            if (reducing) {
                if (mc.currentScreen == null) {
                    resetKeybindings(mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindForward,
                            mc.gameSettings.keyBindJump, mc.gameSettings.keyBindBack);
                }

                reducing = false;
            }
        }
    }

    @EventTarget
    public void onVelocity(VelocityEvent event) {
        if (mc.thePlayer == null) return;

        if (this.shouldVelocity) {
            if (mc.thePlayer.getDistanceToEntity(target) > 3.0) {
                this.reset();
                return;
            }

            if (!mc.thePlayer.serverSprintState) {
                PacketUtils.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                mc.thePlayer.setSprinting(true);
            }

            for (int i = 0; i < 5; i++) {
                PacketUtils.sendPacket(new C0APacketAnimation());
                PacketUtils.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
            }

            if (!mc.thePlayer.serverSprintState) {
                PacketUtils.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }

            event.setReduceAmount(0.07776D);
            this.shouldVelocity = false;
        }
    }

    private Entity getNearTarget() {
        Entity target = null;
        EntityLivingBase clientTarget = getModule(KillAura.class).target;
        if (clientTarget != null) {
            target = clientTarget;
            return target;
        } else {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!entity.equals(mc.thePlayer) && !entity.isDead && invalidEntity.get()) {
                    if (entity instanceof EntityArrow entityArrow) {
                        if (entityArrow.ticksInGround <= 0) target = entityArrow;
                    }

                    if (entity instanceof EntitySnowball) target = entity;

                    if (entity instanceof EntityEgg) target = entity;

                    if (entity instanceof EntityTNTPrimed) target = entity;

                    if (entity instanceof EntityFishHook) target = entity;
                }
            }
        }
        return target;
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (getModule(LongJump.class).isEnabled()) return;

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
        if (getModule(LongJump.class).isEnabled()) return;

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

    public static boolean isPressed(KeyBinding key) {
        return Keyboard.isKeyDown(key.getKeyCode());
    }

    public static void resetKeybinding(KeyBinding key) {
        if (mc.currentScreen != null) {
            key.setPressed(false);
        } else {
            key.setPressed(isPressed(key));
        }
    }

    public static void resetKeybindings(KeyBinding... keys) {
        for (KeyBinding key : keys) {
            resetKeybinding(key);
        }
    }
}
