package wtf.moonlight.component;

import lombok.Setter;
import wtf.moonlight.util.misc.InstanceAccess;

public class FreeLookComponent implements InstanceAccess {
    public static float cameraYaw;
    public static float cameraPitch;
    @Setter
    public static boolean freelooking;

    public static void overrideMouse(float f3, float f4) {
        cameraYaw += f3 * 0.15f;
        cameraPitch -= f4 * 0.15f;
        cameraPitch = Math.max(-90.0f, Math.min(90.0f, cameraPitch));
    }

    public static float getYaw() {
        return freelooking ? cameraYaw : mc.thePlayer.rotationYaw;
    }

    public static float getPitch() {
        return freelooking ? cameraPitch : mc.thePlayer.rotationPitch;
    }

    public float getPrevYaw() {
        return freelooking  ? cameraYaw : mc.thePlayer.prevRotationYaw;
    }

    public float getPrevPitch() {
        return freelooking ? cameraPitch : mc.thePlayer.prevRotationPitch;
    }

    public static void enable() {
        setFreelooking(true);
        cameraYaw = mc.thePlayer.rotationYaw;
        cameraPitch = mc.thePlayer.rotationPitch;
    }

    public static void disable() {
        setFreelooking(false);
        cameraYaw = mc.thePlayer.rotationYaw;
        cameraPitch = mc.thePlayer.rotationPitch;
    }
}
