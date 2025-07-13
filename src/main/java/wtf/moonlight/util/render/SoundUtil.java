package wtf.moonlight.util.render;

import net.minecraft.util.ResourceLocation;
import wtf.moonlight.util.misc.Multithreading;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;

import static wtf.moonlight.util.misc.InstanceAccess.mc;

public class SoundUtil {
    public static void playSound(ResourceLocation location, float volume) {
        Multithreading.runAsync((() -> {
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(mc.getResourceManager().getResource(location).getInputStream());
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedInputStream);

                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);

                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);

                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }));
    }
}
