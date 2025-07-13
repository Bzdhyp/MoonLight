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
package wtf.moonlight.module.impl.combat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.MovementCorrection;
import wtf.moonlight.util.player.PlayerUtil;
import wtf.moonlight.util.player.RotationUtil;

import java.util.Objects;

@ModuleInfo(name = "BowAimBot", category = Categor.Combat)
public class BowAimBot extends Module {

    private final SliderValue fov = new SliderValue("FOV",180,1,180,this);
    private final SliderValue range = new SliderValue("Range", 30, 3, 200, 1, this);
    private final SliderValue predictSize = new SliderValue("Predict Size", 2, 0.1f, 5, 0.1f, this);
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
    private final BoolValue moveFix = new BoolValue("Move Fix", true, this);
    public final ListValue moveFixMode = new ListValue("Move Fix Mode", new String[]{"Silent", "Strict"}, "Silent", this);
    private EntityPlayer target;

    @EventTarget
    public void onUpdate(UpdateEvent event) {

        target = PlayerUtil.getTarget(range.getValue());

        if (target == null)
            return;
        if ((RotationUtil.getRotationDifference(target) <= fov.getValue() || fov.getValue() == 180) && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem()) {
            float[] finalRotation = RotationUtil.faceTrajectory(target, true, predictSize.getValue());

            if (customRotationSetting.get()) {
                RotationUtil.setRotation(finalRotation,smoothMode.getValue(), moveFix.get() ? (Objects.equals(moveFixMode.getValue(), "Silent") ? MovementCorrection.SILENT : MovementCorrection.STRICT) : MovementCorrection.OFF, minYawRotSpeed.getValue(), maxYawRotSpeed.getValue(), minPitchRotSpeed.getValue(), maxPitchRotSpeed.getValue(),
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
                RotationUtil.setRotation(finalRotation, moveFix.get() ? (Objects.equals(moveFixMode.getValue(), "Silent") ? MovementCorrection.SILENT : MovementCorrection.STRICT) : MovementCorrection.OFF);
            }
        }
    }
}
