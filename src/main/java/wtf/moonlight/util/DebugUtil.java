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

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import static wtf.moonlight.util.misc.InstanceAccess.mc;

public class DebugUtil {
    public static void sendMessage(String message) {
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE + "Moonlight" + EnumChatFormatting.WHITE + EnumChatFormatting.GRAY + ">> " + EnumChatFormatting.WHITE + message));
    }

    public static void send(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.sendChatMessage(message);
        }
    }
}
