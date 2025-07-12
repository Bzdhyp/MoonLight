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
package wtf.moonlight.module.impl.misc.hackerdetector.impl;

import net.minecraft.entity.player.EntityPlayer;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.module.impl.misc.hackerdetector.Check;
import wtf.moonlight.util.player.MovementUtil;

public class MotionCheck extends Check {

    @Override
    public String getName() {
        return "Invalid motion";
    }

    @Override
    public void onPacketReceive(PacketEvent event, EntityPlayer player) {

    }

    @Override
    public void onUpdate(EntityPlayer player) {
        double base = MovementUtil.getBaseMoveSpeed(player);
        double speed = Math.hypot(player.motionX, player.motionZ);
        if (speed > (base * 1.25f) && player.hurtTime == 0) {
            flag(player, "Too fast");
        }

        if (!player.onGround && !MovementUtil.isMoving(player) && player.motionY == 0.0D && player.offGroundTicks >= 5) {
            flag(player, "Not moving on air for a long time");
        }
    }
}