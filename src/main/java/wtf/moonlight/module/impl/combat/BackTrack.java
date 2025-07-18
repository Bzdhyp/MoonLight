package wtf.moonlight.module.impl.combat;

import com.cubk.EventTarget;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import wtf.moonlight.Client;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.render.Render3DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.MultiBoolValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.animations.advanced.ContinualAnimation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "BackTrack", category = Categor.Combat)
public class BackTrack extends Module {
    public final MultiBoolValue packetsToDelay = new MultiBoolValue("Packets To Delay", Arrays.asList(
            new BoolValue("Velocity", true),
            new BoolValue("Velocity Explosion", false),
            new BoolValue("Time Update", false),
            new BoolValue("Keep Alive", false)
    ), this);

    public final MultiBoolValue targets = new MultiBoolValue("Targets", Arrays.asList(
            new BoolValue("Player", true),
            new BoolValue("Mob", false),
            new BoolValue("Animal", false),
            new BoolValue("Villager", false),
            new BoolValue("ArmorStand", false)
    ), this);

    private final SliderValue range = new SliderValue("Pre Aim Range", 4, 0, 15, this);
    private final SliderValue hitRange = new SliderValue("Max Hit Range", 6, 3, 6, this);
    private final SliderValue timerDelay = new SliderValue("Time", 4000, 0f, 30000, 100, this);

    private final BoolValue onlyWhenNeed = new BoolValue("Only WhenNeed", true, this);
    private final BoolValue onlyKillAura = new BoolValue("Only KillAura", true, this);

    // ESP Settings
    private final BoolValue esp = new BoolValue("ESP", true, this);
    private final ListValue espMode = new ListValue("ESP Mode", new String[]{"Box"}, "Box", this, esp::get);
    private final ListValue espColorMode = new ListValue("ESP Color", new String[]{"Sync", "Static", "Rainbow", "Health"}, "Static", this, esp::get);
    private final BoolValue espFill = new BoolValue("ESP Fill", true, this, esp::get);
    private final SliderValue espFillAlpha = new SliderValue("Fill Alpha", 50, 0, 255, 1, this, esp::get);
    private final BoolValue espOutline = new BoolValue("ESP Outline", true, this, esp::get);
    private final BoolValue espTargetOnly = new BoolValue("Target Only", true, this, esp::get);

    public boolean b;
    public boolean bb;

    private WorldClient lastWorld;
    public AxisAlignedBB boundingBox;
    private EntityLivingBase entity = null;
    private INetHandler packetListener = null;
    private final TimerUtil timeHelper = new TimerUtil();
    private final ArrayList<Packet<?>> packets = new ArrayList<>();

    private final ContinualAnimation animatedX = new ContinualAnimation();
    private final ContinualAnimation animatedY = new ContinualAnimation();
    private final ContinualAnimation animatedZ = new ContinualAnimation();
    private Vec3 realPosition = new Vec3(0.0D, 0.0D, 0.0D);
    private KillAura killAura;

    @Override
    public void onEnable() {
        super.onEnable();
        this.b = true;
        this.killAura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);

        if (mc.theWorld != null && mc.thePlayer != null) {
            for (final Entity entity : mc.theWorld.loadedEntityList) {
                if (entity instanceof EntityLivingBase entityLivingBase) {
                    entityLivingBase.realPosX = entityLivingBase.serverPosX;
                    entityLivingBase.realPosZ = entityLivingBase.serverPosZ;
                    entityLivingBase.realPosY = entityLivingBase.serverPosY;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (!this.packets.isEmpty() && this.packetListener != null) {
            this.resetPackets(this.packetListener);
        }
        this.packets.clear();
    }

    @EventTarget
    public void onEventEarlyTick(final TickEvent event) {
        setTag(timerDelay.getValue().intValue() + "ms");

        if (isEnabled(KillAura.class)) {
            this.entity = getModule(KillAura.class).target;
        } else {
            final List<Entity> listOfTargets = mc.theWorld.loadedEntityList.stream()
                    .filter(this::canAttacked)
                    .sorted(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceToEntity(entity)))
                    .toList();

            if (!listOfTargets.isEmpty()) {
                this.entity = (EntityLivingBase) listOfTargets.get(0);
            }

            if (this.onlyKillAura.get()) {
                this.entity = null;
            }
        }

        if (this.entity != null) {
            realPosition = new Vec3(entity.realPosX / 32.0, entity.realPosY / 32.0, entity.realPosZ / 32.0);
        }

        if (this.entity != null && mc.thePlayer != null && this.packetListener != null && mc.theWorld != null) {
            final double d0 = this.entity.realPosX / 32.0;
            final double d2 = this.entity.realPosY / 32.0;
            final double d3 = this.entity.realPosZ / 32.0;
            final double d4 = this.entity.serverPosX / 32.0;
            final double d5 = this.entity.serverPosY / 32.0;
            final double d6 = this.entity.serverPosZ / 32.0;
            final float f = this.entity.width / 2.0f;

            final AxisAlignedBB entityServerPos = new AxisAlignedBB(d4 - f, d5, d6 - f, d4 + f, d5 + this.entity.height, d6 + f);

            final Vec3 positionEyes = mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks);
            final double currentX = MathHelper.clamp_double(positionEyes.xCoord, entityServerPos.minX, entityServerPos.maxX);
            final double currentY = MathHelper.clamp_double(positionEyes.yCoord, entityServerPos.minY, entityServerPos.maxY);
            final double currentZ = MathHelper.clamp_double(positionEyes.zCoord, entityServerPos.minZ, entityServerPos.maxZ);

            final AxisAlignedBB entityPosMe = new AxisAlignedBB(d0 - f, d2, d3 - f, d0 + f, d2 + this.entity.height, d3 + f);

            final double realX = MathHelper.clamp_double(positionEyes.xCoord, entityPosMe.minX, entityPosMe.maxX);
            final double realY = MathHelper.clamp_double(positionEyes.yCoord, entityPosMe.minY, entityPosMe.maxY);
            final double realZ = MathHelper.clamp_double(positionEyes.zCoord, entityPosMe.minZ, entityPosMe.maxZ);

            double distance = this.hitRange.getValue();

            if (!mc.thePlayer.canEntityBeSeen(this.entity)) {
                distance = (Math.min(distance, 3.0));
            }

            final double collision = this.entity.getCollisionBorderSize();
            final double width = mc.thePlayer.width / 2.0f;
            final double mePosXForPlayer = mc.thePlayer.getLastServerPosition().xCoord + (mc.thePlayer.getSeverPosition().xCoord - mc.thePlayer.getLastServerPosition().xCoord) / MathHelper.clamp_int(mc.thePlayer.rotIncrement, 1, 3);
            final double mePosYForPlayer = mc.thePlayer.getLastServerPosition().yCoord + (mc.thePlayer.getSeverPosition().yCoord - mc.thePlayer.getLastServerPosition().yCoord) / MathHelper.clamp_int(mc.thePlayer.rotIncrement, 1, 3);
            final double mePosZForPlayer = mc.thePlayer.getLastServerPosition().zCoord + (mc.thePlayer.getSeverPosition().zCoord - mc.thePlayer.getLastServerPosition().zCoord) / MathHelper.clamp_int(mc.thePlayer.rotIncrement, 1, 3);
            AxisAlignedBB mePosForPlayerBox = new AxisAlignedBB(mePosXForPlayer - width, mePosYForPlayer, mePosZForPlayer - width, mePosXForPlayer + width, mePosYForPlayer + mc.thePlayer.height, mePosZForPlayer + width);
            mePosForPlayerBox = mePosForPlayerBox.expand(collision, collision, collision);
            final Vec3 entityPosEyes = new Vec3(d4, d5 + this.entity.getEyeHeight(), d6);
            final double bestX = MathHelper.clamp_double(entityPosEyes.xCoord, mePosForPlayerBox.minX, mePosForPlayerBox.maxX);
            final double bestY = MathHelper.clamp_double(entityPosEyes.yCoord, mePosForPlayerBox.minY, mePosForPlayerBox.maxY);
            final double bestZ = MathHelper.clamp_double(entityPosEyes.zCoord, mePosForPlayerBox.minZ, mePosForPlayerBox.maxZ);
            boolean b = entityPosEyes.distanceTo(new Vec3(bestX, bestY, bestZ)) > 3.0 || (mc.thePlayer.hurtTime < 8 && mc.thePlayer.hurtTime > 3);

            if (!this.onlyWhenNeed.get()) {
                b = true;
            }
            if (b && positionEyes.distanceTo(new Vec3(realX, realY, realZ)) > positionEyes.distanceTo(new Vec3(currentX, currentY, currentZ)) && mc.thePlayer.getSeverPosition().distanceTo(new Vec3(d0, d2, d3)) < distance && !this.timeHelper.reached(this.timerDelay.getValue().longValue())) {
                this.resetPackets(this.packetListener);
                this.timeHelper.reset();
            }
        }
    }

    @EventTarget
    public synchronized void onEventReadPacket(PacketEvent event) {
        if (event.getNetHandler() != null) {
            this.packetListener = event.getNetHandler();
        }

        if (event.getDirection() != EnumPacketDirection.CLIENTBOUND) return;

        Packet<?> p = event.getPacket();
        if (p instanceof S08PacketPlayerPosLook) this.resetPackets(event.getNetHandler());

        if (p instanceof S14PacketEntity packet) {
            final Entity entity1 = mc.theWorld.getEntityByID(packet.getEntityId());
            if (entity1 instanceof EntityLivingBase) {
                final EntityLivingBase entityLivingBase2;
                final EntityLivingBase entityLivingBase = entityLivingBase2 = (EntityLivingBase)entity1;
                entityLivingBase2.realPosX += packet.getX();
                entityLivingBase.realPosY += packet.getY();
                entityLivingBase.realPosZ += packet.getZ();

                if (entity1 == this.entity) {
                    realPosition.xCoord += packet.getX() / 32D;
                    realPosition.yCoord += packet.getY() / 32D;
                    realPosition.zCoord += packet.getZ() / 32D;
                }
            }
        }

        if (p instanceof S18PacketEntityTeleport packet2) {
            final Entity entity1 = mc.theWorld.getEntityByID(packet2.getEntityId());

            if (entity1 instanceof EntityLivingBase entityLivingBase) {
                entityLivingBase.realPosX = packet2.getX();
                entityLivingBase.realPosY = packet2.getY();
                entityLivingBase.realPosZ = packet2.getZ();

                if (entity1 == this.entity) {
                    realPosition = new Vec3(packet2.getX() / 32D, packet2.getY() / 32D, packet2.getZ() / 32D);
                }
            }
        }

        if (this.entity == null) {
            this.resetPackets(event.getNetHandler());
            return;
        }

        if (mc.theWorld != null && mc.thePlayer != null) {
            if (this.lastWorld != mc.theWorld) {
                this.resetPackets(event.getNetHandler());
                this.lastWorld = mc.theWorld;
                return;
            }
            this.addPackets(p, event);
        }

        this.lastWorld = mc.theWorld;
    }

    private boolean canAttacked(Entity entity) {
        if (!(entity instanceof EntityLivingBase livingEntity) || entity == mc.thePlayer) {
            return false;
        }

        if (livingEntity.isInvisible() || livingEntity.deathTime > 1 || livingEntity.isDead || livingEntity.ticksExisted < 50) {
            return false;
        }

        if (mc.thePlayer.getDistanceToEntity(livingEntity) >= this.range.getValue()) {
            return false;
        }

        if (livingEntity instanceof EntityArmorStand && !targets.isEnabled("Armor Stand")) {
            return false;
        }

        if (livingEntity instanceof EntityAnimal && !targets.isEnabled("Animal")) {
            return false;
        }

        if (livingEntity instanceof EntityMob && !targets.isEnabled("Mob")) {
            return false;
        }

        if (livingEntity instanceof EntityVillager && !targets.isEnabled("Villager")) {
            return false;
        }

        if (livingEntity instanceof EntityPlayer player) {
            String name = player.getName();

            if (name.equals("§aShop") || name.equals("SHOP") || name.equals("UPGRADES")) {
                return false;
            }

            if (getModule(KillAura.class).filter.isEnabled("Teams") && PlayerUtil.isInTeam(player)) {
                return false;
            }

            if (getModule(KillAura.class).filter.isEnabled("Friends") &&
                    Client.INSTANCE.getFriendManager().isFriend(player)) {
                return false;
            }

            if (isEnabled(AntiBot.class)) return !getModule(AntiBot.class).isBot(player);
        }

        return true;
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || !esp.get()) return;

        EntityLivingBase renderTarget = this.entity;
        if (!shouldRenderESP(renderTarget)) return;

        if (killAura != null && killAura.isEnabled() && killAura.target != renderTarget && espTargetOnly.get()) {
            return;
        }

        animatedX.animate((float)(realPosition.xCoord - mc.getRenderManager().viewerPosX), 20);
        animatedY.animate((float)(realPosition.yCoord - mc.getRenderManager().viewerPosY), 20);
        animatedZ.animate((float)(realPosition.zCoord - mc.getRenderManager().viewerPosZ), 20);

        AxisAlignedBB box = renderTarget.getEntityBoundingBox().expand(0.1D, 0.1D, 0.1D);
        AxisAlignedBB axis = new AxisAlignedBB(
                box.minX - renderTarget.posX + animatedX.getOutput(),
                box.minY - renderTarget.posY + animatedY.getOutput(),
                box.minZ - renderTarget.posZ + animatedZ.getOutput(),
                box.maxX - renderTarget.posX + animatedX.getOutput(),
                box.maxY - renderTarget.posY + animatedY.getOutput(),
                box.maxZ - renderTarget.posZ + animatedZ.getOutput()
        );

        Color color = getESPColor(renderTarget);

        if (espMode.is("Box")) {
            RenderUtil.drawAxisAlignedBB(axis, espFill.get(), espOutline.get(), color.getRGB());
        }
    }

    private boolean shouldRenderESP(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;

        if (espTargetOnly.get()) {
            return killAura != null && killAura.isEnabled() && entity.equals(killAura.target);
        }

        return mc.thePlayer.getDistanceToEntity(entity) <= range.getValue();
    }

    private Color getESPColor(EntityLivingBase entity) {
        int fillAlpha = espFillAlpha.getValue().intValue();

        Color baseColor = switch (espColorMode.getValue()) {
            case "Rainbow" -> {
                float hue = (System.currentTimeMillis() % 2000) / 2000f;
                yield Color.getHSBColor(hue, 1f, 1f);
            }
            case "Health" -> {
                float healthPercent = entity.getHealth() / entity.getMaxHealth();
                yield new Color(
                        (int) (255 * (1 - healthPercent)),
                        (int) (255 * healthPercent),
                        0
                );
            }
            case "Sync" -> new Color(Client.INSTANCE.getModuleManager().getModule(Interface.class).color());
            default -> new Color(50, 255, 255); // Static
        };

        return new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                fillAlpha
        );
    }

    private void resetPackets(INetHandler netHandler) {
        if (!this.packets.isEmpty()) {
            while (!this.packets.isEmpty()) {
                Packet packet = this.packets.get(0);

                try {
                    if (packet != null) {
                        if (isEnabled(Velocity.class) && (getModule(Velocity.class).mode.is("Boost"))) {
                            if (!(packet instanceof S12PacketEntityVelocity)) {
                                if (!(packet instanceof S27PacketExplosion) || !getModule(Velocity.class).mode.is("Boost")) {
                                    packet.processPacket(netHandler);
                                }
                            }
                        } else {
                            packet.processPacket(netHandler);
                        }
                    }
                } catch (ThreadQuickExitException e) {
                    // e.printStackTrace();
                }
                this.packets.remove(this.packets.get(0));
            }
        }
    }

    private void addPackets(Packet<?> packet, PacketEvent eventReadPacket) {
        synchronized (this.packets) {
            if (this.delayPackets(packet)) {
                this.packets.add(packet);
                eventReadPacket.setCancelled(true);
            }
        }
    }

    private boolean delayPackets(Packet<?> packet) {
        if (mc.currentScreen != null) return false;

        if (packet instanceof S03PacketTimeUpdate) return packetsToDelay.isEnabled("Time Update");

        if (packet instanceof S00PacketKeepAlive) return packetsToDelay.isEnabled("Keep Alive");

        if (packet instanceof S12PacketEntityVelocity) return packetsToDelay.isEnabled("Velocity");

        if (packet instanceof S27PacketExplosion) return packetsToDelay.isEnabled("Velocity Explosion");

        if (packet instanceof S19PacketEntityStatus entityStatus) {
            return entityStatus.getOpCode() != 2 || !(mc.theWorld.getEntityByID(entityStatus.getEntityId()) instanceof EntityLivingBase);
        }

        return !(packet instanceof S06PacketUpdateHealth) && !(packet instanceof S29PacketSoundEffect) && !(packet instanceof S3EPacketTeams) && !(packet instanceof S0CPacketSpawnPlayer);
    }
}