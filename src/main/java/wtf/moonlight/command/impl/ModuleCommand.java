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

import wtf.moonlight.command.Command;
import wtf.moonlight.command.CommandExecutionException;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.values.Value;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.util.DebugUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ModuleCommand extends Command {
    private final Module module;
    private final List<Value> values;

    public ModuleCommand(Module module, List<Value> values) {
        this.module = module;
        this.values = values;
    }

    @Override
    public String getUsage() {
        return module.getName().toLowerCase(Locale.getDefault()) +  " <setting> <value>";
    }

    @Override
    public String[] getAliases() {
        return new String[]{module.getName()};
    }

    @Override
    public void execute(String[] args) throws CommandExecutionException {

        if (args.length == 1) {
            DebugUtil.sendMessage("Usage: " + getUsage());
            return;
        }

        Value value = module.getValue(args[1]);

        if (value == null)
            return;

        if (value instanceof BoolValue boolValue) {
            boolean newValue = !boolValue.get();
            boolValue.set(newValue);

            DebugUtil.sendMessage(module.getName() + " " + args[1] + " was toggled " + (newValue ? "§8on" : "§8off") + ".");
        } else {

            if (args.length < 3) {
                if (value instanceof SliderValue || value instanceof ColorValue)
                    DebugUtil.sendMessage(args[1].toLowerCase() + " <value>");
                else if (value instanceof ListValue modeValue)
                    DebugUtil.sendMessage(args[1].toLowerCase() + " <" + Arrays.stream(modeValue.getModes())
                            .map(String::toLowerCase).reduce((s1, s2) -> s1 + "/" + s2).orElse("") + ">");
                return;
            }

            if (value instanceof ColorValue colorValue) {
                colorValue.setValue(new Color(Integer.parseInt(args[2])));
                DebugUtil.sendMessage(module.getName() + " " + args[1] + " was set to " + colorValue.getValue() + ".");
            } else if (value instanceof SliderValue sliderValue) {
                sliderValue.setValue(Float.parseFloat(args[2]));
                DebugUtil.sendMessage(module.getName() + " " + args[1] + " was set to " + sliderValue.getValue() + ".");
            } else if (value instanceof MultiBoolValue multiBoolValue) {

                multiBoolValue.getValues().forEach(boolValue -> {
                    if (Objects.equals(boolValue.getName(), args[2])) {
                        boolean newValue = !boolValue.get();
                        boolValue.set(newValue);
                        DebugUtil.sendMessage(module.getName() + " " + args[1] + " was set to " + boolValue.get() + ".");
                    }
                });
            } else if (value instanceof ListValue modeValue) {
                modeValue.setValue(args[2]);
                DebugUtil.sendMessage(module.getName() + " " + args[1] + " was set to " + modeValue.getValue() + ".");
            }
        }
    }
}
