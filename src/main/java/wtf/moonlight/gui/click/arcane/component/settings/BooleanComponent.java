package wtf.moonlight.gui.click.arcane.component.settings;

import wtf.moonlight.Client;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 23:16
 */
public class BooleanComponent extends Component {
    private final BoolValue setting;
    private final Animation enabled = new DecelerateAnimation(250,1);
    private final Animation hover = new DecelerateAnimation(250,1);
    public BooleanComponent(BoolValue setting) {
        this.setting = setting;
        setHeight(22);
        enabled.setDirection(setting.get() ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        enabled.setDirection(setting.get() ? Direction.FORWARDS : Direction.BACKWARDS);
        hover.setDirection(RenderUtil.isHovering(getX() + 172, getY() + 15, 22, 12,mouseX,mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);
        Fonts.Bold.get(18).drawString(setting.getName(), getX() + 10, getY() + 4, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),0.4f));

        Color bgColor = setting.get()
                ? new Color(ColorUtil.applyOpacity(Client.INSTANCE.getModuleManager().getModule(Interface.class).color(), 0.4f))
                : new Color(0, 0, 0, 80);
        RoundedUtil.drawRound(getX() + 135, getY() + 4, 20, 10, 4, bgColor);

        RenderUtil.drawCircleCGUI(getX() + 141 + enabled.getOutput() * 9f, getY() + 9, 8,new Color(INSTANCE.getModuleManager().getModule(Interface.class).color(1)).darker().getRGB());

        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering(getX() + 135, getY() + 4, 20, 10,mouseX,mouseY) && mouseButton == 0){
            setting.toggle();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
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
