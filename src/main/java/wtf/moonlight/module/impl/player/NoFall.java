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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.component.BlinkComponent;
import wtf.moonlight.util.MovementCorrection;
import wtf.moonlight.util.player.MovementUtil;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RotationUtil;

@ModuleInfo(name = "NoFall", category = Categor.Player)
public class NoFall extends Module {

    public final ListValue mode = new ListValue("Mode", new String[]{"NoGround", "Blink", "Extra","Water"}, "Water", this);
    public final SliderValue minDistance = new SliderValue("Min Distance", 3, 0, 8, 1, this, () -> !mode.is("NoGround"));
    private boolean blinked = false;


    private boolean prevOnGround = false;

    private int slot;

    private int lastslot;
    private double fallDistance = 0;
    private boolean timed = false;

    @Override
    public void onEnable() {
        if (PlayerUtil.nullCheck())
            this.fallDistance = mc.thePlayer.fallDistance;
    }

    @Override
    public void onDisable() {
        if (blinked) {
            BlinkComponent.dispatch();
            blinked = false;
        }
        mc.timer.timerSpeed = 1f;
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(mode.getValue());
        if (!PlayerUtil.nullCheck())
            return;

        if (event.isPost())
            return;

        if (mc.thePlayer.onGround)
            fallDistance = 0;
        else {
            fallDistance += (float) Math.max(mc.thePlayer.lastTickPosY - event.getY(), 0);

            fallDistance -= MovementUtil.predictedMotionY(mc.thePlayer.motionY, 1);
        }

        //if (mc.thePlayer.capabilities.allowFlying) return;
        if (isVoid()) {
            if (blinked) {
                BlinkComponent.dispatch();
                blinked = false;
            }
            return;
        }

        switch (mode.getValue()) {
            case "NoGround":
                event.setOnGround(false);
                break;

            case "Extra":
                if (fallDistance >= minDistance.getValue() && !isEnabled(Scaffold.class)) {
                    mc.timer.timerSpeed = (float) 0.5;
                    timed = true;
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    fallDistance = 0;
                } else if (timed) {
                    mc.timer.timerSpeed = 1;
                    timed = false;
                }
                break;
            case "Water":
                //md isBlockUnder太玄幻了写了一个早上还是概率
                if (PlayerUtil.isBlockUnder1(14) && mc.thePlayer.fallDistance >= minDistance.getValue()) {
                    slot = getWaterBucket();
                    if (slot != -1) {
                        mc.thePlayer.inventory.currentItem = slot;
                        RotationUtil.setRotation(new float[]{mc.thePlayer.rotationYaw, 90}, MovementCorrection.SILENT, 5);
                        for (int i = 0; i < 4; i++) {
                            final Block block = PlayerUtil.blockRelativeToPlayer(0, -i, 0);
                            if (block.getMaterial() == Material.water) {
                                break;
                            }
                            if (block.isFullBlock()) {
                                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem());
                            }
                        }
                        mc.thePlayer.inventory.currentItem = lastslot;
                    }

                }else {
                    slot = getBucket();
                    if (mc.thePlayer.isInWater() && slot != -1 && mc.thePlayer.onGround){
                        mc.thePlayer.inventory.currentItem = slot;
                        RotationUtil.setRotation(new float[]{mc.thePlayer.rotationYaw, 90}, MovementCorrection.SILENT, 5);
                        mc.playerController.sendUseItem(mc.thePlayer,mc.theWorld,mc.thePlayer.inventory.getCurrentItem());
                        mc.thePlayer.inventory.currentItem = lastslot;
                    }else {
                        lastslot = mc.thePlayer.inventory.currentItem;
                    }
                }
            case "Blink":
                if (mc.thePlayer.onGround) {
                    if (blinked) {
                        BlinkComponent.dispatch();
                        blinked = false;
                    }

                    this.prevOnGround = true;
                } else if (this.prevOnGround) {
                    if (shouldBlink()) {
                        if (!BlinkComponent.blinking)
                            BlinkComponent.blinking = true;
                        blinked = true;
                    }

                    prevOnGround = false;
                } else if (PlayerUtil.isBlockUnder() && BlinkComponent.blinking && (this.fallDistance - mc.thePlayer.motionY) >= minDistance.getValue()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                    this.fallDistance = 0.0F;
                }
                break;
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        if (mode.is("Blink")) {
            if (blinked)
                mc.fontRendererObj.drawStringWithShadow("Blinking: " + BlinkComponent.packets.size(), (float) sr.getScaledWidth() / 2.0F - (float) mc.fontRendererObj.getStringWidth("Blinking: " + BlinkComponent.packets.size()) / 2.0F, (float) sr.getScaledHeight() / 2.0F + 13.0F, -1);
        }
    }

    private boolean isVoid() {
        return PlayerUtil.overVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    private boolean shouldBlink() {
        return !mc.thePlayer.onGround && !PlayerUtil.isBlockUnder((int) Math.floor(minDistance.getValue())) && PlayerUtil.isBlockUnder() && !getModule(Scaffold.class).isEnabled();
    }
    private int getWaterBucket() {
        for (int i = 0;i < 9;i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null)
                continue;
            if (stack.getItem() == Items.water_bucket) {
                return i;
            }
        }
        return -1;
    }
    private int getBucket() {
        for (int i = 0;i < 9;i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null)
                continue;
            if (stack.getItem() == Items.bucket) {
                return i;
            }
        }
        return -1;
    }
}
