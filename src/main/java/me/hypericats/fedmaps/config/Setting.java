package me.hypericats.fedmaps.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

public interface Setting {
    String getName();

    default ArgumentType<?> getArgument() {
        return StringArgumentType.string();
    }

    String getAsString();
    void setFromObject(Object value);
}
