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

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import com.cubk.EventTarget;
import wtf.moonlight.events.render.Render3DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ColorValue;
import wtf.moonlight.util.render.RenderUtil;

import java.awt.*;

@ModuleInfo(name = "ChestESP",category = Categor.Visual)
public class ChestESP extends Module {

    public final BoolValue outline = new BoolValue("Outline", false, this);
    public final BoolValue filled = new BoolValue("Filled", true, this);
    public final BoolValue syncColor = new BoolValue("Sync Color", false, this);
    public final ColorValue color = new ColorValue("Color",new Color(255,255,128),this ,() -> !syncColor.get());

    @EventTarget
    public void onRender3D(Render3DEvent event) {

        for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
            if (tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityEnderChest) {
                if (!tileEntity.isInvalid() && mc.theWorld.getBlockState(tileEntity.getPos()) != null) {
                    if (syncColor.get()) {
                        RenderUtil.renderBlock(tileEntity.getPos(),getModule(Interface.class).color(0),outline.get(),filled.get());
                    } else {
                        RenderUtil.renderBlock(tileEntity.getPos(),color.getValue().getRGB(),outline.get(),filled.get());
                    }
                }
            }
        }
    }
}
