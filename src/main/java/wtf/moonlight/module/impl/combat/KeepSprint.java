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
package wtf.moonlight.module.impl.combat;

import com.cubk.EventTarget;
import wtf.moonlight.events.player.KeepSprintEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;

@ModuleInfo(name = "KeepSprint", category = Categor.Combat)
public class KeepSprint extends Module {

    @EventTarget
    public void onKeepSprint(KeepSprintEvent event) {
        event.setCancelled(true);
    }
}
