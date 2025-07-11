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
import wtf.moonlight.module.Module;
import wtf.moonlight.utils.DebugUtils;

public final class ToggleCommand extends Command {
    @Override
    public String[] getAliases() {
        return new String[]{"toggle", "t"};
    }

    @Override
    public void execute(final String[] arguments) throws CommandExecutionException {
        if (arguments.length == 2) {
            final String moduleName = arguments[1];
            for (final Module module : Client.INSTANCE.getModuleManager().getModules()) {
                if (module.getName().replaceAll(" ", "").equalsIgnoreCase(moduleName)) {
                    module.toggle();
                    DebugUtils.sendMessage(module.getName() + " has been " + (module.isEnabled() ? "\u00a7AEnabled\u00a77." : "\u00a7CDisabled\u00a77."));
                    return;
                }
            }
        }
        throw new CommandExecutionException(this.getUsage());
    }

    @Override
    public String getUsage() {
        return "toggle/t <module name>";
    }
}
