package wtf.moonlight.module.impl.movement;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.Unpooled;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import com.cubk.EventTarget;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.events.player.SlowDownEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.combat.KillAura;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.component.BlinkComponent;
import wtf.moonlight.util.player.MovementUtil;

@ModuleInfo(name = "NoSlowdown", category = Categor.Movement)
public class NoSlowdown extends Module {
    public final ListValue mode = new ListValue("Mode", new String[]{"Vanilla", "Grim", "Blink"}, "Vanilla", this);
    public final BoolValue sprint = new BoolValue("Sprint", false, this);
    public final BoolValue swordValue = new BoolValue("Sword", false, this);
    public final BoolValue foodValue = new BoolValue("Food", false, this, () -> !mode.is("Grim"));
    public final BoolValue potionValue = new BoolValue("Potion", false, this);
    public final BoolValue bowValue = new BoolValue("Bow", false, this);

    boolean usingItem;
    int usingItemTick = 0;

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(mode.getValue());

        switch (mode.getValue()) {
            case "Blink": {
                if (event.isPre()) {
                    if (mc.thePlayer.getCurrentEquippedItem() == null) return;

                    final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();

                    if (mc.thePlayer.isUsingItem()) {
                        if (item instanceof ItemFood && foodValue.get() || item instanceof ItemPotion && potionValue.get() || item instanceof ItemBow && bowValue.get()) {
                            BlinkComponent.blinking = true;
                        }

                        usingItem = true;
                    } else if (usingItem) {
                        usingItem = false;

                        BlinkComponent.blinking = false;
                    }
                    break;
                }
            }

            case "Grim": {
                if (event.isPre()) {
                    if (mc.thePlayer.isUsingItem()) {
                        usingItemTick++;
                    } else {
                        usingItemTick = 0;
                    }

                    if (mode.is("Grim")) {
                        if (mc.thePlayer.isUsingItem() && !mc.thePlayer.isEating() && mc.thePlayer.getItemInUseCount() < 25) {
                            mc.thePlayer.stopUsingItem();
                        }

                        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem() && !getModule(KillAura.class).isBlocking) {
                            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        }

                        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow
                                && mc.thePlayer.isUsingItem() && MovementUtil.isMoving()) {
                            mc.getNetHandler()
                                    .addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                            mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("test", new PacketBuffer(Unpooled.buffer())));
                            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                        }
                    }
                } else {
                    if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem() && !getModule(KillAura.class).isBlocking) {
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255,
                                mc.thePlayer.inventory.getCurrentItem(), 0.0F, 0.0F, 0.0F));

                        if (!mc.isSingleplayer()) {
                            PacketWrapper use = PacketWrapper.create(29, null,
                                    Via.getManager().getConnectionManager().getConnections().iterator().next());
                            use.write(Type.VAR_INT, 1);
                            PacketUtil.sendToServer(use, Protocol1_8To1_9.class, true, true);
                        }
                    }

                }
                break;
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (mc.thePlayer == null) return;

        if(mode.is("Grim")) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem != null && heldItem.getItem() instanceof ItemFood && foodValue.get()) {
                if (packet instanceof C08PacketPlayerBlockPlacement currentPacket) {
                    if (currentPacket.getPlacedBlockDirection() == 255 && currentPacket.getPosition().equals(C08PacketPlayerBlockPlacement.field_179726_a) && heldItem.stackSize >= 2) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    }
                }

                if (packet instanceof S30PacketWindowItems && mc.thePlayer.isUsingItem()) {
                    event.setCancelled(true);
                }

                if (packet instanceof S2FPacketSetSlot && mc.thePlayer.isUsingItem()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventTarget
    public void onSlowDown(SlowDownEvent event) {
        event.setSprinting(sprint.get());

        if (mode.is("Grim")) {
            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow &&
                    usingItemTick > 1 && bowValue.get()) {
                event.setCancelled(true);
            }
            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood &&
                    foodValue.get() && mc.thePlayer.getHeldItem().stackSize >= 2 && mc.thePlayer.isEating() && usingItemTick > 1) {
                event.setCancelled(true);
            }

            if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && swordValue.get()) {
                event.setCancelled(true);
            }
        } else {
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
}