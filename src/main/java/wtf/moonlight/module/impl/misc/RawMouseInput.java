package wtf.moonlight.module.impl.misc;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleCategory;
import wtf.moonlight.module.ModuleInfo;

@ModuleInfo(name = "RawMouseInput",category = ModuleCategory.Misc)
public class RawMouseInput extends Module {

    @Override
    public void onEnable(){
        if (Mouse.isCreated()) {
            if (GLFW.glfwRawMouseMotionSupported()) {
                GLFW.glfwSetInputMode(Display.getWindow(), GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_FALSE);
            }
        }
    }

    @Override
    public void onDisable(){
        if (Mouse.isCreated()) {
            if (GLFW.glfwRawMouseMotionSupported()) {
                GLFW.glfwSetInputMode(Display.getWindow(), GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
            }
        }
    }
}
