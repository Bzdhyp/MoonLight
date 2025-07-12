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
package wtf.moonlight.util.render;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static net.minecraft.client.renderer.GlStateManager.glBegin;
import static net.minecraft.client.renderer.GlStateManager.glEnd;
import static org.lwjgl.opengl.GL11.*;

public class GLUtil {

    private static final FloatBuffer windowPosition = GLAllocation.createDirectFloatBuffer(4);
    private static final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private static final FloatBuffer modelMatrix = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer projectionMatrix = GLAllocation.createDirectFloatBuffer(16);
    private static final float[] BUFFER = new float[3];

    public static void enableDepth() {
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
    }

    public static void disableDepth() {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
    }
    public static void setupRendering(int mode, Runnable runnable) {
        glBegin(mode);
        runnable.run();
        glEnd();
    }
    public static void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void endBlend() {
        GlStateManager.disableBlend();
    }

    public static void setup2DRendering(Runnable f) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        f.run();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }

    public static float[] project2D(float x,
                                    float y,
                                    float z,
                                    int scaleFactor) {
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        if (GLU.gluProject(x, y, z, modelMatrix, projectionMatrix, viewport, windowPosition)) {
            BUFFER[0] = windowPosition.get(0) / scaleFactor;
            BUFFER[1] = (Display.getHeight() - windowPosition.get(1)) / scaleFactor;
            BUFFER[2] = windowPosition.get(2);
            return BUFFER;
        }

        return null;
    }

    public static void render(int mode, Runnable render){
        glBegin(mode);
        render.run();
        glEnd();
    }

}
