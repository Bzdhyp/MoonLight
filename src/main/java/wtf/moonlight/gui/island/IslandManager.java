package wtf.moonlight.gui.island;

import wtf.moonlight.module.Module;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class IslandManager {
    private final Map<Class<? extends Island>, Island> islands = new ConcurrentHashMap<>();
}
