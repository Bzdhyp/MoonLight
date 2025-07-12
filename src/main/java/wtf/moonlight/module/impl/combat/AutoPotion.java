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

import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RotationUtil;

@ModuleInfo(name = "AutoPotion", category = Categor.Combat)
public class AutoPotion extends Module {
    private final SliderValue health = new SliderValue("Health", 15, 1, 20, 1, this);
    private final SliderValue delay = new SliderValue("Delay", 500, 50, 5000, 50, this);
    private final TimerUtil timer = new TimerUtil();
    private long nextThrow;

    @EventTarget
    public void onUpdate(UpdateEvent event) {

        if (!mc.thePlayer.onGround || !timer.hasTimeElapsed(nextThrow) || isEnabled(Scaffold.class) || isEnabled(KillAura.class) && getModule(KillAura.class).target != null) {
            return;
        }

        for (int i = 0; i < 9; i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null) {
                continue;
            }

            final Item item = stack.getItem();

            if (item instanceof ItemPotion potion) {
                final PotionEffect effect = potion.getEffects(stack).get(0);

                if (!ItemPotion.isSplash(stack.getMetadata()) ||
                        !PlayerUtil.goodPotion(effect.getPotionID()) ||
                        (effect.getPotionID() == Potion.regeneration.id ||
                                effect.getPotionID() == Potion.heal.id) &&
                                mc.thePlayer.getHealth() > this.health.getValue()) {
                    continue;
                }

                if (mc.thePlayer.isPotionActive(effect.getPotionID()) &&
                        mc.thePlayer.activePotionsMap.get(effect.getPotionID()).getDuration() != 0) {
                    continue;
                }

                RotationUtil.setRotation(new float[]{mc.thePlayer.rotationYaw, 87});

                mc.thePlayer.inventory.currentItem = i;

                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));

                this.nextThrow = delay.getValue().longValue();
                timer.reset();
                break;
            }
        }
    }
}
