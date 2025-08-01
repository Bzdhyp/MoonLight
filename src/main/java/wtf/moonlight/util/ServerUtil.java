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
package wtf.moonlight.util;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class ServerUtil {
    @Getter
    public static Server server = Server.Unknown;
    private static final Map<String, Long> serverIpPingCache = new HashMap<>();

    public static void update(final String ip, final long ping) {
        ServerUtil.serverIpPingCache.put(ip, ping);
    }


    public static boolean isOnServer(final String ip) {
        return !Minecraft.getMinecraft().isSingleplayer() && getCurrentServerIP().endsWith(ip);
    }

    public static String getCurrentServerIP() {
        if (Minecraft.getMinecraft().isSingleplayer()) {
            return "Singleplayer";
        }
        return Minecraft.getMinecraft().getCurrentServerData().serverIP;
    }

    public static boolean isHypixelLobby() {
        String[] strings = new String[]{"CLICK TO PLAY"};
        for (Entity entity : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (entity.getName().startsWith("§e§l")) {
                for (String string : strings) {
                    if (entity.getName().equals("§e§l" + string)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isOnHypixel() {
        return isOnServer("hypixel.net");
    }

    public static boolean isOnLoyisa() {
        return isOnServer("eu.loyisa.cn");
    }

    public enum Server {
        Hypixel, HypixelCN, Mineplex, CubeCraft, Unknown, SinglePlayer
    }

}
