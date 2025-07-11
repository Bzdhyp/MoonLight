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
package wtf.moonlight.module.impl.movement;

import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.events.player.SlowDownEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.component.BlinkComponent;
import wtf.moonlight.utils.packet.PacketUtils;

@ModuleInfo(name = "NoSlowdown", category = ModuleCategory.Movement)
public class NoSlowdown extends Module {

    public final ListValue mode = new ListValue("Mode", new String[]{"Blink"}, "Blink", this);
    public final BoolValue sprint = new BoolValue("Sprint", false, this);
    public final BoolValue foodValue = new BoolValue("Food", false, this);
    public final BoolValue potionValue = new BoolValue("Potion", false, this);
    public final BoolValue swordValue = new BoolValue("Sword", false, this);
    public final BoolValue bowValue = new BoolValue("Bow", false, this);
    boolean usingItem;

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(mode.getValue());

        if (event.isPre()) {
            if (mc.thePlayer.getCurrentEquippedItem() == null) return;

            final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();

            if (mc.thePlayer.isUsingItem()) {
                if (item instanceof ItemSword && swordValue.get()) {
                    BlinkComponent.blinking = true;

                    if (mc.thePlayer.ticksExisted % 5 == 0) {
                        PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        BlinkComponent.dispatch();
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                    }
                } else if (item instanceof ItemFood && foodValue.get() || item instanceof ItemBow && bowValue.get()) {
                    BlinkComponent.blinking = true;
                }

                usingItem = true;
            } else if (usingItem) {
                usingItem = false;

                BlinkComponent.blinking = false;
            }
        }
    }

    @EventTarget
    public void onSlowDown(SlowDownEvent event) {
        event.setSprinting(sprint.get());

        if (foodValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
            event.setForward(1);
            event.setStrafe(1);
        }
        if (potionValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
            event.setForward(1);
            event.setStrafe(1);
        }   
        if (swordValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            event.setForward(1);
            event.setStrafe(1);
        }
        if (bowValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
            event.setForward(1);
            event.setStrafe(1);
        }
    }
}