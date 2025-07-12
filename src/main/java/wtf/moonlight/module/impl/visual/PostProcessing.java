package wtf.moonlight.module.impl.visual;

import net.minecraft.client.shader.Framebuffer;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.utils.render.RenderUtils;
import wtf.moonlight.utils.render.shader.impl.KawaseBloom;
import wtf.moonlight.utils.render.shader.impl.KawaseBlur;

@ModuleInfo(name = "Shaders", category = ModuleCategory.Visual)
public class PostProcessing extends Module {
    public final BoolValue blur = new BoolValue("Blur", true, this);
    private final SliderValue blurRadius = new SliderValue("Blur Radius", 8, 1, 50, 1, this, this.blur::get);
    private final SliderValue blurCompression = new SliderValue("Blur Compression",2, 1, 50, 1f, this, this.blur::get);
    private final BoolValue shadow = new BoolValue("Shadow", true, this);
    private final SliderValue shadowRadius = new SliderValue("Shadow Radius", 10, 1, 20, 1, this, shadow::get);
    private final SliderValue shadowOffset = new SliderValue("Shadow Offset", 1, 1, 15, 1, this, shadow::get);
    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    public void renderShaders() {
        if (!this.isEnabled()) return;

        if (this.blur.get()) {
            stencilFramebuffer = RenderUtils.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);

            KawaseBlur.startBlur();
            INSTANCE.getEventManager().call(new Shader2DEvent(Shader2DEvent.ShaderType.BLUR));
            RenderUtils.resetColor();
            KawaseBlur.endBlur(blurRadius.getValue(), blurCompression.getValue().intValue());
        }

        if (shadow.get()) {
            stencilFramebuffer = RenderUtils.createFrameBuffer(stencilFramebuffer, true);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(true);

            INSTANCE.getEventManager().call(new Shader2DEvent(Shader2DEvent.ShaderType.SHADOW));
            stencilFramebuffer.unbindFramebuffer();


            KawaseBloom.renderBloom(stencilFramebuffer.framebufferTexture, shadowRadius.getValue().intValue(), shadowOffset.getValue().intValue());
        }
    }
}
