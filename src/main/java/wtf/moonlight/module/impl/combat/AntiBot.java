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
package wtf.moonlight.module.impl.combat;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.EnumChatFormatting;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.MultiBoolValue;
import wtf.moonlight.util.Workers;
import wtf.moonlight.util.render.RenderUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ModuleInfo(name = "AntiBot", category = Categor.Combat)
public class AntiBot extends Module {
    public final MultiBoolValue options = new MultiBoolValue("Options", Arrays.asList(
            new BoolValue("Tab", false),
            new BoolValue("Matrix Test", false),
            new BoolValue("Matrix Armor", false),
            new BoolValue("HYT Get Name", false),
            new BoolValue("Hypixel", false),
            new BoolValue("Miniblox", false))
            , this);
    private final ListValue hytGetNameModes = new ListValue("GetName Mode", new String[]{"Bw4v4", "Bw1v1", "Bw32", "Bw16"}, "Bw4v4", this, () -> options.isEnabled("HYT Get Name"));
    public final Set<EntityPlayer> bots = new HashSet<>();
    private final Set<String> playerName = ConcurrentHashMap.newKeySet();
    private static final String VALID_USERNAME_REGEX = "^[a-zA-Z0-9_]{1,16}+$";

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player != mc.thePlayer)
                if (isBot(player))
                    bots.add(player);
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        Packet<?> packet = event.getPacket();
        if (options.isEnabled("HYT Get Name") && packet instanceof S02PacketChat s02PacketChat) {
            if (s02PacketChat.getChatComponent().getUnformattedText().contains("获得胜利!") || s02PacketChat.getChatComponent().getUnformattedText().contains("游戏开始 ...")) {
                playerName.clear();
            }
            switch (hytGetNameModes.getValue()) {
                case "Bw4v4":
                case "Bw1v1":
                case "Bw32": {
                    Matcher matcher = Pattern.compile("杀死了 (.*?)\\(").matcher(s02PacketChat.getChatComponent().getUnformattedText());
                    Matcher matcher2 = Pattern.compile("起床战争>> (.*?) (\\((((.*?) 死了!)))").matcher(s02PacketChat.getChatComponent().getUnformattedText());
                    if (matcher.find() && !s02PacketChat.getChatComponent().getUnformattedText().contains(": 起床战争>>") || !s02PacketChat.getChatComponent().getUnformattedText().contains(": 杀死了")) {
                        String name = matcher.group(1).trim();
                        if (!name.isEmpty() && playerName.add(name)) {
                            Workers.IO.execute(() -> {
                                try {
                                    Thread.sleep(6000);
                                    playerName.remove(name);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                    if (matcher2.find() && !s02PacketChat.getChatComponent().getUnformattedText().contains(": 起床战争>>") || !s02PacketChat.getChatComponent().getUnformattedText().contains(": 杀死了")) {
                        String name = matcher2.group(1).trim();
                        if (!name.isEmpty() && playerName.add(name)) {
                            Workers.IO.execute(() -> {
                                try {
                                    Thread.sleep(6000);
                                    playerName.remove(name);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                    break;
                }
                case "Bw16": {
                    Matcher matcher = Pattern.compile("击败了 (.*?)!").matcher(s02PacketChat.getChatComponent().getUnformattedText());
                    Matcher matcher2 = Pattern.compile("玩家 (.*?)死了！").matcher(s02PacketChat.getChatComponent().getUnformattedText());
                    if (matcher.find() && !s02PacketChat.getChatComponent().getUnformattedText().contains(": 击败了") || !s02PacketChat.getChatComponent().getUnformattedText().contains(": 玩家 ")) {
                        String name = matcher.group(1).trim();
                        if (!name.isEmpty() && playerName.add(name)) {
                            Workers.IO.execute(() -> {
                                try {
                                    Thread.sleep(10000);
                                    playerName.remove(name);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                    if (matcher2.find() && !s02PacketChat.getChatComponent().getUnformattedText().contains(": 击败了") || !s02PacketChat.getChatComponent().getUnformattedText().contains(": 玩家 ")) {
                        String name = matcher2.group(1).trim();
                        if (!name.isEmpty() && playerName.add(name)) {
                            Workers.IO.execute(() -> {
                                try {
                                    Thread.sleep(10000);
                                    playerName.remove(name);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                    break;

                }
            }
        }

        if (options.isEnabled("Matrix Test")) {
            if (packet instanceof S38PacketPlayerListItem s38) {
                for (S38PacketPlayerListItem.AddPlayerData data : s38.getEntries()) {
                    if (s38.getAction().equals(S38PacketPlayerListItem.Action.ADD_PLAYER) &&
                            data.getProfile().getProperties().isEmpty() && s38.getEntries().size() == 1
                            && mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfo(data.getProfile().getName()) != null) {
                        if (!bots.contains(data.getProfile().getId())) {
                            bots.add(mc.theWorld.getPlayerEntityByName(data.getProfile().getName()));
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        bots.clear();
    }

    @Override
    public void onDisable() {
        bots.clear();
    }

    public boolean isBot(EntityPlayer player) {

        if (!isEnabled())
            return false;

        if (options.isEnabled("Tab")) {
            String targetName = RenderUtil.stripColor(player.getDisplayName().getFormattedText());

            boolean shouldReturn = true;

            for (NetworkPlayerInfo networkPlayerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                shouldReturn = !RenderUtil.stripColor(networkPlayerInfo.getFullName()).contains(targetName);
            }

            return !shouldReturn;
        }

        if (options.isEnabled("Hypixel")) {
            for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                return info.getGameProfile().getId().compareTo(player.getUniqueID()) != 0 || this.nameStartsWith(player, "[NPC] ") || !player.getName().matches(VALID_USERNAME_REGEX);
            }
        }

        if (options.isEnabled("Miniblox")) {
            for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                return this.nameEqualsTo(player, "BOT");
            }
        }

        if (options.isEnabled("Matrix Armor")) {
            ItemStack helmet = player.getInventory()[3];
            ItemStack chestplate = player.getInventory()[2];

            if (helmet == null || chestplate == null)
                return true;
            if (helmet.getItem() == null || chestplate.getItem() == null)
                return true;

            int helmetColor = ((ItemArmor) helmet.getItem()).getColor(helmet);
            int chestplateColor = ((ItemArmor) chestplate.getItem()).getColor(chestplate);
            return !(helmetColor > 0 && chestplateColor == helmetColor);
        }

        if (options.isEnabled("HYT Get Name")) {
            return playerName.contains(player.getName());
        }

        return false;
    }

    private boolean nameStartsWith(EntityPlayer player, String prefix) {
        return EnumChatFormatting.getTextWithoutFormattingCodes(player.getDisplayName().getUnformattedText()).startsWith(prefix);
    }

    private boolean nameEqualsTo(EntityPlayer player, String name) {
        return EnumChatFormatting.getTextWithoutFormattingCodes(player.getDisplayName().getUnformattedText()).equals(name);
    }
}