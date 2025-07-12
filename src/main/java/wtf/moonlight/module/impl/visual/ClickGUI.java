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

import net.minecraft.client.gui.GuiScreen;
import org.lwjglx.input.Keyboard;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ColorValue;
import wtf.moonlight.module.values.impl.ListValue;

import java.awt.*;

@ModuleInfo(name = "ClickGUI", category = Categor.Visual, key = Keyboard.KEY_RSHIFT)
public class ClickGUI extends Module {
    public final ListValue mode = new ListValue("Mode", new String[]{"NeverLose", "DropDown", "Arcane"}, "Arcane", this);

    public final ColorValue color = new ColorValue("Color", new Color(128, 128, 255), this);
    public final BoolValue rainbow = new BoolValue("Rainbow",true,this,() -> mode.is("Exhi"));

    @Override
    public void onEnable() {
        GuiScreen guiScreen = switch (mode.getValue()) {
            case "NeverLose" -> INSTANCE.getNeverLose();
            case "DropDown" -> INSTANCE.getDropdownGUI();
            case "Arcane" -> INSTANCE.getArcaneClickGui();
            default -> null;
        };
        mc.displayGuiScreen(guiScreen);
        toggle();
        super.onEnable();
    }
}
