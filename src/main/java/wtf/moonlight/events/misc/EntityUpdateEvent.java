package wtf.moonlight.events.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;
import com.cubk.impl.Event;

@AllArgsConstructor
@Getter
public class EntityUpdateEvent implements Event {
    public final EntityLivingBase entity;
}
