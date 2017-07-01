package com.mengcraft.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.WeakHashMap;

/**
 * Created on 16-3-13.
 */
public interface ActionBar {

    String SCRIPT = "" +
            "ChatComponentText = Java.type(\"net.minecraft.server.\" + version + \".ChatComponentText\");\n" +
            "PacketPlayOutChat = Java.type(\"net.minecraft.server.\" + version + \".PacketPlayOutChat\");\n" +
            "function sendPacket(p, packet) {\n" +
            "    p.getHandle().playerConnection.sendPacket(packet);\n" +
            "}\n" +
            "function send(p, text) {\n" +
            "    var packet;\n" +
            "    if (pool.containsKey(text)) {\n" +
            "        packet = pool.get(text);\n" +
            "    } else {\n" +
            "        pool.put(text, packet = new PacketPlayOutChat(new ChatComponentText(text), 2));\n" +
            "    }\n" +
            "    sendPacket(p, packet);\n" +
            "}";

    static ActionBar of(Plugin plugin) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        try {
            engine.put("version", plugin.getServer().getClass().getName().split("\\.")[3]);
            engine.put("pool", new WeakHashMap<>());
            engine.eval(SCRIPT);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        return Invocable.class.cast(engine).getInterface(ActionBar.class);
    }

    void send(Player p, String text);

}
