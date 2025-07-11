package wtf.moonlight.module.impl.display;

import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;

@ModuleInfo(name = "TargetHUD", category = ModuleCategory.Display)
public class TargetHUD extends Module {
    public final ListValue targetHudMode = new ListValue("Mode", new String[]{"Astolfo", "Type 1", "Type 2", "Type 3", "Type 4",
            "Felix","Exhi","Adjust","Moon", "Augustus","New","Novo 1","Novo 2","Novo 3","Novo 4","Novo 5","Akrien","Innominate"}, "Astolfo", this);
    public final BoolValue targetHudParticle = new BoolValue("Particle",true, this);
}
