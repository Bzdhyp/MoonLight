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
package wtf.moonlight.module.impl.visual;

import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.SliderValue;

@ModuleInfo(name = "AspectRatio", category = Categor.Visual)
public class AspectRatio extends Module {
    public final SliderValue aspect = new SliderValue("Aspect",1.0f, 0.1f, 5.0f, 0.1f,this);
}
