package wtf.moonlight.util.render.animations;

public class AnimationUtil {
    public static double animate(double target, double current, double speed) {
        boolean larger;

        larger = target > current;

        if (speed < 0.0) {
            speed = 0.0;
        } else if (speed > 1.0) {
            speed = 1.0;
        }

        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;

        if (factor < 0.1) factor = 0.1;

        current = larger ? current + factor : current - factor;
        return current;
    }

    public static float calculateCompensation(float target, float current, long delta, int speed) {
        float diff = current - target;
        if (delta < 1L) {
            delta = 1L;
        }
        if (diff > (float)speed) {
            double xD = (double)((float)((long)speed * delta) / 16.0f) < 0.25 ? 0.5 : (double)((float)((long)speed * delta) / 16.0f);
            if ((current -= (float)xD) < target) {
                current = target;
            }
        } else if (diff < (float)(-speed)) {
            double xD = (double)((float)((long)speed * delta) / 16.0f) < 0.25 ? 0.5 : (double)((float)((long)speed * delta) / 16.0f);
            if ((current += (float)xD) > target) {
                current = target;
            }
        } else {
            current = target;
        }
        return current;
    }
}
