package wtf.moonlight.gui.click.arcane.component.settings;

import net.minecraft.util.MathHelper;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MathUti;
import wtf.moonlight.util.animations.advanced.Animation;
import wtf.moonlight.util.animations.advanced.Direction;
import wtf.moonlight.util.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 23:27
 */
public class NumberComponent extends Component {
    private final SliderValue setting;
    private boolean dragging;
    private final Animation drag = new DecelerateAnimation(250, 1);
    public NumberComponent(SliderValue setting) {
        this.setting = setting;
        setHeight(30);
        drag.setDirection(Direction.BACKWARDS);
    }
    private float anim;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        int w = 145;
        anim = RenderUtil.animate(anim, (float) (w * (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin())), 50);
        float sliderWidth = anim;
        drag.setDirection(dragging ? Direction.FORWARDS : Direction.BACKWARDS);

        Fonts.Bold.get(18).drawString(setting.getName(), getX() + 10, getY() + 4, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),0.4f));
        Fonts.Bold.get(18).drawString(String.valueOf(setting.getValue()), getX() + 155 - Fonts.Bold.get(18).getStringWidth(String.valueOf(setting.getValue())), getY() + 4, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),0.4f));

        RoundedUtil.drawRound(getX() + 10, getY() + 18, w, 2, 2, INSTANCE.getArcaneClickGui().versionColor);
        RoundedUtil.drawGradientHorizontal(getX() + 10, getY() + 18, sliderWidth, 2, 2, new Color(INSTANCE.getModuleManager().getModule(Interface.class).color(1)),new Color(-1));
        RoundedUtil.drawRound(getX() + 5 + sliderWidth, getY() + 16.5f, 6, 6, 3, new Color(INSTANCE.getModuleManager().getModule(Interface.class).color(1)));

        if (dragging) {
            final double difference = this.setting.getMax() - this.setting
                    .getMin(), //
                    value = this.setting.getMin() + MathHelper
                            .clamp_double((mouseX - (getX() + 10)) / w, 0, 1) * difference;
            setting.setValue((float) MathUti.incValue(value, setting.getIncrement()));
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int w = 145;
        if (RenderUtil.isHovering(getX() + 10, getY() + 16, w, 6,mouseX, mouseY) && mouseButton == 0) {
            dragging = true;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0){
            dragging = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
    }
    @Override
    public boolean isVisible() {
        return setting.getVisible().get();
    }
}
