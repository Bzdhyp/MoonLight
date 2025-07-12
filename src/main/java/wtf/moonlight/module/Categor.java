package wtf.moonlight.module;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Categor {

    Combat("Combat","A"),
    Movement("Movement","B"),
    Player("Player","D"),
    Misc("Misc","C"),
    Visual("Visuals","F"),
    Display("Display", "G"),
    Config("Configs","A"),
    Search("Search","A");

    private final String name;
    public final String icon;
}