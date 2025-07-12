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
package wtf.moonlight.gui.click.dropdown.component;

import lombok.Getter;
import lombok.Setter;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.impl.visual.ClickGUI;
import wtf.moonlight.module.values.Value;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.click.IComponent;
import wtf.moonlight.gui.click.dropdown.component.impl.*;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.util.animations.advanced.Direction;
import wtf.moonlight.util.animations.advanced.impl.EaseInOutQuad;
import wtf.moonlight.util.animations.advanced.impl.EaseOutSine;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.MouseUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class ModuleComponent implements IComponent {
    private float x, y, width, height = 19;
    private final Module module;
    private final CopyOnWriteArrayList<Component> settings = new CopyOnWriteArrayList<>();
    private boolean opened;
    private final EaseInOutQuad openAnimation = new EaseInOutQuad(250, 1);
    private final EaseOutSine toggleAnimation = new EaseOutSine(300, 1);
    private final EaseOutSine hoverAnimation = new EaseOutSine(200, 1);

    public ModuleComponent(Module module) {
        this.module = module;
        openAnimation.setDirection(Direction.BACKWARDS);
        toggleAnimation.setDirection(Direction.BACKWARDS);
        hoverAnimation.setDirection(Direction.BACKWARDS);
        for (Value value : module.getValues()) {
            if (value instanceof BoolValue boolValue) {
                settings.add(new BooleanComponent(boolValue));
            }
            if (value instanceof ColorValue colorValue) {
                settings.add(new ColorPickerComponent(colorValue));
            }
            if (value instanceof SliderValue sliderValue) {
                settings.add(new SliderComponent(sliderValue));
            }
            if (value instanceof ListValue modeValue) {
                settings.add(new ModeComponent(modeValue));
            }
            if (value instanceof MultiBoolValue multiBoolValue) {
                settings.add(new MultiBooleanComponent(multiBoolValue));
            }
            if(value instanceof StringValue textValue){
                settings.add(new StringComponent(textValue));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float yOffset = 19;
        openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
        toggleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        hoverAnimation.setDirection(isHovered(mouseX, mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);

        RoundedUtil.drawRound(x,y,width,yOffset - 4,3,new Color(ColorUtil.darker(INSTANCE.getModuleManager().getModule(ClickGUI.class).color.getValue().getRGB(), (float) (0.15f + (0.15 * toggleAnimation.getOutput())))));

        Fonts.interRegular.get((float) (14 - 1 * hoverAnimation.getOutput())).drawCenteredString(module.getName(), x + getWidth() / 2, y + yOffset / 2 - 3 + 0.5 * hoverAnimation.getOutput(), ColorUtil.interpolateColor2(Color.GRAY, INSTANCE.getModuleManager().getModule(ClickGUI.class).color.getValue(), (float) toggleAnimation.getOutput()));

        if (!settings.isEmpty()) {
            RenderUtil.drawCircle(x + width - 10, y + 7, 0, 360, 1f, .1f, true, INSTANCE.getModuleManager().getModule(ClickGUI.class).color.getValue().getRGB());
        }

        for (Component component : settings) {
            if (!component.isVisible()) continue;
            component.setX(x);
            component.setY((float) (y + yOffset * openAnimation.getOutput()));
            component.setWidth(width);
            if (openAnimation.getOutput() > .7f) {
                component.drawRoundBackground(new Color(ColorUtil.darker(INSTANCE.getModuleManager().getModule(ClickGUI.class).color.getValue().getRGB(), (float) (0.15f + (0.15 * toggleAnimation.getOutput())))));
                component.drawScreen(mouseX, mouseY);
            }
            yOffset += (float) (component.getHeight() * openAnimation.getOutput());
            this.height = yOffset + 2;
        }

        IComponent.super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
            switch (mouseButton) {
                case 0 -> module.toggle();
                case 1 -> opened = !opened;
            }
        }
        if (opened && !isHovered(mouseX, mouseY)) {
            settings.forEach(setting -> setting.mouseClicked(mouseX, mouseY, mouseButton));
        }
        IComponent.super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (opened && !isHovered(mouseX, mouseY)) {
            settings.forEach(setting -> setting.mouseReleased(mouseX, mouseY, state));
        }
        IComponent.super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (opened) {
            settings.forEach(setting -> setting.keyTyped(typedChar, keyCode));
        }
        IComponent.super.keyTyped(typedChar, keyCode);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return MouseUtil.isHovered2(x + 2, y, width - 2, 17, mouseX, mouseY);
    }
}
