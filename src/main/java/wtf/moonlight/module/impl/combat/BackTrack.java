package wtf.moonlight.module.impl.combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import com.cubk.EventTarget;
import wtf.moonlight.Client;
import wtf.moonlight.component.PingSpoofComponent;
import wtf.moonlight.events.misc.TickEvent;
import wtf.moonlight.events.packet.PacketEvent;
import wtf.moonlight.events.player.AttackEvent;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.events.render.Render3DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.animations.advanced.ContinualAnimation;
import wtf.moonlight.util.render.RenderUtil;

import java.awt.*;

@ModuleInfo(name = "BackTrack", category = Categor.Combat)
public class BackTrack extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Tick", "Ping"}, "Tick", this);
    private final SliderValue amount = new SliderValue("Amount", 1.0f, 1.0f, 3.0f, 0.1f, this, () -> mode.is("Tick"));
    private final SliderValue range = new SliderValue("Range", 3.0f, 1.0f, 5.0f, 0.1f, this, () -> mode.is("Tick"));
    private final SliderValue interval = new SliderValue("Interval Tick", 1, 0, 10, 1f, this, () -> mode.is("Tick"));
    private final SliderValue maxPingSpoof = new SliderValue("Max Ping Spoof", 1000, 50, 10000, 1, this);

    private final BoolValue esp = new BoolValue("ESP", true, this);
    private final ListValue espMode = new ListValue("ESP Mode", new String[]{"Box"}, "Box", this, esp::get);
    private final ListValue espColorMode = new ListValue("ESP Color", new String[]{"Sync", "Static", "Rainbow", "Health"}, "Static", this, esp::get);
    private final BoolValue espFill = new BoolValue("ESP Fill", true, this, esp::get);
    private final SliderValue espFillAlpha = new SliderValue("Fill Alpha", 50, 0, 255, 1, this, esp::get);
    private final BoolValue espOutline = new BoolValue("ESP Outline", true, this, esp::get);
    private final BoolValue espTargetOnly = new BoolValue("Target Only", true, this, esp::get);

    private EntityLivingBase target;
    private Vec3 realPosition = new Vec3(0.0D, 0.0D, 0.0D);
    private final ContinualAnimation animatedX = new ContinualAnimation();
    private final ContinualAnimation animatedY = new ContinualAnimation();
    private final ContinualAnimation animatedZ = new ContinualAnimation();
    private int tick = 0;
    private KillAura killAura;

    @Override
    public void onEnable() {
        killAura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
    }

    @EventTarget
    public void onDisable() {
        tick = 0;
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (killAura != null && killAura.isEnabled() &&
                event.getTargetEntity() instanceof EntityLivingBase newTarget && !newTarget.isDead) {
            target = newTarget;
            realPosition = new Vec3(target.posX, target.posY, target.posZ);
        } else {
            target = null;
        }
    }

    private EntityLivingBase getEffectiveTarget() {
        if (killAura != null && killAura.isEnabled() && killAura.target != null && !killAura.target.isDead) {
            return killAura.target;
        }

        return target;
    }

    @EventTarget
    public void onTick(TickEvent e) {
        setTag(mode.getValue());
        EntityLivingBase currentTarget = getEffectiveTarget();

        if (mode.is("Tick")) {
            if (currentTarget == null) {
                target = null;
                return;
            }

            if (this.tick <= this.interval.getValue())
                this.tick++;
            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= this.range.getValue()
                    && (new Vec3(currentTarget.posX, currentTarget.posY, currentTarget.posZ)).distanceTo(this.realPosition) < this.amount.getValue()
                    && this.tick > this.interval.getValue()) {
                currentTarget.posX = currentTarget.prevPosX;
                currentTarget.posY = currentTarget.prevPosY;
                currentTarget.posZ = currentTarget.prevPosZ;
                tick = 0;
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent e) {
        if (mode.is("Ping")) {
            EntityLivingBase currentTarget = killAura != null && killAura.isEnabled() ? killAura.target : null;

            if (currentTarget == null) {
                target = null;
                PingSpoofComponent.disable();
                PingSpoofComponent.dispatch();
                return;
            }

            if (currentTarget != target) {
                target = currentTarget;
                realPosition = new Vec3(target.posX, target.posY, target.posZ);
            }

            if (!(mc.thePlayer.isSwingInProgress && killAura.isEnabled())) {
                PingSpoofComponent.disable();
                PingSpoofComponent.dispatch();
                return;
            }

            double realDistance = realPosition.distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            double clientDistance = target.getDistanceToEntity(mc.thePlayer);

            boolean shouldActivate = realDistance > clientDistance && realDistance > 2.3 && realDistance < 5.9;

            if (shouldActivate) {
                PingSpoofComponent.spoof(maxPingSpoof.getValue().intValue(), true, true, true, true);
            } else {
                PingSpoofComponent.disable();
                PingSpoofComponent.dispatch();
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (target == null) return;

        if (mode.is("Tick")) {
            if (event.getPacket() instanceof S18PacketEntityTeleport s18) {
                if (s18.getEntityId() == target.getEntityId())
                    realPosition = new Vec3(s18.getX() / 32.0D, s18.getY() / 32.0D, s18.getZ() / 32.0D);
            }
        } else {
            if (packet instanceof S14PacketEntity s14PacketEntity) {
                if (s14PacketEntity.getEntityId() == target.getEntityId()) {
                    realPosition.xCoord += s14PacketEntity.getPosX() / 32D;
                    realPosition.yCoord += s14PacketEntity.getPosY() / 32D;
                    realPosition.zCoord += s14PacketEntity.getPosZ() / 32D;
                }
            } else if (packet instanceof S18PacketEntityTeleport s18PacketEntityTeleport) {

                if (s18PacketEntityTeleport.getEntityId() == target.getEntityId()) {
                    realPosition = new Vec3(s18PacketEntityTeleport.getX() / 32D, s18PacketEntityTeleport.getY() / 32D, s18PacketEntityTeleport.getZ() / 32D);
                }
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || !esp.get()) return;

        EntityLivingBase renderTarget = getEffectiveTarget();
        if (!shouldRenderESP(renderTarget)) return;

        if (killAura != null && killAura.isEnabled() && killAura.target != renderTarget) {
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
}