package wtf.moonlight.component;

import com.cubk.EventTarget;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.Client;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.util.MathUtil;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.ContinualAnimation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.misc.InstanceAccess;
import wtf.moonlight.util.player.ScaffoldUtil;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;

public class BackgroundProcess implements InstanceAccess {
    private final Scaffold scaffold = Client.INSTANCE.getModuleManager().getModule(Scaffold.class);
    private final ContinualAnimation animation = new ContinualAnimation();
    private final Animation anim = new DecelerateAnimation(250, 1);
    public static int lost = 0, killed = 0, won = 0;

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            S02PacketChat s02 = (S02PacketChat) event.getPacket();
            String xd = s02.getChatComponent().getUnformattedText();
            if (xd.contains("was killed by " + mc.thePlayer.getName())) {
                ++killed;
            }

            if (xd.contains("You Died! Want to play again?")) {
                ++lost;
            }
        }

        if (packet instanceof S45PacketTitle && ((S45PacketTitle) packet).getType().equals(S45PacketTitle.Type.TITLE)) {
            String unformattedText = ((S45PacketTitle) packet).getMessage().getUnformattedText();
            if (unformattedText.contains("VICTORY!")) {
                ++won;
            }

            if (unformattedText.contains("GAME OVER!") || unformattedText.contains("DEFEAT!") || unformattedText.contains("YOU DIED!")) {
                ++lost;
            }
        }
    }

    @EventTarget
    public void drawCounter(Render2DEvent event) {
        if (!scaffold.counter.is("Normal")) {
            if (!scaffold.isEnabled()) return;
        }

        int slot = ScaffoldUtil.getBlockSlot();
        ScaledResolution sr = new ScaledResolution(mc);
        int count = slot == -1 ? 0 : ScaffoldUtil.getBlockCount();
        String countStr = String.valueOf(count);

        String str = countStr + " block" + (count != 1 ? "s" : "");
        net.minecraft.client.gui.FontRenderer fr = mc.fontRendererObj;
        ItemStack heldItem = slot == -1 ? null : mc.thePlayer.inventory.mainInventory[slot];
        float output = (float) anim.getOutput();

        switch (scaffold.counter.getValue()) {
            case "None":
                break;

            case "Hanabi": {
                hanabiBlockCount(sr.getScaledWidth(), sr.getScaledHeight() / 2f);
                break;
            }

            case "Basic": {
                float x = sr.getScaledWidth() / 2F - fr.getStringWidth(str) / 2F + 1;
                float y = sr.getScaledHeight() / 2F + 10;

                fr.drawStringWithShadow(str, x, y, -1);
                break;
            }

            case "Adjust": {
                Fonts.interRegular.get(16).drawCenteredStringWithShadow(
                        countStr + " " + EnumChatFormatting.GRAY + "blocks", sr.getScaledWidth() / 2f, sr.getScaledHeight() / 2f + 22,
                        Client.INSTANCE.getModuleManager().getModule(Interface.class).color(0));
                break;
            }

            case "Simple": {
                float percentage = Math.min(1.0f, ScaffoldUtil.getBlockCount() / 128.0f);

                int color = Color.HSBtoRGB(percentage / 3, 1.0F, 1.0F);
                if (!mc.gameSettings.showDebugInfo) {
                    fr.drawStringWithShadow(countStr, sr.getScaledWidth() / 2.0f - fr.getStringWidth(countStr) / 2.0f, sr.getScaledHeight() / 2.0f - 25.0f, color);
                }
                break;
            }

            case "Counter": {
                float x = sr.getScaledWidth() / 2.0F;
                float y = sr.getScaledHeight() / 2.0F + (12);
                float thickness = 2.5F;

                float percentage = Math.min(1, ScaffoldUtil.getBlockCount() / 128.0F);
                animation.animate(percentage, 18);
                float percentageWidth = animation.getOutput();

                float width = 80.0F;
                float half = width / 2;

                Gui.drawRect2(x - half - 0.5, y - 0.6, 81, 3.8, new Color(0, 0, 0, 120).getRGB());

                int color = Client.INSTANCE.getModuleManager().getModule(Interface.class).getMainColor().getRGB();
                RenderUtil.drawGradientRect2(x - half, y, x - half + width * percentageWidth, y + thickness, false,
                        color, ColorUtil.darker(color));
                break;
            }


            case "Augustus": {
                float x, y;

                float blockWH = heldItem != null ? 15 : -2;
                int spacing = 3;
                float textWidth = Fonts.interBold.get(18).getStringWidth(countStr);

                float totalWidth = ((textWidth + blockWH + spacing) + 6);
                x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
                y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 120);
                float height = 20;

                RoundedUtil.drawRound(x, y, totalWidth, height, 3, new Color(INSTANCE.getModuleManager().getModule(Interface.class).bgColor(), true));
                fr.drawString(countStr, x + 2 + blockWH + spacing, y + 6.5f, -1);

                if (heldItem != null) {
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int) x + 2, (int) (y + 10 - (blockWH / 2)));
                    RenderHelper.disableStandardItemLighting();
                }

                break;
            }

            case "Normal": {
                anim.setDirection(scaffold.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);

                float x, y;

                float blockWH = heldItem != null ? 15 : -2;
                int spacing = 3;
                String text = "§l" + countStr + "§r block" + (count != 1 ? "s" : "");
                float textWidth = Fonts.interBold.get(18).getStringWidth(text);

                float totalWidth = ((textWidth + blockWH + spacing) + 6) * output;
                x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
                y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 20);
                float height = 20;
                RenderUtil.scissor(x - 1.5, y - 1.5, totalWidth + 3, height + 3);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                RoundedUtil.drawRound(x, y, totalWidth, height, 5, new Color(INSTANCE.getModuleManager().getModule(Interface.class).bgColor(), true));

                Fonts.interBold.get(18).drawString(text, x + 3 + blockWH + spacing, y + Fonts.interBold.get(18).getMiddleOfBox(height) + 2.5f, -1);

                if (heldItem != null) {
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int) x + 3, (int) (y + 10 - (blockWH / 2)));
                    RenderHelper.disableStandardItemLighting();
                }

                GL11.glDisable(GL11.GL_SCISSOR_TEST);

                break;
            }
        }
    }

    @EventTarget
    public void drawShader2D(Shader2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        if (!scaffold.counter.is("Normal")) {
            if (!scaffold.isEnabled()) return;
        }

        if (scaffold.counter.is("Hanabi")) {
            hanabiBlockCount(sr.getScaledWidth(), sr.getScaledHeight() / 2f);
        }

        float x, y;
        int slot = ScaffoldUtil.getBlockSlot();
        int count = slot == -1 ? 0 : ScaffoldUtil.getBlockCount();
        String countStr = String.valueOf(count);
        ItemStack heldItem = slot == -1 ? null : mc.thePlayer.inventory.mainInventory[slot];
        float blockWH = heldItem != null ? 15 : -2;

        if (scaffold.counter.is("Augustus")) {
            float textWidth = Fonts.interBold.get(18).getStringWidth(countStr);
            int spacing = 3;

            float totalWidth = ((textWidth + blockWH + spacing) + 6);
            x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
            y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 120);
            float height = 20;

            RoundedUtil.drawRound(x, y, totalWidth, height, 3, Color.BLACK);

            if (heldItem != null) {
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int) x + 2, (int) (y + 10 - (blockWH / 2)));
                RenderHelper.disableStandardItemLighting();
            }
        }

        if (scaffold.counter.is("Normal")) {
            anim.setDirection(scaffold.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);

            int spacing = 3;
            String text = "§l" + countStr + "§r block" + (count != 1 ? "s" : "");

            float textWidth = Fonts.interBold.get(18).getStringWidth(text);
            float totalWidth = ((textWidth + blockWH + spacing) + 6) * (float) anim.getOutput();

            float height = 20;
            x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
            y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 20);

            RenderUtil.scissor(x - 1.5, y - 1.5, totalWidth + 3, height + 3);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RoundedUtil.drawRound(x, y, totalWidth, height, 5, Color.BLACK);

            if (heldItem != null) {
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int) x + 3, (int) (y + 10 - (blockWH / 2)));
                RenderHelper.disableStandardItemLighting();
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    public int blockCount = 0;
    public float alphaAnimation = 0;
    public float yAxisAnimation = 0;

    public void hanabiBlockCount(float width, float height) {
        boolean state = scaffold.isEnabled();

        this.alphaAnimation = RenderUtil.getAnimationState(this.alphaAnimation, state ? 0.7f : 0, 10f);
        this.yAxisAnimation = RenderUtil.getAnimationState(this.yAxisAnimation, state ? 0 : 10, (float) Math.max(10, (Math.abs(this.yAxisAnimation - (state ? 0 : 10)) * 50) * 0.5));

        float trueHeight = 18;

        if (alphaAnimation > 0.2f) {
            try {
                blockCount = ScaffoldUtil.getBlockCount();
            } catch (Exception ignore) {
                blockCount = 0;
            }
            String cunt = "block" + (blockCount > 1 ? "s" : "");
            FontRenderer font = Fonts.interBold.get(20);
            FontRenderer font2 = Fonts.interBold.get(18);
            float length = font.getStringWidth(blockCount + "  ") + font2.getStringWidth(cunt) + 1f;
            RenderUtil.drawRoundedRect(width / 2 - (length / 2), height + trueHeight - this.yAxisAnimation, length, 15, 2, ColorUtil.reAlpha(Color.BLACK.getRGB(), alphaAnimation), 0.5f, ColorUtil.reAlpha(Color.BLACK.getRGB(), alphaAnimation));

            font.drawString(blockCount + "", width / 2 - (length / 2 - 2f), height + (trueHeight + 3.5f) - this.yAxisAnimation, ColorUtil.reAlpha(Color.WHITE.getRGB(), MathUtil.clampValue(alphaAnimation + 0.25f, 0f, 1f)));
            font2.drawString(cunt, width / 2 - (length / 2 - 1f) + font.getStringWidth(blockCount + " "), height + (trueHeight + 4) - this.yAxisAnimation, ColorUtil.reAlpha(Color.WHITE.getRGB(), MathUtil.clampValue(alphaAnimation - 0.1f, 0f, 1f)));
        }
    }
}
