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
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;

@ModuleInfo(name = "VClip", category = ModuleCategory.Movement)
public class VClip extends Module {
    ListValue mode = new ListValue("Mode", new String[]{"Up","Down","Smart"}, "Down", this);
    SliderValue upAmount = new SliderValue("Up Blocks", 1, 0.1f, 10.0f, 0.05f, this, () -> mode.is("Up") || mode.is("Smart"));
    SliderValue downAmount = new SliderValue("Down Blocks", 1, 0.1f, 10.0f, 0.05f, this, () -> mode.is("Down") || mode.is("Smart"));

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.getValue());
        if (mode.is("Up"))  {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + upAmount.getValue(), mc.thePlayer.posZ);
            setEnabled(false);
        }
        if (mode.is("Down"))  {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - downAmount.getValue(), mc.thePlayer.posZ);
            setEnabled(false);
        }
        if (mode.is("Smart")) {
            if (mc.thePlayer.rotationPitch <= 0)
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + upAmount.getValue(), mc.thePlayer.posZ);
            setEnabled(false);
            if (mc.thePlayer.rotationPitch >= 0)
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - downAmount.getValue(), mc.thePlayer.posZ);
            setEnabled(false);
        }
    }
}
