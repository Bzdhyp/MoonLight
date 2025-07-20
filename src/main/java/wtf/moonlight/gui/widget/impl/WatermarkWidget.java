package wtf.moonlight.gui.widget.impl;

import net.minecraft.client.Minecraft;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.click.neverlose.NeverLose;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.gui.widget.Widget;
import wtf.moonlight.module.impl.display.Watermark;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static wtf.moonlight.gui.click.neverlose.NeverLose.*;
import static wtf.moonlight.gui.click.neverlose.NeverLose.textRGB;

public class WatermarkWidget extends Widget {
    private final DateFormat dateFormat = new SimpleDateFormat("hh:mm a");

    public WatermarkWidget() {
        super("Watermark");
        this.x = 2.0f;
        this.y = 4.0f;
    }

    @Override
    public void render() {
        renderWatermark(false);

        String clientName = setting.clientName.getValue();
        FontRenderer titleFont = Fonts.MuseBold.get(20);
        FontRenderer info = Fonts.interSemiBold.get(16);
        FontRenderer iconfont = Fonts.nursultan.get(18);

        Watermark watermarkModule = Client.INSTANCE.getModuleManager().getModule(Watermark.class);
        boolean showUser = watermarkModule.nlOptions.isEnabled("User");
        boolean showFPS = watermarkModule.nlOptions.isEnabled("FPS");
        boolean showTime = watermarkModule.nlOptions.isEnabled("Time");

        float totalWidth = 2 + titleFont.getStringWidth(clientName) + 8;

        boolean hasInfoSection = showUser || showFPS || showTime;
        if (hasInfoSection) {
            totalWidth += 6;
            float infoSectionWidth = 0;

            // Add user
            if (showUser) {
                infoSectionWidth += iconfont.getStringWidth("W ") + info.getStringWidth(mc.thePlayer.getName());
            }

            // Add FPS
            if (showFPS) {
                if (showUser) infoSectionWidth += 6;
                infoSectionWidth += iconfont.getStringWidth("X ") + info.getStringWidth(Minecraft.getDebugFPS() + "fps");
            }

            // Add time
            if (showTime) {
                if (showUser || showFPS) infoSectionWidth += 6;
                infoSectionWidth += iconfont.getStringWidth("V ") + info.getStringWidth(dateFormat.format(new Date()));
            }

            totalWidth += infoSectionWidth + 6;
        }

        this.width = totalWidth;
        this.height = Fonts.interRegular.get(20).getHeight() + 3;
    }

    @Override
    public void onShader(Shader2DEvent event) {
        renderWatermark(true);
    }

    public void renderWatermark(boolean shadow) {
        String clientName = setting.clientName.getValue();
        String displayName = clientName.isEmpty() ? "NL" : clientName;

        Watermark watermarkModule = Client.INSTANCE.getModuleManager().getModule(Watermark.class);
        boolean showUser = watermarkModule.nlOptions.isEnabled("User");
        boolean showFPS = watermarkModule.nlOptions.isEnabled("FPS");
        boolean showTime = watermarkModule.nlOptions.isEnabled("Time");

        // Fonts
        FontRenderer titleFont = Fonts.MuseBold.get(20);
        FontRenderer info = Fonts.interSemiBold.get(16);
        FontRenderer iconfont = Fonts.nursultan.get(18);

        // Icons
        String userIcon = "W ";
        String fpsIcon = "X ";
        String timeIcon = "V ";

        // Positions
        int bgY = (int) (renderY + 0.5);
        int textY = (int) (renderY + 5.5);

        float clientNameWidth = titleFont.getStringWidth(displayName) + 8;

        float infoSectionWidth = 0;
        boolean hasInfoSection = showUser || showFPS || showTime;

        if (hasInfoSection) {
            if (showUser) {
                infoSectionWidth += iconfont.getStringWidth(userIcon) + info.getStringWidth(mc.thePlayer.getName());
            }

            if (showFPS) {
                if (showUser) infoSectionWidth += 6;
                infoSectionWidth += iconfont.getStringWidth(fpsIcon) + info.getStringWidth(Minecraft.getDebugFPS() + "fps");
            }

            if (showTime) {
                if (showUser || showFPS) infoSectionWidth += 6;
                infoSectionWidth += iconfont.getStringWidth(timeIcon) + info.getStringWidth(dateFormat.format(new Date()));
            }

            infoSectionWidth += 6;
        }

        // Render
        if (!shadow) {
            RoundedUtil.drawRound(renderX + 2, bgY, clientNameWidth,
                    Fonts.interRegular.get(20).getHeight() + 2, 4, ColorUtil.applyOpacity(NeverLose.bgColor, 1f));
            titleFont.drawOutlinedString(displayName, renderX + 6, textY - 1, textRGB, outlineTextRGB);

            if (hasInfoSection) {
                // Draw info background
                RoundedUtil.drawRound(renderX + 2 + clientNameWidth + 6, bgY,
                        infoSectionWidth,
                        Fonts.interRegular.get(20).getHeight() + 2, 4, ColorUtil.applyOpacity(NeverLose.bgColor, 1f));

                float currentX = renderX + 2 + clientNameWidth + 6 + 6;

                // Draw user
                if (showUser) {
                    iconfont.drawString(userIcon, currentX, textY + 0.5, iconRGB);
                    info.drawString(mc.thePlayer.getName(), currentX + iconfont.getStringWidth(userIcon) - 6, textY, textRGB);
                    currentX += iconfont.getStringWidth(userIcon) + info.getStringWidth(mc.thePlayer.getName());
                }

                // Draw FPS
                if (showFPS) {
                    if (showUser) currentX += 6;
                    iconfont.drawString(fpsIcon, currentX, textY + 0.5, iconRGB);
                    info.drawString(Minecraft.getDebugFPS() + "fps", currentX + iconfont.getStringWidth(fpsIcon) - 6, textY, textRGB);
                    currentX += iconfont.getStringWidth(fpsIcon) + info.getStringWidth(Minecraft.getDebugFPS() + "fps");
                }

                // Draw Time
                if (showTime) {
                    if (showUser || showFPS) currentX += 6;
                    String times = dateFormat.format(new Date());
                    iconfont.drawString(timeIcon, currentX, textY + 0.5, iconRGB);
                    info.drawString(times, currentX + iconfont.getStringWidth(timeIcon) - 6, textY, textRGB);
                }
            }
        } else {
            RoundedUtil.drawRound(renderX + 2, bgY, clientNameWidth,
                    Fonts.interRegular.get(20).getHeight() + 2, 4, ColorUtil.applyOpacity(Color.BLACK, 1f));

            if (hasInfoSection) {
                RoundedUtil.drawRound(renderX + 2 + clientNameWidth + 6, bgY,
                        infoSectionWidth,
                        Fonts.interRegular.get(20).getHeight() + 2, 4, ColorUtil.applyOpacity(Color.BLACK, 1f));
            }
        }
    }

    @Override
    public boolean shouldRender() {
        return Client.INSTANCE.getModuleManager().getModule(Watermark.class).isEnabled() &&
                Client.INSTANCE.getModuleManager().getModule(Watermark.class).watemarkMode.is("NeverLose");
    }
}