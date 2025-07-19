package wtf.moonlight.gui.widget.impl;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Shader2DEvent;
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
        this.x = 10;
        this.y = 10;
    }

    @Override
    public void render() {
        this.hotKeyRender(false);
    }

    @Override
    public void onShader(Shader2DEvent event) {
        this.hotKeyRender(true);
    }

    public void hotKeyRender(boolean shadow) {
        FontRenderer font = arrayListMod.getFont();
        ScaledResolution sr = new ScaledResolution(mc);

        ArrayList<Module> enabledMods = getModuleArrayList(font);
        int count = 0;
        int counts = 0;
        float hight = 13;

        boolean flip = renderX + width / 2 <= sr.getScaledWidth() / 2f;

        int renderx;
        int rendery;

        // Hot Keys
        if (arrayListMod.hotkeysDisplay.get()) {
            renderx = (int) renderX + (flip ? 18 : -23);
            rendery = (int) renderY + 24;

            if (!shadow) {
                RoundedUtil.drawRound(renderx - 15, rendery - 23, 88, 13, arrayListMod.radius.getValue().intValue(), new Color(21, 21, 21, arrayListMod.bgAlpha.getValue().intValue()));
            } else {
                RoundedUtil.drawRound(renderx - 15, rendery - 23, 88, 13, arrayListMod.radius.getValue().intValue(), Color.BLACK);
            }

            Fonts.Bold.get(16).drawStringWithShadow("HotKeys", renderx - 1, rendery - 18.5, -1);
            Fonts.Icon.get(21).drawStringWithShadow("W", renderx - 13, rendery - 18, -1);

        } else {
            renderx = (int) renderX + (flip ? 18 : -23);
            rendery = (int) renderY + 7;
        }

        for (Module module : enabledMods) {
            counts++;

            Animation moduleAnimation = module.getAnimation();
            moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
            if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;
            if (module.isHidden()) continue;

            String displayText = module.getName() + module.getTag();

            int x = flip ? (renderx + 4) : (int) (renderx + (this.width - (arrayListMod.fontMode.is("Minecraft") ? mc.fontRendererObj.getStringWidth(displayText) : font.getStringWidth(displayText))));
            int y = rendery + count + 13;

            if (flip) {
                RenderUtil.scaleStart(x, rendery + count + mc.fontRendererObj.FONT_HEIGHT, (float) moduleAnimation.getOutput());
            } else {
                RenderUtil.scaleStart(x + font.getStringWidth(displayText), rendery + count + mc.fontRendererObj.FONT_HEIGHT, (float) moduleAnimation.getOutput());
            }

            int textColor = ColorUtil.swapAlpha(setting.color(counts), 255);
            int width = arrayListMod.fontMode.is("Minecraft") ? mc.fontRendererObj.getStringWidth(displayText) + 4 : font.getStringWidth(displayText) + 6;

            // Background
            if (!shadow) {
                RoundedUtil.drawRound(x - 2, rendery + count - 4, width, hight, arrayListMod.radius.getValue().intValue(), new Color(21, 21, 21, arrayListMod.bgAlpha.getValue().intValue()));
            } else {
                RoundedUtil.drawRound(x - 2, rendery + count - 4, width, hight, arrayListMod.radius.getValue().intValue(), Color.BLACK);
            }

            int x1 = flip ? renderx - 15 : renderx + 60;

            // Icon Render
            if (!shadow) {
                RoundedUtil.drawRound(x1, rendery + count - 4, 13, hight, arrayListMod.radius.getValue().intValue(), new Color(21, 21, 21, arrayListMod.bgAlpha.getValue().intValue()));
            } else {
                RoundedUtil.drawRound(x1, rendery + count - 4, 13, hight, arrayListMod.radius.getValue().intValue(), Color.BLACK);
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
                    Fonts.Icon.get(20).drawStringWithShadow("Q", x1 + 1.8, y - hight + 1, textColor);
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
                    Fonts.Icon.get(20).drawStringWithShadow("Q", x1 + 1.8, iconY - 1, textColor);
                }
            }

            RenderUtil.scaleEnd();

            count += (int) (moduleAnimation.getOutput() * (hight * this.arrayListMod.count.getValue()));
            this.height = count;
        }
        this.width = 52;
    }

    private java.util.ArrayList<Module> getModuleArrayList(FontRenderer string) {
        Comparator<Module> sort = (m1, m2) -> {
            double ab = arrayListMod.fontMode.is("Minecraft") ? mc.fontRendererObj.getStringWidth(m1.getName() + m1.getTag()) : string.getStringWidth(m1.getName() + m1.getTag());
            double bb = arrayListMod.fontMode.is("Minecraft") ? mc.fontRendererObj.getStringWidth(m2.getName() + m2.getTag()) : string.getStringWidth(m2.getName() + m2.getTag());
            return Double.compare(bb, ab);
        };
        java.util.ArrayList<Module> enabledMods = new java.util.ArrayList<>(INSTANCE.getModuleManager().getModules());
        enabledMods.sort(sort);
        return enabledMods;
    }

    @Override
    public boolean shouldRender() {
        return Client.INSTANCE.getModuleManager().getModule(ArrayListMod.class).isEnabled() && Client.INSTANCE.getModuleManager().getModule(ArrayListMod.class).renderMod.is("Hot Key");
    }
}