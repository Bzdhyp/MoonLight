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
package wtf.moonlight.command.impl;

import wtf.moonlight.Client;
import wtf.moonlight.command.Command;
import wtf.moonlight.command.CommandExecutionException;
import wtf.moonlight.util.DebugUtil;

import static wtf.moonlight.util.misc.InstanceAccess.mc;

public class FriendCommand extends Command {
    @Override
    public String[] getAliases() {
        return new String[]{"friend", "f", "fr"};
    }

    @Override
    public void execute(final String[] arguments) throws CommandExecutionException {
        if (arguments.length == 1) {
            DebugUtil.sendMessage("Usage: " + getUsage());
            return;
        }
        final String lowerCase = arguments[1].toLowerCase();
        if (arguments.length == 2) {
            switch (lowerCase) {
                case "clear": {
                    DebugUtil.sendMessage("Cleared all friended players");
                    Client.INSTANCE.getFriendManager().getFriends().clear();
                    break;
                }
                case "list": {
                    if (!Client.INSTANCE.getFriendManager().getFriends().isEmpty()) {
                        DebugUtil.sendMessage("Friend§7[§f" + Client.INSTANCE.getFriendManager().getFriends().size() + "§7]§f : §a" + Client.INSTANCE.getFriendManager().getFriendsName());
                        break;
                    }
                    DebugUtil.sendMessage("The friend list is empty");
                    break;
                }
            }
        } else {
            if (arguments.length != 3) {
                throw new CommandExecutionException(this.getUsage());
            }
            if (arguments[2].contains(mc.thePlayer.getName())) {
                DebugUtil.sendMessage("§c§lNO");
                return;
            }
            final String lowerCase2 = arguments[1].toLowerCase();
            switch (lowerCase2) {
                case "add": {
                    DebugUtil.sendMessage("§b" + arguments[2] + " §7has been §2friended");
                    Client.INSTANCE.getFriendManager().add(arguments[2]);
                    break;
                }
                case "remove": {
                    DebugUtil.sendMessage("§b" + arguments[2] + " §7has been §2unfriended");
                    Client.INSTANCE.getFriendManager().remove(arguments[2]);
                    break;
                }
            }
        }
    }

    @Override
    public String getUsage() {
        return "friend add <name> | remove <name> | list | clear";
    }
}
