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

import org.lwjglx.input.Mouse;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;

@ModuleInfo(name = "FreeLook", category = ModuleCategory.Visual)
public class FreeLook extends Module {
    private boolean released;

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.isPost()) {
            if (Mouse.isButtonDown(2)) {
                mc.gameSettings.thirdPersonView = 1;
                released = false;
            } else {
                if (!released) {
                    mc.gameSettings.thirdPersonView = 0;
                    released = true;
                }
            }
        }
    }
}
