package wtf.moonlight.module.impl.display;

import com.cubk.EventTarget;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.combat.KillAura;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@ModuleInfo(name = "TargetHUD", category = Categor.Display)
public class TargetHUD extends Module {
    public final ListValue targetHudMode = new ListValue("Mode", new String[]{"Novo 1","Novo 2","Novo 3","Novo 4","Novo 5", "Type 1", "Type 2", "Type 3", "Type 4",
            "Felix", "Exhi", "Moon", "Augustus", "Rise", "Adjust", "Astolfo", "Akrien","Innominate"}, "Astolfo", this);
    public final BoolValue targetHudParticle = new BoolValue("Particle",true, this);

    public static final Map<EntityPlayer, DecelerateAnimation> animationEntityPlayerMap = new HashMap<>();

    @EventTarget
    public void onTick(TickEvent event) {
        KillAura aura = getModule(KillAura.class);
        if (aura.isEnabled()) {
            animationEntityPlayerMap.entrySet().removeIf(entry -> entry.getKey().isDead || (!aura.targets.contains(entry.getKey()) && entry.getKey() != mc.thePlayer));
        }

        if (!aura.isEnabled() && !(mc.currentScreen instanceof GuiChat)) {
            this.TargetAnim();
        }

        if (!aura.targets.isEmpty() && !(mc.currentScreen instanceof GuiChat)) {
            for (EntityLivingBase entity : aura.targets) {
                if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                    animationEntityPlayerMap.putIfAbsent((EntityPlayer) entity, new DecelerateAnimation(175, 1));
                    animationEntityPlayerMap.get(entity).setDirection(Direction.FORWARDS);
                }
            }
        }

        if (aura.isEnabled() && aura.target == null && !(mc.currentScreen instanceof GuiChat)) {
            this.TargetAnim();
        }

        if (mc.currentScreen instanceof GuiChat) {
            animationEntityPlayerMap.putIfAbsent(mc.thePlayer, new DecelerateAnimation(175, 1));
            animationEntityPlayerMap.get(mc.thePlayer).setDirection(Direction.FORWARDS);
        }
    }

    public void TargetAnim() {
        Iterator<Map.Entry<EntityPlayer, DecelerateAnimation>> iterator = animationEntityPlayerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EntityPlayer, DecelerateAnimation> entry = iterator.next();
            DecelerateAnimation animation = entry.getValue();

            animation.setDirection(Direction.BACKWARDS);
            if (animation.finished(Direction.BACKWARDS)) {
                iterator.remove();
            }
        }
    }
}
