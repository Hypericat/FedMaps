package me.hypericats.fedmaps.config;

import me.hypericats.fedmaps.config.settings.ESPSetting;
import me.hypericats.fedmaps.config.settings.MapSetting;

import java.util.Collection;
import java.util.HashMap;

public class SettingHandler {
    private static final HashMap<String, Setting> settings = new HashMap<>();

    public static void init() {
        addSetting(new MapSetting());
        addSetting(new ESPSetting());
    }

    private static void addSetting(Setting setting) {
        settings.put(setting.getName(), setting);
    }

    public static Collection<Setting> getSettings() {
        return settings.values();
    }

    public static <T extends Setting> T fromName(String name, Class<T> clzz) {
        return (T) settings.get(name.toLowerCase());
    }


    public static Iterable<String> getNames() {
        return settings.keySet();
    }
}
