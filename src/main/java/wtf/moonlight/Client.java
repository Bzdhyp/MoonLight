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
import wtf.moonlight.config.ConfigManager;
import wtf.moonlight.gui.click.arcane.ArcaneClickGui;
import wtf.moonlight.component.VideoComponent;
import wtf.moonlight.util.friend.FriendManager;
import wtf.moonlight.module.ModuleManager;
import wtf.moonlight.gui.widget.impl.ScaffoldCounter;
import wtf.moonlight.gui.click.dropdown.DropdownGUI;
import wtf.moonlight.gui.click.neverlose.NeverLose;
import wtf.moonlight.gui.notification.NotificationManager;
import wtf.moonlight.gui.notification.NotificationType;
import wtf.moonlight.gui.widget.WidgetManager;
import wtf.moonlight.util.DiscordInfo;
import wtf.moonlight.component.SpoofSlotComponent;
import wtf.moonlight.component.BadPacketsComponent;
import wtf.moonlight.component.BlinkComponent;
import wtf.moonlight.component.PingSpoofComponent;
import wtf.moonlight.component.FallDistanceComponent;
import wtf.moonlight.util.player.RotationUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Getter
public class Client {
    // Logger instance for logging events and errors
    public static final Logger LOGGER = LogManager.getLogger(Client.class);

    // Singleton instance of Moonlight
    public static final Client INSTANCE = new Client();

    // Client information
    public final String clientName = "Moonlight";
    public final String version = "Latest";
    public final String clientCloud = "https://randomguy3725.github.io/MoonLightCloud/";

    // Directory for configuration files and other data
    private final File mainDir = new File(Minecraft.getMinecraft().mcDataDir, clientName);

    // Managers and GUI components
    private EventManager eventManager;
    private NotificationManager notificationManager;
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

    // Start time tracking
    private int startTime;
    private long startTimeLong;

    // Load status
    private boolean loaded;

    private Path dataFolder;

    public void init() {
        loaded = false;

        setupMainDirectory();
        initializeManagers();
        registerEventHandlers();
        initializeStartTime();
        initializeViaMCP();
        setupDiscordRPC();
        setupSystemTray();
        handleFastRender();

        loaded = true;

        dataFolder = Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath()).resolve(clientName);
        LOGGER.info("{} {} initialized successfully.", clientName, version);
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

    private void initializeManagers() {
        eventManager = new EventManager();
        notificationManager = new NotificationManager();
        moduleManager = new ModuleManager();
        widgetManager = new WidgetManager();
        configManager = new ConfigManager();
        commandManager = new CommandManager();
        friendManager = new FriendManager();

        VideoComponent.ensureVideoExists();
        VideoComponent.startVideoPlayback();

        neverLose = new NeverLose();
        dropdownGUI = new DropdownGUI();
        arcaneClickGui = new ArcaneClickGui();
    }

    private void registerEventHandlers() {
        eventManager.register(new ScaffoldCounter());
        eventManager.register(new RotationUtil());
        eventManager.register(new FallDistanceComponent());
        eventManager.register(new BadPacketsComponent());
        eventManager.register(new PingSpoofComponent());
        eventManager.register(new BlinkComponent());
        eventManager.register(new SpoofSlotComponent());

        LOGGER.info("Event handlers registered.");
    }

    private void initializeStartTime() {
        startTime = (int) System.currentTimeMillis();
        startTimeLong = System.currentTimeMillis();
        LOGGER.info("Start time initialized: {} ms", startTime);
    }

    private void initializeViaMCP() {
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

    private void setupSystemTray() {
        if (isWindows() && SystemTray.isSupported()) {
            try {
                Image trayImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/minecraft/moonlight/img/logo.png")));
                trayIcon = new TrayIcon(trayImage, clientName);
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip(clientName);

                SystemTray.getSystemTray().add(trayIcon);
                trayIcon.displayMessage(clientName, "Client started successfully.", TrayIcon.MessageType.INFO);

                LOGGER.info("System tray icon added.");
            } catch (IOException | AWTException | NullPointerException e) {
                LOGGER.error("Failed to create or add TrayIcon.", e);
            }
        } else {
            LOGGER.warn("System tray not supported or not running on Windows.");
        }
    }

    private void handleFastRender() {
        if (Minecraft.getMinecraft().gameSettings.ofFastRender) {
            notificationManager.post(NotificationType.WARNING, "Fast Rendering has been disabled", "due to compatibility issues");
            Minecraft.getMinecraft().gameSettings.ofFastRender = false;
            LOGGER.info("Fast Rendering was disabled due to compatibility issues.");
        }
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
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
