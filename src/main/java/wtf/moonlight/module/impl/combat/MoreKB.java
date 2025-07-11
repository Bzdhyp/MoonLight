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
package wtf.moonlight.module.impl.combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.AttackEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.utils.player.MovementUtils;

@ModuleInfo(name = "MoreKB", category = ModuleCategory.Combat)
public class MoreKB extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Legit Fast", "Packet"}, "Legit Test", this);
    private final BoolValue onlyGround = new BoolValue("Only Ground", true, this);
    public int ticks;

    private EntityLivingBase target = null;

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (event.getTargetEntity() != null && event.getTargetEntity() instanceof EntityLivingBase t) {
            target = t;
            ticks = 2;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (target != null && MovementUtils.isMoving()) {
            if ((onlyGround.get() && mc.thePlayer.onGround || !onlyGround.get())) {
                switch (mode.getValue()) {
                    case "Legit Fast":
                        mc.thePlayer.sprintingTicksLeft = 0;
                        break;
                    case "Packet":
                        sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                        sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                        break;
                }
            }
            target = null;
        }
    }
}