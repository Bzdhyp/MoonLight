package wtf.moonlight.module.impl.display;

import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;

@ModuleInfo(name = "Scoreboard", category = Categor.Display)
public class ScoreboardMod extends Module {
    public final BoolValue hideScoreRed = new BoolValue("Hide Scoreboard Red Points", true, this);
    public final BoolValue fixHeight = new BoolValue("Fix Height", true, this);
    public final BoolValue hideBackground = new BoolValue("Hide Background", true, this);

    public ScoreboardMod() {
        this.setEnabled(true);
    }
}
