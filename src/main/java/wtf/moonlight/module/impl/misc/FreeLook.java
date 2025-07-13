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

import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import com.cubk.EventTarget;
import wtf.moonlight.component.FreeLookComponent;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;

@ModuleInfo(name = "FreeLook", category = Categor.Misc)
public class FreeLook extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Mouse", "Keyboard"}, "Mouse", this);
    private boolean loackActivate;

    @Override
    public void onDisable() {
        loackActivate = false;
        FreeLookComponent.setFreelooking(false);
        mc.gameSettings.thirdPersonView = 0;
        super.onDisable();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc == null || mc.thePlayer == null) {
            stop();
            return;
        }

        if (mc.thePlayer.ticksExisted < 10) {
            stop();
            return;
        }

        try {
            boolean activate = false;

            if (mode.is("Mouse")) {
                activate = Mouse.isButtonDown(2);
            } else {
                int key = getKeyBind();
                if (key >= 0 && key < Keyboard.KEYBOARD_SIZE) {
                    activate = Keyboard.isKeyDown(key);
                }
            }

            if (activate) {
                if (!loackActivate) {
                    loackActivate = true;
                    FreeLookComponent.enable();
                    FreeLookComponent.cameraYaw += 180;
                    mc.gameSettings.thirdPersonView = 1;
                }
            } else if (loackActivate) {
                stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }
    }

    private void stop() {
        if (mode.is("Keyboard")) toggle();

        FreeLookComponent.setFreelooking(false);
        loackActivate = false;
        if (mc.gameSettings != null) {
            mc.gameSettings.thirdPersonView = 0;
        }
    }
}
