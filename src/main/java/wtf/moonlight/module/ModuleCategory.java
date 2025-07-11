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
package wtf.moonlight.module;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModuleCategory {

    Combat("Combat","A"),
    Movement("Movement","B"),
    Player("Player","D"),
    Misc("Misc","C"),
    Visual("Visuals","F"),
    Display("Display", "G"),
    Config("Configs","A"),
    Search("Search","A");

    private final String name;
    public String icon;
}