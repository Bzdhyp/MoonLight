package wtf.moonlight.module.impl.display;

import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;

/**
 * @Author：Guyuemang
 * @Date：2025/7/12 16:22
 */
@ModuleInfo(name = "ArrayList", category = Categor.Display)
public class ArrayList extends wtf.moonlight.module.Module {
    public final BoolValue importantModules = new BoolValue("Important", false, this);
    public ListValue fontmode = new ListValue("FontMode", new String[]{"Custom", "Bold", "Semibold", "Regular", "Light"}, "Custom", this);
    public ListValue textShadow = new ListValue("Text Shadow", new String[]{"Black", "Colored", "None"}, "None", this);
    public BoolValue suffixColor = new BoolValue("SuffixColor", false, this);
    public ListValue tags = new ListValue("Suffix", new String[]{"None", "Simple", "Bracket", "Dash"}, "Bracket", this);
    public ListValue animation = new ListValue("Animation", new String[]{"Move In", "Scale In"}, "Move In", this);
    public SliderValue hight = new SliderValue("ArrayHight", 12.0f, 1.0f, 20.0f,0.1f, this);
    public SliderValue fontSize = new SliderValue("Font Size", 1.0f, -20.0f, 20.0f,0.1f, this);
    public SliderValue count = new SliderValue("ArrayCount", 1, 1.0f, 5,0.1f, this);
    public final ListValue color = new ListValue("Color Setting", new String[]{"Custom", "Rainbow", "Dynamic", "Double", "Astolfo", "Tenacity"}, "Fade", this);
    public final SliderValue colorspeed = new SliderValue("ColorSpeed", 4, 1, 10, 1, this, () -> color.is("Dynamic") || color.is("Fade") || color.is("Tenacity"));
    public final SliderValue colorIndex = new SliderValue("Color Seperation", 1, 1, 50, 1, this);
    public BoolValue background = new BoolValue("BackGround", false, this);
    public ListValue misc = new ListValue("Rectangle", new String[]{"None", "Top", "Side"}, "None", this);
    public ListValue bgMode = new ListValue("BackGroundMod", new String[]{"Rect", "Round"}, "Rect", this, () -> background.get());
    public SliderValue radius = new SliderValue("radius", 3, 0, 8, 0.1f, this, () -> background.get());
    public final SliderValue backgroundAlpha = new SliderValue("Background Alpha", 0.5f, 0, 1,0.1f, this, () -> background.get());
    public SliderValue hight2 = new SliderValue("RectangleHight", 12.0f, 1.0f, 20.0f,0.1f, this);
}
