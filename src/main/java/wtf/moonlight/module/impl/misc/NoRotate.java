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

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import com.cubk.EventTarget;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.TeleportEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;

@ModuleInfo(name = "NoRotate", category = Categor.Misc)
public class NoRotate extends Module {
    public final ListValue mode = new ListValue("Mode", new String[]{"Edit","Packet"}, "Edit", this);
    private float yaw, pitch;
    private boolean teleport;

    @EventTarget
    public void onTeleport(TeleportEvent event) {

        if (getModule(Disabler.class).options.isEnabled("Watchdog Motion") && getModule(Disabler.class).testTicks == -1 || !getModule(Disabler.class).options.isEnabled("Watchdog Motion")) {

            switch (mode.getValue()) {
                case "Packet":
                    event.setYaw(mc.thePlayer.rotationYaw);
                    event.setPitch(mc.thePlayer.rotationPitch);
                    break;
                case "Edit":
                    this.yaw = event.getYaw();
                    this.pitch = event.getPitch();

                    event.setYaw(mc.thePlayer.rotationYaw);
                    event.setPitch(mc.thePlayer.rotationPitch);

                    this.teleport = true;
                    break;
            }
        }
    }

    @EventTarget
    private void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (mode.is("Edit") && this.teleport && packet instanceof C03PacketPlayer.C06PacketPlayerPosLook c06PacketPlayerPosLook) {

            c06PacketPlayerPosLook.yaw = this.yaw;
            c06PacketPlayerPosLook.pitch = this.pitch;

            event.setPacket(c06PacketPlayerPosLook);

            this.teleport = false;
        }
    }
}
