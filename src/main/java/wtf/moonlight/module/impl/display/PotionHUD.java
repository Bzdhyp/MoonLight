package wtf.moonlight.module.impl.display;

import com.cubk.EventTarget;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

import static net.minecraft.client.gui.Gui.drawTexturedModalRect;

@ModuleInfo(name = "PotionHUD", category = Categor.Display)
public class PotionHUD extends Module {
    public final ListValue potionHudMode = new ListValue("Potion Mode", new String[]{"Default", "Exhi", "Moon", "Sexy", "Type 1", "Type 2", "NeverLose"}, "Default", this);

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (potionHudMode.is("Default")) {
            GL11.glPushMatrix();
            GL11.glTranslatef(25, event.scaledResolution().getScaledHeight() / 2f, 0F);
            float yPos = -75F;
            float width = 0F;
            for (final PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
                final Potion potion = Potion.potionTypes[effect.getPotionID()];
                final String number = intToRomanByGreedy(effect.getAmplifier());
                final String name = I18n.format(potion.getName()) + " " + number;
                final float stringWidth = mc.fontRendererObj.getStringWidth(name)
                        + mc.fontRendererObj.getStringWidth("§f" + Potion.getDurationString(effect));

                if (width < stringWidth)
                    width = stringWidth;
                final float finalY = yPos;
                mc.fontRendererObj.drawString(name, 2f, finalY - 7f, Color.white.getRGB(), true);
                mc.fontRendererObj.drawStringWithShadow("§f" + Potion.getDurationString(effect), 2f, finalY + 4, -1);
                if (potion.hasStatusIcon()) {
                    GL11.glPushMatrix();
                    final boolean is2949 = GL11.glIsEnabled(2929);
                    final boolean is3042 = GL11.glIsEnabled(3042);
                    if (is2949)
                        GL11.glDisable(2929);
                    if (!is3042)
                        GL11.glEnable(3042);
                    GL11.glDepthMask(false);
                    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                    final int statusIconIndex = potion.getStatusIconIndex();
                    mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                    drawTexturedModalRect(-20F, finalY - 5, statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18);
                    GL11.glDepthMask(true);
                    if (!is3042)
                        GL11.glDisable(3042);
                    if (is2949)
                        GL11.glEnable(2929);
                    GL11.glPopMatrix();
                }

                yPos += mc.fontRendererObj.FONT_HEIGHT + 15;
            }
            GL11.glPopMatrix();
        }

        if (potionHudMode.is("Exhi")) {
            ArrayList<PotionEffect> potions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
            potions.sort(Comparator.comparingDouble(effect -> -mc.fontRendererObj.getStringWidth(I18n.format(Potion.potionTypes[effect.getPotionID()].getName()))));
            float y = mc.currentScreen instanceof GuiChat ? -14.0f : -3.0f;
            for (PotionEffect potionEffect : potions) {
                Potion potionType = Potion.potionTypes[potionEffect.getPotionID()];
                String potionName = I18n.format(potionType.getName());
                String type = "";
                if (potionEffect.getAmplifier() == 1) {
                    potionName = potionName + " II";
                } else if (potionEffect.getAmplifier() == 2) {
                    potionName = potionName + " III";
                } else if (potionEffect.getAmplifier() == 3) {
                    potionName = potionName + " IV";
                }
                if (potionEffect.getDuration() < 600 && potionEffect.getDuration() > 300) {
                    type = type + " §6" + Potion.getDurationString(potionEffect);
                } else if (potionEffect.getDuration() < 300) {
                    type = type + " §c" + Potion.getDurationString(potionEffect);
                } else if (potionEffect.getDuration() > 600) {
                    type = type + " §7" + Potion.getDurationString(potionEffect);
                }
                GlStateManager.pushMatrix();
                mc.fontRendererObj.drawString(potionName, (float) event.scaledResolution().getScaledWidth() - mc.fontRendererObj.getStringWidth(type + potionName) - 1.0f, (event.scaledResolution().getScaledHeight()  - 9) + y, new Color(potionType.getLiquidColor()).getRGB(), true);
                mc.fontRendererObj.drawString(type, (float) event.scaledResolution().getScaledWidth() - mc.fontRendererObj.getStringWidth(type) - 1.0f, (event.scaledResolution().getScaledHeight() - 9) + y, new Color(255, 255, 255).getRGB(), true);
                GlStateManager.popMatrix();
                y -= 9.0f;
            }
        }

        if (potionHudMode.is("Moon")) {
            ArrayList<PotionEffect> potions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
            potions.sort(Comparator.comparingDouble(effect -> -Fonts.interMedium.get(19).getStringWidth(I18n.format(Potion.potionTypes[effect.getPotionID()].getName()))));
            float y = mc.currentScreen instanceof GuiChat ? -14.0f : -3.0f;
            for (PotionEffect potionEffect : potions) {
                Potion potionType = Potion.potionTypes[potionEffect.getPotionID()];
                String potionName = I18n.format(potionType.getName());
                String type = " §7-";
                if (potionEffect.getAmplifier() == 1) {
                    potionName = potionName + " 2";
                } else if (potionEffect.getAmplifier() == 2) {
                    potionName = potionName + " 3";
                } else if (potionEffect.getAmplifier() == 3) {
                    potionName = potionName + " 4";
                }
                if (potionEffect.getDuration() < 600 && potionEffect.getDuration() > 300) {
                    type = type + " §f" + Potion.getDurationString(potionEffect);
                } else if (potionEffect.getDuration() < 300) {
                    type = type + " §f" + Potion.getDurationString(potionEffect);
                } else if (potionEffect.getDuration() > 600) {
                    type = type + " §f" + Potion.getDurationString(potionEffect);
                }
                GlStateManager.pushMatrix();
                Fonts.interMedium.get(17).drawStringWithShadow(potionName, (float) event.scaledResolution().getScaledWidth() - Fonts.interSemiBold.get(17).getStringWidth(type + potionName) - 2.0f, (event.scaledResolution().getScaledHeight() - 9) + y, new Color(potionType.getLiquidColor()).getRGB());
                Fonts.interMedium.get(17).drawStringWithShadow(type, (float) event.scaledResolution().getScaledWidth() - Fonts.interMedium.get(17).getStringWidth(type) - 2.0f, (event.scaledResolution().getScaledHeight() - 9) + y, new Color(255, 255, 255).getRGB());

                GlStateManager.popMatrix();
                y -= 9.5f;
            }
        }
    }

    private String intToRomanByGreedy(int num) {
        int[] values = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
        String[] symbols = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        while (i < values.length && num >= 0) {
            while (values[i] <= num) {
                num -= values[i];
                stringBuilder.append(symbols[i]);
            }
            i++;
        }
        return stringBuilder.toString();
    }
}
