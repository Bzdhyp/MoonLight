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
package wtf.moonlight.module.impl.display;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.Client;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.events.render.RenderGuiEvent;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.combat.KillAura;
import wtf.moonlight.module.impl.player.ChestStealer;
import wtf.moonlight.module.impl.visual.island.IslandRenderer;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.gui.click.neverlose.NeverLose;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.util.animations.advanced.Direction;
import wtf.moonlight.util.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.render.ColorUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static wtf.moonlight.gui.click.neverlose.NeverLose.*;

@ModuleInfo(name = "Interface", category = Categor.Display)
public class Interface extends Module {
    public final StringValue clientName = new StringValue("Client Name", "Moonlight", this);

    public final MultiBoolValue elements = new MultiBoolValue("Elements", Arrays.asList(
            new BoolValue("Island",true),
            new BoolValue("Health",true),
            new BoolValue("Arraylist",true),
            new BoolValue("Notification",true)), this);

    public final BoolValue cFont = new BoolValue("C Fonts",true,this, () -> elements.isEnabled("Arraylist"));
    public final ListValue fontMode = new ListValue("C Fonts Mode", new String[]{"Bold","Semi Bold","Medium","Regular","Tahoma", "SFUI"}, "Semi Bold", this,() -> cFont.canDisplay() && cFont.get());
    public final SliderValue fontSize = new SliderValue("Font Size",15,10,25,this,cFont::get);
    public final SliderValue animSpeed = new SliderValue("anim Speed", 200, 100, 400, 25, this, () -> elements.isEnabled("Arraylist"));
    public final ListValue animation = new ListValue("Animation", new String[]{"ScaleIn", "MoveIn","Slide In"}, "ScaleIn", this, () -> elements.isEnabled("Arraylist"));
    public final SliderValue textHeight = new SliderValue("Text Height", 2, 0, 10, this, () -> elements.isEnabled("Arraylist"));
    public final ListValue tags = new ListValue("Suffix", new String[]{"None", "Simple", "Bracket", "Dash"}, "None", this, () -> elements.isEnabled("Arraylist"));
    public final BoolValue line = new BoolValue("Line",true,this, () -> elements.isEnabled("Arraylist"));
    public final BoolValue outLine = new BoolValue("Outline",true,this, () -> line.canDisplay() && line.get());
    public final ListValue notificationMode = new ListValue("Notification Mode", new String[]{"Default", "Test","Type 2","Type 3","Type 4","Type 5", "Test2","Exhi"}, "Default", this,() -> elements.isEnabled("Notification"));
    public final BoolValue centerNotif = new BoolValue("Center Notification",true,this,() -> notificationMode.is("Exhi"));
    public final ListValue color = new ListValue("Color Setting", new String[]{"Custom", "Rainbow", "Dynamic", "Fade","Astolfo","NeverLose"}, "NeverLose", this);
    public final ColorValue mainColor = new ColorValue("Main Color", new Color(128, 128, 255), this,() -> !color.is("NeverLose"));
    private final ColorValue secondColor = new ColorValue("Second Color", new Color(128, 255, 255), this, () -> color.is("Fade"));
    public final SliderValue fadeSpeed = new SliderValue("Fade Speed", 1, 1, 10, 1, this, () -> color.is("Dynamic") || color.is("Fade"));
    public final BoolValue background = new BoolValue("Background",true,this, () -> elements.isEnabled("Arraylist"));
    public final ListValue bgColor = new ListValue("Background Color", new String[]{"Dark", "Synced","Custom","NeverLose"}, "Synced", this,background::get);
    private final ColorValue bgCustomColor = new ColorValue("Background Custom Color", new Color(32, 32, 64), this,() -> bgColor.canDisplay() && bgColor.is("Custom"));
    private final SliderValue bgAlpha = new SliderValue("Background Alpha",100,1,255,1,this);
    public final BoolValue customScoreboard = new BoolValue("Custom Scoreboard", false, this);
    public final BoolValue hideScoreboard = new BoolValue("Hide Scoreboard", false, this,() -> !customScoreboard.get());
    public final BoolValue hideScoreRed = new BoolValue("Hide Scoreboard Red Points", true, this, customScoreboard::get);
    public final BoolValue fixHeight = new BoolValue("Fix Height", true, this, customScoreboard::get);
    public final BoolValue hideBackground = new BoolValue("Hide Background", true, this, customScoreboard::get);
    public final BoolValue chatCombine = new BoolValue("Chat Combine", true, this);
    public final BoolValue hotBar = new BoolValue("New Hot Bar", false, this);

    public final BoolValue cape = new BoolValue("Cape", true, this);
    public final ListValue capeMode = new ListValue("Cape Mode", new String[]{"Default", "Sexy", "Sexy 2"}, "Default", this);
    public final BoolValue wavey = new BoolValue("Wavey Cape", true, this);
    public final BoolValue enchanted = new BoolValue("Enchanted", true, this, () -> cape.get() && !wavey.get());
    private final DecimalFormat healthFormat = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ENGLISH));
    public final Map<EntityPlayer, DecelerateAnimation> animationEntityPlayerMap = new HashMap<>();
    public int lost = 0, killed = 0, won = 0;
    public int prevMatchKilled = 0,matchKilled = 0,match;
    private final Random random = new Random();
    public int scoreBoardHeight = 0;
    private final Pattern LINK_PATTERN = Pattern.compile("(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[A-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)");

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (elements.isEnabled("Island")) {
            IslandRenderer.INSTANCE.render(event.scaledResolution(),false);
        }

        if(elements.isEnabled("Health")){
            renderHealth();
        }

        if (elements.isEnabled("Notification")) {
            Client.INSTANCE.getNotificationManager().publish(new ScaledResolution(mc),false);
        }
    }

    @EventTarget
    public void onShader2D(Shader2DEvent event) {

        if (elements.isEnabled("Island")) {
            IslandRenderer.INSTANCE.render(new ScaledResolution(mc), true);
        }

        if (elements.isEnabled("Notification")) {
            Client.INSTANCE.getNotificationManager().publish(new ScaledResolution(mc),true);
        }
    }

    @EventTarget
    public void onRenderGui(RenderGuiEvent event){
        if(elements.isEnabled("Health")) {
            if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest && !getModule(ChestStealer.class).isStealing || mc.currentScreen instanceof GuiContainerCreative) {
                renderHealth();
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        mainColor.setRainbow(color.is("Rainbow"));
        KillAura aura = getModule(KillAura.class);
        if (aura.isEnabled()) {
            animationEntityPlayerMap.entrySet().removeIf(entry -> entry.getKey().isDead || (!aura.targets.contains(entry.getKey()) && entry.getKey() != mc.thePlayer));
        }
        if (!aura.isEnabled() && !(mc.currentScreen instanceof GuiChat)) {
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
        if (!aura.targets.isEmpty() && !(mc.currentScreen instanceof GuiChat)) {
            for (EntityLivingBase entity : aura.targets) {
                if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                    animationEntityPlayerMap.putIfAbsent((EntityPlayer) entity, new DecelerateAnimation(175, 1));
                    animationEntityPlayerMap.get(entity).setDirection(Direction.FORWARDS);
                }
            }
        }
        if (aura.isEnabled() && aura.target == null && !(mc.currentScreen instanceof GuiChat)) {
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
        if (mc.currentScreen instanceof GuiChat) {
            animationEntityPlayerMap.putIfAbsent(mc.thePlayer, new DecelerateAnimation(175, 1));
            animationEntityPlayerMap.get(mc.thePlayer).setDirection(Direction.FORWARDS);
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event){
        prevMatchKilled = matchKilled;
        matchKilled = 0;
        match += 1;

        if(match > 6)
            match = 6;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            S02PacketChat s02 = (S02PacketChat) event.getPacket();
            String xd = s02.getChatComponent().getUnformattedText();
            if (xd.contains("was killed by " + mc.thePlayer.getName())) {
                ++this.killed;
                prevMatchKilled = matchKilled;
                ++matchKilled;
            }

            if (xd.contains("You Died! Want to play again?")) {
                ++lost;
            }
        }

        if (packet instanceof S45PacketTitle && ((S45PacketTitle) packet).getType().equals(S45PacketTitle.Type.TITLE)) {
            String unformattedText = ((S45PacketTitle) packet).getMessage().getUnformattedText();
            if (unformattedText.contains("VICTORY!")) {
                ++this.won;
            }
            if (unformattedText.contains("GAME OVER!") || unformattedText.contains("DEFEAT!") || unformattedText.contains("YOU DIED!")) {
                ++this.lost;
            }
        }
    }

    public void renderHealth(){
        ScaledResolution sr = new ScaledResolution(mc);
        int xWidth = 0;
        GuiScreen screen = mc.currentScreen;
        float absorptionHealth = mc.thePlayer.getAbsorptionAmount();
        String string = this.healthFormat.format(mc.thePlayer.getHealth() / 2.0f) + "§c\u2764 " + (absorptionHealth <= 0.0f ? "" : "§e" + this.healthFormat.format(absorptionHealth / 2.0f) + "§6\u2764");
        int offsetY = 0;
        if (mc.thePlayer.getHealth() >= 0.0f && mc.thePlayer.getHealth() < 10.0f || mc.thePlayer.getHealth() >= 10.0f && mc.thePlayer.getHealth() < 100.0f) {
            xWidth = 3;
        }
        if (screen instanceof GuiInventory) {
            offsetY = 70;
        } else if (screen instanceof GuiContainerCreative) {
            offsetY = 80;
        } else if (screen instanceof GuiChest) {
            offsetY = ((GuiChest)screen).ySize / 2 - 15;
        }
        int x = new ScaledResolution(mc).getScaledWidth() / 2 - xWidth;
        int y = new ScaledResolution(mc).getScaledHeight() / 2 + 25 + offsetY;
        Color color = new Color(ColorUtil.getHealthColor(mc.thePlayer));
        mc.fontRendererObj.drawString(string, absorptionHealth > 0.0f ? x - 15.5f : x - 3.5f, y, color.getRGB(), true);
        GL11.glPushMatrix();
        mc.getTextureManager().bindTexture(Gui.icons);
        random.setSeed(mc.ingameGUI.getUpdateCounter() * 312871L);
        float width = sr.getScaledWidth() / 2.0f - mc.thePlayer.getMaxHealth() / 2.5f * 10.0f / 2.0f;
        float maxHealth = mc.thePlayer.getMaxHealth();
        int lastPlayerHealth = mc.ingameGUI.lastPlayerHealth;
        int healthInt = MathHelper.ceiling_float_int(mc.thePlayer.getHealth());
        int l2 = -1;
        boolean flag = mc.ingameGUI.healthUpdateCounter > (long) mc.ingameGUI.getUpdateCounter() && (mc.ingameGUI.healthUpdateCounter - (long) mc.ingameGUI.getUpdateCounter()) / 3L % 2L == 1L;
        if (mc.thePlayer.isPotionActive(Potion.regeneration)) {
            l2 = mc.ingameGUI.getUpdateCounter() % MathHelper.ceiling_float_int(maxHealth + 5.0f);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        for (int i6 = MathHelper.ceiling_float_int(maxHealth / 2.0f) - 1; i6 >= 0; --i6) {
            int xOffset = 16;
            if (mc.thePlayer.isPotionActive(Potion.poison)) {
                xOffset += 36;
            } else if (mc.thePlayer.isPotionActive(Potion.wither)) {
                xOffset += 72;
            }
            int k3 = 0;
            if (flag) {
                k3 = 1;
            }
            float renX = width + (float)(i6 % 10 * 8);
            float renY = (float)sr.getScaledHeight() / 2.0f + 15.0f + (float)offsetY;
            if (healthInt <= 4) {
                renY += (float)random.nextInt(2);
            }
            if (i6 == l2) {
                renY -= 2.0f;
            }
            int yOffset = 0;
            if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
                yOffset = 5;
            }
            Gui.drawTexturedModalRect(renX, renY, 16 + k3 * 9, 9 * yOffset, 9, 9);
            if (flag) {
                if (i6 * 2 + 1 < lastPlayerHealth) {
                    Gui.drawTexturedModalRect(renX, renY, xOffset + 54, 9 * yOffset, 9, 9);
                }
                if (i6 * 2 + 1 == lastPlayerHealth) {
                    Gui.drawTexturedModalRect(renX, renY, xOffset + 63, 9 * yOffset, 9, 9);
                }
            }
            if (i6 * 2 + 1 < healthInt) {
                Gui.drawTexturedModalRect(renX, renY, xOffset + 36, 9 * yOffset, 9, 9);
            }
            if (i6 * 2 + 1 != healthInt) continue;
            Gui.drawTexturedModalRect(renX, renY, xOffset + 45, 9 * yOffset, 9, 9);
        }
        GL11.glPopMatrix();
    }

    public void drawScoreboard(ScaledResolution scaledRes, ScoreObjective objective, Scoreboard scoreboard, Collection<Score> scores) {
        List<Score> list = Lists.newArrayList(Iterables.filter(scores, p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")));

        if (list.size() > 15)
        {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        }
        else
        {
            scores = list;
        }

        int i = mc.fontRendererObj.getStringWidth(objective.getDisplayName());

        for (Score score : scores)
        {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
            i = Math.max(i, mc.fontRendererObj.getStringWidth(s));
        }

        int i1 = scores.size() * mc.fontRendererObj.FONT_HEIGHT;
        int j1 = scaledRes.getScaledHeight() / 2 + i1 / 3;
        int k1 = 3;
        int l1 = scaledRes.getScaledWidth() - i - k1;
        int j = 0;

        if (this.fixHeight.get()) {
            j1 = Math.max(j1, scoreBoardHeight + i1 + mc.fontRendererObj.FONT_HEIGHT + 17);
        }

        for (Score score1 : scores)
        {
            ++j;

            ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
            String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
            String s2 = EnumChatFormatting.RED + "" + score1.getScorePoints();
            int k = j1 - j * mc.fontRendererObj.FONT_HEIGHT;

            int l = scaledRes.getScaledWidth() - k1 + 2;

            if(!hideBackground.get())
                drawRect(l1 - 2, k, l, k + mc.fontRendererObj.FONT_HEIGHT, 1342177280);

            final Matcher linkMatcher = LINK_PATTERN.matcher(s1);
            if(Client.INSTANCE.getModuleManager().getModule(Interface.class).isEnabled() && linkMatcher.find()) {
                s1 = "MoonLight@github";
                mc.fontRendererObj.drawGradientWithShadow(s1, l1, k,(index) -> new Color(Client.INSTANCE.getModuleManager().getModule(Interface.class).color(index)));
            } else {
                mc.fontRendererObj.drawString(s1, l1, k, 553648127, true);
            }

            if(!(Client.INSTANCE.getModuleManager().getModule(Interface.class).isEnabled() && Client.INSTANCE.getModuleManager().getModule(Interface.class).
                    hideScoreRed.get()))
                mc.fontRendererObj.drawString(s2, l - mc.fontRendererObj.getStringWidth(s2), k, 553648127);

            if (j == scores.size())
            {
                String s3 = objective.getDisplayName();
                if(!hideBackground.get()) {
                    drawRect(l1 - 2, k - mc.fontRendererObj.FONT_HEIGHT - 1, l, k - 1, 1610612736);
                    drawRect(l1 - 2, k - 1, l, k, 1342177280);
                }
                mc.fontRendererObj.drawString(s3, l1 + i / 2 - mc.fontRendererObj.getStringWidth(s3) / 2, k - mc.fontRendererObj.FONT_HEIGHT, 553648127);
            }
        }
    }

    public FontRenderer getFr() {

        return switch (fontMode.getValue()) {
            case "Bold" -> Fonts.interBold.get(fontSize.getValue());
            case "Semi Bold" -> Fonts.interSemiBold.get(fontSize.getValue());
            case "Medium" -> Fonts.interMedium.get(fontSize.getValue());
            case "Regular" -> Fonts.interRegular.get(fontSize.getValue());
            case "Tahoma" -> Fonts.Tahoma.get(fontSize.getValue());
            case "SFUI" -> Fonts.sfui.get(fontSize.getValue());
            default -> null;
        };
    }

    public Color getMainColor() {
        return mainColor.getValue();
    }

    public Color getSecondColor() {
        return secondColor.getValue();
    }

    public int getRainbow(int counter) {
        return Color.HSBtoRGB(getRainbowHSB(counter)[0], getRainbowHSB(counter)[1], getRainbowHSB(counter)[2]);
    }
    public static int astolfoRainbow(final int offset, final float saturation, final float brightness) {
        double currentColor = Math.ceil((double)(System.currentTimeMillis() + offset * 20L)) / 6.0;
        return Color.getHSBColor(((float)((currentColor %= 360.0) / 360.0) < 0.5) ? (-(float)(currentColor / 360.0)) : ((float)(currentColor / 360.0)), saturation, brightness).getRGB();
    }

    public float[] getRainbowHSB(int counter) {
        final int width = 20;

        double rainbowState = Math.ceil(System.currentTimeMillis() - (long) counter * width) / 8;
        rainbowState %= 360;

        float hue = (float) (rainbowState / 360);
        float saturation = mainColor.getSaturation();
        float brightness = mainColor.getBrightness();

        return new float[]{hue, saturation, brightness};
    }

    public int color() {
        return color(0);
    }

    public int color(int counter, int alpha) {
        int colors = getMainColor().getRGB();
        colors = switch (color.getValue()) {
            case "Rainbow" -> ColorUtil.swapAlpha(getRainbow(counter), alpha);
            case "Dynamic" ->
                    ColorUtil.swapAlpha(ColorUtil.colorSwitch(getMainColor(), new Color(ColorUtil.darker(getMainColor().getRGB(), 0.25F)), 2000.0F, counter, 75L, fadeSpeed.getValue()).getRGB(), alpha);
            case "Fade" ->
                    ColorUtil.swapAlpha((ColorUtil.colorSwitch(getMainColor(), getSecondColor(), 2000.0F, counter, 75L, fadeSpeed.getValue()).getRGB()), alpha);
            case "Astolfo" ->
                    ColorUtil.swapAlpha(astolfoRainbow(counter, mainColor.getSaturation(), mainColor.getBrightness()), alpha);
            case "NeverLose" -> ColorUtil.swapAlpha(iconRGB, alpha);
            case "Custom" -> ColorUtil.swapAlpha(mainColor.getValue().getRGB(), alpha);
            default -> colors;
        };
        return new Color(colors,true).getRGB();
    }

    public int color(int counter) {
        return color(counter, getMainColor().getAlpha());
    }

    public int bgColor(int counter, int alpha) {
        int colors = getMainColor().getRGB();
        colors = switch (bgColor.getValue()) {
            case "Dark" -> (new Color(21, 21, 21, alpha)).getRGB();
            case "Synced" ->
                    new Color(ColorUtil.applyOpacity(color(counter, alpha), alpha / 255f), true).darker().darker().getRGB();
            case "None" -> new Color(0, 0, 0, 0).getRGB();
            case "Custom" -> ColorUtil.swapAlpha(bgCustomColor.getValue().getRGB(), alpha);
            case "NeverLose" -> ColorUtil.swapAlpha(NeverLose.bgColor.getRGB(), alpha);
            default -> colors;
        };
        return colors;
    }
    public int bgColor(int counter) {
        return bgColor(counter, bgAlpha.getValue().intValue());
    }

    public int bgColor() {
        return bgColor(0);
    }
}
