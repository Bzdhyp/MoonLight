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
package wtf.moonlight.module.impl.visual;

import lombok.Getter;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ColorValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.util.render.RenderUtil;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@Getter
@ModuleInfo(name = "Chams", category = Categor.Visual)
public class Chams extends Module {
    private final ListValue modeValue = new ListValue("Mode", new String[]{"Color", "CSGO"}, "Color", this);
    private final ColorValue visibleColorValue = new ColorValue("Visible", Color.RED, this);
    private final ColorValue invisibleColorValue = new ColorValue("Invisible", Color.GREEN, this);
    private final BoolValue rainbow = new BoolValue("Rainbow", false, this);

    public boolean isValid(Entity entity) {
        if (entity.isInvisible()) return false;

        if (entity.isDead || entity.isInvisible()) return false;

        if (entity instanceof EntityPlayer) {
            if (entity == mc.thePlayer) {
                return mc.gameSettings.thirdPersonView != 0;
            }
            return !entity.getDisplayName().getUnformattedText().contains("[NPC");
        }
        return false;
    }
}
