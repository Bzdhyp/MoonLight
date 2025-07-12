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

import net.minecraft.block.BlockAir;
import com.cubk.EventTarget;
import wtf.moonlight.events.render.Render3DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ColorValue;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.render.RenderUtil;

import java.awt.*;

@ModuleInfo(name = "BlockOverlay",category = Categor.Visual)
public class BlockOverlay extends Module {

    public final BoolValue outline = new BoolValue("Outline", true, this);
    public final BoolValue filled = new BoolValue("Filled", false, this);
    public final BoolValue syncColor = new BoolValue("Sync Color", false, this);
    public final ColorValue color = new ColorValue("Color",new Color(255,255,255),this ,() -> !syncColor.get());

    @EventTarget
    public void onRender3D(Render3DEvent event) {

        if(PlayerUtil.getBlock(mc.objectMouseOver.getBlockPos()) instanceof BlockAir)
            return;

        if (syncColor.get()) {
            RenderUtil.renderBlock(mc.objectMouseOver.getBlockPos(), getModule(Interface.class).color(0), outline.get(), filled.get());
        } else {
            RenderUtil.renderBlock(mc.objectMouseOver.getBlockPos(), color.getValue().getRGB(), outline.get(), filled.get());
        }

    }
}
