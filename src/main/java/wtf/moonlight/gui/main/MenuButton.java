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
package wtf.moonlight.gui.main;

import wtf.moonlight.Client;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.gui.main.button.Button;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.SmoothStepAnimation;
import wtf.moonlight.util.render.MouseUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;

public class MenuButton implements Button {
    public final String text;
    private Animation hoverAnimation;
    public float x, y, width, height;
    public Runnable clickAction;

    public MenuButton(String text) {
        this.text = text;
    }

    @Override
    public void initGui() {
        hoverAnimation = new SmoothStepAnimation(400, 1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        boolean hovered = MouseUtil.isHovered2(x, y, width, height, mouseX, mouseY);
        hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
        Color rectColor = new Color(0, 0, 0, 64);
        Color outline = hovered ? new Color(Client.INSTANCE.getModuleManager().getModule(Interface.class).color()) : new Color(43, 166, 253, 48);
        RoundedUtil.drawRoundOutline(x, y, width, height, 8, 0.01f, rectColor, outline);

        Fonts.interBold.get(15).drawCenteredString(text, x + width / 2f, y + Fonts.interBold.get(15).getMiddleOfBox(height) + 2, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hovered = MouseUtil.isHovered2(x, y, width, height, mouseX, mouseY);
        if (hovered) clickAction.run();
    }
}