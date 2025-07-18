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
package wtf.moonlight.gui.click.neverlose.components;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.values.Value;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.click.neverlose.components.settings.*;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.MouseUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;

import static wtf.moonlight.gui.click.neverlose.NeverLose.*;

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
            }
            if (setting instanceof SliderValue slider) {
                components.add(new SliderComponent(slider));
            }
            if (setting instanceof ListValue mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof MultiBoolValue modes) {
                components.add(new MultiBoxComponent(modes));
            }
            if (setting instanceof ColorValue color) {
                components.add(new ColorPickerComponent(color));
            }
            if (setting instanceof StringValue string) {
                components.add(new StringComponent(string));
            }
        }
        enabled.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        hover.setDirection(Direction.BACKWARDS);
    }
    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float y = getY() + 6 + scroll;
        //name
        Fonts.interSemiBold.get(14).drawString(module.getName().replaceAll("(?<=[a-z])(?=[A-Z])", " ").toUpperCase(),getX() + 4,y,moduleTextRGB);
        //rect
        RoundedUtil.drawRoundOutline(getX(),y + 10,getWidth() / 2,getHeight(),4,0.1f,bgColor4,outlineColor);
        //enable
        enabled.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        hover.setDirection(MouseUtil.isHovered2(getX() + 154,y + 16,20,10,mouseX,mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);
        Fonts.interSemiBold.get(18).drawString("Enabled",getX() + 6,y + 18,textRGB);
        RoundedUtil.drawRound(getX() + 154,y + 16,20,10,4,new Color(ColorUtil.interpolateColor2(new Color(ColorUtil.interpolateColor2(boolBgColor,boolBgColor2,(float) enabled.getOutput())),
                new Color(ColorUtil.interpolateColor2(boolBgColor,boolBgColor2,(float) enabled.getOutput())).brighter().brighter(), (float) hover.getOutput())));
        RenderUtil.drawCircle(getX() + 159 + 10 * (float) enabled.getOutput(),y + 21,0,360,5,.1f,true, ColorUtil.interpolateColor2(new Color(ColorUtil.interpolateColor2(boolCircleColor2,boolCircleColor,(float) enabled.getOutput())),
                new Color(ColorUtil.interpolateColor2(boolCircleColor.darker().darker(),boolCircleColor,(float) enabled.getOutput())).brighter().brighter(), (float) hover.getOutput()));
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
        super.drawScreen(mouseX, mouseY);
    }
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (MouseUtil.isHovered2(getX() + 154,getY() + scroll + 22,20,10,mouseX,mouseY) && mouseButton == 0){
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
        return (int) (((getY() - INSTANCE.getNeverLose().getPosY()) + getHeight()) * 4);
    }
}
