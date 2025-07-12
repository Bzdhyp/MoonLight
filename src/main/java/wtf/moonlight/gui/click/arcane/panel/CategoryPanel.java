package wtf.moonlight.gui.click.arcane.panel;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Mouse;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.click.IComponent;
import wtf.moonlight.gui.click.arcane.component.ModuleComponent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.util.MathUti;
import wtf.moonlight.util.animations.advanced.Animation;
import wtf.moonlight.util.animations.advanced.Direction;
import wtf.moonlight.util.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.animations.advanced.impl.SmoothStepAnimation;
import wtf.moonlight.util.misc.InstanceAccess;
import wtf.moonlight.util.render.RenderUtil;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 19:53
 */
@Getter
@Setter
public class CategoryPanel implements IComponent, InstanceAccess {
    private int posX, posY;
    private float maxScroll = Float.MAX_VALUE, rawScroll, scroll;
    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);
    public final Categor category;
    @Setter
    private boolean selected;
    private final ObjectArrayList<ModuleComponent> moduleComponents = new ObjectArrayList<>();
    private final Animation animation = new DecelerateAnimation(250,1);

    public CategoryPanel(Categor category) {
        this.category = category;
        for (Module module : INSTANCE.getModuleManager().getModules()){
            if (module.getCategory().equals(this.category)){
                moduleComponents.add(new ModuleComponent(module));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        //update coordinate
        posX = INSTANCE.getArcaneClickGui().getX();
        posY = INSTANCE.getArcaneClickGui().getY();
        //select anim
        animation.setDirection(selected ? Direction.FORWARDS : Direction.BACKWARDS);
        //render module components
        if (isSelected()) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.scissor(getPosX(), getPosY() + 75, INSTANCE.getArcaneClickGui().w, INSTANCE.getArcaneClickGui().h - 120);

            float left = 0, right = 0;
            for (int i = 0; i < moduleComponents.size(); i++) {
                ModuleComponent module = moduleComponents.get(i);
                float componentOffset = getComponentOffset(i,left,right);

                module.drawScreen(mouseX, mouseY);

                double scroll = getScroll();
                module.setScroll((int) MathUti.roundToHalf(scroll));
                onScroll(30, mouseX, mouseY);

                maxScroll = Math.max(0, moduleComponents.isEmpty() ? 0 : moduleComponents.get(moduleComponents.size() - 1).getMaxScroll());
                if ((i + 1) % 2 == 0) {
                    left += 30 + componentOffset;
                } else {
                    right += 30 + componentOffset;
                }
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        IComponent.super.drawScreen(mouseX, mouseY);
    }
    private float getComponentOffset(int i, float left, float right) {
        ModuleComponent component = moduleComponents.get(i);
        component.setLeft((i + 1) % 2 != 0);
        component.setX(component.isLeft() ? posX + 10 : posX + 185);
        component.setHeight(24);
        component.setY(posY + 54 + component.getHeight() + ((i + 1) % 2 == 0 ? left : right));
        float componentOffset = 0;
        for (Component component2 : component.getComponents()) {
            if (component2.isVisible())
                componentOffset += component2.getHeight();
        }
        component.setHeight(component.getHeight() + componentOffset);
        return componentOffset;
    }

    public void onScroll(int ms, int mx, int my) {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        if (RenderUtil.isHovering(getPosX(), getPosY() + 75, INSTANCE.getArcaneClickGui().w, INSTANCE.getArcaneClickGui().h - 120, mx, my) && moduleComponents.stream().noneMatch(moduleComponent -> moduleComponent.getComponents().stream().anyMatch(component -> component.isHovered(mx,my)))) {
            rawScroll += (float) Mouse.getDWheel() * 30;
        }
        rawScroll = Math.max(Math.min(0, rawScroll), -maxScroll);
        scrollAnimation = new SmoothStepAnimation(ms, rawScroll - scroll, Direction.BACKWARDS);
    }
    public float getScroll() {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        return scroll;
    }
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isSelected()) {
            moduleComponents.forEach(moduleComponent -> moduleComponent.mouseClicked(mouseX,mouseY,mouseButton));
        }
        IComponent.super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (isSelected()) {
            moduleComponents.forEach(moduleComponent -> moduleComponent.mouseReleased(mouseX,mouseY,state));
        }
        IComponent.super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (isSelected()) {
            moduleComponents.forEach(moduleComponent -> moduleComponent.keyTyped(typedChar,keyCode));
        }
        IComponent.super.keyTyped(typedChar, keyCode);
    }
}
