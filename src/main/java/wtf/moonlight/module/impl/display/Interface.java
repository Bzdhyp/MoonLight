package wtf.moonlight.module.impl.display;

import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.util.render.ColorUtil;

import java.awt.*;

@ModuleInfo(name = "Interface", category = Categor.Display)
public class Interface extends Module {
    public final StringValue clientName = new StringValue("Client Name", "Moonlight", this);

    public ListValue soundMode = new ListValue("Sound Mode", new String[]{"None", "Default", "Sigma", "Augustus"}, "Default", this);

    public final BoolValue cFont = new BoolValue("C Fonts",true,this);
    public final ListValue fontMode = new ListValue("C Fonts Mode", new String[]{"WQY", "Bold", "SFUI", "Medium", "Tahoma", "Regular", "Semi Bold"}, "Semi Bold", this,() -> cFont.canDisplay() && cFont.get());
    public final SliderValue fontSize = new SliderValue("Font Size",15,10,25,this,cFont::get);

    public final ListValue color = new ListValue("Color Mode", new String[]{"Custom", "Fade", "White", "Astolfo", "Rainbow", "Dynamic"}, "White", this);
    public final SliderValue speedValue = new SliderValue("Speed", 2.0f, 1.0f, 10.0f, 0.5f, this, () -> color.is("Fade") || color.is("Rainbow") || color.is("Dynamic"));
    public final ColorValue mainColor = new ColorValue("Main Color", new Color(128, 128, 255), this,() -> !color.is("White"));
    private final ColorValue secondColor = new ColorValue("Second Color", new Color(4, 179, 232), this, () -> color.is("Dynamic"));
    public final SliderValue astolfoSaturationValue = new SliderValue("Saturation", 0.75f, 0.35f, 1.0f, 0.1f, this, () -> color.is("Astolfo"));
    public final SliderValue astolfoBrightnessValue = new SliderValue("Brightness", 0.85f, 0.35f, 1.0f, 0.1f, this, () -> color.is("Astolfo"));

    public final BoolValue background = new BoolValue("Background",true,this);
    public final ListValue bgColor = new ListValue("Background Color", new String[]{"None", "Custom", "Dark", "White", "Synced"}, "Synced", this,background::get);
    private final ColorValue bgCustomColor = new ColorValue("Background Custom Color", new Color(32, 32, 64), this,() -> bgColor.canDisplay() && bgColor.is("Custom"));
    private final SliderValue bgAlpha = new SliderValue("Background Alpha",100,1,255,1,this);
    public final BoolValue chatCombine = new BoolValue("Chat Combine", true, this);

    public final BoolValue cape = new BoolValue("Cape", true, this);
    public final ListValue capeMode = new ListValue("Cape Mode", new String[]{"Default", "Sexy", "Sexy 2"}, "Default", this);
    public final BoolValue wavey = new BoolValue("Wavey Cape", true, this);
    public final BoolValue enchanted = new BoolValue("Enchanted", true, this, () -> cape.get() && !wavey.get());
    float xyz = 0.0f;

    public FontRenderer getFr() {
        return switch (fontMode.getValue()) {
            case "WQY" -> Fonts.wqy.get(fontSize.getValue());
            case "Bold" -> Fonts.interBold.get(fontSize.getValue());
            case "SFUI" -> Fonts.sfui.get(fontSize.getValue());
            case "Medium" -> Fonts.interMedium.get(fontSize.getValue());
            case "Tahoma" -> Fonts.Tahoma.get(fontSize.getValue());
            case "Regular" -> Fonts.interRegular.get(fontSize.getValue());
            case "Semi Bold" -> Fonts.interSemiBold.get(fontSize.getValue());
            default -> null;
        };
    }

    public Color getMainColor() {
        return mainColor.getValue();
    }

    public Color getSecondColor() {
        return secondColor.getValue();
    }

    public int color() {
        return color(0);
    }

    public int color(int counter) {
        return color(counter, getMainColor().getAlpha());
    }

    public int color(int counter, int alpha) {
        this.xyz = (float) ((double) this.xyz + this.speedValue.getValue() / 45.0);
        if (this.xyz > 255.0f) this.xyz = 0.0f;

        float qwerty = this.xyz + counter;
        int colors = getMainColor().getRGB();
        long ms = this.speedValue.getValue().longValue() * 1000L;

        if (qwerty > 255.0f) {
            qwerty = qwerty % 255.0f;
        }

        colors = switch (color.getValue()) {
            case "Custom" -> ColorUtil.swapAlpha(mainColor.getValue().getRGB(), alpha);
            case "Fade" ->
                    ColorUtil.swapAlpha(ColorUtil.fadeBetween(colors, this.getSecondColor().getRGB(), (float)((System.currentTimeMillis() + ms) % ms) / ((float)ms / 2.0f)), alpha);
            case "White" -> ColorUtil.swapAlpha(new Color(255, 255, 255).getRGB(), alpha);
            case "Astolfo" ->
                    ColorUtil.swapAlpha(ColorUtil.astolfoRainbow(0, this.astolfoSaturationValue.getValue(), this.astolfoBrightnessValue.getValue()), alpha);
            case "Rainbow" -> ColorUtil.swapAlpha(Color.getHSBColor(qwerty / 255.0f, 0.55f, 0.9f).getRGB(), alpha);
            case "Dynamic" -> ColorUtil.swapAlpha(ColorUtil.fade(
                    mainColor.get().getRGB(),
                    ColorUtil.darker(mainColor.get().getRGB(), 0.3f),
                    (float) ((System.currentTimeMillis() + (ms + (long) counter * 100L)) % ms) / ((float) ms / 2.0f)), alpha);
            default -> colors;
        };
        return new Color(colors,true).getRGB();
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
            case "Synced" ->
                    new Color(ColorUtil.applyOpacity(color(counter, alpha), alpha / 255f), true).darker().darker().getRGB();
            default -> colors;
        };
        return colors;
    }
}
