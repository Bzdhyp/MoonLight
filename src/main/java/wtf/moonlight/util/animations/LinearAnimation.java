package wtf.moonlight.util.animations;

public class LinearAnimation {

    private static int delta;

    public static int getDelta() {
        return delta;
    }

    public static void setDelta(int newDelta) {
        delta = newDelta;
    }

    public static float animate(float current, float target, float speed) {
        return purse(target, current, getDelta(), Math.abs(target - current) * speed);
    }

    public static float purse(float target, float current, long delta, float speed) {

        if (delta < 1L) delta = 1L;

        final float difference = current - target;

        final float smoothing = Math.max(speed * (delta / 16F), .15F);

        if (difference > speed)
            current = Math.max(current - smoothing, target);
        else if (difference < -speed)
            current = Math.min(current + smoothing, target);
        else current = target;

        return current;
    }
}
