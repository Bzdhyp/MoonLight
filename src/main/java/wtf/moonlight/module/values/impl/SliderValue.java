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
import net.minecraft.util.MathHelper;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.values.Value;

import java.util.function.Supplier;

public class SliderValue extends Value {
    private float value;
    @Getter
    private final float min;
    @Getter
    private final float max;
    @Getter
    private final float increment;


    public SliderValue(String name, float value, float min, float max, float increment, Module module, Supplier<Boolean> visible) {
        super(name, module, visible);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public SliderValue(String name, float value, float min, float max, Module module, Supplier<Boolean> visible) {
        super(name, module, visible);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = 1;
    }

    public SliderValue(String name, float value, float min, float max, float increment, Module module) {
        super(name, module, () -> true);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public SliderValue(String name, float value, float min, float max, Module module) {
        super(name, module, () -> true);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = 1;
    }

    public Float getValue() {
        return MathHelper.clamp_float(value, getMin(), getMax());
    }

    public void setValue(float value) {
        this.value = MathHelper.clamp_float(value, getMin(), getMax());
    }
}
