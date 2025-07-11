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
import com.cubk.EventTarget;
import wtf.moonlight.events.player.AttackEvent;
import wtf.moonlight.events.player.StrafeEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.movement.Freeze;
import wtf.moonlight.module.impl.movement.Speed;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.utils.player.MovementUtils;

@ModuleInfo(name = "Critical", category = ModuleCategory.Combat)
public class Critical extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Jump", "AutoFreeze", "AutoSpeed"}, "Jump", this);
    private boolean attacking;
    public boolean stuckEnabled;

    @Override
    public void onEnable() {
        stuckEnabled = false;
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (mc.thePlayer.onGround && event.getTargetEntity() instanceof EntityLivingBase entity) {
            if(entity.hurtTime == 9)
                mc.thePlayer.onCriticalHit(entity);
            attacking = true;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.getValue());
        switch (mode.getValue()) {
            case "AutoFreeze":
                if (getModule(KillAura.class).target != null && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                }
                if (mc.thePlayer.fallDistance > 0) {
                    getModule(Freeze.class).setEnabled(true);
                    stuckEnabled = true;
                }
                if (getModule(KillAura.class).target == null && stuckEnabled) {
                    getModule(Freeze.class).setEnabled(false);
                    stuckEnabled = false;
                }
                break;
            case "AutoSpeed":
                if (getModule(KillAura.class).target != null) {
                    if (isDisabled(Speed.class)) {
                        getModule(Speed.class).setEnabled(true);
                    } else {
                        if (!MovementUtils.isMoving() && mc.thePlayer.onGround) {
                            mc.thePlayer.jump();
                        }
                    }
                } else {
                    if (isEnabled(Speed.class)) {
                        getModule(Speed.class).setEnabled(false);
                    }
                }
                break;
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event){
        if(mode.is("Jump") && attacking && mc.thePlayer.onGround){
            mc.thePlayer.jump();
            attacking = false;
        }
    }
}
