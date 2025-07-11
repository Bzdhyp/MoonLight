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

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.utils.math.MathUtils;
import wtf.moonlight.utils.math.TimerUtils;

@ModuleInfo(name = "AutoClicker", category = ModuleCategory.Legit)
public class AutoClicker extends Module {
    private final SliderValue minAps = new SliderValue("Min Aps", 10, 1, 20, this);
    private final SliderValue maxAps = new SliderValue("Max Aps", 12, 1, 20, this);
    private final BoolValue breakBlocks = new BoolValue("Break Blocks", true, this);
    private final TimerUtils clickTimer = new TimerUtils();

    @Override
    public void onEnable(){
        clickTimer.reset();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (breakBlocks.get() && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            return;

        if (mc.gameSettings.keyBindAttack.isKeyDown()) {
            if (clickTimer.hasTimeElapsed(1000 / MathUtils.nextInt((int) minAps.getValue(), (int) maxAps.getValue()))) {
                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                clickTimer.reset();
            }
        }
    }
}
