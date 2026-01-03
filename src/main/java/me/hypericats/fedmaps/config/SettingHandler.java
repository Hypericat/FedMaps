package me.hypericats.fedmaps.config;

import me.hypericats.fedmaps.config.settings.Test;

import java.util.Collection;
import java.util.HashMap;

public class SettingHandler {
    private static final HashMap<String, Setting> settings = new HashMap<>();

    public static void init() {
        settings.put("test", new Test());
    }

    public static Collection<Setting> getSettings() {
        return settings.values();
    }

    public static Setting fromName(String name) {
        return settings.get(name.toLowerCase());
    }

    public static Iterable<String> getNames() {
        return settings.keySet();
    }
}
