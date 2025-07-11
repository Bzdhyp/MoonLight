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
package wtf.moonlight.module.impl.player;

import net.minecraft.item.ItemBlock;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.utils.math.MathUtils;
import wtf.moonlight.utils.player.PlayerUtils;

@ModuleInfo(name = "FastPlace", category = ModuleCategory.Player)
public class FastPlace extends Module {

    public final SliderValue speed = new SliderValue("Speed", 1, 0, 4, this);

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(MathUtils.incValue(speed.getValue(), 1) + "");
        if (!PlayerUtils.nullCheck())
            return;
        if (mc.thePlayer.getHeldItem() == null)
            return;
        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)
            mc.rightClickDelayTimer = (int) speed.getValue();
    }
}
