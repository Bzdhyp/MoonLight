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
package wtf.moonlight.module.impl.misc;

import lombok.Getter;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.gui.notification.NotificationManager;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.MultiBoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.gui.notification.NotificationType;
import wtf.moonlight.util.ServerUtil;
import wtf.moonlight.util.packet.PacketUtils;
import wtf.moonlight.util.player.MovementUtil;
import wtf.moonlight.util.player.RotationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Disabler", category = Categor.Misc)
public class Disabler extends Module {

    public final MultiBoolValue options = new MultiBoolValue("Disablers", Arrays.asList(
            new BoolValue("Watchdog Motion", false),
            new BoolValue("Watchdog Inventory", false),
            new BoolValue("Matrix Semi", false),
            new BoolValue("Grim", false),
            new BoolValue("Verus", false),
            new BoolValue("Miniblox", false)
    ), this);

    public final SliderValue tick = new SliderValue("Tick",3,1,10,this,() -> options.isEnabled("Watchdog Motion"));
    public final MultiBoolValue grim = new MultiBoolValue("Grim Addons", Arrays.asList(new BoolValue("Post", false),
            new BoolValue("Speed Mine", false), new BoolValue("BadPackets F", false),
            new BoolValue("BadPackets G", false)), this, () -> options.isEnabled("Grim"));

    public final MultiBoolValue verus = new MultiBoolValue("Verus Addons", List.of(new BoolValue("Combat", false)), this, () -> options.isEnabled("Verus"));
    public final BoolValue singlePlayerCheck = new BoolValue("Singleplayer Check", true, this, () -> options.isEnabled("Watchdog Motion"));
    public int testTicks;
    private boolean jump = false;
    private boolean lastResult = false;
    public boolean disabled = false;
    @Getter
    private final CopyOnWriteArrayList<Packet<INetHandler>> storedPackets = new CopyOnWriteArrayList<>();
    @Getter
    private final ConcurrentLinkedDeque<Integer> pingPackets = new ConcurrentLinkedDeque<>();
    private final ArrayList<Packet<?>> packetsQueue = new ArrayList<>();
    private boolean sprinting;
    private boolean sneaking;
    private double lastMotionX = 0.0;
    private double lastMotionY = 0.0;
    private double lastMotionZ = 0.0;
    private boolean pendingFlagApplyPacket = false;
    public boolean transaction;

    @Override
    public void onEnable() {
        testTicks = 0;
        jump = false;
        sprinting = false;
        sneaking = false;
    }

    @Override
    public void onDisable() {
        if(!packetsQueue.isEmpty()) {
            for(Packet<?> packet : packetsQueue) {
                PacketUtils.sendPacketNoEvent(packet);
            }

            packetsQueue.clear();
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event){
        disabled = false;
        jump = true;
        testTicks = 0;
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        final List<BoolValue> enabledBooleanSettings = options.getValues().stream().filter(BoolValue::get).toList();

        if(enabledBooleanSettings.size() == 1) {
            setTag(enabledBooleanSettings.get(0).getName());
        } else if(enabledBooleanSettings.size() > 1) {
            setTag(enabledBooleanSettings.size() + " Enabled");
        } else {
            setTag("None");
        }

        if (options.isEnabled("Watchdog Motion")) {
            if (mc.isSingleplayer() && singlePlayerCheck.get())
                return;

            if (!checkCompass()) {
                if (mc.thePlayer.onGround && jump) {
                    jump = false;
                    disabled = true;
                    mc.thePlayer.jump();
                } else if (testTicks != -1 && disabled && mc.thePlayer.offGroundTicks >= tick.getValue()) {
                    if (mc.thePlayer.offGroundTicks % 2 == 0) {
                        RotationUtil.setRotation(new float[]{(float) (mc.thePlayer.rotationYaw - (10) + (Math.random() - 0.5) * 3), mc.thePlayer.rotationPitch});
                        event.setX(event.getX() + 0.095+Math.random() / 100);
                    }

                    MovementUtil.stop();
                }
            }
        }

        if (options.isEnabled("Grim")) {
            if (grim.isEnabled("Post")) {
                if (event.isPre() && !getPost()) {
                    processPackets();
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.thePlayer == null) return;
        Packet<?> packet = event.getPacket();

        if (options.isEnabled("Watchdog Motion")) {
            if (mc.isSingleplayer() && singlePlayerCheck.get())
                return;

            if (packet instanceof S08PacketPlayerPosLook) {
                if(testTicks != -1)
                    testTicks++;
                if (testTicks >= 20) {
                    disabled = false;
                    testTicks = -1;
                    NotificationManager.post(NotificationType.OKAY, "Successfully Disabled Watchdog.", "Enjoy lowhopping yay", 3);
                } else {
                    mc.thePlayer.motionY = mc.thePlayer.motionZ = mc.thePlayer.motionX = 0;
                }
                if (disabled) {
                    NotificationManager.post(NotificationType.WARNING, "Don't Move! ", "Disabling Motion Checks", 3);
                }
            }
        }

        if (options.isEnabled("Watchdog Inventory")) {
            if (!ServerUtil.isHypixelLobby()) return;

            if(packet instanceof C16PacketClientStatus || packet instanceof C0EPacketClickWindow) {
                event.setCancelled(true);
                packetsQueue.add(packet);
            } else if(packet instanceof C0DPacketCloseWindow) {
                if(!packetsQueue.isEmpty()) {
                    for(Packet<?> i : packetsQueue) {
                        PacketUtils.sendPacketNoEvent(i);
                    }

                    packetsQueue.clear();
                }
            }
        }

        if (options.isEnabled("Grim")) {
            if (packet instanceof C0BPacketEntityAction wrapped) {
                if (grim.isEnabled("BadPackets F")) {
                    if (wrapped.getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                        if (sprinting)
                            event.setCancelled(true);

                        sprinting = true;
                    }

                    if (wrapped.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                        if (!sprinting)
                            event.setCancelled(true);

                        sprinting = false;
                    }
                }

                if (grim.isEnabled("BadPackets G")) {
                    if (wrapped.getAction() == C0BPacketEntityAction.Action.START_SNEAKING) {
                        if (sneaking)
                            event.setCancelled(true);

                        sneaking = true;
                    }

                    if (wrapped.getAction() == C0BPacketEntityAction.Action.STOP_SNEAKING) {
                        if (!sneaking)
                            event.setCancelled(true);

                        sneaking = false;
                    }
                }
            }
            if (grim.isEnabled("Speed Mine")) {
                if (packet instanceof C07PacketPlayerDigging wrapped) {
                    if (wrapped.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                        sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, wrapped.getPosition(), wrapped.getFacing()));
                    }
                }
            }
        }

        if (options.isEnabled("Matrix Semi")) {
            if (packet instanceof C03PacketPlayer.C06PacketPlayerPosLook && pendingFlagApplyPacket) {
                pendingFlagApplyPacket = false;
                mc.thePlayer.motionX = lastMotionX;
                mc.thePlayer.motionY = lastMotionY;
                mc.thePlayer.motionZ = lastMotionZ;
            } else if (packet instanceof S08PacketPlayerPosLook) {
                pendingFlagApplyPacket = true;
                lastMotionX = mc.thePlayer.motionX;
                lastMotionY = mc.thePlayer.motionY;
                lastMotionZ = mc.thePlayer.motionZ;
            }
        }

        if (options.isEnabled("Miniblox")) {
            if (packet instanceof S08PacketPlayerPosLook s08 && mc.thePlayer.ticksExisted >= 100) {
                event.setCancelled(true);

                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(
                                s08.getX(),
                                s08.getY(),
                                s08.getZ(),
                                s08.getYaw(),
                                s08.getPitch(),
                                mc.thePlayer.onGround
                        )
                );

                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ,
                                mc.thePlayer.rotationYaw,
                                mc.thePlayer.rotationPitch,
                                mc.thePlayer.onGround
                        )
                );
            }
        }


        if(options.isEnabled("Verus")){
            if(verus.isEnabled("Combat")){
                if (packet instanceof S32PacketConfirmTransaction) {
                    event.setCancelled(true);
                    sendPacketNoEvent(new C0FPacketConfirmTransaction((transaction ? 1 : -1), (short) (transaction  ? 1 : -1), transaction));
                    transaction = !transaction;
                }
            }
        }
    }

    public boolean postDelay(Packet<?> packet) {
        if (mc.thePlayer == null && !isEnabled(this.getClass())) {
            return false;
        }
        if (packet instanceof S12PacketEntityVelocity velocityPacket) {
            return velocityPacket.getEntityID() == mc.thePlayer.getEntityId();
        } else return packet instanceof S32PacketConfirmTransaction ||
                packet instanceof S22PacketMultiBlockChange ||
                packet instanceof S04PacketEntityEquipment ||
                packet instanceof S08PacketPlayerPosLook ||
                packet instanceof S06PacketUpdateHealth ||
                packet instanceof S23PacketBlockChange ||
                packet instanceof S27PacketExplosion ||
                packet instanceof S00PacketKeepAlive ||
                packet instanceof S0FPacketSpawnMob ||
                packet instanceof S14PacketEntity;
    }

    public void processPackets() {
        if (options.isEnabled("Grim")) {
            if (!storedPackets.isEmpty()) {
                for (Packet<INetHandler> packet : storedPackets) {
                    PacketEvent event = new PacketEvent(packet, PacketEvent.State.INCOMING);

                    INSTANCE.getEventManager().call(event);

                    if (!event.isCancelled()) {
                        packet.processPacket(mc.getNetHandler());
                    }
                }
                storedPackets.clear();
            }
        }
    }

    public boolean getPost() {
        boolean result = mc.thePlayer != null && mc.thePlayer.isEntityAlive() && mc.thePlayer.ticksExisted >= 10 && !(mc.currentScreen instanceof GuiDownloadTerrain);
        if (this.lastResult && !result) {
            this.lastResult = false;
            mc.addScheduledTask(this::processPackets);
        }
        return this.lastResult = result;
    }

    public boolean checkCompass(){
        boolean compass = false;
        for (int i = 0; i < 9; i++) {
            final ItemStack stackInSlot = mc.thePlayer.inventory.getStackInSlot(i);

            if (stackInSlot != null && stackInSlot.getUnlocalizedName().toLowerCase().contains("compass")) {
                compass = true;
            }
        }
        return compass;
    }
}
