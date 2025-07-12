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
import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjglx.input.Keyboard;
import org.lwjglx.opengl.Display;
import org.lwjglx.util.glu.GLU;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.WorldEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.events.render.Render3DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.combat.KillAura;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MathUti;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.player.InventoryUtil;
import wtf.moonlight.util.player.MovementCorrection;
import wtf.moonlight.util.player.RotationUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@ModuleInfo(name = "ChestStealer", category = Categor.Player, key = Keyboard.KEY_L)
public final class ChestStealer extends Module {
    private final SliderValue minDelay = new SliderValue("Min Delay", 1, 0, 5, 1, this);
    private final SliderValue maxDelay = new SliderValue("Max Delay", 1, 0, 5, 1, this);
    public final BoolValue menuCheck = new BoolValue("Menu Check", true, this);
    public final BoolValue silent = new BoolValue("Silent", false, this);
    public final BoolValue silentChestView = new BoolValue("Silent View", true, this);
    public final BoolValue aura = new BoolValue("Aura", false, this);
    private final BoolValue startDelay = new BoolValue("Start Delay", true, this);

    public final BoolValue furnace = new BoolValue("Furnace", false, this);
    public final BoolValue brewingStand = new BoolValue("Brewing Stand", false, this);
    public final BoolValue avoid = new BoolValue("Avoid", false, this, aura::get);
    private final SliderValue range = new SliderValue("Range", 4f, 1.5f, 4f, this);
    private final TimerUtil timer = new TimerUtil(), timerAura = new TimerUtil(), timerAvoid = new TimerUtil();
    public boolean isStealing;
    private final List<BlockPos> posList = new CopyOnWriteArrayList<>();
    private int chestIndex;
    public static float[] rotation;
    public int slot;
    private BlockPos currentContainerPos;
    private final String[] list = new String[]{"mode", "delivery", "menu", "selector", "game", "gui", "server", "inventory", "play", "teleporter", //
            "shop", "melee", "armor", "block", "castle", "mini", "warp", "teleport", "user", "team", "tool", "sure", "trade", "cancel", "accept",  //
            "soul", "book", "recipe", "profile", "tele", "port", "map", "kit", "select", "lobby", "vault", "lock", "anticheat", "travel", "settings", //
            "user", "preference", "compass", "cake", "wars", "buy", "upgrade", "ranged", "potions", "utility"};

    public void rotate(BlockPos blockPos) {
        rotation = RotationUtil.getRotations(blockPos);

        RotationUtil.setRotation(rotation, MovementCorrection.SILENT);
    }

    @Override
    public void onDisable() {
        isStealing = false;
        posList.clear();
        chestIndex = 0;
        super.onDisable();
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        posList.clear();
        chestIndex = 0;
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        rotation = null;

        if (!event.isPre())
            return;

        if (!aura.get() || (isEnabled(Scaffold.class) || getModule(KillAura.class).isBlocking))
            return;

        if (!isStealing) {
            for (TileEntity chest : tileEntityList()) {
                if (!posList.contains(chest.getPos()) && timerAura.hasTimeElapsed(300)) {
                    rotate(chest.getPos());
                    if (RotationUtil.rayTrace(RotationUtil.currentRotation, range.getValue(), 1).getBlockPos().equals(chest.getPos()) && (chest instanceof TileEntityChest || brewingStand.get() && chest instanceof TileEntityBrewingStand || furnace.get() && chest instanceof TileEntityFurnace)) {
                        mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), chest.getPos(), Block.getFacingDirection(chest.getPos()), getVec3(chest.getPos()));
                        posList.add(chest.getPos());
                        timerAura.reset();
                    }
                }
            }
        } else {
            timerAura.reset();
        }

        if (!avoid.get())
            return;

        if (!isStealing) {
            if (chestIndex >= posList.size()) {
                return;
            }

            BlockPos pos = posList.get(chestIndex);
            if (mc.thePlayer.getDistance(pos) <= range.getValue()) {
                BlockPos up = pos.up();
                if (mc.theWorld.getBlockState(up).getBlock() == Blocks.air && getBlockSlot() != -1 && timerAvoid.hasTimeElapsed(1000)) {
                    int prevItem = mc.thePlayer.inventory.currentItem;
                    mc.thePlayer.inventory.currentItem = getBlockSlot();
                    rotate(pos);
                    if (RotationUtil.rayTrace(RotationUtil.currentRotation, range.getValue(), 1).getBlockPos().equals(pos)) {
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), up, Block.getFacingDirection(up), getVec3(up))) {
                            mc.thePlayer.swingItem();
                        }

                        chestIndex += 1;
                        mc.thePlayer.inventory.currentItem = prevItem;
                        timerAvoid.reset();
                    }
                }
            }
        } else {
            timerAvoid.reset();
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        for (BlockPos blockPos : posList) {
            RenderUtil.renderBlock(blockPos, getModule(Interface.class).color(), true, true);
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (silentChestView.get()) {
            if (mc.thePlayer.openContainer == null || mc.currentScreen == null || !isStealing) return;
            Container container = mc.thePlayer.openContainer;
            int slots = container.inventorySlots.size();

            int scaleFactor = event.scaledResolution().getScaleFactor();

            if (slots > 0) {
                float[] projection = calculate(currentContainerPos, scaleFactor);
                if (projection == null) return;

                float roundX = projection[0] - (164 / 2F);
                float roundY = projection[1] / 1.5F;

                GlStateManager.pushMatrix();
                GlStateManager.translate(roundX + 82, roundY + 30, 0);
                GlStateManager.translate(-(roundX + 82), -(roundY + 30), 0);

                RoundedUtil.drawRound(roundX, roundY, 164, 60, 3, new Color(0, 0, 0, 120));

                double startX = roundX + 5;
                double startY = roundY + 5;

                RenderItem itemRender = mc.getRenderItem();

                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.zLevel = 200.0F;

                for (Slot slot : container.inventorySlots) {
                    if (!slot.inventory.equals(mc.thePlayer.inventory)) {
                        int x = (int) (startX + (slot.slotNumber % 9) * 18);
                        int y = (int) (startY + ((double) slot.slotNumber / 9) * 18);

                        itemRender.renderItemAndEffectIntoGUI(slot.getStack(), x, y);
                    }
                }

                GlStateManager.popMatrix();

                itemRender.zLevel = 0.0F;
                GlStateManager.popMatrix();
                GlStateManager.disableLighting();
            }
        }
    }

    public float[] calculate(BlockPos blockPos, int factor) {
        try {
            float renderX = (float) mc.getRenderManager().renderPosX;
            float renderY = (float) mc.getRenderManager().renderPosY;
            float renderZ = (float) mc.getRenderManager().renderPosZ;

            float x = blockPos.getX() + 0.5f - renderX;
            float y = blockPos.getY() + 0.5f - renderY;
            float z = blockPos.getZ() + 0.5f - renderZ;

            float[] projectedCenter = project(x, y, z, factor);
            if (projectedCenter != null && projectedCenter[2] >= 0.0D && projectedCenter[2] < 1.0D) {
                return new float[]{projectedCenter[0],projectedCenter[1],projectedCenter[0],projectedCenter[1]};
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static float[] project(double x, double y, double z, int factor) {
        if (GLU.gluProject((float) x, (float) y, (float) z, ActiveRenderInfo.MODELVIEW, ActiveRenderInfo.PROJECTION, ActiveRenderInfo.VIEWPORT, ActiveRenderInfo.OBJECTCOORDS)) {
            return new float[]{(ActiveRenderInfo.OBJECTCOORDS.get(0) / factor), ((Display.getHeight() - ActiveRenderInfo.OBJECTCOORDS.get(1)) / factor), ActiveRenderInfo.OBJECTCOORDS.get(2)};
        }
        return null;
    }

    private List<TileEntity> tileEntityList() {
        return mc.theWorld.loadedTileEntityList.stream().filter(te -> mc.thePlayer.getDistance(te.getPos()) <= range.getValue())
                .sorted(Comparator.comparing(o -> mc.thePlayer.getDistance(((TileEntity) o).getPos())).reversed()).collect(Collectors.toList());
    }

    @EventTarget
    private void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.openContainer != null) {
            if (mc.thePlayer.openContainer instanceof ContainerChest) {
                if (isStealing) {
                    ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
                    if (menuCheck.get()) {

                        String name = container.getLowerChestInventory().getDisplayName().getUnformattedText().toLowerCase();
                        for (String str : list) {
                            if (name.contains(str))
                                return;
                        }
                    }

                    for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); ++i) {
                        if (container.getLowerChestInventory().getStackInSlot(i) != null && (timer.hasTimeElapsed((long) (MathUti.nextInt(minDelay.getValue().intValue(), maxDelay.getValue().intValue())) * 100L) || MathUti.nextInt(minDelay.getValue().intValue(), maxDelay.getValue().intValue()) == 0) && InventoryUtil.isValid(container.getLowerChestInventory().getStackInSlot(i))) {
                            slot = i;
                            mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                            timer.reset();
                        }
                    }
                    if (InventoryUtil.isInventoryFull() || InventoryUtil.isInventoryEmpty(container.getLowerChestInventory())) {
                        mc.thePlayer.closeScreen();
                        isStealing = false;
                    }
                }
            }

            int index;
            if (furnace.get()) {
                if (mc.thePlayer.openContainer instanceof ContainerFurnace container) {
                    if (isStealing) {
                        for (index = 0; index < container.tileFurnace.getSizeInventory(); ++index) {
                            if (container.tileFurnace.getStackInSlot(index) != null || (timer.hasTimeElapsed(MathUti.nextInt(minDelay.getValue().intValue(), maxDelay.getValue().intValue()) * 100L) || MathUti.nextInt(minDelay.getValue().intValue(), maxDelay.getValue().intValue()) == 0)) {
                                mc.playerController.windowClick(container.windowId, index, 0, 1, mc.thePlayer);
                                timer.reset();
                            }
                        }

                        if (isFurnaceEmpty(container)) {
                            mc.thePlayer.closeScreen();
                            isStealing = false;
                        }
                    }
                }
            }

            if (brewingStand.get()) {
                if (mc.thePlayer.openContainer instanceof ContainerBrewingStand container) {
                    if (isStealing) {
                        for (index = 0; index < container.tileBrewingStand.getSizeInventory(); ++index) {
                            if (container.tileBrewingStand.getStackInSlot(index) != null || (timer.hasTimeElapsed(MathUti.nextInt(minDelay.getValue().intValue(), maxDelay.getValue().intValue()) * 100L) || MathUti.nextInt(minDelay.getValue().intValue(), maxDelay.getValue().intValue()) == 0)) {
                                mc.playerController.windowClick(container.windowId, index, 0, 1, mc.thePlayer);
                                timer.reset();
                            }
                        }

                        if (isBrewingStandEmpty(container)) {
                            mc.thePlayer.closeScreen();
                            isStealing = false;
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof S2DPacketOpenWindow packetOpenWindow) {
            String title = packetOpenWindow.getWindowTitle().getUnformattedText().toLowerCase();
            for (String blacklisted : list) {
                if (title.contains(blacklisted)) {
                    isStealing = false;
                    return;
                }
            }
            if(startDelay.get())
                timer.reset();
            isStealing = (packetOpenWindow.getGuiId().equals("minecraft:chest") || packetOpenWindow.getGuiId().equals("minecraft:container") )|| furnace.get() && packetOpenWindow.getGuiId().equals("minecraft:furnace");
        }

        if (silentChestView.get()) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof C08PacketPlayerBlockPlacement wrapper) {
                if (wrapper.getPosition() != null) {
                    Block block = mc.theWorld.getBlockState(wrapper.getPosition()).getBlock();
                    if (block instanceof BlockContainer) {
                        currentContainerPos = wrapper.getPosition();
                    }
                }
            }
        }
    }

    private boolean isFurnaceEmpty(ContainerFurnace c) {
        for (int i = 0; i < c.tileFurnace.getSizeInventory(); ++i) {
            if (c.tileFurnace.getStackInSlot(i) == null) continue;
            return false;
        }
        return true;
    }

    private boolean isBrewingStandEmpty(ContainerBrewingStand c) {
        for (int i = 0; i < c.tileBrewingStand.getSizeInventory(); ++i) {
            if (c.tileBrewingStand.getStackInSlot(i) == null) continue;
            return false;
        }
        return true;
    }

    public static int getBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i + 36);
            if (slot.getHasStack()
                    && slot.getStack().getItem() instanceof ItemBlock) {
                return i;
            }
        }
        return -1;
    }

    public Vec3 getVec3(BlockPos pos) {
        Vec3 vector = new Vec3(pos);
        EnumFacing facing = Block.getFacingDirection(pos);
        double random = ThreadLocalRandom.current().nextDouble();

        switch (facing) {
            case NORTH -> vector.xCoord += random;
            case SOUTH -> {
                vector.xCoord += random;
                vector.zCoord += 1.0;
            }
            case WEST -> vector.zCoord += random;
            case EAST -> {
                vector.zCoord += random;
                vector.xCoord += 1.0;
            }
        }

        if (facing == EnumFacing.UP) {
            vector.xCoord += random;
            vector.zCoord += random;
            vector.yCoord += 1.0;
        } else {
            vector.yCoord += random;
        }

        return vector;
    }
}
