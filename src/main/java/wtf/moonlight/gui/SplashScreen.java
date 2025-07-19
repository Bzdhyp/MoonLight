package wtf.moonlight.gui;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.Client;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.gui.main.MainMenu;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.misc.InstanceAccess;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;

public class SplashScreen implements InstanceAccess {
    public static Animation progressAnim;
    public static boolean menu;
    public static Animation animation = new DecelerateAnimation(250, 1);
    private static Framebuffer framebuffer;
    private static Animation progress2Anim;

    private static void drawScreen(float width, float height) {
        animation.setDirection((float) progressAnim.getOutput() >= 0.5 ? Direction.FORWARDS : Direction.BACKWARDS);
        float progress = (float) progress2Anim.getOutput();
        RenderUtil.drawRect(0, 0, width, height, new Color(0, 0, 0, 255));

        FontRenderer font = Fonts.interBold.get(40);
        String text = Client.INSTANCE.getModuleManager().getModule(Interface.class).clientName.getValue();
        int textColor = ColorUtil.swapAlpha(Client.INSTANCE.getModuleManager().getModule(Interface.class).color(), 255);
        int defaultColor = new Color(255, 255, 255, 255).getRGB();
        float y = height / 2 - 50 + 7;

        if (text != null && !text.isEmpty()) {
            int totalWidth = font.getStringWidth(text.substring(0, 4)) + font.getStringWidth(text.substring(4));
            float x = width / 2f - totalWidth / 2f;

            MainMenu.renderColoredText(font, text, x, y, textColor, defaultColor);
        } else {
            String clientName = Client.INSTANCE.getClientName();
            int totalWidth = font.getStringWidth(clientName);
            float x = width / 2f - totalWidth / 2f;

            font.drawStringWithShadow(clientName, x, y, defaultColor);
        }

        RoundedUtil.drawRound(width / 2 - (float) 170 / 2, height / 2 + 15, 170, 5, 2, new Color(221, 228, 255));
        RoundedUtil.drawGradientHorizontal(width / 2 - (float) 170 / 2, height / 2 + 15, 170 * progress, 5, 2,
                new Color(Client.INSTANCE.getModuleManager().getModule(Interface.class).color(1)),
                new Color(Client.INSTANCE.getModuleManager().getModule(Interface.class).color(7)));
    }

    public static void drawScreen() {
        ScaledResolution sr = new ScaledResolution(mc);
        int scaleFactor = sr.getScaleFactor();

        // Create the scale factor
        // Bind the width and height to the framebuffer
        framebuffer = RenderUtil.createFrameBuffer(framebuffer);
        progressAnim = new DecelerateAnimation(9000, 1);
        progress2Anim = new DecelerateAnimation(5000, 1);

        while (!progressAnim.isDone()) {
            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);

            // Create the projected image to be rendered
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, sr.getScaledWidth(), sr.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();

            GlStateManager.color(0, 0, 0, 0);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            drawScreen(sr.getScaledWidth(), sr.getScaledHeight());

            framebuffer.unbindFramebuffer();
            framebuffer.framebufferRender(sr.getScaledWidth() * scaleFactor, sr.getScaledHeight() * scaleFactor);
            RenderUtil.setAlphaLimit(1);
            mc.updateDisplay();
        }
    }
}