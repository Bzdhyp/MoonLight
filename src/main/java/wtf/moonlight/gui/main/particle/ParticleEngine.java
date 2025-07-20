package wtf.moonlight.gui.main.particle;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.util.render.GLUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.animations.AnimationUtil;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParticleEngine {

    public Color cc = new Color(255, 255, 255);
    public CopyOnWriteArrayList<Particle> particles = Lists.newCopyOnWriteArrayList();
    public float lastMouseX;
    public float lastMouseY;

    public ParticleEngine(Color c) {
        cc = c;
    }

    public ParticleEngine() {

    }

    public void render(float mouseX, float mouseY) {
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        RenderUtil.resetColor();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        float xOffset = sr.getScaledWidth() / 2f - mouseX;
        float yOffset = sr.getScaledHeight() / 2f - mouseY;

        for (particles.size(); particles.size() < (int) (sr.getScaledWidth() / 19.2f); particles.add(new Particle(sr, new Random().nextFloat() * 2 + 2, new Random().nextFloat() * 5 + 5)));

        List<Particle> toremove = Lists.newArrayList();
        for (Particle p : particles) {
            p.opacity = AnimationUtil.moveUD(p.opacity, 32,10f / Minecraft.getDebugFPS(),3f / Minecraft.getDebugFPS());
            Color c = new Color(cc.getRed(), cc.getGreen(), cc.getBlue(), (int) p.opacity);

            GL11.glEnable(2848);
            GL11.glEnable(2881);
            GL11.glEnable(2832);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glHint(3154, 4354);
            GL11.glHint(3155, 4354);
            GL11.glHint(3153, 4354);

            drawBorderedCircle(p.x + MathHelper.sin(p.ticks / 2) * 50 + -xOffset / 5, sr.getScaledHeight() - ((p.ticks * p.speed) * p.ticks / 10 + yOffset / 5), p.radius * (p.opacity / 32), c.getRGB(), c.getRGB());

            GL11.glDisable(2848);
            GL11.glDisable(2881);
            GL11.glEnable(2832);

            p.ticks += (0.9f / (float) Minecraft.getDebugFPS());
            if (((p.ticks * p.speed) * p.ticks / 10 + yOffset / 5) > sr.getScaledHeight() || ((p.ticks * p.speed) * p.ticks / 10 + yOffset / 5) < 0 || (p.x + MathHelper.sin(p.ticks / 2) * 50 + -xOffset / 5) > sr.getScaledWidth() || (p.x + MathHelper.sin(p.ticks / 2) * 50 + -xOffset / 5) < 0) {
                toremove.add(p);
            }
        }

        particles.removeAll(toremove);
        GlStateManager.color(1, 1, 1, 1);
        GL11.glColor4f(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        lastMouseX = GLUtil.getMouseX();
        lastMouseY = GLUtil.getMouseY();
    }

    public static void drawBorderedCircle(double x, double y, float radius, int outsideC, int insideC) {
        //  GL11.glEnable((int)3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        float scale = 0.1f;
        GL11.glScalef(0.1f, 0.1f, 0.1f);
        drawCircle(x *= 10, y *= 10, radius *= 10.0f, insideC);
        // drawUnfilledCircle(x, y, radius, 1.0f, outsideC);
        GL11.glScalef(10.0f, 10.0f, 10.0f);
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        //  GL11.glDisable((int)3042);
        GL11.glDisable(2848);
    }

    public static void drawCircle(double x, double y, float radius, int color) {
        float alpha = (float)(color >> 24 & 255) / 255.0f;
        float red = (float)(color >> 16 & 255) / 255.0f;
        float green = (float)(color >> 8 & 255) / 255.0f;
        float blue = (float)(color & 255) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(9);
        int i = 0;
        while (i <= 360) {
            GL11.glVertex2d(x + Math.sin((double)i * 3.141526 / 180.0) * (double)radius, y + Math.cos((double)i * 3.141526 / 180.0) * (double)radius);
            ++i;
        }
        GL11.glEnd();
    }
}
