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
package wtf.moonlight.gui.notification;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.util.misc.InstanceAccess;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.Translate;
import wtf.moonlight.util.render.animations.advanced.impl.EaseOutSine;
import wtf.moonlight.util.TimerUtil;


@Getter
public class Notification implements InstanceAccess {

    private final NotificationType notificationType;
    private final String title, description;
    private final float time;
    private final TimerUtil timerUtil;
    private final Animation animation;
    private final Translate translate;

    public Notification(NotificationType type, String title, String description) {
        this(type, title, description, NotificationManager.toggleTime);
    }

    public Notification(NotificationType type, String title, String description, float time) {
        this.title = title;
        this.description = description;
        this.time = (long) (time * 1000);
        timerUtil = new TimerUtil();
        this.notificationType = type;
        this.animation = new EaseOutSine(250, 1);
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.translate = new Translate(sr.getScaledWidth() - this.getWidth(), sr.getScaledHeight() - getHeight());
    }
    
    public double getWidth(){
        return switch (INSTANCE.getModuleManager().getModule(Interface.class).notificationMode.getValue()) {
            case "Default" -> Fonts.interMedium.get(15).getStringWidth(getDescription()) + 5;
            case "Test" ->
                    Math.max(Fonts.interSemiBold.get(15).getStringWidth(getTitle()), Fonts.interSemiBold.get(15).getStringWidth(getDescription())) + 5;
            case "Test2" ->
                    Math.max(Fonts.interSemiBold.get(17).getStringWidth(getTitle()), Fonts.interRegular.get(17).getStringWidth(getDescription())) + 10;
            case "Exhi" ->
                    Math.max(100.0f, Math.max(Fonts.interRegular.get(18).getStringWidth(getTitle()) + 2, Fonts.interRegular.get(14).getStringWidth(getDescription())) + 24.0f);
            case "Type 2" ->
                    Math.max(100.0f, Math.max(Fonts.interRegular.get(18).getStringWidth(getTitle()), Fonts.interRegular.get(14).getStringWidth(getDescription())) + 70);
            case "Type 3" ->
                    Math.max(Fonts.interRegular.get(22).getStringWidth(getTitle()), Fonts.interRegular.get(20).getStringWidth(getDescription())) + 50;
            case "Augustus" ->
                    Math.max(Fonts.esp.get(15).getStringWidth(getTitle()), Fonts.esp.get(15).getStringWidth(getDescription())) + 23;
            case "Type 4" ->
                    Math.max(140, Math.max(Fonts.interRegular.get(10).getStringWidth(getTitle()), Fonts.interRegular.get(6).getStringWidth(getDescription())) + 40);
            case "Augustus 2" ->
                    Math.max(mc.fontRendererObj.getStringWidth(getTitle()), mc.fontRendererObj.getStringWidth(getDescription())) + 10;
            default -> 0;
        };
    }

    public double getHeight(){
        return switch (INSTANCE.getModuleManager().getModule(Interface.class).notificationMode.getValue()) {
            case "Default" -> 16;
            case "Test" -> Fonts.interRegular.get(15).getHeight() * 2 + 2;
            case "Test2", "Augustus 2" -> 33;
            case "Exhi" -> 26;
            case "Type 2" -> 30;
            case "Type 3" -> 35;
            case "Augustus" -> 29;
            case "Type 4" -> 27f;
            case "Type 5" -> 24;
            default -> 0;
        };
    }
}