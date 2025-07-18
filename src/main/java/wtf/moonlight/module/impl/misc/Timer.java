package wtf.moonlight.module.impl.misc;

import com.cubk.EventTarget;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import wtf.moonlight.component.BadPacketsComponent;
import wtf.moonlight.component.PingSpoofComponent;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MathUtil;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.render.animations.advanced.ContinualAnimation;
import wtf.moonlight.util.packet.*;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Timer", category = Categor.Misc)
public class Timer extends Module {
    public final ListValue mode = new ListValue("Mode", new String[]{"Vanilla", "Balance", "Hypixel Cancel"}, "Vanilla", this);
    public final SliderValue speed = new SliderValue("Speed", 1, 0.1f, 5, 0.05f, this);
    public final SliderValue balanceValue = new SliderValue("Balance", 3000, 0, 10000, 10, this, () -> mode.is("Balance"));
    public final BoolValue onlyWhenStill = new BoolValue("Only Still", false, this, () -> mode.is("Balance"));
    public boolean blinked;
    private double balance = 0;
    private boolean speeding = false;
    private final ContinualAnimation animation = new ContinualAnimation();
    private final TimerUtil timerUtil = new TimerUtil();
    private final TimerUtil hypixelTimer = new TimerUtil();
    private final CopyOnWriteArrayList<Packet> watchdogC0FC00Packets = new CopyOnWriteArrayList<>();

    @Override
    public void onEnable() {
        blinked = false;
        balance = 0;
        timerUtil.reset();
        speeding = false;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mode.is("Balance")) {
            PingSpoofComponent.spoof(14000, true, false, false, false);
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(mode.getValue() + " " + MathUtil.roundToHalf(speed.getValue()));

        switch (mode.getValue()) {
            case "Vanilla":
            case "Hypixel Cancel":
                mc.timer.timerSpeed = speed.getValue();
                break;
            case "Balance":
                if (event.isPre()) {
                    if (balance > balanceValue.getValue()) {
                        mc.timer.timerSpeed = speed.getValue();
                        speeding = true;
                    }
                    if (speeding && balance <= 0) {
                        reset();
                    }
                }
                break;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (mode.is("Balance")) {
            if (packet instanceof C03PacketPlayer c03PacketPlayer) {

                if (!c03PacketPlayer.getRotating() && !c03PacketPlayer.isMoving() && !BadPacketsComponent.bad() &&
                        (!onlyWhenStill.get() || (mc.thePlayer.posX == mc.thePlayer.lastTickPosX && mc.thePlayer.posY == mc.thePlayer.lastTickPosY && mc.thePlayer.posZ == mc.thePlayer.lastTickPosZ))) {
                    event.setCancelled(true);
                }

                if (!event.isCancelled()) {
                    this.balance -= 50;
                }

                this.balance += timerUtil.getTime();
                this.timerUtil.reset();
            }
        }

        if (mode.is("Hypixel Cancel")) {
            if (packet instanceof C03PacketPlayer c03) {
                if (!c03.isMoving() && !c03.getRotating()) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (!hypixelTimer.hasTimeElapsed(20 * speed.getValue() * 10)) {
                if (packet instanceof C0FPacketConfirmTransaction || packet instanceof C00PacketKeepAlive) {
                    event.setCancelled(true);
                    watchdogC0FC00Packets.add(packet);
                }
            } else if (!watchdogC0FC00Packets.isEmpty()) {
                watchdogC0FC00Packets.forEach(PacketUtils::sendPacketNoEvent);
                watchdogC0FC00Packets.clear();
            }
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        watchdogC0FC00Packets.clear();
        hypixelTimer.reset();
        toggle();
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mode.is("Balance")) {
            final ScaledResolution resolution = new ScaledResolution(mc);
            final int x = resolution.getScaledWidth() / 2;
            final int y = resolution.getScaledHeight() - 75;
            final float thickness = 5F;

            float percentage = (float) Math.max(0.01, Math.min(1, balance / balanceValue.getValue()));

            final int width = 100;
            final int half = width / 2;
            animation.animate((width - 2) * percentage, 40);

            RoundedUtil.drawRound(x - half - 1, y - 1 - 12, width + 1, (int) (thickness + 1) + 12 + 3, 2, new Color(getModule(Interface.class).bgColor()));
            RoundedUtil.drawRound(x - half - 1, y - 1, width + 1, (int) (thickness + 1), 2, new Color(getModule(Interface.class).bgColor()));

            RoundedUtil.drawGradientHorizontal(x - half, y + 1, animation.getOutput(), thickness, 2, new Color(getModule(Interface.class).color(0)), new Color(getModule(Interface.class).color(90)));

            Fonts.interRegular.get(15).drawCenteredString("Balance", x, y - 1 - 11 + 3, -1);

            Fonts.interRegular.get(12).drawCenteredString(new DecimalFormat("0.0").format(percentage * 100) + "%", x, y + 2, -1);
        }
    }

    @Override
    public void onDisable() {
        if (mode.is("Balance")) {
            reset();
        }
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }

    private void reset() {
        this.balance = 0;
        this.timerUtil.reset();
        mc.timer.timerSpeed = 1;
    }
}