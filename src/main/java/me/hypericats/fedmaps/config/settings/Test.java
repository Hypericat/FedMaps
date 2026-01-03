package me.hypericats.fedmaps.config.settings;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import me.hypericats.fedmaps.config.Setting;

public class Test implements Setting {
    private float value;
    @Override
    public String getName() {
        return "test";
    }

    @Override
    public ArgumentType<?> getArgument() {
        return FloatArgumentType.floatArg();
    }

    @Override
    public String getAsString() {
        return String.valueOf(value);
    }

    @Override
    public void setFromObject(Object value) {
        this.value = (float) value;
    }
}
