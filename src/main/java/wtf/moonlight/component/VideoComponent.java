package wtf.moonlight.component;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import wtf.moonlight.Client;
import wtf.moonlight.gui.main.video.VideoPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public final class VideoComponent {
    private static final String DEFAULT_VIDEO = "background.mp4";
    private static File currentVideoFile;

    public static void ensureVideoExists() {
        currentVideoFile = new File(Minecraft.getMinecraft().mcDataDir, DEFAULT_VIDEO);
        if (!currentVideoFile.exists()) {
            unpackFile(currentVideoFile);
        }
    }

    public static void startVideoPlayback() {
        try {
            if (currentVideoFile != null && currentVideoFile.exists()) {
                VideoPlayer.init(currentVideoFile);
            }
        } catch (Exception e) {
            Client.LOGGER.error("Failed to start video playback", e);
        }
    }

    @SneakyThrows
    private static void unpackFile(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            IOUtils.copy(Objects.requireNonNull(
                    VideoComponent.class.getClassLoader().getResourceAsStream("assets/minecraft/moonlight/background.mp4")), fos);
        }
    }
}