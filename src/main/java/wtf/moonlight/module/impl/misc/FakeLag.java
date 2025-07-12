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
package wtf.moonlight.module.impl.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import com.cubk.EventTarget;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.*;
import wtf.moonlight.util.MathUti;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.component.PingSpoofComponent;

import java.util.Arrays;
import java.util.List;

@ModuleInfo(name = "FakeLag", category = Categor.Misc)
public class FakeLag extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Constant", "Dynamic"}, "Dynamic", this);
    private final SliderValue minDelay = new SliderValue("Min Delay", 300, 0, 1000, 1, this);
    private final SliderValue maxDelay = new SliderValue("Max Delay", 600, 0, 1000, 1, this);
    private final SliderValue recoilTime = new SliderValue("Recoil Time", 250, 0, 1000, 1, this);
    private final SliderValue range = new SliderValue("Range", 3.5f, 0, 10, 0.1f, this);

    public final MultiBoolValue flushOn = new MultiBoolValue("Flush On", Arrays.asList(
            new BoolValue("Entity Interact", false),
            new BoolValue("Block Interact", false),
            new BoolValue("Action", false)), this);

    private final TimerUtil chronometer = new TimerUtil();
    private final TimerUtil delayTimer = new TimerUtil();
    private int nextDelay;
    private boolean isEnemyNearby;

    public FakeLag() {
        nextDelay = getRandomDelay();
        delayTimer.reset();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        isEnemyNearby = isEnemyNearby(range.getValue());

        if (delayTimer.hasTimeElapsed(nextDelay)) {
            nextDelay = getRandomDelay();
            delayTimer.reset();
            PingSpoofComponent.dispatch();
        }

        setTag(mode.getValue());
    }

    private boolean isEnemyNearby(float range) {
        if (mc.theWorld == null || mc.thePlayer == null) {
            return false;
        }

        List<Entity> entities = mc.theWorld.getLoadedEntityList();
        for (Entity entity : entities) {
            if (entity instanceof EntityPlayer player && entity != mc.thePlayer) {

                if (mc.thePlayer.getDistanceToEntity(player) <= range &&
                        player.isEntityAlive() &&
                        !player.isInvisible()) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (event.getState() == PacketEvent.State.INCOMING || mc.thePlayer.isDead || mc.thePlayer.isInWater() ||
                mc.currentScreen != null || !chronometer.hasTimeElapsed(recoilTime.getValue().longValue())) {
            return;
        }

        if (shouldFlush(packet)) {
            chronometer.reset();
            delayTimer.reset();
            PingSpoofComponent.dispatch();
            event.setCancelled(false);
            return;
        }

        if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem() != null &&
                mc.thePlayer.getHeldItem().getItem().isItemTool(mc.thePlayer.getHeldItem())) {
            return;
        }

        if (mode.is("Constant")) {
            event.setCancelled(true);
            PingSpoofComponent.spoof(nextDelay, true, true, true, true, true, true);
        } else if (mode.is("Dynamic")) {
            if (!isEnemyNearby) return;

            event.setCancelled(true);
            PingSpoofComponent.spoof(nextDelay, true, true, true, true, true, true);
        }
    }

    private boolean shouldFlush(Packet<?> packet) {
        // Position/status packets
        if (packet instanceof S08PacketPlayerPosLook || packet instanceof C19PacketResourcePackStatus) {
            return true;
        }

        // Entity interact
        if ((packet instanceof C02PacketUseEntity || packet instanceof C0APacketAnimation) &&
                flushOn.isEnabled("Entity Interact")) {
            return true;
        }

        // Block interact
        if ((packet instanceof C08PacketPlayerBlockPlacement || packet instanceof C12PacketUpdateSign) &&
                flushOn.isEnabled("Block Interact")) {
            return true;
        }

        // Action
        if (packet instanceof C07PacketPlayerDigging && flushOn.isEnabled("Action")) {
            return true;
        }

        // Knock back
        if (packet instanceof S12PacketEntityVelocity velocity) {
            if (velocity.getEntityID() == mc.thePlayer.getEntityId() &&
                    (velocity.getMotionX() != 0 || velocity.getMotionY() != 0 || velocity.getMotionZ() != 0)) {
                return true;
            }
        }

        // Damage
        return packet instanceof S06PacketUpdateHealth;
    }

    private int getRandomDelay() {
        return (int) MathUti.getAdvancedRandom(minDelay.getValue(), maxDelay.getValue());
    }

    @Override
    public void onDisable() {
        isEnemyNearby = false;
        PingSpoofComponent.dispatch();
        super.onDisable();
    }
}