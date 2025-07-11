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
package wtf.moonlight.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.cubk.impl.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public class MoveEvent extends CancellableEvent {
    public double x, y, z;
}