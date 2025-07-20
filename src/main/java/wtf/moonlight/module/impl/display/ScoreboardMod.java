package wtf.moonlight.module.impl.display;

import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.SliderValue;

@ModuleInfo(name = "Scoreboard", category = Categor.Display)
public class ScoreboardMod extends Module {
    public final BoolValue redNumbers = new BoolValue("Red Numbers", false, this);
    public final BoolValue textShadow = new BoolValue("Text Shadow", true, this);
    public final SliderValue yOffset = new SliderValue("Y Offset", 0, 1, 250, 5, this);

    public ScoreboardMod() {
        this.setEnabled(true);
    }
}
