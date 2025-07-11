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

import net.minecraft.network.play.server.S03PacketTimeUpdate;
import com.cubk.EventTarget;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ColorValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;

import java.awt.*;

@ModuleInfo(name = "Atmosphere", category = ModuleCategory.Visual)
public class Atmosphere extends Module {
    private final BoolValue time = new BoolValue("Time Editor",true,this);
    private final SliderValue timeValue = new SliderValue("Time", 18000, 0, 24000, 1000, this,time::get);
    private final BoolValue weather = new BoolValue("Weather Editor",true,this);
    private final ListValue weatherValue = new ListValue("Weather",new String[]{"Clean", "Rain", "Thunder"},"Clean",this,weather::get);
    public final BoolValue worldColor = new BoolValue("World Color", true, this);
    public final ColorValue worldColorRGB = new ColorValue("World Color RGB", Color.WHITE, this, worldColor::get);
    public final BoolValue worldFog = new BoolValue("World Fog", false, this);
    public final ColorValue worldFogRGB = new ColorValue("World Fog RGB", Color.WHITE, this, worldFog::get);
    public final SliderValue worldFogDistance = new SliderValue("World Fog Distance", 0.10F, -1F, 0.9F, 0.1F, this, worldFog::get);

    @EventTarget
    private void onUpdate(UpdateEvent event) {
        if(time.get())
            mc.theWorld.setWorldTime((long) timeValue.getValue());
        if (weather.get()) {
            switch (weatherValue.getValue()) {
                case "Rain":
                    mc.theWorld.setRainStrength(1);
                    mc.theWorld.setThunderStrength(0);
                    break;
                case "Thunder":
                    mc.theWorld.setRainStrength(1);
                    mc.theWorld.setThunderStrength(1);
                    break;
                default:
                    mc.theWorld.setRainStrength(0);
                    mc.theWorld.setThunderStrength(0);
            }
        }
    }

    @EventTarget
    private void onPacket(PacketEvent event) {
        if (time.get() && event.getPacket() instanceof S03PacketTimeUpdate)
            event.setCancelled(true);
    }
}
