package me.hypericats.fedmaps.API;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SessionAPI {
    // Stolen from SchubiAuth

    public static String[] getProfileInfo(String token) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
            request.setHeader("Authorization", "Bearer " + token);

            try (CloseableHttpResponse response = client.execute(request)) {
                String jsonString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

                String ign = jsonObject.get("name").getAsString();
                String uuid = jsonObject.get("id").getAsString().replaceAll("-", "");

                return new String[]{ign, uuid};
            }
        }
    }

    public static boolean validateSession(String token) {
        try {
            String[] profileInfo = getProfileInfo(token);
            String ign = profileInfo[0];
            String uuid = profileInfo[1];

            MinecraftClient client = MinecraftClient.getInstance();
            Session session = client.getSession();

            return ign.equals(session.getUsername())
                    && uuid.equals(session.getUuidOrNull().toString().replace("-", ""));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkOnline(String uuid) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("https://api.slothpixel.me/api/players/" + uuid);

            try (CloseableHttpResponse response = client.execute(request)) {
                String jsonString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

                return jsonObject.get("online").getAsBoolean();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static UUID undashedToUUID(String uuid) {
        return UUID.fromString(
                uuid.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"
                )
        );
    }

    public static int changeName(String newName, String token) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(
                    "https://api.minecraftservices.com/minecraft/profile/name/" + newName
            );
            request.setHeader("Authorization", "Bearer " + token);

            try (CloseableHttpResponse response = client.execute(request)) {
                return response.getStatusLine().getStatusCode();
            }
        }
    }

    public static int changeSkin(String url, String token) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(
                    "https://api.minecraftservices.com/minecraft/profile/skins"
            );
            request.setHeader("Authorization", "Bearer " + token);
            request.setHeader("Content-Type", "application/json");

            String jsonString = """
                    {
                      "variant": "classic",
                      "url": "%s"
                    }
                    """.formatted(url);

            request.setEntity(new StringEntity(jsonString));

            try (CloseableHttpResponse response = client.execute(request)) {
                return response.getStatusLine().getStatusCode();
            }
        }
    }
}

