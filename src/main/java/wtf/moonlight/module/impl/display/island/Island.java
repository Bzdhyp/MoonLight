package wtf.moonlight.module.impl.display.island;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import wtf.moonlight.Client;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.text.DecimalFormat;

import static wtf.moonlight.util.misc.InstanceAccess.mc;

@Getter
@Setter
public class Island {
    public String name;
    public String suffix;
    public Type type;
    public TimerUtil timerUtil = new TimerUtil();
    public long delay;
    int x,y,w,h;

    public Island(String name,String suffix,Type type,long delay,int x,int y,int w,int h){
        this.name = name;
        this.suffix = suffix;
        this.type = type;
        this.delay = delay;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    private final DecimalFormat bpsFormat = new DecimalFormat("0.00");

    public void custom(){
        int wight = Fonts.Icon.get(46).getStringWidth("Z") + Fonts.interBold.get(22).getStringWidth("Moon§fLight§r" + " | " + mc.getDebugFPS() + "fps | " + this.bpsFormat.format(getBPS()) + "bps") / 2;
        background(x - wight,y,w + wight * 2 - 4,h);
        Fonts.Icon.get(46).drawString("Z",x + 4 - wight,y + 2, Client.INSTANCE.getModuleManager().getModule(Interface.class).color());
        Fonts.interBold.get(22).drawString("Moon§fLight§r" + " | " + mc.getDebugFPS() + "fps | " + this.bpsFormat.format(getBPS()) + "bps",x + 22 - wight,y + 6, Client.INSTANCE.getModuleManager().getModule(Interface.class).color());
    }

    public static double getBPS() {
        return getBPS(mc.thePlayer);
    }

    public static double getBPS(EntityPlayer player) {
        if (player == null || player.ticksExisted < 1) {
            return 0.0;
        }
        return getDistance(player.lastTickPosX, player.lastTickPosZ) * (20.0f * mc.timer.timerSpeed);
    }

    public static double getDistance(final double x, final double z) {
        final double xSpeed = mc.thePlayer.posX - x;
        final double zSpeed = mc.thePlayer.posZ - z;
        return MathHelper.sqrt_double(xSpeed * xSpeed + zSpeed * zSpeed);
    }

    public void background(int x,int y,int w,int h){
        RoundedUtil.drawRound(x,y,w,h,8,new Color(0x121314));
    }

    public enum Type {
        Chest,
        Toggle,
        Scaffold,
        PlayerList,
        Info,
    }
}
