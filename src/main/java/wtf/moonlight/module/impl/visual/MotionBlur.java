package wtf.moonlight.module.impl.visual;

import com.cubk.EventTarget;
import net.minecraft.util.ResourceLocation;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.SliderValue;

@ModuleInfo(name = "MotionBlur", category = Categor.Visual)
public class MotionBlur extends Module {
    public final SliderValue amount = new SliderValue("Motion Blur Amount", 1, 1, 10, 1, this);

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.theWorld != null) {
            if (isEnabled()) {
                if ((mc.entityRenderer.getShaderGroup() == null))
                    mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));
                float uniform = 1F - Math.min(amount.getValue() / 10F, 0.9f);
                if (mc.entityRenderer.getShaderGroup() != null) {
                    mc.entityRenderer.getShaderGroup().listShaders.get(0).getShaderManager().getShaderUniform("Phosphor").set(uniform, 0F, 0F);
                }
            } else {
                if (mc.entityRenderer.isShaderActive())
                    mc.entityRenderer.stopUseShader();
            }
        }
    }
}
