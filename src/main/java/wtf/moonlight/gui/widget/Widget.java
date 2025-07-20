/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package wtf.moonlight.gui.widget;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjglx.input.Mouse;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.util.misc.InstanceAccess;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.MouseUtil;
import wtf.moonlight.util.render.RoundedUtil;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;

import java.awt.*;

public abstract class Widget implements InstanceAccess {
    @Expose
    @SerializedName("name")
    public String name;
    @Expose
    @SerializedName("x")
    public float x;
    @Expose
    @SerializedName("y")
    public float y;
    protected float renderX, renderY;
    public float width;
    public float height;
    public boolean dragging;
    private int dragX, dragY;
    public int align;
    protected ScaledResolution sr;
    protected Interface setting = INSTANCE.getModuleManager().getModule(Interface.class);

    public Animation hoverAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);

    public Widget(String name) {
        this.name = name;
        this.x = 0f;
        this.y = 0f;
        this.width = 100f;
        this.height = 100f;
        this.align = WidgetAlign.LEFT | WidgetAlign.TOP;
    }

    public Widget(String name, int align) {
        this(name);
        this.align = align;
    }

    public abstract void render();

    public abstract void onShader(Shader2DEvent event);

    public abstract boolean shouldRender();

    public void updatePos() {
        sr = new ScaledResolution(mc);
        renderX = x * sr.getScaledWidth();
        renderY = y * sr.getScaledHeight();

        if (align != (WidgetAlign.LEFT | WidgetAlign.TOP)) {
            if ((align & WidgetAlign.RIGHT) != 0) {
                renderX -= width;
            } else if ((align & WidgetAlign.CENTER) != 0) {
                renderX -= width / 2f;
            }
            if ((align & WidgetAlign.BOTTOM) != 0) {
                renderY -= height;
            } else if ((align & WidgetAlign.MIDDLE) != 0) {
                renderY -= height / 2f;
            }
        }

        if (width <= sr.getScaledWidth()) {
            if (renderX < 0) {
                x = 0;
                renderX = 0;
            } else if (renderX > sr.getScaledWidth() - width) {
                x = (sr.getScaledWidth() - width) / sr.getScaledWidth();
                renderX = sr.getScaledWidth() - width;
            }
        }

        if (height <= sr.getScaledHeight()) {
            if (renderY < 0) {
                y = 0;
                renderY = 0;
            } else if (renderY > sr.getScaledHeight() - height) {
                y = (sr.getScaledHeight() - height) / sr.getScaledHeight();
                renderY = sr.getScaledHeight() - height;
            }
        }
    }

    public final void onChatGUI(int mouseX, int mouseY, boolean drag) {
        boolean hovering = MouseUtil.isHovered2(renderX, renderY, width, height, mouseX, mouseY);

        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);

        if (!hoverAnimation.isDone() || hoverAnimation.finished(Direction.FORWARDS)) {
            RoundedUtil.drawRoundOutline(renderX - 2, renderY - 4, width + 6, height + 6, 2f, 0.05f,
                    ColorUtil.applyOpacity(Color.WHITE, 0), ColorUtil.applyOpacity(Color.WHITE, (float) hoverAnimation.getOutput()));
        }

        if (hovering && Mouse.isButtonDown(0) && !dragging && drag) {
            dragging = true;
            dragX = mouseX;
            dragY = mouseY;
        }

        if (!Mouse.isButtonDown(0)) dragging = false;

        if (dragging) {
            float deltaX = (float) (mouseX - dragX) / sr.getScaledWidth();
            float deltaY = (float) (mouseY - dragY) / sr.getScaledHeight();

            x += deltaX;
            y += deltaY;

            dragX = mouseX;
            dragY = mouseY;
        }
    }
}