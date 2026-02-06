package me.hypericats.fedmaps.config.settings;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import me.hypericats.fedmaps.config.Setting;

public class ESPSetting implements Setting {
    private boolean value;
    @Override
    public String getName() {
        return "esp";
    }

    @Override
    public ArgumentType<?> getArgument() {
        return BoolArgumentType.bool();
    }

    @Override
    public String getAsString() {
        return String.valueOf(value);
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public void setFromObject(Object value) {
        this.value = (boolean) value;
    }
}
