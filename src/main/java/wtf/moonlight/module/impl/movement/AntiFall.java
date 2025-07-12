package wtf.moonlight.module.impl.movement;

import net.minecraft.block.BlockAir;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import com.cubk.EventTarget;
import net.minecraft.util.AxisAlignedBB;
import wtf.moonlight.Client;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.utils.packet.PacketUtils;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "AntiFall", category = ModuleCategory.Movement)
public class AntiFall extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Hypixel"},"Hypixel", this);
    private final SliderValue fallDist = new SliderValue("Fall Distance", 3F, 1F, 20F, 0.5F, this);
    private double lastGroundY;

    private final List<Packet<?>> packets = new ArrayList<>();

    public boolean isActive() {
        return !packets.isEmpty() && mc.thePlayer.fallDistance >= fallDist.getValue();
    }

    @EventTarget
    public void onPacketSend(PacketEvent event) {
        if(mode.is("Hypixel")) {
            if(event.getPacket() instanceof C03PacketPlayer) {
                Scaffold scaffold = Client.INSTANCE.getModuleManager().getModule(Scaffold.class);
                if (scaffold.data == null || scaffold.data.blockPos == null || scaffold.data.facing == null && !(mc.theWorld.getBlockState(scaffold.targetBlock).getBlock() instanceof BlockAir))
                    return;

                if (isEnabled(LongJump.class) || scaffold.isEnabled() && scaffold.data.blockPos != null)
                    return;

                if(!isBlockUnder()) {
                    if(mc.thePlayer.fallDistance < fallDist.getValue()) {
                        event.setCancelled(true);
                        packets.add(event.getPacket());
                    } else {
                        if(!packets.isEmpty()) {
                            for(Packet<?> packet : packets) {
                                C03PacketPlayer c03 = (C03PacketPlayer) packet;
                                c03.setY(lastGroundY);
                                PacketUtils.sendPacketNoEvent(packet);
                            }
                            packets.clear();
                        }
                    }
                } else {
                    lastGroundY = mc.thePlayer.posY;
                    if(!packets.isEmpty()) {
                        packets.forEach(PacketUtils::sendPacketNoEvent);
                        packets.clear();
                    }
                }
            }
        }
    }

    private boolean isBlockUnder() {
        if (mc.thePlayer.posY < 0) return false;
        for (int offset = 0; offset < (int) mc.thePlayer.posY + 2; offset += 2) {
            AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
