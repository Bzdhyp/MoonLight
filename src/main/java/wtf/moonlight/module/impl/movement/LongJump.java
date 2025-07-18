package wtf.moonlight.module.impl.movement;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.EnumChatFormatting;
import org.lwjglx.input.Mouse;
import wtf.moonlight.Client;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.gui.notification.NotificationManager;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.gui.notification.NotificationType;
import wtf.moonlight.util.packet.PacketUtils;

import java.util.concurrent.LinkedBlockingQueue;

@ModuleInfo(name = "LongJump", category = Categor.Movement)
public class LongJump extends Module {
    private int kbCount = 0;
    public static LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();

    @EventTarget
    public void onEnable() {
        if (getFireball() == -1) {
            NotificationManager.post(NotificationType.WARNING,
                    EnumChatFormatting.RED + "Inventory",
                    "You don't have a Fireball.", 5);
            this.toggle();
            return;
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (!packets.isEmpty()) {
            packets.forEach(PacketUtils::queue);
            packets.clear();
        }
        kbCount = 0;
    }

    @EventTarget
    public void onRender2d(Render2DEvent event) {
        ScaledResolution sr = event.scaledResolution();
        Fonts.Bold.get(18).drawString("KB Count: " + kbCount, sr.getScaledWidth() / 2 - Fonts.Bold.get(18).getStringWidth("KB Count: " + kbCount) / 2, sr.getScaledHeight() / 2 - 18, -1);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof S12PacketEntityVelocity || packet instanceof C0FPacketConfirmTransaction
                || packet instanceof C00PacketKeepAlive || packet instanceof S00PacketKeepAlive) {
            if ((packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId())) {
                kbCount++;
                packets.add(packet);
                event.setCancelled(true);
            }
            if (!(packet instanceof S12PacketEntityVelocity)) {
                packets.add(packet);
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (Mouse.isButtonDown(4)) {
            if (!packets.isEmpty()) {
                packets.forEach(PacketUtils::queue);
                packets.clear();
            }
            kbCount = 0;
        }
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