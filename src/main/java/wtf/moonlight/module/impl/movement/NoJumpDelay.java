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

import com.cubk.EventTarget;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;

@ModuleInfo(name = "NoJumpDelay", category = Categor.Movement)
public class NoJumpDelay extends Module {

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        mc.thePlayer.jumpTicks = 0;
    }
}
