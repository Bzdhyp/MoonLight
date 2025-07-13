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
package wtf.moonlight.gui.widget.impl;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import com.cubk.EventTarget;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.util.misc.InstanceAccess;
import wtf.moonlight.util.animations.advanced.Animation;
import wtf.moonlight.util.animations.advanced.Direction;
import wtf.moonlight.util.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.player.ScaffoldUtil;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;

public class ScaffoldCounter implements InstanceAccess {
    private final Animation anim = new DecelerateAnimation(175, 1);

    @EventTarget
    public void drawCounter(Render2DEvent event) {
        Scaffold scaffold = INSTANCE.getModuleManager().getModule(Scaffold.class);
        ScaledResolution sr = event.scaledResolution();
        switch (scaffold.counter.getValue().toLowerCase()) {
            case "normal": {
                anim.setDirection(scaffold.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                //if (!scaffold.isEnabled() && anim.isDone()) return;
                int slot = ScaffoldUtil.getBlockSlot();
                ItemStack heldItem = slot == -1 ? null : mc.thePlayer.inventory.mainInventory[slot];
                int count = slot == -1 ? 0 : ScaffoldUtil.getBlockCount();
                String countStr = String.valueOf(count);
                float x, y;
                float output = (float) anim.getOutput();
                float blockWH = heldItem != null ? 15 : -2;
                int spacing = 3;
                String text = "§l" + countStr + "§r block" + (count != 1 ? "s" : "");
                float textWidth = Fonts.interBold.get(18).getStringWidth(text);

                float totalWidth = ((textWidth + blockWH + spacing) + 6) * output;
                x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
                y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 120);
                float height = 20;
                GL11.glPushMatrix();
                RenderUtil.scissor(x - 1.5, y - 1.5, totalWidth + 3, height + 3);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                RoundedUtil.drawRound(x, y, totalWidth, height, 5, new Color(INSTANCE.getModuleManager().getModule(Interface.class).bgColor(), true));

                Fonts.interBold.get(18).drawString(text, x + 3 + blockWH + spacing, y + height / 2F - Fonts.interBold.get(18).getHeight() / 2F + 2.5f, -1);

                if (heldItem != null) {
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int) x + 3, (int) (y + 10 - (blockWH / 2)));
                    RenderHelper.disableStandardItemLighting();
                }
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GL11.glPopMatrix();
                break;
            }
            case "exhibition": {
                if (!scaffold.isEnabled()) return;
                int c = ColorUtil.getColor(255, 0, 0, 150);
                if (ScaffoldUtil.getBlockCount() >= 64 && 128 > ScaffoldUtil.getBlockCount()) {
                    c = ColorUtil.getColor(255, 255, 0, 150);
                } else if (ScaffoldUtil.getBlockCount() >= 128) {
                    c = ColorUtil.getColor(0, 255, 0, 150);
                }
                ScaledResolution scaledResolution = new ScaledResolution(mc);
                mc.fontRendererObj.drawString(String.valueOf(ScaffoldUtil.getBlockCount()), scaledResolution.getScaledWidth() / 2f - (mc.fontRendererObj.getStringWidth(String.valueOf(ScaffoldUtil.getBlockCount())) / 2f) - 1, scaledResolution.getScaledHeight() / 2f - 36, 0xff000000, false);
                mc.fontRendererObj.drawString(String.valueOf(ScaffoldUtil.getBlockCount()), scaledResolution.getScaledWidth() / 2f - (mc.fontRendererObj.getStringWidth(String.valueOf(ScaffoldUtil.getBlockCount())) / 2f) + 1, scaledResolution.getScaledHeight() / 2f - 36, 0xff000000, false);
                mc.fontRendererObj.drawString(String.valueOf(ScaffoldUtil.getBlockCount()), scaledResolution.getScaledWidth() / 2f - (mc.fontRendererObj.getStringWidth(String.valueOf(ScaffoldUtil.getBlockCount())) / 2f), scaledResolution.getScaledHeight() / 2f - 35, 0xff000000, false);
                mc.fontRendererObj.drawString(String.valueOf(ScaffoldUtil.getBlockCount()), scaledResolution.getScaledWidth() / 2f - (mc.fontRendererObj.getStringWidth(String.valueOf(ScaffoldUtil.getBlockCount())) / 2f), scaledResolution.getScaledHeight() / 2f - 37, 0xff000000, false);
                mc.fontRendererObj.drawString(String.valueOf(ScaffoldUtil.getBlockCount()), scaledResolution.getScaledWidth() / 2f - (mc.fontRendererObj.getStringWidth(String.valueOf(ScaffoldUtil.getBlockCount())) / 2f), scaledResolution.getScaledHeight() / 2f - 36, c, false);
                break;
            }
            case "adjust":
                if (!scaffold.isEnabled()) return;
                //Fonts.interRegular.get(16).drawStringWithShadow("blocks", sr.getScaledWidth() / 2f + Fonts.Tahoma.get(16).getStringWidth(ScaffoldUtil.getBlockCount() + "") / 2f, sr.getScaledHeight() / 2f + 22, new Color(255,255,255).getRGB());
                //Fonts.Tahoma.get(16).drawStringWithShadow(ScaffoldUtil.getBlockCount() + "", sr.getScaledWidth() / 2f - Fonts.interRegular.get(16).getStringWidth("blocks") / 2f, sr.getScaledHeight() / 2f + 22, new Color(Moonlight.INSTANCE.getModuleManager().getModule(Interface.class).color()).getRGB());

                Fonts.interRegular.get(16).drawCenteredStringWithShadow(ScaffoldUtil.getBlockCount() + " " + EnumChatFormatting.GRAY + "blocks", sr.getScaledWidth() / 2f, sr.getScaledHeight() / 2f + 22, -1);
                break;
            case "simple": {
                if (!scaffold.isEnabled()) return;
                int c = ColorUtil.getColor(255, 0, 0, 150);
                if (ScaffoldUtil.getBlockCount() >= 64 && 128 > ScaffoldUtil.getBlockCount()) {
                    c = ColorUtil.getColor(255, 255, 0, 150);
                } else if (ScaffoldUtil.getBlockCount() >= 128) {
                    c = ColorUtil.getColor(0, 255, 0, 150);
                }
                Fonts.interMedium.get(18).drawCenteredStringWithShadow(String.valueOf(ScaffoldUtil.getBlockCount()), sr.getScaledWidth() / 2f, sr.getScaledHeight() / 2f + 10, new Color(c).brighter().getRGB());
                break;
            }

            case "novo": {
                if (!scaffold.isEnabled()) return;
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(ScaffoldUtil.getBlockSlot());

                if (stack != null && stack.getItem() instanceof ItemBlock) {
                    float width = Fonts.interRegular.get(18).getStringWidth("/" + ScaffoldUtil.getBlockCount());
                    float x = sr.getScaledWidth() / 2f - width / 2, y = sr.getScaledHeight() / 2f;

                    RenderUtil.renderItemStack(stack, x - 5.0F, y + 11, 1);
                    Fonts.interRegular.get(18).drawStringWithShadow(Integer.toString(ScaffoldUtil.getBlockCount()), x + 11.0F, y + 16, -1);
                }
            }

            break;
        }
    }

    @EventTarget
    public void drawShader2D(Shader2DEvent event) {
        Scaffold scaffold = INSTANCE.getModuleManager().getModule(Scaffold.class);
        switch (scaffold.counter.getValue().toLowerCase()) {
            case "normal": {
                anim.setDirection(scaffold.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                //if (!scaffold.isEnabled() && anim.isDone()) return;
                int slot = ScaffoldUtil.getBlockSlot();
                ItemStack heldItem = slot == -1 ? null : mc.thePlayer.inventory.mainInventory[slot];
                int count = slot == -1 ? 0 : ScaffoldUtil.getBlockCount();
                String countStr = String.valueOf(count);
                ScaledResolution sr = new ScaledResolution(mc);
                float x, y;
                float output = (float) anim.getOutput();
                float blockWH = heldItem != null ? 15 : -2;
                int spacing = 3;
                String text = "§l" + countStr + "§r block" + (count != 1 ? "s" : "");
                float textWidth = Fonts.interBold.get(18).getStringWidth(text);
                float totalWidth = ((textWidth + blockWH + spacing) + 6) * output;
                x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
                y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 120);
                float height = 20;
                GL11.glPushMatrix();
                RenderUtil.scissor(x - 1.5, y - 1.5, totalWidth + 3, height + 3);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                RoundedUtil.drawRound(x, y, totalWidth, height, 5, new Color(INSTANCE.getModuleManager().getModule(Interface.class).bgColor(),true));
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GL11.glPopMatrix();
                break;
            }
        }
    }
}
