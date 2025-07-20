package wtf.moonlight.module.impl.display.island;

import com.cubk.EventTarget;
import org.lwjglx.opengl.Display;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.events.render.Shader2DEvent;
import wtf.moonlight.module.impl.display.island.impl.InfoIsland;

import java.util.*;

import static wtf.moonlight.util.misc.InstanceAccess.mc;

public class IslandManager {
    private final List<Island> islandList = new ArrayList<>();

    public IslandManager() {
        Client.INSTANCE.getEventManager().register(this);

        register(new InfoIsland());
    }

    private void register(Island island) {
        this.islandList.add(island);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mc.gameSettings.showDebugInfo) return;

        if (Display.isVisible()) {
            for (Island island : islandList) {
                if (island.shouldRender()) {
                    island.render();
                }
            }
        }
    }

    @EventTarget
    public void onShader(Shader2DEvent event) {
        if (mc.gameSettings.showDebugInfo) return;

        if (Display.isVisible()) {
            for (Island island : islandList) {
                if (island.shouldRender()) {
                    island.onShader(event);
                }
            }
        }
    }
}