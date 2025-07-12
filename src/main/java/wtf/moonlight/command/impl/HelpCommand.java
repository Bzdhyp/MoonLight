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
import wtf.moonlight.util.DebugUtil;

import java.util.Arrays;

public final class HelpCommand extends Command {
    @Override
    public String[] getAliases() {
        return new String[]{"help", "h"};
    }

    @Override
    public void execute(final String[] arguments) {
        for (final Command command : Client.INSTANCE.getCommandManager().cmd) {
            if(!(command instanceof ModuleCommand))
                DebugUtil.sendMessage(Arrays.toString(command.getAliases()) + ": " + command.getUsage());
        }
    }

    @Override
    public String getUsage() {
        return "help/h";
    }
}
