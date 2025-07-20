package wtf.moonlight.module.impl.display;

import com.cubk.EventTarget;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.animations.Translate;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

@ModuleInfo(name = "Arraylist", category = Categor.Display)
public class ArrayListMod extends Module {
    public final ListValue renderMod = new ListValue("Styles", new String[]{"Normal", "Hot Key"}, "Normal", this);
    public final ListValue fontMode = new ListValue("Fonts Mode", new String[]{"Minecraft", "Bold", "SFUI", "Medium", "Tahoma", "Regular", "Semi Bold"}, "Semi Bold", this);
    public final SliderValue fontSize = new SliderValue("Font Size", 15, 10, 25, this,() -> !fontMode.is("Minecraft"));

    public final BoolValue importantModules = new BoolValue("Important", false, this);

    public final ListValue iconStyle = new ListValue("Icon Style", new String[]{"Categor", "Toggle"}, "Categor", this, () -> renderMod.is("Hot Key"));
    public final ListValue bgColor = new ListValue("Back Ground C", new String[]{"Dark", "Synced"}, "Dark", this, () -> renderMod.is("Hot Key"));

    public final BoolValue hotkeysDisplay = new BoolValue("HotKeys Display", false, this, () -> renderMod.is("Hot Key"));
    public SliderValue count = new SliderValue("Array Count", 1.4f, 1.4f, 2.5f, 0.1f, this, () -> renderMod.is("Hot Key"));
    public SliderValue radius = new SliderValue("Radius", 3, 0, 6, 0.1f, this, () -> renderMod.is("Hot Key"));

    public final SliderValue positionOffset = new SliderValue("Position", 0, 0, 50, this, () -> renderMod.is("Normal"));
    public final SliderValue textHeight = new SliderValue("Text Height", 4, 0, 10, this, () -> renderMod.is("Normal"));

    public final ListValue animation = new ListValue("Animation", new String[]{"Scale In", "Move In", "Slide In"}, "Scale In", this, () -> renderMod.is("Normal"));
    public final ListValue tags = new ListValue("Suffix", new String[]{"None", "Simple", "Bracket", "Dash"}, "Simple", this);
    public final ListValue rectangleValue = new ListValue("Rectangle", new String[]{"None", "Top", "Side"}, "Top", this, () -> renderMod.is("Normal"));
    public final BoolValue backgroundValue = new BoolValue("Back Ground", true, this, () -> renderMod.is("Normal"));
    public final SliderValue bgAlpha = new SliderValue("Back Ground Alpha", 100, 1, 255, this, () -> backgroundValue.get() || renderMod.is("Normal") || bgColor.is("Dark"));

    public ArrayListMod() {
        setEnabled(true);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);

        this.moduleList(false, sr);
    }

    @EventTarget
    public void onShader(Shader2DEvent event) {
        if (!backgroundValue.get()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        this.moduleList(true, sr);
    }

    public void moduleList(boolean shadow, ScaledResolution sr) {
        if (renderMod.is("Hot Key")) return;

        int count = 1;
        float fontHeight = textHeight.getValue();
        float yValue = 1 + positionOffset.getValue();

        FontRenderer font = getFont();
        int screenWidth = sr.getScaledWidth();

        boolean customFontMode = !fontMode.is("Minecraft");

        Comparator<Module> sort = (m1, m2) -> {
            double ab = customFontMode ? getFont().getStringWidth(m1.getName() + m1.getTag()) : mc.fontRendererObj.getStringWidth(m1.getName() + m1.getTag());
            double bb = customFontMode ? getFont().getStringWidth(m2.getName() + m2.getTag()) : mc.fontRendererObj.getStringWidth(m2.getName() + m2.getTag());
            return Double.compare(bb, ab);
        };

        ArrayList<Module> enabledMods = new ArrayList<>(Client.INSTANCE.getModuleManager().getModules());
        if (animation.is("Slide In")) {
            enabledMods.sort(sort);
            for (Module module : enabledMods) {
                if (module.isHidden()) continue;

                if (importantModules.get()){
                    if (module.getCategory() == Categor.Visual) continue;
                    if (module.getCategory() == Categor.Display) continue;
                }

                Translate translate = module.getTranslate();
                float moduleWidth = customFontMode ? getFont().getStringWidth(module.getName() + module.getTag()) : mc.fontRendererObj.getStringWidth(module.getName() + module.getTag());

                if (module.isEnabled() && !module.isHidden()) {
                    translate.translate((screenWidth - moduleWidth - 1.0f - positionOffset.getValue()), yValue);
                    yValue += (customFontMode ? getFont().getHeight() : mc.fontRendererObj.FONT_HEIGHT) + fontHeight;
                } else {
                    translate.animate((screenWidth - 1) + positionOffset.getValue(), -25.0);
                }

                if (translate.getX() >= screenWidth) {
                    continue;
                }

                float leftSide = (float) (translate.getX() - 2f);
                float bottom = (customFontMode ? font.getHeight() : mc.fontRendererObj.FONT_HEIGHT) + fontHeight;

                float textYOffset = (bottom - (customFontMode ? font.getHeight() - 5f : mc.fontRendererObj.FONT_HEIGHT)) / 2.0f;

                if (backgroundValue.get()) {
                    if (!shadow) {
                        RenderUtil.drawRect(leftSide, (float) translate.getY(), moduleWidth + 3, bottom, new Color(21, 21, 21, bgAlpha.getValue().intValue()).getRGB());
                    } else {
                        RenderUtil.drawRect(leftSide, (float) translate.getY(), moduleWidth + 3, bottom, Color.BLACK.getRGB());
                    }
                }

                if (!shadow) {
                    switch (rectangleValue.getValue()) {
                        case "Top":
                            if (count == 1) {
                                Gui.drawRect2(translate.getX() - 2, translate.getY(), moduleWidth + 3, 1, getModule(Interface.class).color(count));
                            }
                            break;
                        case "Side":
                            Gui.drawRect2(translate.getX() + moduleWidth + 0.1, translate.getY() + 0.2, 1, bottom, getModule(Interface.class).color(count));
                            break;
                    }
                } else {
                    switch (rectangleValue.getValue()) {
                        case "Top":
                            if (count == 1) {
                                Gui.drawRect2(translate.getX() - 2, translate.getY(), moduleWidth + 3, 1, Color.BLACK.getRGB());
                            }
                            break;
                        case "Side":
                            Gui.drawRect2(translate.getX() + moduleWidth + 0.1, translate.getY(), 1, bottom, Color.BLACK.getRGB());
                            break;
                    }
                }

                if (customFontMode) {
                    float xOffset = -1f;

                    switch (fontMode.getValue()) {
                        case "Medium", "Semi Bold": {
                            xOffset = -0.8f;
                            break;
                        }
                    }

                    font.drawStringWithShadow(module.getName() + module.getTag(),
                            (float) translate.getX() + xOffset,
                            (float) translate.getY() + textYOffset,
                            getModule(Interface.class).color(count));
                } else {
                    mc.fontRendererObj.drawStringWithShadow(module.getName() + module.getTag(),
                            (float) translate.getX() - 0.7f,
                            (float) translate.getY() + textYOffset,
                            getModule(Interface.class).color(count));
                }

                count -= 1;
            }
        }

        if (!animation.is("Slide In")) {
            enabledMods.sort(sort);
            for (Module module : enabledMods) {
                if (module.isHidden()) continue;

                if (importantModules.get()){
                    if (module.getCategory() == Categor.Visual) continue;
                    if (module.getCategory() == Categor.Display) continue;
                }

                Animation moduleAnimation = module.getAnimation();
                moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;
                float moduleWidth = customFontMode ? font.getStringWidth(module.getName() + module.getTag()) : mc.fontRendererObj.getStringWidth(module.getName() + module.getTag());
                float xValue = (screenWidth - moduleWidth - 1.0f - positionOffset.getValue());

                float alphaAnimation = 1.0f;

                switch (animation.getValue()) {
                    case "Move In":
                        xValue += (float) Math.abs((moduleAnimation.getOutput() - 1.0) * (2.0 + moduleWidth));
                        break;
                    case "Scale In":
                        RenderUtil.scaleStart(xValue + (moduleWidth / 2.0f), yValue + mc.fontRendererObj.FONT_HEIGHT, (float) moduleAnimation.getOutput());
                        alphaAnimation = (float) moduleAnimation.getOutput();
                        break;
                }

                float leftSide = xValue - 2f;
                float bottom = (customFontMode ? font.getHeight() : mc.fontRendererObj.FONT_HEIGHT) + fontHeight;
                float textYOffset = (bottom - (customFontMode ? font.getHeight() - 4f : mc.fontRendererObj.FONT_HEIGHT)) / 2.0f;

                int textcolor = ColorUtil.swapAlpha(getModule(Interface.class).color(count), alphaAnimation * 255);

                if (backgroundValue.get()) {
                    if (!shadow) {
                        RenderUtil.drawRect(leftSide - 1, yValue, moduleWidth + 3, bottom, new Color(21, 21, 21, bgAlpha.getValue().intValue()).getRGB());
                    } else {
                        RenderUtil.drawRect(leftSide - 1, yValue, moduleWidth + 3, bottom, Color.BLACK.getRGB());
                    }
                }

                if (!shadow) {
                    switch (rectangleValue.getValue()) {
                        case "Top":
                            if (count == 1) {
                                Gui.drawRect2(xValue - 3, yValue, moduleWidth + 3, 1, textcolor);
                            }
                            break;
                        case "Side":
                            Gui.drawRect2(xValue + moduleWidth + 0.2, yValue, 1, bottom, textcolor);
                            break;
                    }
                } else {
                    switch (rectangleValue.getValue()) {
                        case "Top":
                            if (count == 1) {
                                Gui.drawRect2(xValue - 3, yValue, moduleWidth + 3, 1, Color.BLACK.getRGB());
                            }
                            break;
                        case "Side":
                            Gui.drawRect2(xValue + moduleWidth + 0.2, yValue, 1, bottom, Color.BLACK.getRGB());
                            break;
                    }
                }

                if (customFontMode) {
                    float xOffset = -2.0f;
                    switch (fontMode.getValue()) {
                        case "Medium", "Semi Bold": {
                            xOffset = -2.1f;
                            break;
                        }
                    }

                    font.drawStringWithShadow(module.getName() + module.getTag(),
                            xValue + xOffset,
                            yValue + textYOffset,
                            textcolor);
                } else {
                    mc.fontRendererObj.drawStringWithShadow(module.getName() + module.getTag(),
                            xValue - 0.8f,
                            yValue + textYOffset,
                            textcolor);
                }

                if (animation.is("Scale In")) {
                    RenderUtil.scaleEnd();
                }

                yValue += (float) (moduleAnimation.getOutput() * ((customFontMode ? font.getHeight() : mc.fontRendererObj.FONT_HEIGHT) + fontHeight));
                count -= 2;
            }
        }
    }

    public FontRenderer getFont() {
        return switch (fontMode.getValue()) {
            case "Bold" -> Fonts.interBold.get(fontSize.getValue());
            case "SFUI" -> Fonts.sfui.get(fontSize.getValue());
            case "Medium" -> Fonts.interMedium.get(fontSize.getValue());
            case "Tahoma" -> Fonts.Tahoma.get(fontSize.getValue());
            case "Regular" -> Fonts.interRegular.get(fontSize.getValue());
            default -> Fonts.interSemiBold.get(fontSize.getValue());
        };
    }
}