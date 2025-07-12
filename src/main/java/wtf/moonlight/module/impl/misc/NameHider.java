package wtf.moonlight.module.impl.misc;

import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.StringValue;

@ModuleInfo(name = "NameHider",category = Categor.Misc)
public class NameHider extends Module {
    public final StringValue name = new StringValue("Name","Randomguy",this);
    public String getFakeName(String s) {
        if (mc.thePlayer != null) {
            s = s.replace(mc.thePlayer.getName(), name.getValue());
        }
        return s;
    }
}
