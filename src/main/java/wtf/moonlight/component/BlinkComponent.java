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
package wtf.moonlight.component;

import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import com.cubk.EventPriority;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.util.misc.InstanceAccess;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.packet.PacketUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class BlinkComponent implements InstanceAccess {

    public static final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    public static boolean blinking, dispatch;
    public static ArrayList<Class<?>> exemptedPackets = new ArrayList<>();
    public static TimerUtil exemptionWatch = new TimerUtil();

    public static void setExempt(Class<?>... packets) {
        exemptedPackets = new ArrayList<>(Arrays.asList(packets));
        exemptionWatch.reset();
    }

    @EventTarget
    @EventPriority(-1)
    public void onPacketSend(PacketEvent event){
        if (mc.thePlayer == null) {
            packets.clear();
            exemptedPackets.clear();
            return;
        }
        
        if(event.getState() == PacketEvent.State.OUTGOING) {
            if (mc.thePlayer.isDead || mc.isSingleplayer() || !mc.getNetHandler().doneLoadingTerrain) {
                packets.forEach(PacketUtils::sendPacketNoEvent);
                packets.clear();
                blinking = false;
                exemptedPackets.clear();
                return;
            }

            final Packet<?> packet = event.getPacket();

            if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart ||
                    packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing ||
                    packet instanceof C01PacketEncryptionResponse) {
                return;
            }

            if (blinking && !dispatch) {
                if (exemptionWatch.hasTimeElapsed(100)) {
                    exemptionWatch.reset();
                    exemptedPackets.clear();
                }

                if (!event.isCancelled() && exemptedPackets.stream().noneMatch(packetClass ->
                        packetClass == packet.getClass())) {
                    packets.add(packet);
                    event.setCancelled(true);
                }
            } else if (packet instanceof C03PacketPlayer) {
                packets.forEach(PacketUtils::sendPacketNoEvent);
                packets.clear();
                dispatch = false;
            }
        }
    }

    public static void dispatch() {
        dispatch = true;
    }

    @EventTarget
    @EventPriority(-1)
    public void onWorld(WorldEvent event) {
        packets.clear();
        BlinkComponent.blinking = false;
    }
}
