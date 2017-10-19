package com.mengcraft.util;

import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Created on 16-3-13.
 */
public class Title {

    private static final String SCRIPT = "" +
            "PacketPlayOutTitle = Java.type(\"net.minecraft.server.\" + version + \".PacketPlayOutTitle\");\n" +
            "EnumTitleAction = Java.type(\"net.minecraft.server.\" + version + \".PacketPlayOutTitle$EnumTitleAction\");\n" +
            "ChatComponentText = Java.type(\"net.minecraft.server.\" + version + \".ChatComponentText\");\n" +
            "function send(p, title) {\n" +
            "    if (title == null) {\n" +
            "        sendReset(p);\n" +
            "    } else {\n" +
            "        if (title.display > 0) {\n" +
            "            sendTime(p, title);\n" +
            "        }\n" +
            "        sendSubTitle(p, title);\n" +
            "        sendTitle(p, title);\n" +
            "    }\n" +
            "}\n" +
            "function sendPacket(p, packet) {\n" +
            "    p.getHandle().playerConnection.sendPacket(packet);\n" +
            "}\n" +
            "function sendTitle(p, title) {\n" +
            "    sendPacket(p, new PacketPlayOutTitle(EnumTitleAction.TITLE, new ChatComponentText(title.title)));\n" +
            "}\n" +
            "function sendSubTitle(p, title) {\n" +
            "    sendPacket(p, new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, new ChatComponentText(title.subtitle)));\n" +
            "}\n" +
            "function sendTime(p, title) {\n" +
            "    sendPacket(p, new PacketPlayOutTitle(title.fadeIn, title.display, title.fadeOut));\n" +
            "}\n" +
            "function sendReset(p) {\n" +
            "    sendPacket(p, new PacketPlayOutTitle(EnumTitleAction.RESET, null));\n" +
            "}";

    public interface ITitle {

        void send(Player p, TitleEntry entry);
    }

    private enum Hold {

        INSTANCE;

        private final ITitle title;

        @SneakyThrows
        Hold() {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
            engine.put("version", Bukkit.getServer().getClass().getName().split("\\.")[3]);
            engine.eval(SCRIPT);
            title = Invocable.class.cast(engine).getInterface(ITitle.class);
        }
    }

    public static void send(Player p, String title, String subtitle) {
        send(p, TitleEntry.builder().title(title).subtitle(subtitle).build());
    }

    public static void send(Player p, TitleEntry entry) {
        Hold.INSTANCE.title.send(p, entry);
    }

    public static void send(Iterable<Player> list, TitleEntry entry) {
        for (val p : list) send(p, entry);
    }

}
