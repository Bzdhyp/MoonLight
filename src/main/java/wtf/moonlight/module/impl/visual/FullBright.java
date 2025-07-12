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

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;

@ModuleInfo(name = "FullBright",category = Categor.Visual)
public class FullBright extends Module {

    @EventTarget
    public void onUpdate(UpdateEvent event){
        mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 5200, 1));
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer.isPotionActive(Potion.nightVision)) {
            mc.thePlayer.removePotionEffect(Potion.nightVision.id);
        }
    }
}
