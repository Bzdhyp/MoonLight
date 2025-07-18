package wtf.moonlight.gui.click.arcane;

import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;
import org.lwjglx.input.Mouse;
import wtf.moonlight.Client;
import wtf.moonlight.gui.click.arcane.panel.CategoryPanel;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.impl.visual.ESP2D;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 18:57
 */
@Getter
public class ArcaneClickGui extends GuiScreen {
    private final List<CategoryPanel> categoryPanels = new ArrayList<>();
    public int x;
    public int y;
    public int w = 360;
    public final int h = 380;
    private int dragX;
    private int dragY;
    private boolean dragging = false;
    public Color backgroundColor;
    public Color backgroundColor2;
    public Color backgroundColor3;
    public Color smallbackgroundColor;
    public Color smallbackgroundColor2;
    public Color linecolor;
    public Color versionColor;
    public Color fontcolor;
    private final Animation animations = new DecelerateAnimation(250, 1);
    private final Animation animations2 = new DecelerateAnimation(250, 1);
    private final Animation hoverAnimation = new DecelerateAnimation(1000, 1);
    public final ESPComponent espPreviewComponent = new ESPComponent();
    boolean sb = false;

    public ArcaneClickGui(){
        Arrays.stream(Categor.values()).forEach(moduleCategory -> {
            CategoryPanel panel = new CategoryPanel(moduleCategory);
            if (moduleCategory == Categor.Combat) {
                panel.setSelected(true);
            }
            categoryPanels.add(panel);
        });
        animations2.setDirection(Direction.FORWARDS);
        backgroundColor = new Color(22, 22, 26, 255);
        backgroundColor2 = new Color(17, 17, 19, 255);
        backgroundColor3 = new Color(15, 15, 17, 255);
        smallbackgroundColor = new Color(22, 22, 26, 255);
        smallbackgroundColor2 = new Color(29, 29, 35, 255);
        linecolor = new Color(30, 30, 30, 255);
        versionColor = new Color(255, 255, 255, 50);
        fontcolor = new Color(255, 255, 255, 255);
        x = 260;
        y = 50;
    }

    public boolean GuiInvMove() {
        return true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            x = mouseX + dragX;
            y = mouseY + dragY;
        }

        RoundedUtil.drawRound(x, y, w, h, 7, backgroundColor);
        RenderUtil.startGlScissor(x, y + 35, w, h - 35);
        RoundedUtil.drawGradientVertical(x, y + 30, w, h - 30, 7, backgroundColor2, backgroundColor3);
        RenderUtil.stopGlScissor();
        String firstLetter = Client.INSTANCE.clientName.substring(0, 1);
        String remainingText = Client.INSTANCE.clientName.substring(1);
        int firstLetterWidth = Fonts.Bold.get(30).getStringWidth(firstLetter);

        Fonts.Bold.get(30).drawStringDynamic(firstLetter, x + 10, y + 10, 1, 6);
        Fonts.Bold.get(30).drawStringWithShadow(remainingText, x + 10 + firstLetterWidth, y + 10, fontcolor.getRGB());

        Fonts.Bold.get(18).drawStringDynamic("v2", x + w - 32, y + 16, 1, 6);

        RoundedUtil.drawRound(x + w - 68, y + 44, 60, 25, 5, smallbackgroundColor);
        RoundedUtil.drawRound(x + 10, y + 44, 96, 25, 5, smallbackgroundColor);
        RoundedUtil.drawRound(x + 53, y + 44, 1, 25, 5, smallbackgroundColor2);

        if (Mouse.isButtonDown(0)) {
            if (RenderUtil.isHovering(x + 10, y + 44, 96 / 2, 25, mouseX, mouseY)) {
                animations2.setDirection(Direction.BACKWARDS);
                for (CategoryPanel panel : categoryPanels) {
                    panel.setSelected(panel.getCategory() == Categor.Visual);
                    sb = true;
                }
            } else if (RenderUtil.isHovering(x + 10 + 96 / 2, y + 44, 96 / 2, 25, mouseX, mouseY)) {
                animations2.setDirection(Direction.FORWARDS);
                sb = false;
            }
        }
        RoundedUtil.drawRound((float) (x + 10 + (96 / 2) * animations2.getOutput()), y + 44, 96 / 2, 25, 5, smallbackgroundColor2);

        if (Client.INSTANCE.getModuleManager().getModule(ESP2D.class).isEnabled() && sb) {
            espPreviewComponent.drawScreen(mouseX, mouseY);
        }

        Animation moduleAnimation = animations;
        if (Mouse.isButtonDown(0)) {
            if (RenderUtil.isHovering(x + w - 38, y + 44, 30, 25, mouseX, mouseY)) {
                backgroundColor = new Color(250, 250, 254, 255);
                backgroundColor2 = new Color(255, 255, 255, 255);
                backgroundColor3 = new Color(217, 217, 216, 255);
                smallbackgroundColor = new Color(246, 248, 252, 255);
                smallbackgroundColor2 = new Color(234, 236, 243, 255);
                linecolor = new Color(210, 210, 210, 255);
                versionColor = new Color(0, 0, 0, 50);
                fontcolor = new Color(19, 17, 19, 255);
                moduleAnimation.setDirection(Direction.BACKWARDS);
            } else if (RenderUtil.isHovering(x + w - 68, y + 44, 30, 25, mouseX, mouseY)) {
                backgroundColor = new Color(22, 22, 26, 255);
                backgroundColor2 = new Color(17, 17, 19, 255);
                backgroundColor3 = new Color(15, 15, 17, 255);
                smallbackgroundColor = new Color(22, 22, 26, 255);
                smallbackgroundColor2 = new Color(29, 29, 35, 255);
                linecolor = new Color(50, 50, 50, 255);
                versionColor = new Color(255, 255, 255, 50);
                fontcolor = new Color(255, 255, 255, 255);
                moduleAnimation.setDirection(Direction.FORWARDS);
            }
        }

        RoundedUtil.drawRound((float) (x + w - 30 * moduleAnimation.getOutput() - 38), y + 44, 30, 25, 5, smallbackgroundColor2);

        RoundedUtil.drawRound(x + 10, y + h - 35, 158, 25, 5, smallbackgroundColor);
        Fonts.Icon.get(18).drawStringDynamic("R", x + w - 56, y + 55, 1, 6);
        Fonts.Icon.get(20).drawStringDynamic("S", x + w - 28, y + 54.5f, 1, 6);
        RoundedUtil.drawRound(x + w - 134, y + h - 35, 124, 25, 5, smallbackgroundColor);
        Fonts.Bold.get(16).drawStringDynamic(mc.thePlayer.getNameClear(), x + w - 132, y + h - 30, 1, 6);
        RenderUtil.renderPlayer2D(mc.thePlayer, x + w - 34, y + h - 34, 22, 8, -1);
        Fonts.Semibold.get(16).drawString("Time remaining:", x + w - 132, y + h - 20, fontcolor.getRGB());
        Fonts.Semibold.get(16).drawStringDynamic("Lifetime", x + w - 132 + Fonts.Semibold.get(16).getStringWidth("Time remaining:"), y + h - 20, 1, 6);

        Fonts.Icon.get(25).drawStringDynamic("M", x + 16, y + 53.5f, 1, 6);
        Fonts.Icon.get(20).drawStringDynamic("G", x + 62, y + 54.5f, 1, 6);
        Fonts.Bold.get(16).drawString("ESP", x + 28, y + 54.5f, fontcolor.getRGB());

        Color rectColor = smallbackgroundColor2;
        rectColor = ColorUtil.interpolateColorC(rectColor, ColorUtil.brighter(rectColor, 0.6f), (float) this.hoverAnimation.getOutput());
        boolean hovered = RenderUtil.isHovering(x + 10, y + h - 35, 158, 25, mouseX, mouseY);
        for (CategoryPanel categoryPanel : categoryPanels) {
            hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
            categoryPanel.drawScreen(mouseX, mouseY);
            if (categoryPanel.getCategory() == Categor.Search || categoryPanel.getCategory() == Categor.Config)
                continue;
            if (categoryPanel.isSelected()) {
                RoundedUtil.drawRound((categoryPanel.getCategory().ordinal() >= 5 ? x + 3 + categoryPanel.getCategory().ordinal() * 28
                        : categoryPanel.getCategory().ordinal() == 4 ? x + 8.5f + categoryPanel.getCategory().ordinal() * 28
                        : categoryPanel.getCategory().ordinal() == 3 ? x + 10.5f + categoryPanel.getCategory().ordinal() * 28
                        : categoryPanel.getCategory().ordinal() == 2 ? x + 10 + categoryPanel.getCategory().ordinal() * 28
                        : categoryPanel.getCategory().ordinal() == 1 ? x + 13 + categoryPanel.getCategory().ordinal() * 28
                        : x + 15), categoryPanel.getCategory().ordinal() >= 6 ? y + 44
                        : y + h - 30.5f, 15, 15, 5, rectColor.brighter());
                Fonts.Icon.get(22).drawStringDynamic(categoryPanel.getCategory().icon,
                        (categoryPanel.getCategory().ordinal() >= 6 ? x + 62
                                : categoryPanel.getCategory().ordinal() == 5 ? x + 6.5f + categoryPanel.getCategory().ordinal() * 28
                                : categoryPanel.getCategory().ordinal() == 4 ? x + 10 + categoryPanel.getCategory().ordinal() * 28
                                : categoryPanel.getCategory().ordinal() == 3 ? x + 12.5f + categoryPanel.getCategory().ordinal() * 28
                                : categoryPanel.getCategory().ordinal() == 2 ? x + 13 + categoryPanel.getCategory().ordinal() * 28
                                : categoryPanel.getCategory().ordinal() == 1 ? x + 17 + categoryPanel.getCategory().ordinal() * 28
                                : x + 18), categoryPanel.getCategory().ordinal() >= 6 ? y + 54.5f
                                : y + h - 25
                        , 1, 6);
            } else {
                Fonts.Icon.get(22).drawString(categoryPanel.getCategory().icon,
                        (categoryPanel.getCategory().ordinal() >= 6 ? x + 62
                                : categoryPanel.getCategory().ordinal() == 5 ? x + 6.5f + categoryPanel.getCategory().ordinal() * 28
                                : categoryPanel.getCategory().ordinal() == 4 ? x + 10 + categoryPanel.getCategory().ordinal() * 28
                                : categoryPanel.getCategory().ordinal() == 3 ? x + 12.5f + categoryPanel.getCategory().ordinal() * 28
                                : categoryPanel.getCategory().ordinal() == 2 ? x + 13 + categoryPanel.getCategory().ordinal() * 28
                                : categoryPanel.getCategory().ordinal() == 1 ? x + 17 + categoryPanel.getCategory().ordinal() * 28
                                : x + 18), categoryPanel.getCategory().ordinal() >= 6 ? y + 54.5f
                                : y + h - 25
                        , versionColor.getRGB());

            }
        }
        Fonts.Bold.get(16).drawString("Display", x + 73, y + 54.5f, fontcolor.getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (RenderUtil.isHovering(x,y,100,35, mouseX, mouseY)) {
            dragging = true;
            dragX = x - mouseX;
            dragY = y - mouseY;
        }
        espPreviewComponent.mouseClicked(mouseX, mouseY,mouseButton);
        if (mouseButton == 0){
            for (CategoryPanel panel : categoryPanels) {
                if (handleCategoryPanel(panel, mouseX, mouseY)) {
                    break;
                }
            }
        }
        CategoryPanel selected = getSelected();
        if (selected != null) {
            selected.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        CategoryPanel selected = getSelected();
        if (selected != null) {
            selected.keyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    private boolean handleCategoryPanel(CategoryPanel panel, int mouseX, int mouseY) {
        if (RenderUtil.isHovering((panel.getCategory().ordinal() >= 5 ? x + 3 + panel.getCategory().ordinal() * 28
                : panel.getCategory().ordinal() >= 4 ? x + 8.5f + panel.getCategory().ordinal() * 28
                : panel.getCategory().ordinal() >= 3 ? x + 10.5f + panel.getCategory().ordinal() * 28
                : panel.getCategory().ordinal() >= 2 ? x + 10 + panel.getCategory().ordinal() * 28
                : panel.getCategory().ordinal() >= 1 ? x + 13 + panel.getCategory().ordinal() * 28
                : x + 15 + panel.getCategory().ordinal() * 28),panel.getCategory().ordinal() >= 6 ? y + 44
                : y + h - 30.5f, 15,15, mouseX, mouseY)) {
            for (CategoryPanel p : categoryPanels) {
                p.setSelected(false);
            }
            panel.setSelected(true);
            return true;
        }
        return false;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        espPreviewComponent.mouseReleased(mouseX, mouseY,state);
        if (state == 0){
            dragging = false;
        }
        CategoryPanel selected = getSelected();
        if (selected != null) {
            selected.mouseReleased(mouseX, mouseY, state);
        }
    }

    public CategoryPanel getSelected() {
        return categoryPanels.stream().filter(CategoryPanel::isSelected).findAny().orElse(null);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
