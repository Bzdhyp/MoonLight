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

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.gui.widget.Widget;
import wtf.moonlight.module.impl.display.PotionHUD;
import wtf.moonlight.util.render.animations.advanced.ContinualAnimation;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static wtf.moonlight.gui.click.neverlose.NeverLose.*;

public class PotionHUDWidget extends Widget {
    private final ContinualAnimation widthAnimation = new ContinualAnimation();
    private final ContinualAnimation heightAnimation = new ContinualAnimation();

    public PotionHUDWidget() {
        super("Potion HUD");
        this.x = 0;
        this.y = 0.0f;
    }

    @Override
    public void render() {
        this.PotionDisplay(false);
    }

    @Override
    public void onShader(Shader2DEvent event) {
        this.PotionDisplay(true);
    }

    @Override
    public boolean shouldRender() {
        return Client.INSTANCE.getModuleManager().getModule(PotionHUD.class).isEnabled() && !Client.INSTANCE.getModuleManager().getModule(PotionHUD.class).potionHudMode.is("Exhi");
    }

    public void PotionDisplay(boolean shadow) {
        ArrayList<PotionEffect> potions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());

        switch (Client.INSTANCE.getModuleManager().getModule(PotionHUD.class).potionHudMode.getValue()) {
            case "Sexy": {
                width = 92;
                height = heightAnimation.getOutput();

                if (!shadow) {
                    RoundedUtil.drawRound(renderX, renderY, width, height, 6, new Color(setting.bgColor(), true));
                } else {
                    RoundedUtil.drawRound(renderX, renderY, width, height, 6, new Color(setting.color(), true));
                }

                Fonts.interSemiBold.get(13).drawString("Potions", renderX + 8, renderY + 7 + 2, -1);
                Fonts.nursultan.get(14).drawString("E", renderX + width - 16, renderY + 9, setting.color(0));

                float offset = renderY + 21;
                for (PotionEffect potion : potions) {

                    String name = I18n.format(Potion.potionTypes[potion.getPotionID()].getName()) + " " + (potion.getAmplifier() > 0 ? I18n.format("enchantment.level." + (potion.getAmplifier() + 1)) : "");
                    String duration = Potion.getDurationString(potion);

                    Fonts.psBold.get(11).drawString(name, renderX + 8, offset, -1);
                    Fonts.psBold.get(11).drawString(duration, renderX + width - 8 - Fonts.psBold.get(11).getStringWidth(duration), offset, -1);

                    offset += 10;
                }

                heightAnimation.animate(20 + potions.size() * 10, 20);
                break;
            }
            case "Type 1": {
                float posX = renderX;
                float posY = renderY;
                float fontSize = 13;
                float padding = 5;
                float iconSizeX = 10;

                String name = "Potions";

                if (!shadow) {
                    RoundedUtil.drawRound(posX, posY, width, height, 4, new Color(setting.bgColor(), true));
                } else {
                    RoundedUtil.drawRound(posX, posY, width, height, 4, new Color(setting.color(), true));
                }

                Fonts.interMedium.get(fontSize).drawCenteredString(name, posX - 22 + width / 2, posY + padding + 0.5f + 2, -1);

                float imagePosX = posX + width - iconSizeX - padding;
                Fonts.nursultan.get(fontSize).drawString("E", imagePosX + 2f, posY + 7f + 2, setting.color());

                posY += Fonts.interMedium.get(fontSize).getHeight() + padding * 2;

                float maxWidth = Fonts.interMedium.get(fontSize).getStringWidth(name) + padding * 2;
                float baseHeight = Fonts.interMedium.get(fontSize).getHeight() + padding * 2;

                if (!shadow) {
                    RoundedUtil.drawRound(posX + 0.5f, posY + 1.5f, width - 1, 1.25f, 3, new Color(ColorUtil.darker(setting.color(), 0.4f)));
                } else {
                    RoundedUtil.drawRound(posX + 0.5f, posY + 1.5f, width - 1, 1.25f, 3, new Color(ColorUtil.darker(setting.bgColor(), 0.4f)));
                }

                posY += 7.5f;

                float potionHeight = 0;
                for (PotionEffect ignored : potions) {
                    potionHeight += Fonts.interMedium.get(fontSize).getHeight() + padding;
                }

                heightAnimation.animate(baseHeight + potionHeight + 4.5f + 1.25f, 20);
                height = heightAnimation.getOutput();

                for (PotionEffect effect : potions) {
                    Potion potion = Potion.potionTypes[effect.getPotionID()];
                    String potionName = I18n.format(Potion.potionTypes[potion.getId()].getName());
                    String durationText = Potion.getDurationString(effect);
                    String nameText = potionName + " " + (effect.getAmplifier() > 0 ? I18n.format("enchantment.level." + (effect.getAmplifier() + 1)) : "");
                    float nameWidth = Fonts.interMedium.get(fontSize).getStringWidth(nameText);
                    float bindWidth = Fonts.interMedium.get(fontSize).getStringWidth(durationText);
                    float localWidth = nameWidth + bindWidth + padding * 3;

                    Fonts.interMedium.get(fontSize).drawString(nameText, posX + padding, posY + 2, -1);
                    Fonts.interMedium.get(fontSize).drawString(durationText, posX + width - padding - bindWidth, posY + 2, -1);

                    if (localWidth > maxWidth) {
                        maxWidth = localWidth;
                    }

                    posY += Fonts.interMedium.get(fontSize).getHeight() + padding;
                }
                width = Math.max(maxWidth, 80);
                break;
            }
            case "Type 2": {
                widthAnimation.animate(width, 18);
                potions.sort(Comparator.comparingDouble(effect -> -Fonts.interRegular.get(16).getStringWidth(Objects.requireNonNull(I18n.format(Potion.potionTypes[effect.getPotionID()].getName())))));
                float yOffset = 0;
                heightAnimation.animate(potions.size() * 13 - 14, 18);

                if (!shadow) {
                    RoundedUtil.drawRound(renderX, renderY + yOffset, widthAnimation.getOutput(), Fonts.interBold.get(15).getHeight() + 12f + heightAnimation.getOutput() + 4 + 2, 4, new Color(setting.bgColor(), true));
                } else {
                    RoundedUtil.drawRound(renderX, renderY + yOffset, widthAnimation.getOutput(), Fonts.interBold.get(15).getHeight() + 12f + heightAnimation.getOutput() + 4 + 2, 4, new Color(setting.color(), true));
                }

                Fonts.interBold.get(15).drawString("Potions Status", renderX + 5, renderY + 5.5, setting.color());
                width = (MathHelper.clamp_int(!potions.isEmpty() ? Fonts.interRegular.get(16).getStringWidth(Objects.requireNonNull(potions.stream().max(Comparator.comparingDouble(effect -> Fonts.interRegular.get(16).getStringWidth(Objects.requireNonNull(I18n.format(Potion.potionTypes[effect.getPotionID()].getName()))))).stream().findFirst().orElse(null)).getEffectName()) + 20 : 0, 80, 999));
                height = ((Fonts.interRegular.get(15).getHeight() + 2 + (12 + heightAnimation.getOutput())));
                for (PotionEffect potion : potions) {
                    String potionString = I18n.format(Potion.potionTypes[potion.getPotionID()].getName()) + " " + (potion.getAmplifier() > 0 ? I18n.format("enchantment.level." + (potion.getAmplifier() + 1)) : "");
                    String durationString = Potion.getDurationString(potion);
                    if (Potion.potionTypes[potion.getPotionID()].hasStatusIcon()) {
                        GL11.glPushMatrix();
                        RenderUtil.resetColor();
                        RenderHelper.enableGUIStandardItemLighting();
                        int i1 = Potion.potionTypes[potion.getPotionID()].getStatusIconIndex();
                        GL11.glScaled(0.5, 0.5, 0.5);
                        mc.getTextureManager().bindTexture(GuiContainer.inventoryBackground);
                        Gui.drawTexturedModalRect((renderX + 4) * 9 / 4.5f, ((4 + 2 + Fonts.interRegular.get(15).getHeight() + renderY + yOffset + 0.5f + potions.indexOf(potion) * 13) * 9) / 4.5f, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                        GL11.glScaled(2, 2, 2);
                        RenderHelper.disableStandardItemLighting();
                        GL11.glPopMatrix();
                    }
                    Fonts.interRegular.get(16).drawString(potionString, (renderX + 15), (Fonts.interRegular.get(15).getHeight() + renderY + yOffset + 4 + 2 + 2) + potions.indexOf(potion) * 13, -1);
                    Fonts.interRegular.get(14).drawCenteredString(durationString, (1 + renderX - 6 + widthAnimation.getOutput() - Fonts.interRegular.get(16).getStringWidth(durationString)) + Fonts.interRegular.get(16).getStringWidth(durationString) / 2f + 3, (renderY + yOffset + 4 + Fonts.interBold.get(15).getHeight() + 2 + 2 + 1) + potions.indexOf(potion) * 13, -1);
                }
                break;
            }
            case "NeverLose": {
                height = (15);
                width = MathHelper.clamp_float(!potions.isEmpty() ? Fonts.interSemiBold.get(16).getStringWidth(Objects.requireNonNull(potions.stream().max(Comparator.comparingDouble(effect -> Fonts.interSemiBold.get(16).getStringWidth(Objects.requireNonNull(I18n.format(Potion.potionTypes[effect.getPotionID()].getName()))))).stream().findFirst().orElse(null)).getEffectName()) + 20 : 0, 80, 999);
                widthAnimation.animate(width, 18);
                potions.sort(Comparator.comparingDouble(effect -> -Fonts.interSemiBold.get(16).getStringWidth(Objects.requireNonNull(I18n.format(Potion.potionTypes[effect.getPotionID()].getName())))));

                float yOffset = 18;
                if (!shadow) {
                    RoundedUtil.drawRound(renderX, renderY, widthAnimation.getOutput(), 14f, 4, ColorUtil.applyOpacity(bgColor, 1f));
                } else {
                    RoundedUtil.drawRound(renderX, renderY, widthAnimation.getOutput(), 14f, 4, bgColor);
                }

                Fonts.nursultan.get(15).drawString("E ", renderX + 5, renderY + 5.5f, iconRGB);
                Fonts.interSemiBold.get(15).drawString("Potions Status", renderX + 5 + Fonts.nursultan.get(15).getStringWidth("E "), renderY + 5.5, textRGB);

                heightAnimation.animate(potions.size() * 13 - 14, 18);

                if (!shadow) {
                    RoundedUtil.drawRound(renderX, renderY + yOffset, widthAnimation.getOutput(), 12f + heightAnimation.getOutput(), 4, ColorUtil.applyOpacity(bgColor, 1f));
                } else {
                    RoundedUtil.drawRound(renderX, renderY + yOffset, widthAnimation.getOutput(), 12f + heightAnimation.getOutput(), 4, bgColor);
                }

                for (PotionEffect potion : potions) {
                    String potionString = I18n.format(Potion.potionTypes[potion.getPotionID()].getName()) + " " + (potion.getAmplifier() > 0 ? I18n.format("enchantment.level." + (potion.getAmplifier() + 1)) : "");

                    String durationString = Potion.getDurationString(potion);

                    if (Potion.potionTypes[potion.getPotionID()].hasStatusIcon()) {
                        GL11.glPushMatrix();
                        RenderUtil.resetColor();
                        RenderHelper.enableGUIStandardItemLighting();
                        int i1 = Potion.potionTypes[potion.getPotionID()].getStatusIconIndex();
                        GL11.glScaled(0.5, 0.5, 0.5);
                        mc.getTextureManager().bindTexture(GuiContainer.inventoryBackground);
                        drawTexturedModalRect((renderX + 4) * 9 / 4.5f, (renderY + yOffset + 1f + potions.indexOf(potion) * 13) * 9 / 4.5f, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                        GL11.glScaled(2, 2, 2);
                        RenderHelper.disableStandardItemLighting();
                        GL11.glPopMatrix();
                    }

                    Fonts.interSemiBold.get(16).drawString(potionString, (renderX + 15), (renderY + yOffset + 4) + potions.indexOf(potion) * 13, textRGB);
                    Fonts.interSemiBold.get(14).drawCenteredString(durationString, (renderX - 6 + widthAnimation.getOutput() - Fonts.interSemiBold.get(16).getStringWidth(durationString)) + Fonts.interSemiBold.get(16).getStringWidth(durationString) / 2f + 3, (renderY + yOffset + 4) + potions.indexOf(potion) * 13, iconRGB);
                    //yOffset += (float) (16);
                }
                break;
            }
        }
    }

    private int calculateMaxPotionWidth() {
        List<PotionEffect> potions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
        int maxWidth = 80;

        for (PotionEffect effect : potions) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            String potionName = I18n.format(Potion.potionTypes[potion.getId()].getName());
            int amplifier = effect.getAmplifier();
            String potionNameWithLevel = amplifier > 0 ? potionName + " " + (amplifier + 1) : potionName;

            float nameWidth = Fonts.interSemiBold.get(13).getStringWidth(potionNameWithLevel);
            float iconWidth = 16;
            float totalWidth = nameWidth + iconWidth + 30;

            if (totalWidth > maxWidth) {
                maxWidth = (int) totalWidth;
            }
        }

        return maxWidth;
    }

    private int calculatePotionOffset() {
        List<PotionEffect> potions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
        return potions.isEmpty() ? -1 : potions.size() * 12;
    }
}
