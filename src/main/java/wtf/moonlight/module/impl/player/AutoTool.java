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
package wtf.moonlight.module.impl.player;

import net.minecraft.util.MovingObjectPosition;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.component.SpoofSlotComponent;
import wtf.moonlight.util.player.PlayerUtil;

@ModuleInfo(name = "AutoTool", category = Categor.Player)
public class AutoTool extends Module {

    public final BoolValue ignoreUsingItem = new BoolValue("Ignore Using Item",false,this);
    public final BoolValue spoof = new BoolValue("Spoof",false,this);
    public final BoolValue switchBack = new BoolValue("Switch Back",true,this,() -> !spoof.get());
    private int oldSlot;
    public boolean wasDigging;
    @Override
    public void onDisable() {
        if (this.wasDigging) {
            mc.thePlayer.inventory.currentItem = this.oldSlot;
            this.wasDigging = false;
        }
        SpoofSlotComponent.stopSpoofing();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.gameSettings.keyBindAttack.isKeyDown() && (ignoreUsingItem.get() && !mc.thePlayer.isUsingItem() || !ignoreUsingItem.get()) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && PlayerUtil.findTool(mc.objectMouseOver.getBlockPos()) != -1) {
            if (!this.wasDigging) {
                this.oldSlot = mc.thePlayer.inventory.currentItem;
                if (this.spoof.get()) {
                    SpoofSlotComponent.startSpoofing(this.oldSlot);
                }
            }
            mc.thePlayer.inventory.currentItem = PlayerUtil.findTool(mc.objectMouseOver.getBlockPos());
            this.wasDigging = true;
        } else if (this.wasDigging && (switchBack.get() || spoof.get())) {
            mc.thePlayer.inventory.currentItem = this.oldSlot;
            SpoofSlotComponent.stopSpoofing();
            this.wasDigging = false;
        } else {
            this.oldSlot = mc.thePlayer.inventory.currentItem;
        }
    }
}
