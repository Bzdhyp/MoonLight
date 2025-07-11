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
package wtf.moonlight.features.modules.impl.misc;

import net.minecraft.network.play.server.S02PacketChat;
import wtf.moonlight.events.annotations.EventTarget;
import wtf.moonlight.events.impl.packet.PacketEvent;
import wtf.moonlight.features.modules.Module;
import wtf.moonlight.features.modules.ModuleCategory;
import wtf.moonlight.features.modules.ModuleInfo;
import wtf.moonlight.features.values.impl.ModeValue;

@ModuleInfo(name = "AutoPlay", category = ModuleCategory.Misc)
public class AutoPlay extends Module {

    private final ModeValue mode = new ModeValue("Mode", new String[]{"Solo Insane", "Solo Normal", "BedWars Solo", "BedWars Duo", "BedWars Trio", "BedWars 4s"}, "Solo Insane",this);
    private static final String win = "You won! Want to play again? Click here!";
    private static final String lose = "You died! Want to play again? Click here!";
    private static final String bw = "1st Killer";

    @Override
    public void onEnable() {
        mc.thePlayer.sendChatMessage("/lang english");
    }

    @EventTarget
    private void onPacket(PacketEvent event) {
        this.setTag(mode.get());
        if (!event.isCancelled() && event.getPacket() instanceof S02PacketChat s02PacketChat) {
            String chatMessage = s02PacketChat.getChatComponent().getUnformattedText();
            if (chatMessage.contains(win) ||  chatMessage.contains(lose) || chatMessage.contains(bw)) {
                String command = "/play ";
                switch (mode.get()) {
                    case "Solo Insane" -> command += "solo_insane";
                    case "Solo Normal" -> command += "solo_normal";
                    case "BedWars Solo" -> command += "bedwars_eight_one";
                    case "BedWars Duo" -> command += "bedwars_eight_two";
                    case "BedWars Trio" -> command += "bedwars_four_three";
                    case "BedWars 4s" -> command += "bedwars_four_four";
                }
                mc.thePlayer.sendChatMessage(command);
            }
        }
    }
}
