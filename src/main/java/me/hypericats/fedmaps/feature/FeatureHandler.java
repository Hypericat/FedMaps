package me.hypericats.fedmaps.feature;

import java.util.HashMap;

public class FeatureHandler {
    private static final HashMap<Class<?>, Feature> features = new HashMap<>();
    public static void init() {
        if (!features.isEmpty()) return;
        features.put(Esp.class, new Esp());
    }

    public static<T> T getByClass(Class<T> clzz) {
        return (T) features.get(clzz);
    }
}
