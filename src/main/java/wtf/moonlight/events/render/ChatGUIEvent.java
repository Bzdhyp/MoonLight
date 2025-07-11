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
package wtf.moonlight.events.render;

import lombok.AllArgsConstructor;
import com.cubk.impl.Event;

@AllArgsConstructor
public class ChatGUIEvent implements Event {
    public int mouseX, mouseY;
}
