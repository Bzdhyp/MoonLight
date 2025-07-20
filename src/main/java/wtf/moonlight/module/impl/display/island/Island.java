package wtf.moonlight.module.impl.display.island;

import lombok.Getter;
import lombok.Setter;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.util.TimerUtil;

import java.awt.*;

@Getter
@Setter
public abstract class Island {
    public String name;
    public Type type;
    public TimerUtil timerUtil = new TimerUtil();
    public long delay;
    int x, y, width, hight;
    protected Interface setting = Client.INSTANCE.getModuleManager().getModule(Interface.class);

    public Island(String name, Type type, long delay, int x, int y, int width, int hight) {
        this.name = name;
        this.type = type;
        this.delay = delay;
        this.x = x;
        this.y = y;
        this.width = width;
        this.hight = hight;
    }

    public enum Type {
        Info,
        Scaffold,
        PlayerList,
    }

    public abstract boolean shouldRender();

    public abstract void render();

    public abstract void onShader(Shader2DEvent event);

    public int getThemeColor() {
        return setting.color();
    }

    public int getBackgroundColor() {
        return setting.bgColor();
    }
}