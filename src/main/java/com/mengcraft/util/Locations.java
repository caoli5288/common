package com.mengcraft.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Locations {

    public static String serialize(Location location) {
        return String.format("{\"world\":\"%s\",\"x\":%s,\"y\":%s,\"z\":%s,\"yaw\":%s,\"pitch\":%s}",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    public static Location deserialize(String json) {
        JSONObject parsed = (JSONObject) JSONValue.parse(json);
        return new Location(Bukkit.getWorld(parsed.get("world").toString()),
                ((Number) parsed.get("x")).doubleValue(),
                ((Number) parsed.get("y")).doubleValue(),
                ((Number) parsed.get("z")).doubleValue(),
                ((Number) parsed.get("yaw")).floatValue(),
                ((Number) parsed.get("pitch")).floatValue());
    }

}
