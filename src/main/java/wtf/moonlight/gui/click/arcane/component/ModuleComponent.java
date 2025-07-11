package wtf.moonlight.gui.click.arcane.component;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import wtf.moonlight.Client;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.click.arcane.component.settings.*;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.Value;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.module.Module;
import wtf.moonlight.utils.animations.advanced.Animation;
import wtf.moonlight.utils.animations.advanced.Direction;
import wtf.moonlight.utils.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.utils.render.ColorUtils;
import wtf.moonlight.utils.render.RenderUtils;
import wtf.moonlight.utils.render.RoundedUtils;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 22:47
 */
@Getter
public class ModuleComponent extends Component {
    private final Module module;
    @Setter
    private int scroll = 0;
    @Setter
    private boolean left = true;
    private final ObjectArrayList<Component> components = new ObjectArrayList<>();
    private final Animation enabled = new DecelerateAnimation(250,1);
    private final Animation hover = new DecelerateAnimation(250,1);
    public ModuleComponent(Module module) {
        this.module = module;
        for (Value setting : module.getValues()) {
            if (setting instanceof BoolValue bool) {
                components.add(new BooleanComponent(bool));
            }else if (setting instanceof SliderValue number) {
                components.add(new NumberComponent(number));
            }else if (setting instanceof ListValue modeValue) {
                components.add(new ModeComponent(modeValue));
            }else if (setting instanceof MultiBoolValue booleanValue) {
                components.add(new MultiBoxComponent(booleanValue));
            }else if (setting instanceof ColorValue colorValue) {
                components.add(new ColorPickerComponent(colorValue));
            }else if (setting instanceof StringValue textValue) {
                components.add(new StringComponent(textValue));
            }
        }
        enabled.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
    }
    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float y = getY() + 6 + scroll;
        enabled.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        hover.setDirection(RenderUtils.isHovering(getX() + 135, y + 4, 22, 12,mouseX,mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);
        Animation moduleAnimation = module.getAnimation();
        moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS));

        RoundedUtils.drawRound(getX(), y, 165, getHeight(), 2, INSTANCE.getArcaneClickGui().backgroundColor);
        Fonts.Bold.get(18).drawString(module.getName(), getX() + 10, y + 5,ColorUtils.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),1f));

        Color bgColor = module.isEnabled()
                ? new Color(ColorUtils.applyOpacity(Client.INSTANCE.getModuleManager().getModule(Interface.class).color(), 0.4f))
                : new Color(0, 0, 0, 80);
        RoundedUtils.drawRound(getX() + 135, y + 4, 20, 10, 4, bgColor);

        RenderUtils.drawCircleCGUI(getX() + 141 + moduleAnimation.getOutput() * 9f, y + 9, 8, new Color(Client.INSTANCE.getModuleManager().getModule(Interface.class).color()).darker().getRGB());

        float componentY = y + 22;
        ObjectArrayList<Component> filtered = components.stream()
                .filter(Component::isVisible)
                .collect(ObjectArrayList::new, ObjectArrayList::add, ObjectArrayList::addAll);
        for (Component component : filtered) {
            component.setX(getX());
            component.setY(componentY);
            component.drawScreen(mouseX, mouseY);
            componentY += component.getHeight();
        }
    }
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float y = getY() + 6 + scroll;
        if (RenderUtils.isHovering(getX() + 135, y + 4, 20, 10,mouseX,mouseY) && mouseButton == 0){
            module.toggle();
        }
        for (Component component : components) {
            component.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        for (Component component : components) {
            component.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (Component component : components) {
            component.keyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }
    public int getMaxScroll() {
        return (int) (((getY() - INSTANCE.getArcaneClickGui().getY()) + getHeight()) * 4);
    }
}
