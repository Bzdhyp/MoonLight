package wtf.moonlight.module.impl.display;

import com.cubk.EventTarget;
import net.minecraft.client.gui.ScaledResolution;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.SliderValue;

@ModuleInfo(name = "Island", category = Categor.Display)
public class Island extends Module {
    private SliderValue y = new SliderValue("PosY",10,0,120,1,this);
    @EventTarget
    public void onRender2D(Render2DEvent event){
        ScaledResolution sr = new ScaledResolution(mc);
        wtf.moonlight.gui.island.Island island = new wtf.moonlight.gui.island.Island("Title","None", wtf.moonlight.gui.island.Island.Type.Toggle,1000,sr.getScaledWidth() / 2,y.getValue().intValue(),1,20);
        island.custom();
    }
}
