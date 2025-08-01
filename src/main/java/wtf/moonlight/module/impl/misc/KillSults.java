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

import net.minecraft.entity.player.EntityPlayer;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.player.AttackEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.StringValue;

import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "KillSults", category = Categor.Misc)
public class KillSults extends Module {
    private EntityPlayer currentTarget;
    public final StringValue stringV = new StringValue("Name"," 你已被MoonLight击败",this);

    @EventTarget
    private void onUpdate(UpdateEvent event) {
        if (currentTarget.isDead && !mc.thePlayer.isDead && !mc.thePlayer.isSpectator()) {
            sendMessage(currentTarget.getName());
            currentTarget = null;
        }
    }

    @EventTarget
    private void onWorld(WorldEvent event) {
        currentTarget = null;
    }

    @EventTarget
    private void onAttack(AttackEvent event) {
        if (event.getTargetEntity() instanceof EntityPlayer)
            currentTarget = (EntityPlayer) event.getTargetEntity();
    }

    public void sendMessage(String name) {
        final String[] text = {"人生自古谁无死，"};
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, text.length);
        mc.thePlayer.sendChatMessage("@" + name + " " + text[randomIndex] + stringV.getValue());
    }
}
