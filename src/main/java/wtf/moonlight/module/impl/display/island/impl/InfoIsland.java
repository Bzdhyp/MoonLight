package wtf.moonlight.module.impl.display.island.impl;

import net.minecraft.client.Minecraft;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.impl.display.island.Island;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;

import static wtf.moonlight.util.misc.InstanceAccess.mc;

public class InfoIsland extends Island {
    public InfoIsland() {
        super("Info", Island.Type.Info, 1000, 100, 20, 200, 20);
    }

    @Override
    public boolean shouldRender() {
        return false;
    }

    @Override
    public void render() {
        render(false);
    }

    @Override
    public void onShader(Shader2DEvent event) {
        render(true);
    }

    public void render(boolean shadow) {
        String fullText = name + " | " + mc.thePlayer.getName() + " | " + Minecraft.getDebugFPS() + "fps";

        int iconSize = 36;
        int textSize = 16;
        int padding = 5;

        int iconWidth = Fonts.Icon.get(iconSize).getStringWidth("T");
        int textWidth = Fonts.interMedium.get(textSize).getStringWidth(fullText);
        int totalWidth = iconWidth + textWidth + padding;

        int adjustedHeight = 16;

        if (!shadow) {
            RoundedUtil.drawRound(getX() - (float) totalWidth / 2, getY(), totalWidth, adjustedHeight, 6,
                    new Color(Client.INSTANCE.getModuleManager().getModule(Interface.class).bgColor()));
        } else {
            RoundedUtil.drawRound(getX() - (float) totalWidth / 2, getY(), totalWidth, adjustedHeight, 6, Color.BLACK);
        }

        int accentColor = Client.INSTANCE.getModuleManager().getModule(Interface.class).color();

        Fonts.Icon.get(iconSize).drawString("M", getX() - (float) totalWidth / 2 + 3,
                getY() + (float) (adjustedHeight - iconSize) / 2 + 12.5, accentColor);

        if (!name.isEmpty()) {
            String firstLetter = name.substring(0, 1);
            String remainingText = name.substring(1) + " | " + mc.thePlayer.getName() + " | " + Minecraft.getDebugFPS() + "fps";

            int textX = getX() - totalWidth / 2 + iconWidth + 1;
            int textY = (int) (getY() + 6.5f);

            Fonts.interMedium.get(textSize).drawString(firstLetter, textX, textY, accentColor);

            Fonts.interMedium.get(textSize).drawString(
                    remainingText,
                    textX + Fonts.interMedium.get(textSize).getStringWidth(firstLetter),
                    textY,
                    Color.WHITE.getRGB()
            );
        }
    }
}