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
package wtf.moonlight.gui.click.dropdown;

import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import wtf.moonlight.module.Categor;
import wtf.moonlight.gui.click.dropdown.panel.CategoryPanel;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.EaseOutSine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropdownGUI extends GuiScreen {

    @Getter
    private final Animation openingAnimation = new EaseOutSine(400, 1);
    private boolean closing;
    private final List<CategoryPanel> panels = new ArrayList<>();
    public int scroll;

    public DropdownGUI() {
        openingAnimation.setDirection(Direction.BACKWARDS);
        for (Categor category : Categor.values()) {
            if (category == Categor.Search || category == Categor.Config)
                continue;
            panels.add(new CategoryPanel(category));
            float width = 0;
            for (CategoryPanel panel : panels) {
                panel.setX(50 + width);
                panel.setY(20);
                width += panel.getWidth() + 10;
            }
        }
    }

    @Override
    public void initGui() {
        closing = false;
        openingAnimation.setDirection(Direction.FORWARDS);
        super.initGui();
    }

    public boolean GuiInvMove() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        if (Mouse.hasWheel()) {
            final float wheel = Mouse.getDWheel();

            if (wheel != 0)
                scroll += wheel > 0 ? 15 : -15;
        }

        mouseY -= scroll;

        GlStateManager.translate(0, scroll, 0);
        if (closing) {
            openingAnimation.setDirection(Direction.BACKWARDS);
            if (openingAnimation.finished(Direction.BACKWARDS)) {
                mc.displayGuiScreen(null);
            }
        }

        int finalMouseY = mouseY;
        panels.forEach(panel -> panel.drawScreen(mouseX, finalMouseY));
        GlStateManager.translate(0, -scroll, 0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        mouseY -= scroll;
        GlStateManager.translate(0, scroll, 0);
        int finalMouseY = mouseY;
        panels.forEach(panel -> panel.mouseClicked(mouseX, finalMouseY, mouseButton));
        GlStateManager.translate(0, -scroll, 0);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {

        mouseY -= scroll;
        GlStateManager.translate(0, scroll, 0);
        int finalMouseY = mouseY;
        panels.forEach(panel -> panel.mouseReleased(mouseX, finalMouseY, state));
        GlStateManager.translate(0, -scroll, 0);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        panels.forEach(panel -> panel.keyTyped(typedChar, keyCode));
        if (keyCode == Keyboard.KEY_ESCAPE) {
            closing = true;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
