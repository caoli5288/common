package com.mengcraft.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created on 16-3-13.
 */
public class ActionBar {

    private static final String SCRIPT = "" +
            "ChatComponentText = Java.type(\"net.minecraft.server.\" + version + \".ChatComponentText\");\n" +
            "PacketPlayOutChat = Java.type(\"net.minecraft.server.\" + version + \".PacketPlayOutChat\");\n" +
            "function sendPacket(p, packet) {\n" +
            "    p.getHandle().playerConnection.sendPacket(packet);\n" +
            "}\n" +
            "function send(p, text) {\n" +
            "    sendPacket(p, new PacketPlayOutChat(new ChatComponentText(text), 2));\n" +
            "}";

    public interface IFunc {

        void send(Player p, String text);
    }

    private enum Hold {

        INSTANCE;

        private final IFunc func;

        Hold() {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
            try {
                engine.put("version", Bukkit.getServer().getClass().getName().split("\\.")[3]);
                engine.eval(SCRIPT);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            func = Invocable.class.cast(engine).getInterface(IFunc.class);
        }
    }

    public static void send(Player p, String text) {
        Hold.INSTANCE.func.send(p, text);
    }

}
