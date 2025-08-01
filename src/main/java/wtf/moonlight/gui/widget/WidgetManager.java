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

import org.lwjglx.opengl.Display;
import com.cubk.EventTarget;
import wtf.moonlight.events.render.ChatGUIEvent;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.widget.impl.*;
import wtf.moonlight.util.misc.InstanceAccess;
import wtf.moonlight.util.render.animations.advanced.Direction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WidgetManager implements InstanceAccess {
    public List<Widget> widgetList = new ArrayList<>();

    public WidgetManager() {
        INSTANCE.getEventManager().register(this);

        register(new HotKeyWidget());
        register(new KeyBindWidget());
        register(new PlayerListWidget());
        register(new WatermarkWidget());
        register(new TargetHUDWidget());
        register(new PotionHUDWidget());
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mc.gameSettings.showDebugInfo) return;

        if (Display.isVisible()) {
            for (Widget widget : widgetList) {
                if (widget.shouldRender()) {
                    widget.updatePos();
                    widget.render();
                }
            }
        }
    }

    @EventTarget
    public void onShader2D(Shader2DEvent event) {
        if (mc.gameSettings.showDebugInfo) return;

        if (Display.isVisible()) {
            for (Widget widget : widgetList) {
                if (widget.shouldRender()) {
                    widget.onShader(event);
                }
            }
        }
    }

    @EventTarget
    public void onChatGUI(ChatGUIEvent event) {
        if (Display.isVisible()) {
            Widget draggingWidget = null;
            for (Widget widget : widgetList) {
                if (widget.shouldRender() && widget.dragging) {
                    draggingWidget = widget;
                    break;
                }
            }

            for (Widget widget : widgetList) {
                if (widget.shouldRender()) {
                    if (!widget.hoverAnimation.getDirection().equals(Direction.BACKWARDS)) {
                        widget.hoverAnimation.setDirection(Direction.BACKWARDS);
                    }

                    widget.onChatGUI(event.mouseX, event.mouseY, (draggingWidget == null || draggingWidget == widget));
                    if (widget.dragging) draggingWidget = widget;
                }
            }
        }
    }

    private void register(Widget widget) {
        this.widgetList.add(widget);
    }

    public Widget get(String name) {
        for (Widget widget : widgetList) {
            if (widget.name.equalsIgnoreCase(name)) {
                return widget;
            }
        }
        return null;
    }

    public <module extends Widget> module get(Class<? extends module> moduleClass) {
        Iterator<Widget> var2 = this.widgetList.iterator();
        Widget module;
        do {
            if (!var2.hasNext()) {
                return null;
            }
            module = var2.next();
        } while (module.getClass() != moduleClass);

        return (module) module;
    }
}