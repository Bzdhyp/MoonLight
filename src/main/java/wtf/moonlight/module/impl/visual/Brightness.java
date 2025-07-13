package wtf.moonlight.module.impl.visual;

import com.cubk.EventTarget;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;

@ModuleInfo(name = "Brightness",category = Categor.Visual)
public class Brightness extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Gamma", "Potion"}, "Potion", this);

    private float oldGamma;

    @Override
    public void onEnable() {
        if (mode.is("Gamma"))
            oldGamma = mc.gameSettings.gammaSetting;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        switch (mode.getValue()) {
            case "Gamma":
                mc.gameSettings.gammaSetting = oldGamma;
                break;
            case "Potion":
                mc.thePlayer.removePotionEffect(Potion.nightVision.id);
                break;
        }
        super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.isPre()) {
            switch (mode.getValue()) {
                case "Gamma":
                    mc.gameSettings.gammaSetting = 100;
                    break;
                case "Potion":
                    mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 16340, 1));
                    break;
            }
        }
    }
}

