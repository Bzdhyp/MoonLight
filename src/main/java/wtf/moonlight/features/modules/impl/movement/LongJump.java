package wtf.moonlight.features.modules.impl.movement;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.EnumChatFormatting;
import wtf.moonlight.Moonlight;
import wtf.moonlight.events.annotations.EventTarget;
import wtf.moonlight.events.impl.packet.PacketEvent;
import wtf.moonlight.events.impl.player.*;
import wtf.moonlight.features.modules.Module;
import wtf.moonlight.features.modules.ModuleCategory;
import wtf.moonlight.features.modules.ModuleInfo;
import wtf.moonlight.features.values.impl.ModeValue;
import wtf.moonlight.features.values.impl.SliderValue;
import wtf.moonlight.gui.notification.NotificationType;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "LongJump", category = ModuleCategory.Movement)
public class LongJump extends Module {
    public final ModeValue mode = new ModeValue("Mode", new String[]{"Prediction", "Delay"}, "Prediction", this);
    private final SliderValue delayTime = new SliderValue("Delay Time", 1000, 0, 5000, 1, this);

    private final Queue<DelayedPacket> delayedPackets = new ConcurrentLinkedQueue<>();
    private boolean isActive;

    private static class DelayedPacket {
        final Packet<?> packet;
        final long timestamp;

        DelayedPacket(Packet<?> packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }

    @EventTarget
    public void onEnable() {
        if (getFireball() == -1) {
            Moonlight.INSTANCE.getNotificationManager().post(NotificationType.WARNING,
                    EnumChatFormatting.RED + "Inventory",
                    "You don't have a Fireball.", 5);
            this.toggle();
            return;
        }

        isActive = true;
        delayedPackets.clear();
    }

    @Override
    public void onDisable() {
        isActive = false;
        delayedPackets.clear();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!isActive) return;

        Packet<?> packet = event.getPacket();

        if (mode.is("Prediction")) {
            if (packet instanceof S12PacketEntityVelocity velocityPacket) {
                if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                    delayedPackets.add(new DelayedPacket(packet, System.currentTimeMillis()));
                    event.setCancelled(true);
                }
            }
        }

        if (shouldHideExplosion() && (
                packet instanceof S0EPacketSpawnObject ||
                        packet instanceof S2APacketParticles)) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.get());
        if (!isActive) return;

        if (mode.is("Prediction")) {
            long currentTime = System.currentTimeMillis();
            while (!delayedPackets.isEmpty()) {
                DelayedPacket entry = delayedPackets.peek();
                if (currentTime - entry.timestamp >= delayTime.get()) {
                    releasePacket(entry.packet);
                    delayedPackets.remove();
                } else {
                    break;
                }
            }
        }
    }

    private void releasePacket(Packet<?> packet) {
        if (mc.getNetHandler() != null) {
            mc.getNetHandler().addToSendQueue(packet);
        }
    }

    private boolean shouldHideExplosion() {
        return true;
    }

    private int getFireball() {
        if (mc.thePlayer == null || mc.thePlayer.inventory == null) return -1;

        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == Items.fire_charge) {
                return i;
            }
        }
        return -1;
    }
}