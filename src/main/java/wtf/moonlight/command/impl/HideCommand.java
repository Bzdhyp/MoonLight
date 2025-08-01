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
import wtf.moonlight.Client;
import wtf.moonlight.command.Command;
import wtf.moonlight.command.CommandExecutionException;
import wtf.moonlight.module.Module;
import wtf.moonlight.util.DebugUtil;

import java.util.Optional;


public final class HideCommand extends Command {
    @Override
    public String[] getAliases() {
        return new String[]{"hide", "h", "visible", "v"};
    }

    @Override
    public void execute(final String[] arguments) throws CommandExecutionException {
        if (arguments.length == 2) {
            final String arg = arguments[1];
            if (arg.equalsIgnoreCase("clear")) {
                for (final Module module : Client.INSTANCE.getModuleManager().getModules()) {
                    module.setHidden(false);
                }
                DebugUtil.sendMessage("Cleared all hidden module.");
            } else if (arg.equalsIgnoreCase("list")) {
                DebugUtil.sendMessage("Hidden Modules");
                for (final Module module : Client.INSTANCE.getModuleManager().getModules()) {
                    if (module.isHidden()) {
                        DebugUtil.sendMessage(EnumChatFormatting.GRAY + "- " + EnumChatFormatting.RED + module.getName());
                    }
                }
            } else {
                final Optional<Module> module2 = Optional.ofNullable(Client.INSTANCE.getModuleManager().getModule(arg));
                if (module2.isPresent()) {
                    final Module m = module2.get();
                    m.setHidden(!m.isHidden());
                    DebugUtil.sendMessage(m.getName() + " is now " + (m.isHidden() ? "\u00a7Chidden\u00a77." : "\u00a7Ashown\u00a77."));
                }
            }
            return;
        }
        throw new CommandExecutionException(this.getUsage());
    }

    @Override
    public String getUsage() {
        return "hide <module> | clear | list";
    }
}
