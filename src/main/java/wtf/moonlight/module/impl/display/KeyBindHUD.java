package wtf.moonlight.module.impl.display;

import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;

@ModuleInfo(name = "KeyBindHUD", category = ModuleCategory.Display)
public class KeyBindHUD extends Module {
    public final ListValue keyBindMode = new ListValue("Key Bind Mode", new String[]{"Type 1"}, "Type 1", this);
}
