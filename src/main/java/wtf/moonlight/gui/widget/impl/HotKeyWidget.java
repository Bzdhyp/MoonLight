package wtf.moonlight.gui.widget.impl;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StringUtils;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.widget.Widget;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.impl.display.ArrayListMod;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class HotKeyWidget extends Widget {
    public HotKeyWidget() {
        super("HotKeys");
        this.x = 10;
        this.y = 10;
    }

    ArrayListMod arrayListMod = Client.INSTANCE.getModuleManager().getModule(ArrayListMod.class);

    @Override
    public void render() {
        this.hotKeyRender(true);
    }

    @Override
    public void onShader(Shader2DEvent event) {

    }

    public void hotKeyRender(boolean shadow) {
        FontRenderer font = arrayListMod.getFont();

        Comparator<Module> sort = (m1, m2) -> {
            double ab = font.getStringWidth(m1.getName() + m1.getTag());
            double bb = font.getStringWidth(m2.getName() + m2.getTag());
            return Double.compare(bb, ab);
        };

        int count = 1;
        ArrayList<Module> enabledMods = new ArrayList<>(Client.INSTANCE.getModuleManager().getModules());
        ScaledResolution sr = new ScaledResolution(mc);
        for (Module module : enabledMods) {
            count++;
            enabledMods.sort(sort);
            Animation moduleAnimation = module.getAnimation();
            moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
            if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;

            int renderx = (int) renderX - 2;
            int rendery = (int) renderY + 4;

            boolean flip = renderX + width / 2 <= sr.getScaledWidth() / 2f;
            String displayText = module.getName() + module.getTag();
            float x = flip ? (renderx + 4) : (int) (renderx + (this.width - font.getStringWidth(displayText)));
            float y = (int) (rendery + count + 1.2);

            if (flip) {
                x -= (int) Math.abs((moduleAnimation.getOutput() - 1.0) * (12.0 + font.getStringWidth(displayText)));
            } else {
                x += (int) Math.abs((moduleAnimation.getOutput() - 1.0) * (12.0 + font.getStringWidth(displayText)));
            }
            int textcolor = ColorUtil.swapAlpha(setting.color(count), 255);
            int width = font.getStringWidth(displayText) + 6;

            RoundedUtil.drawRound(x - 2, rendery + count - 4, width, 1, arrayListMod.radius.getValue().intValue(), ColorUtil.applyOpacity3(new Color(1, 1, 1).getRGB(), arrayListMod.bgAlpha.getValue()));

            float f = 2f;
            font.drawString(StringUtils.stripColorCodes(displayText), x + f, y + f - 2,
                    ColorUtil.applyOpacity(Color.BLACK,1f).getRGB());
            RenderUtil.resetColor();
            font.drawString(displayText, x + 1, y - 2, textcolor);

            RenderUtil.scaleEnd();

            count += (int) (moduleAnimation.getOutput() * (13 * 1.4f));
            this.height = count;

            this.width = 52;
            count -= 2;
        }
    }

    @Override
    public boolean shouldRender() {
        return Client.INSTANCE.getModuleManager().getModule(ArrayListMod.class).isEnabled() && Client.INSTANCE.getModuleManager().getModule(ArrayListMod.class).renderMod.is("Hot Key");
    }
}