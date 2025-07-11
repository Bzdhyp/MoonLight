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
package wtf.moonlight.module.values.impl;

import lombok.Getter;
import lombok.Setter;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.values.Value;

import java.util.function.Supplier;

@Getter
@Setter
public class StringValue extends Value {
    private String text;
    private boolean onlyNumber;

    public StringValue(String name, String text, Module module, Supplier<Boolean> visible) {
        super(name, module, visible);
        this.text = text;
        this.onlyNumber = false;
    }

    public StringValue(String name, String text, Module module) {
        super(name, module, () -> true);
        this.text = text;
    }

    public StringValue(String name, String text, boolean onlyNumber, Module module, Supplier<Boolean> visible) {
        super(name, module, visible);
        this.text = text;
        this.onlyNumber = onlyNumber;
    }

    public StringValue(String name, String text, boolean onlyNumber, Module module) {
        super(name, module, () -> true);
        this.text = text;
        this.onlyNumber = onlyNumber;
    }

    public String getValue() {
        return text;
    }
}
