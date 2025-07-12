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
import net.minecraft.item.ItemBlock;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.module.impl.misc.hackerdetector.Check;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.player.MovementUtil;

public class LegitScaffoldCheck extends Check {
    private final TimerUtil timer = new TimerUtil();
    private int sneakFlag;

    @Override
    public String getName() {
        return "Legit Scaffold";
    }

    @Override
    public void onPacketReceive(PacketEvent event, EntityPlayer player) {

    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (player.isSneaking()) {
            timer.reset();
            sneakFlag += 1;
        }

        if (timer.hasTimeElapsed(140)) {
            sneakFlag = 0;
        }
        if (player.rotationPitch > 75 && player.rotationPitch < 90 && player.isSwingInProgress) {
            if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemBlock) {
                if (MovementUtil.getSpeed(player) >= 0.10 && player.onGround && sneakFlag > 5) {
                    flag(player, "Sneak too fast");
                }
                if (MovementUtil.getSpeed(player) >= 0.21 && !player.onGround && sneakFlag > 5) {
                    flag(player, "Sneak too fast");
                }
            }
        }
    }


}

