package me.hypericats.fedmaps.map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

public class RoomTypeHandler {
    private static final HashMap<Integer, RoomData> rooms = new HashMap<>();

    private RoomTypeHandler() {

    }


    public static void initCores() {
        Gson gson = new Gson();
        try {
            InputStreamReader reader = new InputStreamReader(MinecraftClient.getInstance().getResourceManager().getResource(Identifier.of("fedmaps", "rooms.json")).orElseThrow().getInputStream());
            JsonArray jsonArray  = gson.fromJson(reader, JsonArray.class);

            jsonArray.forEach(json -> {
                RoomData data = gson.fromJson(json, RoomData.class);
                Arrays.stream(data.cores()).forEach(core -> rooms.put(core, data));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RoomData getFromCore(int core) {
        return rooms.get(core);
    }
}
