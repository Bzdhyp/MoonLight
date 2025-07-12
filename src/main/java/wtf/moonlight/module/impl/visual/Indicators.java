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
package wtf.moonlight.module.impl.visual;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import com.cubk.EventTarget;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.misc.HackerDetector;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.util.MathUti;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RotationUtil;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.GLUtil;
import wtf.moonlight.util.render.RenderUtil;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "Indicators", category = Categor.Visual)
public class Indicators extends Module {
    private final SliderValue size = new SliderValue("Size", 6, 3, 30, this);
    private final SliderValue radius = new SliderValue("Radius", 100, 15, 250, 1, this);
    private final BoolValue stretch  = new BoolValue("Stretch", true, this);
    private final BoolValue name = new BoolValue("Name", true, this);
    private final BoolValue outline = new BoolValue("Outline", true, this);
    private final BoolValue renderArrows = new BoolValue("Render Arrows", true, this);
    private final BoolValue renderPearls = new BoolValue("Render Pearls", true, this);
    private final BoolValue renderFireballs = new BoolValue("Render Fireballs", true, this);
    private final BoolValue renderPlayers = new BoolValue("Render Player", false, this);
    private final BoolValue playerInfo = new BoolValue("Player Info", true, this, renderPlayers::get);
    private final BoolValue onlyNotInView  = new BoolValue("Only Not In View", true, this);

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        final ScaledResolution scaledResolution = event.scaledResolution();
        FontRenderer fontRenderer = Fonts.psRegular.get(size.getValue() + 3);

        final float hWidth = scaledResolution.getScaledWidth() / 2.0f;
        final float hHeight = scaledResolution.getScaledHeight() / 2.0f;

        final float partialTicks = event.partialTicks();

        glTranslatef(hWidth, hHeight, 0);

        if (outline.get()) {
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

            glLineWidth(1);
        }

        final double arrowSize = this.size.getValue();
        final double radius = this.radius.getValue();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if ((!onlyNotInView.get() || onlyNotInView.get() && !RenderUtil.isInViewFrustum(entity))&& (entity instanceof EntityPlayer && entity != mc.thePlayer && renderPlayers.get()
                    || entity instanceof EntityArrow && !((EntityArrow) entity).inGround && renderArrows.get()
                    || entity instanceof EntityFireball && renderFireballs.get() || entity instanceof EntityEnderPearl && renderPearls.get())) {
                final Entity local = mc.thePlayer;

                final float currentRotation = MathUti.interpolate(local.prevRotationYaw, local.rotationYaw, partialTicks);

                final double currentPosX = MathUti.interpolate(local.prevPosX, local.posX, partialTicks);
                final double currentPosZ = MathUti.interpolate(local.prevPosZ, local.posZ, partialTicks);

                final double playerPosX = MathUti.interpolate(entity.prevPosX, entity.posX, partialTicks);
                final double playerPosZ = MathUti.interpolate(entity.prevPosZ, entity.posZ, partialTicks);

                final float yawToPlayer = RotationUtil.calculateYawFromSrcToDst(currentRotation, currentPosX, currentPosZ,
                        playerPosX, playerPosZ) - currentRotation;

                glPushMatrix();

                final double rads = Math.toRadians(yawToPlayer);

                final double aspectRatio = stretch.get() ? scaledResolution.getScaledWidth_double() / scaledResolution.getScaledHeight_double() : 1;

                glTranslated(radius * Math.sin(rads) * aspectRatio,
                        radius * -Math.cos(rads), 0);
                glDisable(GL_TEXTURE_2D);

                GLUtil.startBlend();


                int color;

                if (entity instanceof EntityPlayer) {
                    color = getModule(HackerDetector.class).isHacker((EntityPlayer) entity) ? new Color(255, 0, 0).getRGB() : (PlayerUtil.isInTeam(entity) ? new Color(96, 252, 66).getRGB() : new Color(252, 96, 66).getRGB());
                } else if (entity instanceof EntityArrow) {
                    color = new Color(184, 184, 184).getRGB();
                } else if (entity instanceof EntityFireball) {
                    color = new Color(255, 128, 8).getRGB();
                } else {
                    color = new Color(128, 128, 255).getRGB();
                }

                if (name.get()) {
                    glEnable(GL_TEXTURE_2D);
                    final String name = entity.getName();
                    final String displayName = name.equals("entity.ThrownEnderpearl.name") ? "Pearl" : name;
                    final String hacker = entity instanceof EntityPlayer && getModule(HackerDetector.class).isHacker((EntityPlayer) entity) ? "[Hacker] " : "";
                    fontRenderer.drawString(hacker + displayName, (double) -fontRenderer.getStringWidth(hacker + displayName) / 2, -arrowSize / 2.0 - fontRenderer.getHeight() - 4, 0xFFFFFFFF);
                    glDisable(GL_TEXTURE_2D);
                }

                if (playerInfo.canDisplay() && playerInfo.get() && entity instanceof EntityPlayer player && entity != mc.thePlayer) {

                    final float health = player.getHealth() / player.getMaxHealth();

                    final int healthColor = ColorUtil.getColorFromPercentage(health);

                    final double barSize = arrowSize + 4;
                    glBegin(GL_QUADS);
                    {
                        // Background
                        RenderUtil.color(0x96000000);
                        addQuadVertices(-barSize / 2.0, arrowSize / 2.0 + 2, barSize, 2);

                        // Colored bar
                        RenderUtil.color(healthColor);
                        final double filled = (barSize - 1) * health;
                        addQuadVertices(-barSize / 2.0 + 0.5, arrowSize / 2.0 + 2 + 0.5, filled, 1);
                    }
                    glEnd();
                }

                glRotatef(yawToPlayer, 0, 0, 1);

                if (outline.get()) {
                    // Draw Outline
                    RenderUtil.color(color | 0xFF000000);

                    glBegin(GL_LINE_LOOP);
                    {
                        addTriangleVertices(arrowSize);
                    }

                    RenderUtil.resetColor();
                    glEnd();
                }

                // Draw Arrow

                glEnable(GL_POLYGON_SMOOTH);
                glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

                RenderUtil.color(color);

                glBegin(GL_TRIANGLES);
                {
                    addTriangleVertices(arrowSize);
                }

                RenderUtil.resetColor();
                glEnd();

                glDisable(GL_POLYGON_SMOOTH);
                glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE);

                GLUtil.endBlend();
                glEnable(GL_TEXTURE_2D);

                glPopMatrix();
            }
        }

        if (outline.get()) {
            glDisable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        }

        glTranslatef(-hWidth, -hHeight, 0);
    }

    private static void addQuadVertices(final double x, final double y, final double width, final double height) {
        glVertex2d(x, y);
        glVertex2d(x, y + height);
        glVertex2d(x + width, y + height);
        glVertex2d(x + width, y);
    }

    private static void addTriangleVertices(final double size) {
        glVertex2d(0, -size / 2);
        glVertex2d(-size / 2, size / 2);
        glVertex2d(size / 2, size / 2);
    }
}
