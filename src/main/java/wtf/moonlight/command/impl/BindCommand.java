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

import net.minecraft.util.EnumChatFormatting;
import org.lwjglx.input.Keyboard;
import wtf.moonlight.Client;
import wtf.moonlight.command.Command;
import wtf.moonlight.command.CommandExecutionException;
import wtf.moonlight.module.Module;
import wtf.moonlight.util.DebugUtil;
import wtf.moonlight.util.StringUtil;

public final class BindCommand extends Command {
    @Override
    public String[] getAliases() {
        return new String[]{"bind", "b"};
    }

    @Override
    public void execute(final String[] arguments) throws CommandExecutionException {
        if (arguments.length == 3) {
            final String moduleName = arguments[1];
            final String keyName = arguments[2];
            boolean foundModule = false;

            for (final Module module : Client.INSTANCE.getModuleManager().getModules()) {
                if (module.getName().equalsIgnoreCase(moduleName)) {
                    module.setKeyBind(Keyboard.getKeyIndex(keyName.toUpperCase()));
                    final String string = "Set " + module.getName() + " to " + StringUtil.upperSnakeCaseToPascal(Keyboard.getKeyName(module.getKeyBind())) + ".";
                    DebugUtil.sendMessage(string);
                    foundModule = true;
                    break;
                }
            }

            if (!foundModule) {
                DebugUtil.sendMessage("Cound not find module.");
            }
        } else {
            if (arguments.length != 2) {
                throw new CommandExecutionException(this.getUsage());
            }

            if (arguments[1].equalsIgnoreCase("clear")) {
                for (final Module module2 : Client.INSTANCE.getModuleManager().getModules()) {
                    module2.setKeyBind(0);
                    DebugUtil.sendMessage("Cleared all binds.");
                }
            } else if (arguments[1].equalsIgnoreCase("list")) {
                DebugUtil.sendMessage("Binds");
                for (final Module module2 : Client.INSTANCE.getModuleManager().getModules()) {
                    if (module2.getKeyBind() != 0) {
                        DebugUtil.sendMessage(EnumChatFormatting.GRAY + "- " + EnumChatFormatting.RED + module2.getName() + ": " + Keyboard.getKeyName(module2.getKeyBind()));
                    }
                }
            }
        }
    }

    @Override
    public String getUsage() {
        return "bind <module> <key> | clear | list";
    }
}
