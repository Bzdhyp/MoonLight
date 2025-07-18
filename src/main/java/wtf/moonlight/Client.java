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
package wtf.moonlight;

import de.florianmichael.viamcp.ViaMCP;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cubk.EventManager;
import wtf.moonlight.command.CommandManager;
import wtf.moonlight.component.*;
import wtf.moonlight.config.ConfigManager;
import wtf.moonlight.gui.click.arcane.ArcaneClickGui;
import wtf.moonlight.config.impl.friend.FriendManager;
import wtf.moonlight.module.ModuleManager;
import wtf.moonlight.gui.click.dropdown.DropdownGUI;
import wtf.moonlight.gui.click.neverlose.NeverLose;
import wtf.moonlight.gui.notification.NotificationManager;
import wtf.moonlight.gui.notification.NotificationType;
import wtf.moonlight.gui.widget.WidgetManager;
import wtf.moonlight.util.DiscordInfo;
import wtf.moonlight.util.player.RotationUtil;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class Client {
    // Logger instance for logging events and errors
    public static final Logger LOGGER = LogManager.getLogger(Client.class);

    // Singleton instance of Moonlight
    public static final Client INSTANCE = new Client();

    // Client information
    public final String clientName = "Moonlight";
    public final String version = "Latest";

    // Directory for configuration files and other data
    private final File mainDir = new File(Minecraft.getMinecraft().mcDataDir, clientName);

    // Managers and GUI components
    private EventManager eventManager;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private WidgetManager widgetManager;
    private CommandManager commandManager;
    private FriendManager friendManager;

    private NeverLose neverLose;
    private DropdownGUI dropdownGUI;
    private ArcaneClickGui arcaneClickGui;

    private DiscordInfo discordRP;

    // System Tray icon
    private TrayIcon trayIcon;
    // Load status
    private boolean loaded;

    private Path dataFolder;

    public void init() {
        loaded = false;
        LOGGER.info("Client starting up...");
        this.initViaMCP();

        long start = System.currentTimeMillis();

        setupMainDirectory();

        LOGGER.info("Initializing managers...");
        eventManager = new EventManager();
        moduleManager = new ModuleManager();
        widgetManager = new WidgetManager();
        configManager = new ConfigManager();
        commandManager = new CommandManager();
        friendManager = new FriendManager();

        neverLose = new NeverLose();
        dropdownGUI = new DropdownGUI();
        arcaneClickGui = new ArcaneClickGui();

        registerEventHandlers();

        setupDiscordRPC();
        handleFastRender();

        VideoComponent.ensureVideoExists();
        VideoComponent.startVideoPlayback();

        loaded = true;

        dataFolder = Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath()).resolve(clientName);

        LOGGER.info("Finished loading in {} seconds.", (System.currentTimeMillis() - start) / 1000f);
    }

    private void registerEventHandlers() {
        LOGGER.info("Registering...");

        eventManager.register(new BackgroundProcess());
        eventManager.register(new RotationUtil());
        eventManager.register(new FallDistanceComponent());
        eventManager.register(new BadPacketsComponent());
        eventManager.register(new PingSpoofComponent());
        eventManager.register(new FreeLookComponent());
        eventManager.register(new BlinkComponent());
        eventManager.register(new SpoofSlotComponent());
    }

    private void setupMainDirectory() {
        if (!mainDir.exists()) {
            boolean dirCreated = mainDir.mkdir();
            if (dirCreated) {
                LOGGER.info("Created main directory at {}", mainDir.getAbsolutePath());
            } else {
                LOGGER.warn("Failed to create main directory at {}", mainDir.getAbsolutePath());
            }
            Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MUSIC, 0);
        } else {
            LOGGER.info("Main directory already exists at {}", mainDir.getAbsolutePath());
        }

        this.dataFolder = Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath()).resolve(clientName);
    }

    private void initViaMCP() {
        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        LOGGER.info("ViaMCP initialized.");
    }

    private void setupDiscordRPC() {
        try {
            discordRP = new DiscordInfo();
            discordRP.init();
            LOGGER.info("Discord Rich Presence initialized.");
        } catch (Throwable throwable) {
            LOGGER.error("Failed to set up Discord RPC.", throwable);
        }
    }

    private void handleFastRender() {
        if (Minecraft.getMinecraft().gameSettings.ofFastRender) {
            NotificationManager.post(NotificationType.WARNING, "Fast Rendering has been disabled", "due to compatibility issues");
            Minecraft.getMinecraft().gameSettings.ofFastRender = false;
            LOGGER.info("Fast Rendering was disabled due to compatibility issues.");
        }
    }

    public void onStop() {
        if (discordRP != null) {
            discordRP.stop();
            LOGGER.info("Discord Rich Presence stopped.");
        }
        configManager.saveConfigs();
        LOGGER.info("All configurations saved.");
    }
}
