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
package wtf.moonlight.util.packet;

import net.minecraft.network.Packet;
import wtf.moonlight.util.TimerUtil;

public class TimedPacket {

    private final Packet packet;
    private final TimerUtil time;
    private final long millis;

    public TimedPacket(Packet packet) {
        this.packet = packet;
        this.time = new TimerUtil();
        this.millis = System.currentTimeMillis();
    }

    public TimedPacket(final Packet packet, final long millis) {
        this.packet = packet;
        this.millis = millis;
        this.time = null;
    }

    public Packet getPacket() {
        return packet;
    }

    public TimerUtil getCold() {
        return getTime();
    }

    public TimerUtil getTime() {
        return time;
    }

    public long getMillis() {
        return millis;
    }

}
