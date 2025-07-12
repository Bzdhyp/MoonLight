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

import de.florianmichael.viamcp.fixes.AttackOrder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.item.ItemFireball;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.movement.LongJump;
import wtf.moonlight.module.impl.movement.Scaffold;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.TimerUtil;
import wtf.moonlight.util.player.MovementCorrection;
import wtf.moonlight.util.player.RotationUtil;

@ModuleInfo(name = "AntiFireball", category = Categor.Player)
public class AntiFireball extends Module {
    private final SliderValue aps = new SliderValue("Aps", 9, 1, 20, this);
    public final SliderValue range = new SliderValue("Range", 6.0F, 2.0F, 6F, .1f, this);
    private final BoolValue customRotationSetting = new BoolValue("Custom Rotation Setting", false, this);
    private final ListValue smoothMode = new ListValue("Rotations Smooth", RotationUtil.smoothModes, RotationUtil.smoothModes[0], this, customRotationSetting::get);
    private final SliderValue minYawRotSpeed = new SliderValue("Min Yaw Rotation Speed", 45, 1,180,1, this, customRotationSetting::get);
    private final SliderValue minPitchRotSpeed = new SliderValue("Min Pitch Rotation Speed", 45, 1,180,1, this, customRotationSetting::get);
    private final SliderValue maxYawRotSpeed = new SliderValue("Max Yaw Rotation Speed", 90, 1,180,1, this, customRotationSetting::get);
    private final SliderValue maxPitchRotSpeed = new SliderValue("Max Pitch Rotation Speed", 90, 1,180,1, this, customRotationSetting::get);
    private final SliderValue bezierP0 = new SliderValue("Bezier P0", 0f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP1 = new SliderValue("Bezier P1", 0.05f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP2 = new SliderValue("Bezier P2", 0.2f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP3 = new SliderValue("Bezier P3", 0.4f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP4 = new SliderValue("Bezier P4", 0.6f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP5 = new SliderValue("Bezier P5", 0.8f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP6 = new SliderValue("Bezier P6", 0.95f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue bezierP7 = new SliderValue("Bezier P7", 0.1f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtil.smoothModes[1]) || smoothMode.is(RotationUtil.smoothModes[8])));
    private final SliderValue elasticity = new SliderValue("Elasticity", 0.3f, 0.1f, 1f,0.01f, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && smoothMode.is(RotationUtil.smoothModes[7]));
    private final SliderValue dampingFactor = new SliderValue("Damping Factor", 0.5f, 0.1f, 1f,0.01f, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && smoothMode.is(RotationUtil.smoothModes[7]));
    public final BoolValue smoothlyResetRotation = new BoolValue("Smoothly Reset Rotation", true, this, customRotationSetting::get);
    private final BoolValue moveFix = new BoolValue("Move Fix", false, this);
    private final TimerUtil attackTimer = new TimerUtil();

    @EventTarget
    public void onUpdate(UpdateEvent event) {

        if (isEnabled(LongJump.class) || isEnabled(Scaffold.class) || mc.thePlayer.getHeldItem().getItem() instanceof ItemFireball)
            return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityFireball && entity.getDistanceToEntity(mc.thePlayer) < range.getValue() && isEnabled(LongJump.class)) {
                if (attackTimer.hasTimeElapsed((long) (1000L / (aps.getValue() + 2)))) {

                    float[] finalRotation = RotationUtil.getAngles(entity);

                    if (customRotationSetting.get()) {
                        RotationUtil.setRotation(finalRotation,smoothMode.getValue(), moveFix.get() ?  MovementCorrection.SILENT : MovementCorrection.OFF, minYawRotSpeed.getValue(), maxYawRotSpeed.getValue(), minPitchRotSpeed.getValue(), maxPitchRotSpeed.getValue(),
                                bezierP0.getValue(),
                                bezierP1.getValue(),
                                bezierP2.getValue(),
                                bezierP3.getValue(),
                                bezierP4.getValue(),
                                bezierP5.getValue(),
                                bezierP6.getValue(),
                                bezierP7.getValue(),
                                elasticity.getValue(),
                                dampingFactor.getValue(), smoothlyResetRotation.get());
                    } else {
                        RotationUtil.setRotation(finalRotation, moveFix.get() ?  MovementCorrection.SILENT : MovementCorrection.OFF);
                    }
                    
                    AttackOrder.sendFixedAttack(mc.thePlayer,entity);
                    attackTimer.reset();
                }
            }
        }
    }
}
