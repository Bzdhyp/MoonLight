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

import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemBlock;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.MoveInputEvent;
import wtf.moonlight.events.player.SafeWalkEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MathUti;
import wtf.moonlight.util.player.PlayerUtil;

@ModuleInfo(name = "SafeWalk", category = Categor.Movement)
public class SafeWalk extends Module {

    public final ListValue mode = new ListValue("Mode", new String[]{"Safe", "Sneak"}, "Safe", this);
    private final BoolValue heldBlocks = new BoolValue("Held Blocks Check", true, this);
    private final BoolValue pitchCheck = new BoolValue("Pitch Check", true, this);
    private final SliderValue minPitch = new SliderValue("Min Pitch", 55, 50, 90, 1, this, pitchCheck::get);
    public final SliderValue maxPitch = new SliderValue("Max Pitch", 75, 50, 90, 1, this, pitchCheck::get);

    @EventTarget
    public void onSafeWalk(SafeWalkEvent event) {
        if (canSafeWalk() && mode.is("Safe"))
            event.setCancelled(true);
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (canSafeWalk() && mode.is("Sneak") && PlayerUtil.blockRelativeToPlayer(0, -1, 0) instanceof BlockAir)
            event.setSneaking(true);
    }

    public boolean canSafeWalk() {
        return mc.thePlayer.onGround && (heldBlocks.get() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock || !heldBlocks.get()) && (pitchCheck.get() && MathUti.inBetween(minPitch.getMin(), maxPitch.getMax(), mc.thePlayer.rotationPitch) || !pitchCheck.get());
    }
}
