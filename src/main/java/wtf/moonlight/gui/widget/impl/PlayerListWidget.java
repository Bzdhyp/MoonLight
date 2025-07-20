package wtf.moonlight.gui.widget.impl;

import com.cubk.EventTarget;
import net.minecraft.entity.player.EntityPlayer;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.click.neverlose.NeverLose;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.gui.widget.Widget;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PlayerListWidget extends Widget {
    public PlayerListWidget() {
        super("PlayerList");
        this.x = 0.5f;
        this.y = 0.3f;
    }

    private final Map<String, PlayerEntry> playerEntries = new HashMap<>();

    @Override
    public void render() {
        this.renderPlayerList(false);
    }

    @Override
    public void onShader(Shader2DEvent event) {
        this.renderPlayerList(true);
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        playerEntries.clear();
    }

    private static class PlayerEntry {
        public final Animation alphaAnim = new DecelerateAnimation(250, 255);
        public final Animation heightAnim = new DecelerateAnimation(250, 1);
        public final EntityPlayer player;
        public int lastHurtTime;

        public PlayerEntry(EntityPlayer player) {
            this.player = player;
            this.lastHurtTime = player.hurtTime;
            this.alphaAnim.setDirection(Direction.FORWARDS);
            this.heightAnim.setDirection(Direction.FORWARDS);
        }
    }

    public void renderPlayerList(boolean shadow) {
        float posX = renderX + 2;
        float posY = renderY + 2;
        float fontSize = 13f;
        float padding = 4f;
        float iconSize = 18f;
        float spacing = 3.5f;
        float avatarWidth = 12f;
        float avatarHeight = 8f;
        float boxHeight = avatarHeight + padding;

        List<EntityPlayer> currentPlayers = mc.theWorld.playerEntities.stream()
                .filter(p -> p != mc.thePlayer && mc.thePlayer.getDistanceToEntity(p) < 64)
                .toList();

        Set<String> currentPlayerNames = new HashSet<>();
        for (EntityPlayer player : currentPlayers) {
            String name = player.getName();
            currentPlayerNames.add(name);

            PlayerEntry entry = playerEntries.get(name);
            if (entry != null) {
                entry.lastHurtTime = player.hurtTime;
                if (entry.alphaAnim.getDirection() != Direction.FORWARDS) {
                    entry.alphaAnim.setDirection(Direction.FORWARDS);
                    entry.alphaAnim.reset();
                }
                if (entry.heightAnim.getDirection() != Direction.FORWARDS) {
                    entry.heightAnim.setDirection(Direction.FORWARDS);
                    entry.heightAnim.reset();
                }
            } else {
                playerEntries.put(name, new PlayerEntry(player));
            }
        }

        for (Map.Entry<String, PlayerEntry> entry : playerEntries.entrySet()) {
            if (!currentPlayerNames.contains(entry.getKey())) {
                if (entry.getValue().alphaAnim.getDirection() != Direction.BACKWARDS) {
                    entry.getValue().alphaAnim.setDirection(Direction.BACKWARDS);
                    entry.getValue().alphaAnim.reset();
                }
                if (entry.getValue().heightAnim.getDirection() != Direction.BACKWARDS) {
                    entry.getValue().heightAnim.setDirection(Direction.BACKWARDS);
                    entry.getValue().heightAnim.reset();
                }
            }
        }

        playerEntries.entrySet().removeIf(entry ->
                entry.getValue().alphaAnim.finished(Direction.BACKWARDS) &&
                        entry.getValue().heightAnim.finished(Direction.BACKWARDS));

        float maxNameWidth = 0;
        for (PlayerEntry entry : playerEntries.values()) {
            float nameWidth = Fonts.interMedium.get(fontSize).getStringWidth(entry.player.getName() + " 99m");
            if (nameWidth > maxNameWidth) {
                maxNameWidth = nameWidth;
            }
        }

        String title = "PlayerList";
        float titleWidth = Fonts.interSemiBold.get(16).getStringWidth(title) + padding * 2 + iconSize;
        float titleHeight = Fonts.interSemiBold.get(fontSize).getHeight() + padding * 2;

        float fixedWidth = avatarWidth + padding * 3 + maxNameWidth;
        if (fixedWidth < titleWidth) fixedWidth = titleWidth;

        if (!shadow) {
            RoundedUtil.drawRound(posX, posY, 62, titleHeight - 2, 4, new Color(setting.bgColor()));
        } else {
            RoundedUtil.drawRound(posX, posY, 62, titleHeight - 2, 4, Color.BLACK);
        }

        Fonts.Icon.get(iconSize).drawString("D", posX + padding - 1f, posY + padding + 2f, NeverLose.iconRGB);
        Fonts.interSemiBold.get(16).drawString(title, posX + iconSize + padding - 4, posY + padding + 1.5f, -1);

        posY += titleHeight + spacing;

        float yOffset = 0;
        float nameOffsetX = avatarWidth + padding * 2;

        List<PlayerEntry> entries = new ArrayList<>(playerEntries.values());
        entries.sort(Comparator.comparing(e -> e.player.getName()));

        for (PlayerEntry entry : entries) {
            EntityPlayer player = entry.player;
            String playerName = player.getName();

            int alpha = (int) Math.min(255, entry.alphaAnim.getOutput());
            float heightFactor = (float) entry.heightAnim.getOutput();

            if (alpha <= 0) continue;

            float animatedY = posY + yOffset;

            if (!shadow) {
                RoundedUtil.drawRound(posX + avatarWidth + padding, animatedY, fixedWidth - avatarWidth - padding * 2, boxHeight, 4,
                        ColorUtil.applyOpacity(NeverLose.bgColor, (float) Math.max(.1, alpha)));
            } else {
                RoundedUtil.drawRound(posX + avatarWidth + padding, animatedY, fixedWidth - avatarWidth - padding * 2, boxHeight, 4,
                        ColorUtil.applyOpacity(Color.BLACK, (float) Math.max(.1, alpha)));
            }

            if (!shadow) {
                int hurtTime = entry.lastHurtTime;
                RenderUtil.renderPlayer2D(
                        player,
                        posX,
                        animatedY + (boxHeight - avatarHeight) / 2f - 2,
                        avatarWidth,
                        4,
                        ColorUtil.interpolateColor2(new Color(255, 255, 255, alpha), new Color(255, 0, 0, alpha), hurtTime / 7f)
                );
                RenderUtil.resetColor();
            }

            Fonts.interMedium.get(fontSize).drawString(
                    playerName,
                    posX + nameOffsetX - 1,
                    animatedY + (boxHeight - Fonts.interMedium.get(fontSize).getHeight()) / 2f + 3,
                    new Color(255, 255, 255, alpha).getRGB()
            );

            int distance = (int) mc.thePlayer.getDistanceToEntity(player);
            String distanceText = distance + "m";
            Fonts.interMedium.get(fontSize).drawString(
                    distanceText,
                    posX + fixedWidth - padding - Fonts.interMedium.get(fontSize).getStringWidth(distanceText) - 2,
                    animatedY + (boxHeight - Fonts.interMedium.get(fontSize).getHeight()) / 2f + 3,
                    new Color(180, 180, 180, alpha).getRGB()
            );

            yOffset += (boxHeight + spacing) * heightFactor;
        }

        this.width = 70;
        this.height = 20;
    }

    @Override
    public boolean shouldRender() {
        return setting.isEnabled() && setting.elements.isEnabled("PlayerList");
    }
}