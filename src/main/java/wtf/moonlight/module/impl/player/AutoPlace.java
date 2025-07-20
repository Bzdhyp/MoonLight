package wtf.moonlight.module.impl.player;

import com.cubk.EventTarget;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.lwjglx.input.Mouse;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.util.player.BlockUtil;

@ModuleInfo(name = "AutoPlace", category = Categor.Player)
public class AutoPlace extends Module {
    private final BoolValue placeUnderWhileOffground = new BoolValue("Place under while ground", false, this);

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindUseItem.setPressed(Mouse.isButtonDown(1));
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if(mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            EnumFacing facing = mc.objectMouseOver.sideHit;

            boolean canPlaceOffGround = placeUnderWhileOffground.get() && !mc.thePlayer.onGround && BlockUtil.isAirOrLiquid(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));

            if((facing != EnumFacing.UP && facing != EnumFacing.DOWN) || canPlaceOffGround) {
                mc.gameSettings.keyBindUseItem.setPressed(true);
                mc.rightClickDelayTimer = 0;
            } else {
                mc.gameSettings.keyBindUseItem.setPressed(Mouse.isButtonDown(1));
            }
        }
    }
}
