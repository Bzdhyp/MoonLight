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

import net.minecraft.util.ResourceLocation;
import com.cubk.EventTarget;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.utils.GifRenderer;

@ModuleInfo(name = "GifTest", category = ModuleCategory.Visual)
public class GifTest extends Module {

    GifRenderer gif = new GifRenderer(new ResourceLocation("moonlight/texture/gif/test.gif"));
    @EventTarget
    public void onRender2D(Render2DEvent event){
        gif.drawTexture((float) event.scaledResolution().getScaledWidth() / 2, (float) event.scaledResolution().getScaledHeight() / 2,this.gif.getWidth(),this.gif.getHeight());
        gif.update();
    }
}
