package wtf.moonlight.module.impl.visual;

import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.MultiBoolValue;
import wtf.moonlight.module.values.impl.SliderValue;

import java.util.Arrays;

@ModuleInfo(name = "Camera", category = Categor.Visual)
public class Camera extends Module {
    public final MultiBoolValue setting = new MultiBoolValue("Option", Arrays.asList(
            new BoolValue("View Clip", true),
            new BoolValue("No Hurt Cam", true),
            new BoolValue("Third Person Distance", false),
            new BoolValue("Bright Players", false),
            new BoolValue("Motion Camera",false),
            new BoolValue("Better Bobbing",false)
    ), this);

    public final SliderValue cameraDistance = new SliderValue("Distance", 4.0f, 1.0f, 8.0f, 1.0f, this, () -> setting.isEnabled("Third Person Distance"));
    public final SliderValue interpolation = new SliderValue("Motion Interpolation", 0.15f, 0.05f, 0.5f, 0.05f,this, () -> setting.isEnabled("Motion Camera"));

    public final BoolValue noFire = new BoolValue("No Fire", false, this);
    public final SliderValue noFireValue = new SliderValue("No Fire Value", -0.7f, -1.0f, 0.4f, 0.1f, this, noFire::get);

    public final BoolValue noFovValue = new BoolValue("NoFov", false, this);
    public final SliderValue fovValue = new SliderValue("Fov", 1.0f, 0.0f, 4.0f, 0.1f, this, noFovValue::get);

    public final BoolValue aspectRatio = new BoolValue("Aspect Ratio", false, this);
    public final SliderValue aspectValue = new SliderValue("Aspect Value", 1.0f, 0.1f, 5.0f, 0.1f, this, aspectRatio::get);
}
