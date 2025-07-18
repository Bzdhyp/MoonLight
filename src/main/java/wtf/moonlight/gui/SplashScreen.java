package wtf.moonlight.gui;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.Client;
import wtf.moonlight.gui.font.Fonts;
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
    private static Animation progress3Anim;
    private static Animation progress4Anim;

    private static void drawScreen(float width, float height) {
        animation.setDirection((float) progressAnim.getOutput() >= 0.5 ? Direction.FORWARDS : Direction.BACKWARDS);
        float progress = (float) progress2Anim.getOutput();
        RenderUtil.drawRect(0, 0, width, height, new Color(0, 0, 0, 255));

        // Get the first character width (like "A" in the reference)
        String firstChar = Client.INSTANCE.getClientName().substring(0, 1);
        float firstCharWidth = Fonts.interBold.get(40).getStringWidth(firstChar);

        if ((float) progress2Anim.getOutput() >= 0.99f) {
            progress3Anim.setDirection(Direction.FORWARDS);
            progress4Anim.setDirection(Direction.FORWARDS);

            String restOfName = Client.INSTANCE.getClientName().substring(1);

            Fonts.interBold.get(40).drawStringWithShadow(
                    firstChar,
                    5 + width / 2 - firstCharWidth / 2 - ((float) Fonts.interBold.get(40).getStringWidth(restOfName) / 2) * (float)progress3Anim.getOutput() - 5,
                    7 + height / 2 - 50,
                    Client.INSTANCE.getModuleManager().getModule(Interface.class).color()
            );

            Fonts.interBold.get(40).drawStringWithShadow(
                    restOfName,
                    width / 2 - ((float) Fonts.interBold.get(40).getStringWidth(restOfName) / 2) + firstCharWidth / 2,
                    7 + height / 2 - 50 - 120 * (float)progress3Anim.getOutput() + 120,
                    ColorUtil.applyOpacity(new Color(255, 255, 255).getRGB(), (float)progress4Anim.getOutput())
            );
        } else {
            Fonts.interBold.get(40).drawStringWithShadow(
                    firstChar,
                    5 + width / 2 - firstCharWidth / 2,
                    height / 2 - 50 + 7,
                    -1
            );
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
        progress3Anim = new DecelerateAnimation(400, 1).setDirection(Direction.BACKWARDS);
        progress4Anim = new DecelerateAnimation(5000, 1).setDirection(Direction.BACKWARDS);

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
