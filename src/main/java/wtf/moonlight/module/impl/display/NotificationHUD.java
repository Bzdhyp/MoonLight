package wtf.moonlight.module.impl.display;

import com.cubk.EventTarget;
import net.minecraft.client.gui.ScaledResolution;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.gui.notification.NotificationManager;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;

@ModuleInfo(name = "Notification", category = Categor.Display)
public class NotificationHUD extends Module {
    public final ListValue notificationMode = new ListValue("Notification Mode", new String[]{"Default", "Test","Type 2","Type 3","Type 4","Type 5", "Test2","Exhi", "Augustus", "Augustus 2"}, "Default", this);
    public final BoolValue centerNotif = new BoolValue("Center Notification",true, this,() -> notificationMode.is("Exhi"));

    public NotificationHUD() {
        this.setEnabled(true);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        NotificationManager.publish(new ScaledResolution(mc),false);
    }

    @EventTarget
    public void onShader2D(Shader2DEvent event) {
        NotificationManager.publish(new ScaledResolution(mc),true);
    }
}
