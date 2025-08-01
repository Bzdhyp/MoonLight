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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import com.cubk.EventTarget;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;

@ModuleInfo(name = "ClientSpoofer", category = Categor.Misc)
public class ClientSpoofer extends Module {
    public final ListValue mode = new ListValue("Mode", new String[]{"Lunar", "Feather"}, "Lunar", this);

    @EventTarget
    public void onPacket(PacketEvent packetEvent) {
        if (packetEvent.getPacket() instanceof C17PacketCustomPayload packet) {

            String data = switch (mode.getValue()) {
                case "Lunar" -> "lunarclient:v2.14.5-2411";
                case "Feather" -> "Feather Forge";
                default -> "";
            };

            ByteBuf byteBuf = Unpooled.wrappedBuffer(data.getBytes());
            PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(byteBuf));

            packet.setData(buffer);
        }
    }
}
