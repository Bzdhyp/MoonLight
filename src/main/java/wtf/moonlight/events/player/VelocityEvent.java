package wtf.moonlight.events.player;

import com.cubk.impl.Event;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VelocityEvent implements Event {
    private double reduceAmount;

    public VelocityEvent(double reduceAmount) {
        this.reduceAmount = reduceAmount;
    }
}
