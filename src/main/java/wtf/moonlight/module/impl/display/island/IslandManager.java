package wtf.moonlight.module.impl.display.island;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IslandManager {
    private final Map<Class<? extends Island>, Island> islands = new ConcurrentHashMap<>();
}
