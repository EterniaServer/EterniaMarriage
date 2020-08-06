package br.com.eterniaserver.eterniamarriage.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.InputStreamReader;
import java.net.URL;

public class UUIDFetcher {

    private static final boolean ONLINE_MODE = Bukkit.getOnlineMode();

    static final HashMap<String, UUID> lookupCache = new HashMap<>();

    public static UUID getUUIDOf(String name) {
        UUID result = lookupCache.get(name);
        if (result == null) {
            if (ONLINE_MODE) {
                try {
                    // Get response from Mojang API
                    URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() == 400) {
                        System.err.println("There is no player with the name \"" + name + "\"!");
                        result = UUID.randomUUID();
                    } else {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        // Parse JSON response and get UUID
                        JsonElement element = new JsonParser().parse(bufferedReader);
                        JsonObject object = element.getAsJsonObject();
                        String uuidAsString = object.get("id").getAsString();
                        // Return UUID
                        result = parseUUIDFromString(uuidAsString);
                    }
                } catch (IOException e) {
                    result = UUID.randomUUID();
                }
            } else {
                result = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            }
            lookupCache.put(name, result);
        }
        return result;
    }

    private static UUID parseUUIDFromString(String uuidAsString) {
        String[] parts = {
                "0x" + uuidAsString.substring(0, 8),
                "0x" + uuidAsString.substring(8, 12),
                "0x" + uuidAsString.substring(12, 16),
                "0x" + uuidAsString.substring(16, 20),
                "0x" + uuidAsString.substring(20, 32)
        };

        long mostSigBits = Long.decode(parts[0]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(parts[1]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(parts[2]);

        long leastSigBits = Long.decode(parts[3]);
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(parts[4]);

        return new UUID(mostSigBits, leastSigBits);
    }

}