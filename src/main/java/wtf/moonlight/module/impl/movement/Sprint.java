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
package wtf.moonlight.module.impl.movement;

import net.minecraft.client.settings.KeyBinding;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.util.player.MovementCorrection;
import wtf.moonlight.util.player.MovementUtil;
import wtf.moonlight.util.player.RotationUtil;

@ModuleInfo(name = "Sprint", category = Categor.Movement)
public class Sprint extends Module {

    private final BoolValue omni = new BoolValue("Omni", false, this);
    private final BoolValue silent = new BoolValue("Silent", false, this);
    private final BoolValue rotate = new BoolValue("Rotate", false, this);
    private final BoolValue onlyOnGround = new BoolValue("Only On Ground", true, this, rotate::get);

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled(Scaffold.class))
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);

        if(omni.get()){
            mc.thePlayer.omniSprint = MovementUtil.isMoving();
        }

        if (silent.get()) {
            mc.thePlayer.serverSprintState = false;
        }

        if (rotate.get()) {
            if (onlyOnGround.get() && !mc.thePlayer.onGround) {
                return;
            }

            float[] finalRotation = new float[]{MovementUtil.getRawDirection(), mc.thePlayer.rotationPitch};

            RotationUtil.setRotation(finalRotation, MovementCorrection.SILENT);
        }

    }

    @Override
    public void onDisable(){
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        mc.thePlayer.omniSprint = false;
    }
}