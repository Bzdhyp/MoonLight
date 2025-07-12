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
package wtf.moonlight.module.impl.movement;

import net.minecraft.util.AxisAlignedBB;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.events.render.Render3DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.component.PingSpoofComponent;
import wtf.moonlight.util.render.RenderUtil;

import java.awt.*;

@ModuleInfo(name = "Blink", category = Categor.Misc)
public class Blink extends Module {
    private final BoolValue showPrevPos = new BoolValue("Show Prev Pos", true, this);
    private double prevX;
    private double prevY;
    private double prevZ;

    @Override
    public void onEnable() {
        if (showPrevPos.get()) {
            prevX = mc.thePlayer.posX;
            prevY = mc.thePlayer.posY;
            prevZ = mc.thePlayer.posZ;
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event){
        if(event.isPost())
            return;
        PingSpoofComponent.blink();
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        double x = prevX - mc.getRenderManager().viewerPosX;
        double y = prevY - mc.getRenderManager().viewerPosY;
        double z = prevZ - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(box.minX - mc.thePlayer.posX + x, box.minY - mc.thePlayer.posY + y, box.minZ - mc.thePlayer.posZ + z, box.maxX - mc.thePlayer.posX + x, box.maxY - mc.thePlayer.posY + y, box.maxZ - mc.thePlayer.posZ + z);
        RenderUtil.drawAxisAlignedBB(axis, true, new Color(255, 255, 255, 150).getRGB());
    }

    @Override
    public void onDisable() {
        PingSpoofComponent.dispatch();
    }
}