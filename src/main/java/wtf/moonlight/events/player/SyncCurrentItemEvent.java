package wtf.moonlight.events.player;

import com.cubk.impl.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class SyncCurrentItemEvent implements Event {
    private int slot;
}