package wtf.moonlight.module.impl.combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import com.cubk.EventTarget;
import wtf.moonlight.Client;
import wtf.moonlight.events.misc.TimerManipulationEvent;
import wtf.moonlight.events.player.MotionEvent;
import wtf.moonlight.events.player.MoveEvent;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.events.render.Render3DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MathUti;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RotationUtil;
import wtf.moonlight.util.player.SimulatedPlayer;
import wtf.moonlight.util.render.RenderUtil;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "TickBase", category = Categor.Combat)
public class TickBase extends Module {
    public final ListValue mode = new ListValue("Mode",new String[]{"Future","Past"},"Future",this);
    public final SliderValue delay = new SliderValue("Delay", 50, 0, 1000,50, this);
    public final SliderValue minActiveRange = new SliderValue("Min Active Range", 3f, 0.1f, 7f, 0.1f, this);
    public final SliderValue maxActiveRange = new SliderValue("Max Active Range", 7f, 0.1f, 7f, 0.1f, this);
    public final SliderValue maxTick = new SliderValue("Max Ticks", 4, 1, 20, this);
    public final BoolValue displayPredictPos = new BoolValue("Dislay Predict Pos",false,this);
    public final BoolValue check = new BoolValue("Check",false,this);
    public TimerUtil timer = new TimerUtil();
    public int skippedTick = 0;
    private long shifted, previousTime;
    public boolean working;
    private boolean firstAnimation = true;
    private final List<PredictProcess> predictProcesses = new ArrayList<>();
    public EntityPlayer target;
    private KillAura killAura;

    @Override
    public void onEnable() {
        shifted = 0;
        previousTime = 0;
        killAura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
    }

    public EntityPlayer getEffectiveTarget() {
        if (killAura != null && killAura.isEnabled() && killAura.target != null && !killAura.target.isDead) {
            return killAura.target instanceof EntityPlayer ? (EntityPlayer) killAura.target : null;
        }

        return PlayerUtil.getTarget(maxActiveRange.getValue() * 3);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        target = getEffectiveTarget();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(mode.getValue());
        if (mode.is("Future")) {
            if (event.getState() == MotionEvent.State.PRE)
                return;

            if (target == null || predictProcesses.isEmpty() || shouldStop()) {
                return;
            }

            if(timer.hasTimeElapsed(delay.getValue())) {
                if (shouldStart()) {
                    firstAnimation = false;
                    while (skippedTick <= maxTick.getValue() && !shouldStop()) {
                        ++skippedTick;
                        try {
                            mc.runTick();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    timer.reset();
                }
            }
            working = false;
        }
    }

    @EventTarget
    public void onTimerManipulation(TimerManipulationEvent event) {
        if (mode.is("Past")) {
            EntityLivingBase currentTarget = killAura != null && killAura.isEnabled() ? killAura.target : null;

            if (!(currentTarget instanceof EntityPlayer) || predictProcesses.isEmpty() || shouldStop()) {
                target = null;
                return;
            }

            target = (EntityPlayer) currentTarget;

            if (shouldStart() && timer.hasTimeElapsed(delay.getValue())) {
                shifted += event.getTime() - previousTime;
            }

            if (shifted >= maxTick.getValue() * (1000 / 20f)) {
                shifted = 0;
                timer.reset();
            }

            previousTime = event.getTime();
            event.setTime(event.getTime() - shifted);
        }
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        predictProcesses.clear();

        SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);

        simulatedPlayer.rotationYaw = RotationUtil.currentRotation != null ? RotationUtil.currentRotation[0] : mc.thePlayer.rotationYaw;

        for (int i = 0; i < (skippedTick != 0 ? skippedTick : maxTick.getValue()); i++) {
            simulatedPlayer.tick();
            predictProcesses.add(new PredictProcess(
                    simulatedPlayer.getPos(),
                    simulatedPlayer.isCollidedHorizontally
            ));
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if(displayPredictPos.get()) {
            Vec3 position = predictProcesses.get(predictProcesses.size() - 1).position;
            double x = position.xCoord - mc.getRenderManager().viewerPosX;
            double y = position.yCoord - mc.getRenderManager().viewerPosY;
            double z = position.zCoord - mc.getRenderManager().viewerPosZ;
            AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
            AxisAlignedBB axis = new AxisAlignedBB(box.minX - mc.thePlayer.posX + x, box.minY - mc.thePlayer.posY + y, box.minZ - mc.thePlayer.posZ + z, box.maxX - mc.thePlayer.posX + x, box.maxY - mc.thePlayer.posY + y, box.maxZ - mc.thePlayer.posZ + z);
            RenderUtil.drawAxisAlignedBB(axis,false, true, new Color(50, 255, 255, 150).getRGB());
        }
    }

    public boolean shouldStart(){
        if (killAura == null || !killAura.isEnabled() || killAura.target == null) {
            return false;
        }

        return predictProcesses.get((int) (maxTick.getValue() - 1)).position.distanceTo(target.getPositionVector()) <
                mc.thePlayer.getPositionVector().distanceTo(target.getPositionVector()) &&
                MathUti.inBetween(minActiveRange.getValue(), maxActiveRange.getValue(),
                        predictProcesses.get((int) (maxTick.getValue() - 1)).position.distanceTo(target.getPositionVector())) &&
                mc.thePlayer.canEntityBeSeen(target) &&
                target.canEntityBeSeen(mc.thePlayer) &&
                (RotationUtil.getRotationDifference(mc.thePlayer, target) <= 90 && check.get() || !check.get()) &&
                !predictProcesses.get((int) (maxTick.getValue() - 1)).isCollidedHorizontally;
    }

    public boolean shouldStop(){
        return mc.thePlayer.hurtTime != 0;
    }

    public boolean handleTick() {
        if (mode.is("Future")) {
            if (working || skippedTick < 0) return true;
            if (isEnabled() && skippedTick > 0) {
                --skippedTick;
                return true;
            }
        }
        return false;
    }

    public boolean freezeAnim(){
        if (skippedTick != 0) {
            if (!firstAnimation) {
                firstAnimation = true;
                return false;
            }
            return true;
        }
        return false;
    }

    public static class PredictProcess {
        private final Vec3 position;
        private final boolean isCollidedHorizontally;

        public PredictProcess(Vec3 position, boolean isCollidedHorizontally) {
            this.position = position;
            this.isCollidedHorizontally = isCollidedHorizontally;
        }
    }
}