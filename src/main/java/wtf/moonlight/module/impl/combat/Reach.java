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
import wtf.moonlight.events.misc.MouseOverEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.utils.math.MathUtils;

@ModuleInfo(name = "Reach", category = ModuleCategory.Combat)
public class Reach extends Module {

    public final SliderValue min = new SliderValue("Min Range", 3.0F, 3, 6F, .1f, this);
    public final SliderValue max = new SliderValue("Max Range", 3.3F, 3, 6F, .1f, this);

    @EventTarget
    public void onMouseOver(MouseOverEvent event) {
        event.setRange(MathUtils.randomizeDouble(min.getMin(), max.getMax()));
    }
}
