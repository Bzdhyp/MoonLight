package wtf.moonlight.module.impl.misc;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import com.cubk.EventTarget;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.combat.KillAura;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.player.RotationUtil;

import java.util.ArrayList;

@ModuleInfo(name = "FakeLag", category = Categor.Misc)
public class FakeLag extends Module {
    public final BoolValue combat = new BoolValue("Combat", false, this);
    public final BoolValue onlyMove = new BoolValue("Only Move", false, this);
    private final SliderValue startDelay = new SliderValue("Start Delay", 300, 0, 1000, 1, this);
    private final SliderValue lagDuration = new SliderValue("Lag Packets", 600, 0, 1000, 1, this);

    public int sentC03Packets = 0;
    private boolean shouldBlockPackets;
    private final TimerUtil delayTimer = new TimerUtil();
    private final ArrayList<Packet<?>> packets = new ArrayList<>();

    @Override
    public void onEnable() {
        this.shouldBlockPackets = false;
        super.onEnable();
    }

    @Override
    public void onPreDisable() {
        this.resetPackets();
        super.onPreDisable();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        int count = 0;
        for (final Packet<?> p : this.packets) {
            if (p instanceof C03PacketPlayer) {
                ++count;
            }
        }
        this.sentC03Packets = count;

        if (this.combat.get()) {
            if (count > this.lagDuration.getValue() || getModule(Scaffold.class).isEnabled()) {
                this.shouldBlockPackets = false;
            }
        } else if (count <= this.lagDuration.getValue() && !getModule(Scaffold.class).isEnabled()) {
            this.shouldBlockPackets = true;
        } else {
            this.shouldBlockPackets = false;
            this.resetPackets();
        }

        if (count <= this.lagDuration.getValue() && !getModule(Scaffold.class).isEnabled()) {
            if (!this.combat.get()) {
                this.shouldBlockPackets = true;
            }
        } else {
            this.shouldBlockPackets = false;
            this.resetPackets();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (this.combat.get()) {
            if (packet instanceof C02PacketUseEntity) {
                this.shouldBlockPackets = false;
                this.resetPackets();
            } else if (packet instanceof C03PacketPlayer && getModule(KillAura.class).isEnabled() && getModule(KillAura.class).target != null) {
                EntityLivingBase entityLivingBase = getModule(KillAura.class).target;
                if (entityLivingBase instanceof EntityPlayer player) {
                    Vec3 positionEyes = mc.thePlayer.getPositionEyes(1.0f);
                    Vec3 positionEyesServer = mc.thePlayer.getSeverPosition().addVector(0.0, mc.thePlayer.getEyeHeight(), 0.0);
                    Vec3 bestHitVec = RotationUtil.getBestHitVec(player);

                    if (!this.shouldBlockPackets && player.hurtTime < 3 && positionEyes.distanceTo(bestHitVec) > 2.9 &&
                            positionEyes.distanceTo(bestHitVec) < 3.3 && positionEyes.distanceTo(bestHitVec) < positionEyesServer.distanceTo(bestHitVec)) {
                        this.shouldBlockPackets = true;
                    }
                }
            }
        }

        if (mc.theWorld != null && this.shouldBlockPackets && this.delayTimer.reached(this.startDelay.getValue().longValue())) {
            if (this.onlyMove.get()) {
                if (packet instanceof C03PacketPlayer && !this.packets.contains(packet)) {
                    this.packets.add(packet);
                    event.setCancelled(true);
                }
            } else if (!this.packets.contains(packet)) {
                this.packets.add(packet);
                event.setCancelled(true);
            }
        }
    }

    private void resetPackets() {
        if (mc.thePlayer != null) {
            if (!this.packets.isEmpty()) {
                this.packets.forEach(packet -> mc.thePlayer.sendQueue.addToSendQueueDirect(packet));
                this.packets.clear();
            }
        }
        else {
            this.packets.clear();
        }
    }
}