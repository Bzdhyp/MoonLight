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
package wtf.moonlight.events.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.cubk.impl.CancellableEvent;

@Getter
@AllArgsConstructor
public class SendMessageEvent extends CancellableEvent {
    private final String message;
}
