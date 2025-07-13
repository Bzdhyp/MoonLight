package wtf.moonlight.module.impl.combat;

import com.cubk.EventTarget;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;

import java.util.ArrayList;

@ModuleInfo(name = "TimerRange", category = Categor.Combat)
public class TimerRange extends Module {
    private final ArrayList<Integer> diffs = new ArrayList<>();
    public long balanceCounter = 0L;
    private long lastTime;
    private WorldClient lastWorld = null;

    @EventTarget
    public void onEventSendPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (event.getState() == PacketEvent.State.INCOMING) {
            if (packet instanceof S08PacketPlayerPosLook) {
                this.balanceCounter -= 100L;
            }
        }

        if (packet instanceof C03PacketPlayer) {
            if (this.lastWorld != null && this.lastWorld != TimerRange.mc.theWorld) {
                this.balanceCounter = 0L;
                this.diffs.clear();
            }

            if (this.balanceCounter > 0L) {
                --this.balanceCounter;
            }

            final long diff = System.currentTimeMillis() - this.lastTime;
            this.diffs.add((int)diff);
            this.balanceCounter += (diff - 50L) * -3L;
            this.lastTime = System.currentTimeMillis();

            this.lastWorld = TimerRange.mc.theWorld;
        }
    }
}
