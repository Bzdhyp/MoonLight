package wtf.moonlight.gui.click.arcane.component.settings;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Mouse;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.utils.animations.advanced.Animation;
import wtf.moonlight.utils.animations.advanced.Direction;
import wtf.moonlight.utils.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.utils.animations.advanced.impl.SmoothStepAnimation;
import wtf.moonlight.utils.render.ColorUtils;
import wtf.moonlight.utils.render.RenderUtils;
import wtf.moonlight.utils.render.RoundedUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Guyuemang
 * @Date：2025/6/14 14:03
 */
public class ModeComponent extends Component {
    private final ListValue setting;
    private float maxScroll = Float.MAX_VALUE, rawScroll, scroll;
    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);
    private final Animation open = new DecelerateAnimation(175, 1);
    private boolean opened;
    private final Map<String, DecelerateAnimation> select = new HashMap<>();
    public ModeComponent(ListValue setting) {
        this.setting = setting;
        setHeight(38);
        open.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
    }
    @Override
    public void drawScreen(int mouseX, int mouseY) {
        RoundedUtils.drawRound(getX() + 10, getY() + getHeight() - 4, 145, 1, 0, INSTANCE.getArcaneClickGui().linecolor);
        Fonts.Bold.get(18).drawString(setting.getName(), getX() + 10, getY() + 4, ColorUtils.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(), 0.4f));
        open.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
        if (open.getOutput() > 0.1) {
            float totalHeight = (float) ((setting.getModes().length * 20 + 2) * open.getOutput());
            float y = (getY() + 12 - getHalfTotalHeight()) < INSTANCE.getArcaneClickGui().getY() + 49 ? INSTANCE.getArcaneClickGui().getY() + 49 : (getY() + 12 - getHalfTotalHeight());

            GlStateManager.translate(0, 0, 2f);


            RoundedUtils.drawRound(getX() + 10, getY() + 32, 145, totalHeight, 2, INSTANCE.getArcaneClickGui().smallbackgroundColor2);
            for (String str : setting.getModes()) {
                select.putIfAbsent(str, new DecelerateAnimation(250, 1));
                select.get(str).setDirection(str.equals(setting.getValue()) ? Direction.FORWARDS : Direction.BACKWARDS);

                if (str.equals(setting.getValue())) {
                    RoundedUtils.drawRound(getX() + 12, ((float) (getY() + 34 + (Arrays.asList(setting.getModes()).indexOf(str) * 20) * open.getOutput())) + getScroll(), 141, 18, 2,
                            new Color(ColorUtils.applyOpacity(INSTANCE.getArcaneClickGui().backgroundColor.getRGB(), (float) select.get(setting.getValue()).getOutput())));
                }
                Fonts.Bold.get(16).drawString(str, getX() + 14, getY() + 40 + (Arrays.asList(setting.getModes()).indexOf(str) * 20) * open.getOutput() + getScroll(), ColorUtils.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(), (float) (1 * open.getOutput())));
            }

            onScroll(30, mouseX, mouseY);
            maxScroll = Math.max(0, setting.getModes().length == 0 ? 0 : (setting.getModes().length - 6) * 20);

            GlStateManager.translate(0, 0, -2f);
        }
        RoundedUtils.drawRound(getX() + 10, getY() + 14, 145, 14, 2, INSTANCE.getArcaneClickGui().smallbackgroundColor2);
        Fonts.Bold.get(16).drawString(setting.getValue(), getX() + 14, getY() + 15 + Fonts.Bold.get(16).getMiddleOfBox(17), ColorUtils.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(), 1));
        Fonts.Icon.get(16).drawString("U", getX() + 145, getY() + 20, ColorUtils.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(), 1));
        super.drawScreen(mouseX, mouseY);
    }
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouse) {
        if (RenderUtils.isHovering(getX() + 10, getY() + 14, 145, 14,mouseX,mouseY) && mouse == 1){
            opened = !opened;
        }
        if (opened){
            for (String str : setting.getModes()) {
                if (RenderUtils.isHovering(getX() + 12, ((float) (getY() + 34 + (Arrays.asList(setting.getModes()).indexOf(str) * 20) * open.getOutput())) + getScroll(), 141, 18, mouseX, mouseY) && mouse == 0) {
                    setting.setValue(str);
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouse);
    }
    public void onScroll(int ms, int mx, int my) {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        float halfTotalHeight = (float) ((getSize() * 20 * open.getOutput()) / 2f);
        float y = (getY() + 12 - halfTotalHeight / 2f) < INSTANCE.getArcaneClickGui().getY() + 49 ? INSTANCE.getArcaneClickGui().getY() + 49 : (getY() + 12 - halfTotalHeight);
        float visibleHeight = getVisibleHeight();

        if (RenderUtils.isHovering(getX() + 115,
                y,
                80f,
                visibleHeight, mx, my)) {
            rawScroll += (float) Mouse.getDWheel() * 20;
        }
        rawScroll = Math.max(Math.min(0, rawScroll), -maxScroll);
        scrollAnimation = new SmoothStepAnimation(ms, rawScroll - scroll, Direction.BACKWARDS);
    }

    private float getVisibleHeight() {
        return (float) ((getY() + 12 - getSize() * 20 * open.getOutput() / 2f < INSTANCE.getArcaneClickGui().getY() + 49 ? MathHelper.clamp_double(getY() + 12 - getSize() * 20 * open.getOutput() / 2f - INSTANCE.getArcaneClickGui().getY() + 49, 0, 999) : 122) * open.getOutput());
    }
    private float getHalfTotalHeight() {
        return (float) ((getSize() * 20 * open.getOutput()) / 2f);
    }
    private int getSize(){
        return Math.min(4, (setting.getModes().length - 1));
    }
    public float getScroll() {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        return scroll;
    }
    @Override
    public boolean isHovered(float mouseX, float mouseY) {
        return opened && RenderUtils.isHovering(getX() + 115,
                (getY() + 12 - getHalfTotalHeight()) < INSTANCE.getArcaneClickGui().getY() + 49 ? INSTANCE.getArcaneClickGui().getY() + 49 : (getY() + 12 - getHalfTotalHeight()),
                80f,
                (float) ((getY() + 12 - getSize() * 20 * open.getOutput() / 2f < INSTANCE.getArcaneClickGui().getY() + 49 ? MathHelper.clamp_double(getY() + 12 - getHalfTotalHeight() - INSTANCE.getArcaneClickGui().getY() + 49,0,999) : 122) * open.getOutput()), (int) mouseX, (int) mouseY);
    }
    @Override
    public boolean isVisible() {
        return setting.getVisible().get();
    }
}
