package wtf.moonlight.gui.widget.impl;

import net.minecraft.client.gui.ScaledResolution;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.click.neverlose.NeverLose;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.gui.widget.Widget;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.impl.display.ArrayListMod;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class HotKeyWidget extends Widget {
    ArrayListMod arrayListMod = Client.INSTANCE.getModuleManager().getModule(ArrayListMod.class);

    public HotKeyWidget() {
        super("HotKeys");
        this.x = 0.2f;
        this.y = 0.5f;
    }

    @Override
    public boolean shouldRender() {
        return Client.INSTANCE.getModuleManager().getModule(ArrayListMod.class).isEnabled() && Client.INSTANCE.getModuleManager().getModule(ArrayListMod.class).renderMod.is("Hot Key");
    }

    @Override
    public void render() {
        this.hotKeyRender(false);
    }

    @Override
    public void onShader(Shader2DEvent event) {
        this.hotKeyRender(true);
    }

    private ArrayList<Module> getModuleArrayList(FontRenderer string) {
        Comparator<Module> sort = (m1, m2) -> {
            double ab = arrayListMod.fontMode.is("Minecraft") ? mc.fontRendererObj.getStringWidth(m1.getName() + m1.getTag()) : string.getStringWidth(m1.getName() + m1.getTag());
            double bb = arrayListMod.fontMode.is("Minecraft") ? mc.fontRendererObj.getStringWidth(m2.getName() + m2.getTag()) : string.getStringWidth(m2.getName() + m2.getTag());
            return Double.compare(bb, ab);
        };

        ArrayList<Module> enabledMods = new ArrayList<>(INSTANCE.getModuleManager().getModules());

        enabledMods.sort(sort);
        return enabledMods;
    }

    public void hotKeyRender(boolean shadow) {
        FontRenderer font = arrayListMod.getFont();
        ScaledResolution sr = new ScaledResolution(mc);

        int count = 0;
        int counts = 0;
        float hight = 13;

        boolean flip = renderX <= sr.getScaledWidth() / 2f;

        int renderx;
        int rendery;

        // Hot Keys
        if (arrayListMod.hotkeysDisplay.get()) {
            renderx = (int) renderX + (flip ? 60 : 17);
            rendery = (int) renderY + 24;

            int xOffset;
            if (flip) {
                xOffset = - 43;
            } else {
                xOffset = 0;
            }

            if (!shadow) {
                RoundedUtil.drawRound(renderx - 15 + xOffset, rendery - 23, 88, 13, arrayListMod.radius.getValue().intValue(),
                        arrayListMod.bgColor.is("Dark") ? new Color(21, 21, 21, arrayListMod.bgAlpha.getValue().intValue()) : new Color(setting.bgColor(counts)));
            } else {
                RoundedUtil.drawRound(renderx - 15 + xOffset, rendery - 23, 88, 13, arrayListMod.radius.getValue().intValue(),
                        arrayListMod.bgColor.is("Dark") ? Color.BLACK : new Color(setting.bgColor(counts)));
            }

            Fonts.interSemiBold.get(16).drawStringWithShadow("HotKeys", renderx - 1 + xOffset, rendery - 18.5, -1);
            Fonts.Icon.get(22).drawStringWithShadow("W", renderx - 14 + xOffset, rendery - 18.5, NeverLose.iconRGB);
        } else {
            renderx = (int) renderX + 60;
            rendery = (int) renderY + 6;
        }

        ArrayList<Module> enabledMods = getModuleArrayList(font);
        for (Module module : enabledMods) {
            counts++;

            if (arrayListMod.importantModules.get()){
                if (module.getCategory() == Categor.Visual) continue;
                if (module.getCategory() == Categor.Display) continue;
            }

            Animation moduleAnimation = module.getAnimation();
            moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
            if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;
            if (module.isHidden()) continue;

            String displayText = module.getName() + module.getTag();

            int x;
            if (flip) {
                x = renderx - 38;
            } else {
                int textWidth = arrayListMod.fontMode.is("Minecraft") ? mc.fontRendererObj.getStringWidth(displayText) : font.getStringWidth(displayText);
                x = renderx - textWidth + 52;
            }

            int y = rendery + count + 13;

            if (flip) {
                RenderUtil.scaleStart(x, rendery + count + mc.fontRendererObj.FONT_HEIGHT, (float) moduleAnimation.getOutput());
            } else {
                RenderUtil.scaleStart(x + (arrayListMod.fontMode.is("Minecraft") ?
                                mc.fontRendererObj.getStringWidth(displayText) :
                                font.getStringWidth(displayText)),
                        rendery + count + mc.fontRendererObj.FONT_HEIGHT,
                        (float) moduleAnimation.getOutput());
            }

            int textColor = ColorUtil.swapAlpha(setting.color(counts), 255);
            int width = arrayListMod.fontMode.is("Minecraft") ?
                    mc.fontRendererObj.getStringWidth(displayText) + 4 :
                    font.getStringWidth(displayText) + 6;

            // Background
            if (!shadow) {
                RoundedUtil.drawRound(x - 2, rendery + count - 4, width, hight, arrayListMod.radius.getValue().intValue(),
                        arrayListMod.bgColor.is("Dark") ? new Color(21, 21, 21, arrayListMod.bgAlpha.getValue().intValue()) : new Color(setting.bgColor(counts)));
            } else {
                RoundedUtil.drawRound(x - 2, rendery + count - 4, width, hight, arrayListMod.radius.getValue().intValue(),
                        arrayListMod.bgColor.is("Dark") ? Color.BLACK : new Color(setting.bgColor(counts)));
            }

            int x1 = flip ? renderx - 58 : x + width + 2;

            // Icon Render
            if (!shadow) {
                RoundedUtil.drawRound(x1, rendery + count - 4, 13, hight, arrayListMod.radius.getValue().intValue(),
                        arrayListMod.bgColor.is("Dark") ? new Color(21, 21, 21, arrayListMod.bgAlpha.getValue().intValue()) : new Color(setting.bgColor(counts)));
            } else {
                RoundedUtil.drawRound(x1, rendery + count - 4, 13, hight, arrayListMod.radius.getValue().intValue(),
                        arrayListMod.bgColor.is("Dark") ? Color.BLACK : new Color(setting.bgColor(counts)));
            }

            Categor category = module.getCategory();
            String icon = category != null ? category.getIcon() : "A";

            float xOffset = switch (icon.charAt(0)) {
                case 'B' -> 2.95f;
                case 'G' -> 1.0f;
                default -> 2.0f;
            };

            if (arrayListMod.fontMode.is("Minecraft")) {
                mc.fontRendererObj.drawStringWithShadow(displayText, x + 1, y - hight - 1, textColor);

                if (arrayListMod.iconStyle.is("Categor")) {
                    Fonts.Icon.get(20).drawStringWithShadow(icon, x1 + xOffset, y - hight + 1, textColor);
                } else {
                    Fonts.Icon.get(20).drawStringWithShadow("X", x1 + 1.8, y - hight + 1, textColor);
                }
            } else {
                float yOffset = 2 + switch (arrayListMod.fontSize.getValue().intValue()) {
                    case 10, 11, 12 -> 0;
                    case 13 -> 1;
                    case 21, 22, 23, 24, 25 -> 4;
                    default -> 2;
                };

                float textY = y - hight + (hight - arrayListMod.fontSize.getValue() + yOffset) / 2;

                font.drawStringWithShadow(displayText, x + 1, textY, textColor);

                float iconY = y - hight + (hight - 10) / 2;

                if (arrayListMod.iconStyle.is("Categor")) {
                    Fonts.Icon.get(20).drawStringWithShadow(icon, x1 + xOffset, iconY, textColor);
                } else {
                    Fonts.Icon.get(20).drawStringWithShadow("X", x1 + 1.8, iconY - 1, textColor);
                }
            }

            RenderUtil.scaleEnd();

            count += (int) (moduleAnimation.getOutput() * (hight * this.arrayListMod.count.getValue()));

            if (arrayListMod.hotkeysDisplay.get()) {
                this.width = 90;
                this.height = 15;
            } else {
                this.width = font.getStringWidth(displayText) + 112;
                this.height = count;
            }
        }
    }
}