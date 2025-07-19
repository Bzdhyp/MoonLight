package wtf.moonlight.module.impl.display;

import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.util.render.ColorUtil;

import java.awt.*;

@ModuleInfo(name = "Interface", category = Categor.Display)
public class Interface extends Module {
    public final StringValue clientName = new StringValue("Client Name", "Moonlight", this);

    public ListValue soundMode = new ListValue("Sound Mode", new String[]{"None", "Default", "Sigma", "Augustus"}, "Default", this);

    public final ListValue colorMode = new ListValue("Color Mode", new String[]{"Custom", "Fade", "Rainbow", "Astolfo", "Dynamic"}, "Dynamic", this);
    public final SliderValue speedValue = new SliderValue("Speed", 2.0f, 1.0f, 10.0f, 0.5f, this, () -> !colorMode.is("Custom"));
    public final ColorValue mainColor = new ColorValue("Main Color", new Color(128, 128, 255), this);
    private final ColorValue secondColor = new ColorValue("Second Color", new Color(128, 255, 255), this, () -> colorMode.is("Fade"));
    public final SliderValue astolfoOffsetValue = new SliderValue("Offset", 5, 0, 20, this, () -> colorMode.is("Astolfo"));
    public final SliderValue astolfoIndexValue = new SliderValue("Index", 107, 0, 200, this, () -> colorMode.is("Astolfo"));

    public final ListValue bgColor = new ListValue("Background Color, Mode", new String[]{"None", "Custom", "Dark", "White", "Synced"}, "Synced", this);
    private final ColorValue bgCustomColor = new ColorValue("Background Custom Color", new Color(32, 32, 64), this,() -> bgColor.canDisplay() && bgColor.is("Custom"));
    private final SliderValue bgAlpha = new SliderValue("Background Alpha",100,1,255,1,this);
    public final BoolValue chatCombine = new BoolValue("Chat Combine", true, this);

    public final BoolValue cape = new BoolValue("Cape", true, this);
    public final ListValue capeMode = new ListValue("Cape Mode", new String[]{"Default", "Sexy", "Sexy 2"}, "Default", this);
    public final BoolValue wavey = new BoolValue("Wavey Cape", true, this);
    public final BoolValue enchanted = new BoolValue("Enchanted", true, this, () -> cape.get() && !wavey.get());

    public Color getMainColor() {
        return mainColor.getValue();
    }

    public Color getSecondColor() {
        return secondColor.getValue();
    }

    public int bgColor() {
        return bgColor(0);
    }

    public int bgColor(int counter) {
        return bgColor(counter, bgAlpha.getValue().intValue());
    }

    public int bgColor(int counter, int alpha) {
        int colors = getMainColor().getRGB();
        colors = switch (bgColor.getValue()) {
            case "None" -> new Color(0, 0, 0, 0).getRGB();
            case "Custom" -> ColorUtil.swapAlpha(bgCustomColor.getValue().getRGB(), alpha);
            case "Dark" -> (new Color(21, 21, 21, alpha)).getRGB();
            case "White" -> ColorUtil.swapAlpha(new Color(255, 255, 255).getRGB(), alpha);
            case "Synced" -> ColorUtil.swapAlpha(color(counter), alpha);
            default -> colors;
        };
        return colors;
    }

    public int color() {
        return color(0);
    }

    public int color(int counter) {
        return color(counter, mainColor.getAlpha());
    }

    public int color(int counter, float opacity) {
        long ms = this.speedValue.getValue().longValue() * 1000L;
        float progress = (float)(System.currentTimeMillis() % ms) / ms;

        int color = -1;
        switch (colorMode.getValue()) {
            case "Custom" -> color = ColorUtil.applyOpacity(getMainColor().getRGB(), opacity);
            case "Fade" ->
                    color = ColorUtil.fadeBetween(this.getMainColor().getRGB(), this.getSecondColor().getRGB(),
                            (float)((System.currentTimeMillis() + (long)counter * 100L) % ms) / ((float)ms / 2.0f));
            case "Rainbow" -> color = ColorUtil.swapAlpha(ColorUtil.getRainbow(counter), opacity);
            case "Astolfo" -> color = ColorUtil.applyOpacity(
                    new Color(ColorUtil.astolfoRainbow(
                            (int)(counter + (progress * 100)),
                            astolfoOffsetValue.getValue().intValue(),
                            astolfoIndexValue.getValue().intValue()
                    )).getRGB(),
                    opacity
            );

            case "Dynamic" -> color = ColorUtil.fadeBetween(this.mainColor.getValue().getRGB(), ColorUtil.darker(this.getMainColor().getRGB(),
                    0.3f), (float)((System.currentTimeMillis() + (ms + (long)counter * 100L)) % ms) / ((float)ms / 2.0f));
        }

        return new Color(color, true).getRGB();
    }
}
