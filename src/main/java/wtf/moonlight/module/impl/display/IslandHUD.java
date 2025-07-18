package wtf.moonlight.module.impl.display;

import com.cubk.EventTarget;
import net.minecraft.client.gui.ScaledResolution;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.display.island.Island;
import wtf.moonlight.module.values.impl.SliderValue;

@ModuleInfo(name = "Island", category = Categor.Display)
public class IslandHUD extends Module {
    private final SliderValue y = new SliderValue("PosY",10,0,120,1,this);

    @EventTarget
    public void onRender2D(Render2DEvent event){
        ScaledResolution sr = new ScaledResolution(mc);
        Island island = new Island("Title","None", Island.Type.Toggle,1000,sr.getScaledWidth() / 2,y.getValue().intValue(),1,20);
        island.custom();
    }
}
