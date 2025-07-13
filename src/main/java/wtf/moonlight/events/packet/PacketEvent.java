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
package wtf.moonlight.events.packet;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import com.cubk.impl.CancellableEvent;

@Getter
public class PacketEvent extends CancellableEvent {
    @Setter
    private Packet<?> packet;
    private final INetHandler netHandler;
    private final EnumPacketDirection direction;
    private final State state;

    public PacketEvent(Packet<?> packet, State state) {
        this(packet, null, null, state);
    }

    public PacketEvent(Packet<?> packet, INetHandler netHandler, EnumPacketDirection direction, State state) {
        this.packet = packet;
        this.netHandler = netHandler;
        this.direction = direction;
        this.state = state;
    }

    public enum State {
        INCOMING,
        OUTGOING
    }
}