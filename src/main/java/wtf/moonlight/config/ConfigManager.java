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
package wtf.moonlight.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import wtf.moonlight.Client;
import wtf.moonlight.config.impl.AccountConfig;
import wtf.moonlight.config.impl.ModuleConfig;
import wtf.moonlight.config.impl.WidgetConfig;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Getter
public class ConfigManager {

    private final ModuleConfig setting = new ModuleConfig("default");
    private final WidgetConfig elements = new WidgetConfig("elements");
    private final AccountConfig account = new AccountConfig("account");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    @Setter
    private String currentConfig = "default";

    public ConfigManager() {
        loadConfigs();
    }

    public boolean loadConfig(Config config) {
        if (config == null) {
            Client.LOGGER.warn("Attempted to load a null configuration.");
            return false;
        }

        try (FileReader reader = new FileReader(config.getFile())) {
            JsonParser parser = new JsonParser(); // Create an instance
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject(); // Use instance method
            config.loadConfig(jsonObject);
            Client.LOGGER.info("Loaded config: {}", config.getName());
            return true;
        } catch (IOException e) {
            Client.LOGGER.error("Failed to load config: {}", config.getName(), e);
            return false;
        }
    }

    public boolean loadOnlineConfig(Config config, JsonObject jsonObject) {
        if (config == null || jsonObject == null) {
            Client.LOGGER.warn("Config or JsonObject is null. Cannot load online config.");
            return false;
        }

        try {
            config.loadConfig(jsonObject);
            Client.LOGGER.info("Loaded online config: {}", config.getName());
            return true;
        } catch (Exception e) {
            Client.LOGGER.error("Failed to load online config: {}", config.getName(), e);
            return false;
        }
    }

    public boolean saveConfig(Config config) {
        if (config == null) {
            Client.LOGGER.warn("Attempted to save a null configuration.");
            return false;
        }

        JsonObject jsonObject = config.saveConfig();
        String jsonString = gson.toJson(jsonObject);

        try (FileWriter writer = new FileWriter(config.getFile())) {
            writer.write(jsonString);
            Client.LOGGER.info("Saved config: {}", config.getName());
            return true;
        } catch (IOException e) {
            Client.LOGGER.error("Failed to save config: {}", config.getName(), e);
            return false;
        }
    }

    public void saveConfigs() {
        if (!saveConfig(setting)) {
            Client.LOGGER.warn("Failed to save setting config.");
        }
        if (!saveConfig(elements)) {
            Client.LOGGER.warn("Failed to save elements config.");
        }
        if (!saveConfig(account)) {
            Client.LOGGER.warn("Failed to save elements config.");
        }
    }

    public void loadConfigs() {
        if (!loadConfig(setting)) {
            Client.LOGGER.warn("Failed to load setting config.");
        }
        if (!loadConfig(elements)) {
            Client.LOGGER.warn("Failed to load elements config.");
        }
        if (!loadConfig(account)) {
            Client.LOGGER.warn("Failed to load elements config.");
        }
    }
}