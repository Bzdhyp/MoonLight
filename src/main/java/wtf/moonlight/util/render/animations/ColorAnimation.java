package wtf.moonlight.util.render.animations;

import java.awt.*;

public class ColorAnimation {
    private Color color;

    private float r;
    private float g;
    private float b;
    private float a;

    public ColorAnimation(Color color) {
        this.color = color;
    }

    public void animateTo(Color color, float speed) {
        this.r = LinearAnimation.animate(this.r, color.getRed(), speed);
        this.g = LinearAnimation.animate(this.g, color.getGreen(), speed);
        this.b = LinearAnimation.animate(this.b, color.getBlue(), speed);
        this.a = LinearAnimation.animate(this.a, color.getAlpha(), speed);

        this.color = new Color((int) r, (int) g, (int) b, (int) a);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
