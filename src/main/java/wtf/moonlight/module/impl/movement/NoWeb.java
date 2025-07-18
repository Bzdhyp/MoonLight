package wtf.moonlight.module.impl.movement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.util.player.PlayerUtil;

import java.util.Map;

@ModuleInfo(name = "NoWeb",category = Categor.Movement)
public class NoWeb extends Module {

    private final ListValue mode = new ListValue("Mode", new String[]{"Vanilla", "Grim", "Intave"}, "Vanilla",this);
    public final BoolValue noDown = new BoolValue("No Down",true,this,() -> mode.is("Intave"));
    public final BoolValue upAndDown = new BoolValue("Up And Down",true,this,() -> mode.is("Intave") && noDown.get());

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(mode.getValue());
        if (!mc.thePlayer.isInWeb) {
            return;
        }

        switch (mode.getValue()) {
            case "Vanilla":
                mc.thePlayer.isInWeb = false;
                break;
            case "Grim":
                Map<BlockPos, Block> searchBlock = PlayerUtil.searchBlocks(2);
                for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
                    if (mc.theWorld.getBlockState(block.getKey()).getBlock() instanceof BlockWeb) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                    }
                }
                mc.thePlayer.isInWeb = false;
                break;
            case "Intave":
                searchBlock = PlayerUtil.searchBlocks(2);
                for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
                    if (mc.theWorld.getBlockState(block.getKey()).getBlock() instanceof BlockWeb) {
                        if (noDown.get()) {
                            if (upAndDown.get())
                                if (mc.gameSettings.keyBindSneak.isKeyDown())
                                    mc.thePlayer.motionY = -0.2;
                                else if (mc.gameSettings.keyBindJump.isKeyDown())
                                    mc.thePlayer.motionY = mc.thePlayer.ticksExisted % 2 == 0 ? 0.2 : -0.01;
                                else
                                    mc.thePlayer.motionY = -0.01;
                            else
                                mc.thePlayer.motionY = -0.01;
                        }

                    }
                }
                break;
        }
    }
}
