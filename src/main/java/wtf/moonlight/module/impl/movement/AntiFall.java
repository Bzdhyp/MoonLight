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
package wtf.moonlight.module.impl.movement;

import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;
import com.cubk.EventTarget;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.component.BlinkComponent;

@ModuleInfo(name = "AntiFall", category = ModuleCategory.Movement)
public class AntiFall extends Module {

    public final ListValue mode = new ListValue("Mode", new String[]{"Universal"}, "Universal", this);
    private double groundX = 0.0;
    private double groundY = 0.0;
    private double groundZ = 0.0;
    private boolean universalStarted = false;
    private boolean universalFlag = false;

    @Override
    public void onEnable() {
        universalStarted = false;
    }

    @Override
    public void onDisable() {
        BlinkComponent.dispatch();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.getValue());

        if (isEnabled(LongJump.class) || isEnabled(Scaffold.class))
            return;

        switch (mode.getValue()) {
            case "Universal":
                if (universalStarted) {
                    if (mc.thePlayer.onGround || mc.thePlayer.fallDistance > 8f) {
                        BlinkComponent.dispatch();
                        universalStarted = false;
                        universalFlag = false;
                    } else if (mc.thePlayer.fallDistance > 6f && !universalFlag) {
                        universalFlag = true;
                        sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(groundX, groundY + 1, groundZ, false));
                    }
                } else if (mc.thePlayer.fallDistance > 0f && !mc.thePlayer.onGround && mc.thePlayer.motionY < 0) {
                    if (isOverVoid()) {
                        universalStarted = true;
                        universalFlag = false;
                        BlinkComponent.blinking = true;
                        groundX = mc.thePlayer.posX;
                        groundY = mc.thePlayer.posY;
                        groundZ = mc.thePlayer.posZ;
                    }
                }
                break;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {

        if (isEnabled(LongJump.class) || isEnabled(Scaffold.class) && getModule(Scaffold.class).data.blockPos != null)
            return;

        if (mode.is("Universal")) {
            if (event.getPacket() instanceof S08PacketPlayerPosLook s08PacketPlayerPosLook) {
                if (s08PacketPlayerPosLook.getX() == groundX && s08PacketPlayerPosLook.getY() == groundY && s08PacketPlayerPosLook.getZ() == groundZ) {
                    BlinkComponent.blinking = false;
                    mc.thePlayer.setPosition(groundX, groundY, groundZ);
                    universalFlag = false;
                    universalStarted = false;
                }
            }
        }
    }

    private boolean isOverVoid() {
        return mc.theWorld.rayTraceBlocks(
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 40, mc.thePlayer.posZ),
                true, true, false) == null;
    }
}
