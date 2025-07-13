package wtf.moonlight.module.impl.display;

import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;

@ModuleInfo(name = "Settings", category = Categor.Display)
public class Settings extends Module {
    public ListValue soundMode = new ListValue("Sound Mode", new String[]{"Default", "Augustus"}, "Augustus", this);

    @Override
    public void onEnable() {
        setEnabled(false);
        super.onEnable();
    }
}
