package wtf.moonlight.module.impl.display;

import com.cubk.EventTarget;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.click.neverlose.NeverLose;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.gui.notification.NotificationManager;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.combat.KillAura;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;

import java.awt.*;
import java.util.*;

import static net.minecraft.client.gui.Gui.drawTexturedModalRect;
import static wtf.moonlight.gui.click.neverlose.NeverLose.iconRGB;

@ModuleInfo(name = "Interface", category = Categor.Display)
public class Interface extends Module {
    public final StringValue clientName = new StringValue("Client Name", "Moonlight", this);

    public final MultiBoolValue elements = new MultiBoolValue("Elements", Arrays.asList(
            new BoolValue("Island",false),
            new BoolValue("Key Bind",false),
            new BoolValue("PlayerList",true),
            new BoolValue("Potion HUD",true),
            new BoolValue("Target HUD",true),
            new BoolValue("Notification",true)), this);

    public ListValue soundMode = new ListValue("Sound Mode", new String[]{"None", "Default", "Sigma", "Augustus"}, "Default", this);

    public final ListValue keyBindMode = new ListValue("Key Bind Mode", new String[]{"Type 1"}, "Type 1", this,() -> elements.isEnabled("Key Bind"));
    public final ListValue potionHudMode = new ListValue("Potion Mode", new String[]{"Default", "Exhi", "Moon", "Sexy", "Type 1", "Type 2", "NeverLose"}, "NeverLose", this,() -> elements.isEnabled("Potion HUD"));
    public final ListValue notificationMode = new ListValue("Notification Mode", new String[]{"Default", "Test","Type 2","Type 3","Type 4","Type 5", "Test2","Exhi"}, "Default", this,() -> elements.isEnabled("Notification"));
    public final BoolValue centerNotif = new BoolValue("Center Notification",true,this,() -> notificationMode.is("Exhi"));
    public final ListValue targetHudMode = new ListValue("Mode", new String[]{"Novo 1","Novo 2","Novo 3","Novo 4","Novo 5",
            "Type 1", "Type 2", "Type 3", "Type 4", "Exhi", "Moon", "Augustus", "Rise", "Adjust", "Astolfo", "Akrien", "NeverLose"}, "Astolfo", this,() -> elements.isEnabled("Target HUD"));

    public final ListValue colorMode = new ListValue("Color Mode", new String[]{"Custom", "Fade", "Rainbow", "Astolfo", "Dynamic", "NeverLose"}, "Dynamic", this);
    public final SliderValue speedValue = new SliderValue("Speed", 2.0f, 1.0f, 10.0f, 0.5f, this, () -> !colorMode.is("Custom"));
    public final ColorValue mainColor = new ColorValue("Main Color", new Color(128, 128, 255), this, () -> !colorMode.is("NeverLose"));
    private final ColorValue secondColor = new ColorValue("Second Color", new Color(128, 255, 255), this, () -> colorMode.is("Fade"));
    public final SliderValue astolfoOffsetValue = new SliderValue("Offset", 5, 0, 20, this, () -> colorMode.is("Astolfo"));
    public final SliderValue astolfoIndexValue = new SliderValue("Index", 107, 0, 200, this, () -> colorMode.is("Astolfo"));

    public final ListValue bgColor = new ListValue("Background Color, Mode", new String[]{"None", "Custom", "Dark", "Synced", "NeverLose"}, "NeverLose", this);
    private final ColorValue bgCustomColor = new ColorValue("Background Custom Color", new Color(32, 32, 64), this,() -> bgColor.canDisplay() && bgColor.is("Custom"));
    private final SliderValue bgAlpha = new SliderValue("Background Alpha",100,1,255,1,this);
    public final BoolValue chatCombine = new BoolValue("Chat Combine", true, this);

  //  public ListValue nlColor = new ListValue("NeverLose Color", new String[]{"Blue", "White"}, "Blue", this, () -> bgColor.canDisplay() && bgColor.is("NeverLose"));

    public final BoolValue cape = new BoolValue("Cape", true, this);
    public final ListValue capeMode = new ListValue("Cape Mode", new String[]{"Default", "Sexy", "Sexy 2"}, "Default", this);
    public final BoolValue wavey = new BoolValue("Wavey Cape", true, this);
    public final BoolValue enchanted = new BoolValue("Enchanted", true, this, () -> cape.get() && !wavey.get());
    public static final Map<EntityPlayer, DecelerateAnimation> animationEntityPlayerMap = new HashMap<>();

    @EventTarget
    public void onShader2D(Shader2DEvent event) {
        if (elements.isEnabled("Notification")) {
            NotificationManager.publish(new ScaledResolution(mc), true);
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (elements.isEnabled("Notification")) {
            NotificationManager.publish(new ScaledResolution(mc), false);
        }

        if (elements.isEnabled("Potion HUD")) {
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
                    mc.fontRendererObj.drawString(potionName, (float) event.scaledResolution().getScaledWidth() - mc.fontRendererObj.getStringWidth(type + potionName) - 1.0f, (event.scaledResolution().getScaledHeight() - 9) + y, new Color(potionType.getLiquidColor()).getRGB(), true);
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
    }

    @EventTarget
    public void onTick(TickEvent event) {
        KillAura aura = getModule(KillAura.class);
        if (aura.isEnabled()) {
            animationEntityPlayerMap.entrySet().removeIf(entry -> entry.getKey().isDead || (!aura.targets.contains(entry.getKey()) && entry.getKey() != mc.thePlayer));
        }

        if (!aura.isEnabled() && !(mc.currentScreen instanceof GuiChat)) {
            this.TargetAnim();
        }

        if (!aura.targets.isEmpty() && !(mc.currentScreen instanceof GuiChat)) {
            for (EntityLivingBase entity : aura.targets) {
                if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                    animationEntityPlayerMap.putIfAbsent((EntityPlayer) entity, new DecelerateAnimation(175, 1));
                    animationEntityPlayerMap.get(entity).setDirection(Direction.FORWARDS);
                }
            }
        }

        if (aura.isEnabled() && aura.target == null && !(mc.currentScreen instanceof GuiChat)) {
            this.TargetAnim();
        }

        if (mc.currentScreen instanceof GuiChat) {
            animationEntityPlayerMap.putIfAbsent(mc.thePlayer, new DecelerateAnimation(175, 1));
            animationEntityPlayerMap.get(mc.thePlayer).setDirection(Direction.FORWARDS);
        }
    }

    public void TargetAnim() {
        Iterator<Map.Entry<EntityPlayer, DecelerateAnimation>> iterator = animationEntityPlayerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EntityPlayer, DecelerateAnimation> entry = iterator.next();
            DecelerateAnimation animation = entry.getValue();

            animation.setDirection(Direction.BACKWARDS);
            if (animation.finished(Direction.BACKWARDS)) {
                iterator.remove();
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

    public Color getMainColor() {
        return mainColor.getValue();
    }

    public Color getSecondColor() {
        return secondColor.getValue();
    }

    public int bgColor() {
        return bgColor(0);
    }

    public int bgColor(int counter) {
        return bgColor(counter, bgAlpha.getValue().intValue());
    }

    public int bgColor(int counter, int alpha) {
        int colors = getMainColor().getRGB();
        colors = switch (bgColor.getValue()) {
            case "None" -> new Color(0, 0, 0, 0).getRGB();
            case "Custom" -> ColorUtil.swapAlpha(bgCustomColor.getValue().getRGB(), alpha);
            case "Dark" -> (new Color(21, 21, 21, alpha)).getRGB();
            case "Synced" -> ColorUtil.swapAlpha(color(counter), alpha);
            case "NeverLose" -> ColorUtil.swapAlpha(NeverLose.bgColor.getRGB(), alpha);
            default -> colors;
        };
        return colors;
    }

    public int color() {
        return color(0);
    }

    public int color(int counter) {
        return color(counter, mainColor.getAlpha());
    }

    public int color(int counter, float opacity) {
        long ms = this.speedValue.getValue().longValue() * 1000L;
        float progress = (float)(System.currentTimeMillis() % ms) / ms;

        int color = getMainColor().getRGB();
        color = switch (colorMode.getValue()) {
            case "Custom" -> ColorUtil.applyOpacity(getMainColor().getRGB(), opacity);
            case "Fade" ->
                    ColorUtil.fadeBetween(this.getMainColor().getRGB(), this.getSecondColor().getRGB(),
                            (float)((System.currentTimeMillis() + (long)counter * 100L) % ms) / ((float)ms / 2.0f));
            case "Rainbow" -> ColorUtil.swapAlpha(ColorUtil.getRainbow(counter), opacity);
            case "Astolfo" -> ColorUtil.applyOpacity(
                    new Color(ColorUtil.astolfoRainbow(
                            (int)(counter + (progress * 100)),
                            astolfoOffsetValue.getValue().intValue(),
                            astolfoIndexValue.getValue().intValue()
                    )).getRGB(),
                    opacity
            );

            case "Dynamic" -> ColorUtil.fadeBetween(this.mainColor.getValue().getRGB(), ColorUtil.darker(this.getMainColor().getRGB(),
                    0.3f), (float)((System.currentTimeMillis() + (ms + (long)counter * 100L)) % ms) / ((float)ms / 2.0f));
            case "NeverLose" -> ColorUtil.swapAlpha(iconRGB, opacity);
            default -> color;
        };
        return color;
    }
}
