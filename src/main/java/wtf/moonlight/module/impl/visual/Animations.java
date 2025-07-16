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
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;

@ModuleInfo(name = "Animations", category = Categor.Visual)
public class Animations extends Module {
    public final ListValue type = new ListValue("Block Anim", new String[]{
            "Swing", "Swong", "Swonk", "Swung", "Swang", "Swank", "Swack", "Jello", "Leaked", "Push", "Smooth", "Ethereal", "E", "Debug", "Jigsaw", "Lunar", "1.7", "1.8 (Loser)"}, "1.7", this);

    public final SliderValue swingSpeed = new SliderValue("Swing Slowdown", 1, -200, 1, 50, this);
    public final SliderValue scaleValue = new SliderValue("Scale", 0, -50, 50, this);

    public final SliderValue itemXValue = new SliderValue("Item-X", 0.0F, -1.0F, 1.0F, .05f, this);
    public final SliderValue itemYValue = new SliderValue("Item-Y", 0.0F, -1.0F, 1.0F, .05f, this);
    public final SliderValue itemZValue = new SliderValue("Item-Z", 0.0F, -1.0F, 1.0F, .05f, this);

    public final SliderValue blockXValue = new SliderValue("Block-X", 0.0F, -1.0F, 1.0F, .05f, this);
    public final SliderValue blockYValue = new SliderValue("Block-Y", 0.0F, -1.0F, 1.0F, .05f, this);
    public final SliderValue blockZValue = new SliderValue("Block-Z", 0.0F, -1.0F, 1.0F, .05f, this);
}
