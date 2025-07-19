package wtf.moonlight.module.impl.misc;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import com.cubk.EventTarget;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.util.StringUtils;
import wtf.moonlight.Client;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.gui.notification.NotificationManager;
import wtf.moonlight.gui.notification.NotificationType;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.combat.KillAura;
import wtf.moonlight.module.impl.player.ChestStealer;
import wtf.moonlight.module.impl.player.InvManager;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.module.values.impl.StringValue;
import wtf.moonlight.util.DebugUtil;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.misc.Multithreading;
import wtf.moonlight.util.misc.ServerUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ModuleInfo(name = "AutoPlay", category = Categor.Misc)
public class AutoPlay extends Module {
    private final BoolValue autoGG = new BoolValue("AutoGG", true);
    private final StringValue autoGGMessage = new StringValue("AutoGG Message", "gg", this);
    private final BoolValue autoPlay = new BoolValue("AutoPlay", true, this);
    private final SliderValue autoPlayDelay = new SliderValue("Delay", 3.5f, 1, 10, 0.5f, this);
    private final BoolValue respawnProperty = new BoolValue("On Respawn", true, this);
    private List<Module> disableOnRespawn;
    private final TimerUtil respawnTimer = new TimerUtil();

    @Override
    public void onEnable() {
        if (this.disableOnRespawn == null) {
            this.disableOnRespawn = Arrays.asList(
                    Client.INSTANCE.getModuleManager().getModule(KillAura.class),
                    Client.INSTANCE.getModuleManager().getModule(ChestStealer.class),
                    Client.INSTANCE.getModuleManager().getModule(InvManager.class));
        }
    }

    @EventTarget
    private void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (ServerUtil.isHypixelLobby()) return;

        if (!event.isCancelled() && event.getPacket() instanceof S02PacketChat s02PacketChat) {
            String message = s02PacketChat.getChatComponent().getUnformattedText(), strippedMessage = StringUtils.stripControlCodes(message);

            String m = s02PacketChat.getChatComponent().toString();
            if (m.contains("ClickEvent{action=RUN_COMMAND, value='/play ")) {
                if (autoGG.get() && !strippedMessage.startsWith("You died!")) {
                    DebugUtil.send("/ac " + autoGGMessage.getValue());
                }
                if (autoPlay.get()) {
                    sendToGame(m.split("action=RUN_COMMAND, value='")[1].split("'}")[0]);
                }
            }
        }

        if (event.getState() == PacketEvent.State.INCOMING) {
            if (this.respawnProperty.get() && packet instanceof S07PacketRespawn) {
                if (this.respawnTimer.hasTimeElapsed(50L)) {
                    if (ServerUtil.isHypixelLobby()) return;

                    boolean msg = false;
                    for (Module module : this.disableOnRespawn) {
                        if (!module.isEnabled()) continue;
                        module.toggle();
                        if (msg) continue;
                        msg = true;
                    }
                    if (msg) {
                        NotificationManager.post(NotificationType.INFO, "Respawn Detected!", "Disabled movement modules/aura.", 3.5f);
                    }
                    this.respawnTimer.reset();
                }
            }
        }
    }

    private void sendToGame(String mode) {
        float delay = autoPlayDelay.getValue();
        String delayText = delay > 0 ? String.format("in %.1f s", delay) : "immediately";
        NotificationManager.post(NotificationType.INFO,"Playing Again!", "Playing again " + delayText + ".", delay);
        Multithreading.schedule(() -> DebugUtil.send(mode), (long) delay, TimeUnit.SECONDS);
    }
}
