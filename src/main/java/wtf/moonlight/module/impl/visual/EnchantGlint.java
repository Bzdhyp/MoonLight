package wtf.moonlight.module.impl.visual;

import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ColorValue;

import java.awt.*;

@ModuleInfo(name = "EnchantGlint", category = Categor.Visual)
public class EnchantGlint extends Module {

    public final BoolValue syncColor = new BoolValue("Sync Color", false, this);
    public final ColorValue color = new ColorValue("Color",new Color(0,255,255),this ,() -> !syncColor.get());
}
