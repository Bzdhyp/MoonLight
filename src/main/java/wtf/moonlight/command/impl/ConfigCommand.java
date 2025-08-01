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
import wtf.moonlight.config.impl.ModuleConfig;
import wtf.moonlight.util.DebugUtil;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class ConfigCommand extends Command {

    private enum Action {
        LOAD, SAVE, LIST, CREATE, REMOVE, OPENFOLDER, CURRENT;

        public static Action fromString(String action) {
            try {
                return Action.valueOf(action.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Override
    public String getUsage() {
        return "config/cf/preset <load/save/list/create/remove/openfolder/current> <config>";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"config", "cf", "preset"};
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            DebugUtil.sendMessage("Usage: " + getUsage());
            return;
        }

        Action action = Action.fromString(args[1]);
        if (action == null) {
            DebugUtil.sendMessage("Invalid action. Usage: " + getUsage());
            return;
        }

        switch (action) {
            case LIST:
                handleList();
                break;
            case OPENFOLDER:
                handleOpenFolder();
                break;
            case CURRENT:
                handleCurrent();
                break;
            default:
                if (args.length < 3) {
                    DebugUtil.sendMessage("Action '" + action.name().toLowerCase() + "' requires an additional argument. Usage: " + getUsage());
                    return;
                }
                String configName = args[2];
                switch (action) {
                    case LOAD:
                        handleLoad(configName);
                        break;
                    case SAVE:
                        handleSave(configName, true);
                        break;
                    case CREATE:
                        handleCreate(configName);
                        break;
                    case REMOVE:
                        handleRemove(configName);
                        break;
                    default:
                        DebugUtil.sendMessage("Unknown action. Usage: " + getUsage());
                }
                break;
        }
    }

    private void handleList() {
        var configs = getConfigList();
        if (configs.length == 0) {
            DebugUtil.sendMessage("No configurations found.");
        } else {
            DebugUtil.sendMessage("Configs: " + String.join(", ", configs));
        }
    }

    private void handleOpenFolder() {
        File directory = Client.INSTANCE.getMainDir();
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(directory);
                DebugUtil.sendMessage("Opened config folder.");
            } catch (IOException e) {
                DebugUtil.sendMessage("Failed to open config folder.");
                e.printStackTrace();
            }
        } else {
            DebugUtil.sendMessage("Opening folder is not supported on this system.");
        }
    }

    private void handleCurrent() {
        String currentConfig = Client.INSTANCE.getConfigManager().getCurrentConfig();
        if (currentConfig != null) {
            DebugUtil.sendMessage("Current config: " + currentConfig);
        } else {
            DebugUtil.sendMessage("No config is currently loaded.");
        }
    }

    private void handleLoad(String configName) {
        ModuleConfig cfg = new ModuleConfig(configName);
        if (Client.INSTANCE.getConfigManager().loadConfig(cfg)) {
            Client.INSTANCE.getConfigManager().setCurrentConfig(configName);
            DebugUtil.sendMessage("Loaded config: " + configName);
        } else {
            DebugUtil.sendMessage("Invalid config: " + configName);
        }
    }

    private void handleSave(String configName) {
        handleSave(configName, true);
    }

    /**
     * Saves the current configuration.
     *
     * @param configName The name of the configuration to save.
     * @param notify     Whether to send a success/failure message.
     */
    private void handleSave(String configName, boolean notify) {
        ModuleConfig cfg = new ModuleConfig(configName);
        if (Client.INSTANCE.getConfigManager().saveConfig(cfg)) {
            if (notify) {
                DebugUtil.sendMessage("Saved config: " + configName);
            }
        } else {
            if (notify) {
                DebugUtil.sendMessage("Failed to save config: " + configName);
            }
        }
    }

    private void handleCreate(String configName) {
        File configFile = new File(Client.INSTANCE.getMainDir(), configName + ".json");
        try {
            if (configFile.createNewFile()) {
                Client.INSTANCE.getConfigManager().setCurrentConfig(configName);
                DebugUtil.sendMessage("Created config and set as current: " + configName);
                // Automatically save the newly created config
                handleSave(configName, false); // Pass false to avoid duplicate messages
                DebugUtil.sendMessage("Automatically saved config: " + configName);
            } else {
                DebugUtil.sendMessage("Config already exists: " + configName);
            }
        } catch (IOException e) {
            DebugUtil.sendMessage("Failed to create config: " + configName);
            e.printStackTrace();
        }
    }

    private void handleRemove(String configName) {
        File configFile = new File(Client.INSTANCE.getMainDir(), configName + ".json");
        if (configFile.exists()) {
            if (configFile.delete()) {
                DebugUtil.sendMessage("Removed config: " + configName);
            } else {
                DebugUtil.sendMessage("Failed to remove config: " + configName);
            }
        } else {
            DebugUtil.sendMessage("Config does not exist: " + configName);
        }
    }

    private String[] getConfigList() {
        File directory = Client.INSTANCE.getMainDir();
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) {
            return new String[0];
        }
        String[] configs = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            configs[i] = files[i].getName().replaceFirst("\\.json$", "");
        }
        return configs;
    }
}