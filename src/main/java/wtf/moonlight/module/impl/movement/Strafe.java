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
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.utils.player.MovementUtils;

@ModuleInfo(name = "Strafe", category = ModuleCategory.Movement)
public class Strafe extends Module {

    public final BoolValue ground = new BoolValue("Ground", true, this);
    public final BoolValue air = new BoolValue("Air", true, this);

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround && ground.get()) MovementUtils.strafe();
        if (!mc.thePlayer.onGround && air.get()) MovementUtils.strafe();
    }
}