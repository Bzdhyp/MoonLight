
package wtf.moonlight.gui.click.arcane.component.settings;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Mouse;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.MultiBoolValue;
import wtf.moonlight.util.animations.advanced.Animation;
import wtf.moonlight.util.animations.advanced.Direction;
import wtf.moonlight.util.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.animations.advanced.impl.SmoothStepAnimation;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 23:16
 */
public class MultiBoxComponent extends Component {
    private final MultiBoolValue setting;
    private final Animation open = new DecelerateAnimation(175, 1);
    private final Animation sb = new DecelerateAnimation(175, 1);
    private float maxScroll = Float.MAX_VALUE, rawScroll, scroll;
    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);
    private boolean opened;
    private final Map<BoolValue, DecelerateAnimation> select = new HashMap<>();
    public MultiBoxComponent(MultiBoolValue setting) {
        this.setting = setting;
        setHeight(38);
        open.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        Fonts.Bold.get(18).drawString(setting.getName(), getX() + 10, getY() + 4, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(), 0.4f));

        open.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
        sb.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);

        if (open.getOutput() > 0.1) {
            GlStateManager.translate(0, 0, 2f);
            float outlineY = getY() + 11 + getHalfTotalHeight();
            float outlineHeight = (float) ((setting.getValues().size() * 20 + 2) * open.getOutput());
            float y = (getY() + 12 + getHalfTotalHeight()) < INSTANCE.getArcaneClickGui().getY() + 49 ? INSTANCE.getArcaneClickGui().getY() + 49 : (getY() + 12 + getHalfTotalHeight());

            RoundedUtil.drawRound(getX() + 10, getY() + 32, 145, outlineHeight, 2, INSTANCE.getArcaneClickGui().smallbackgroundColor2);

            for (BoolValue boolValue : setting.getValues()) {
                select.putIfAbsent(boolValue,new DecelerateAnimation(250, 1));
                select.get(boolValue).setDirection(boolValue.get() ? Direction.FORWARDS : Direction.BACKWARDS);

                if (boolValue.get()) {
                    float boolValueY = (float) ((getY() + 34 + (setting.getValues().indexOf(boolValue) * 20) * open.getOutput())) + getScroll();
                    RoundedUtil.drawRound(getX() + 12, boolValueY, 141, 18, 2,
                            new Color(ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().backgroundColor.getRGB(), (float) select.get(boolValue).getOutput())));
                }
                Fonts.Bold.get(16).drawString(boolValue.getName(), getX() + 14, getY() + 40 + (setting.getValues().indexOf(boolValue) * 20 * open.getOutput()) + getScroll(), ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),  (float) (1 * open.getOutput())));

            }

            onScroll(30,mouseX,mouseY);
            maxScroll = Math.max(0, setting.getValues().isEmpty() ? 0 : (setting.getValues().size() - 6) * 20);

            GlStateManager.translate(0, 0, -2f);
        }
        RoundedUtil.drawRound(getX() + 10, getY() + 14, 145, 14, 2, INSTANCE.getArcaneClickGui().smallbackgroundColor2);
        String enabledText = setting.isEnabled().isEmpty() ? "None" : (setting.isEnabled().length() > 30 ? setting.isEnabled().substring(0, 30) + "..." : setting.isEnabled());
        Fonts.Bold.get(16).drawString(enabledText, getX() + 14, getY() + 15 + Fonts.Bold.get(16).getMiddleOfBox(17), ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(), 1));
        Fonts.Icon.get(16).drawString("U", getX() + 145, getY() + 20, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(), 1));

        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouse) {
        if (RenderUtil.isHovering(getX() + 10, getY() + 14, 145, 14,mouseX,mouseY) && mouse == 1){
            opened = !opened;
        }
        if (opened){
            for (BoolValue boolValue : setting.getValues()) {
                if (RenderUtil.isHovering(getX() + 12, (float) ((getY() + 34 + (setting.getValues().indexOf(boolValue) * 20) * open.getOutput())) + getScroll(), 141, 18, mouseX, mouseY) && mouse == 0) {
                    boolValue.set(!boolValue.get());
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouse);
    }
    public void onScroll(int ms, int mx, int my) {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        if (RenderUtil.isHovering(getX() + 94,
                (getY() + 12 - getHalfTotalHeight()) < INSTANCE.getArcaneClickGui().getY() + 49 ? INSTANCE.getArcaneClickGui().getY() + 49 : (getY() + 12 - getHalfTotalHeight()),
                80f,
                (float) (((((getY() + 12 - (getSize() * 20 * open.getOutput()) / 2f) < INSTANCE.getArcaneClickGui().getY() + 49) ? MathHelper.clamp_float((getY() + 12 - getHalfTotalHeight()) - INSTANCE.getArcaneClickGui().getY() + 49,0,999) : 122)) * open.getOutput()), mx, my)) {
            rawScroll += (float) Mouse.getDWheel() * 20;
        }
        rawScroll = Math.max(Math.min(0, rawScroll), -maxScroll);
        scrollAnimation = new SmoothStepAnimation(ms, rawScroll - scroll, Direction.BACKWARDS);
    }
    public float getScroll() {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        return scroll;
    }
    @Override
    public boolean isHovered(float mouseX, float mouseY) {
        return opened && RenderUtil.isHovering(getX() + 94,
                (getY() + 12 - getHalfTotalHeight()) < INSTANCE.getArcaneClickGui().getY() + 49 ? INSTANCE.getArcaneClickGui().getY() + 49 : (getY() + 12 - getHalfTotalHeight()),
                80f,
                (float) (((((getY() + 12 - (getSize() * 20 * open.getOutput()) / 2f) < INSTANCE.getArcaneClickGui().getY() + 49) ? MathHelper.clamp_float((getY() + 12 - getHalfTotalHeight()) - INSTANCE.getArcaneClickGui().getY() + 49,0,999) : 122)) * open.getOutput()), (int) mouseX, (int) mouseY);
    }
    private float getVisibleHeight() {
        return (float) ((getY() + 12 - getSize() * 20 * open.getOutput() / 2f < INSTANCE.getArcaneClickGui().getY() + 49 ? MathHelper.clamp_double(getY() + 12 - getSize() * 20 * open.getOutput() / 2f - INSTANCE.getArcaneClickGui().getY() + 49, 0, 999) : 122) * open.getOutput());
    }
    private float getHalfTotalHeight() {
        return (float) ((getSize() * 20 * open.getOutput()) / 2f);
    }
    private int getSize(){
        return Math.min(4, (setting.getValues().size() - 1));
    }
    @Override
    public boolean isVisible() {
        return setting.getVisible().get();
    }
}
