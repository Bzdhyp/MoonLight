package wtf.moonlight.gui.main;

import net.minecraft.client.gui.*;
import org.bytedeco.javacv.FrameGrabber;
import wtf.moonlight.Client;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.gui.main.alt.GuiAccountManager;
import wtf.moonlight.gui.main.particle.ParticleEngine;
import wtf.moonlight.gui.main.video.VideoPlayer;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RoundedUtil;
import wtf.moonlight.util.render.shader.KawaseBlur;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class MainMenu extends GuiScreen {
    private final List<MenuButton> buttons = List.of(
            new MenuButton("Singleplayer"),
            new MenuButton("Multiplayer"),
            new MenuButton("Alt Manager"),
            new MenuButton("Settings"),
            new MenuButton("Exit"));
    public ParticleEngine particle = new ParticleEngine();

    @Override
    public void initGui() {
        buttons.forEach(MenuButton::initGui);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        try {
            VideoPlayer.render(0, 0, sr.getScaledWidth(), sr.getScaledHeight());
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }

        int count = 20;
        float buttonWidth = 124;
        float buttonHeight = 20;
        float frameY = height / 2f - (buttons.size() * (buttonHeight + 2)) / 2f - 16;

        particle.render(0, 0);

        RoundedUtil.drawRound(width / 2f - buttonWidth / 2f - 14, (height / 2f) - 80, buttonWidth + 14 * 2, 180, 8, new Color(0, 0, 0, 74));
        KawaseBlur.startBlur();
        RoundedUtil.drawRound(width / 2f - buttonWidth / 2f - 14, (height / 2f) - 80, buttonWidth + 14 * 2, 180, 8, new Color(0, 0, 0, 74));
        KawaseBlur.endBlur(10, 2);

        String text = Client.INSTANCE.getModuleManager().getModule(Interface.class).clientName.getValue();
        int textColor = ColorUtil.swapAlpha(Client.INSTANCE.getModuleManager().getModule(Interface.class).color(), 255);
        int defaultColor = new Color(255, 255, 255, 255).getRGB();
        float y = frameY + 6;

        FontRenderer font = Fonts.interBold.get(40);

        if (text != null && !text.isEmpty()) {
            int splitPoint = Math.min(4, text.length());
            int totalWidth = font.getStringWidth(text.substring(0, splitPoint)) + font.getStringWidth(text.substring(splitPoint));
            float x = width / 2f - totalWidth / 2f;

            MainMenu.renderColoredText(font, text, x, y, textColor, defaultColor);
        } else {
            String clientName = Client.INSTANCE.getClientName();
            int totalWidth = font.getStringWidth(clientName);
            float x = width / 2f - totalWidth / 2f;
            float y2 = frameY + 10;

            font.drawStringWithShadow(clientName, x, y2, defaultColor);
        }

        for (MenuButton button : buttons) {
            button.x = width / 2f - buttonWidth / 2f;
            button.y = ((height / 2f) + count - (buttons.size() * buttonHeight) / 2f) - 1;
            button.width = buttonWidth;
            button.height = buttonHeight;
            button.clickAction = () -> {
                switch (button.text) {
                    case "Singleplayer" -> mc.displayGuiScreen(new GuiSelectWorld(this));
                    case "Multiplayer" -> mc.displayGuiScreen(new GuiMultiplayer(this));
                    case "Alt Manager" -> mc.displayGuiScreen(new GuiAccountManager(this));
                    case "Settings" -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                    case "Exit" -> mc.shutdown();
                }
            };
            button.drawScreen(mouseX, mouseY);
            count += (int) (buttonHeight + 5);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static void renderColoredText(FontRenderer font, String text, float x, float y, int textColor, int defaultColor) {
        if (text == null || text.isEmpty()) return;

        int splitPoint = Math.min(4, text.length());
        String colorPart = text.substring(0, splitPoint);
        String whitePart = text.substring(splitPoint);

        font.drawStringWithShadow(colorPart, x, y, textColor);
        font.drawStringWithShadow(whitePart, x + font.getStringWidth(colorPart), y, defaultColor);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        buttons.forEach(button -> button.mouseClicked(mouseX, mouseY, mouseButton));
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}