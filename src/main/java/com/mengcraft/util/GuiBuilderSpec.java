package com.mengcraft.util;

import lombok.Data;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class GuiBuilderSpec {

    private String name;
    private List<String> contents = new ArrayList<>();
    private List<Element> elements = new ArrayList<>();
    private Map<String, String> messages = new HashMap<>();

    public GuiBuilder builder(Player player) {
        GuiBuilder builder = GuiBuilder.builder()
                .name(name)
                .contents(contents);
        for (Element element : elements) {
            builder.setSymbol(element.getSymbol(), () -> element.apply(player));
        }
        return builder;
    }

    @Data
    public static class Element {

        private String symbol;
        private int id;
        private int data;
        private Map<String, ?> metadata;
        private List<String> commands = new ArrayList<>();

        public GuiBuilder.GuiButton apply(Player player) {
            ItemStack item = new ItemStack(id, 1, (short) data);
            if (metadata != null) {
                ItemMeta meta = item.getItemMeta();
                if (metadata.containsKey("name")) {
                    meta.setDisplayName(PlaceholderAPI.setPlaceholders(player, metadata.get("name").toString()));
                }
                if (metadata.containsKey("lore")) {
                    List<String> lore = (List<String>) metadata.get("lore");
                    meta.setLore(lore.stream().map(line -> PlaceholderAPI.setPlaceholders(player, line)).collect(Collectors.toList()));
                }
                item.setItemMeta(meta);
            }
            return new GuiBuilder.GuiButton(item, e -> {
                if (!commands.isEmpty()) {
                    Player p = (Player) e.getWhoClicked();
                    for (String command : commands) {
                        String res = PlaceholderAPI.setPlaceholders(p, command);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), res);
                    }
                }
            });
        }
    }
}
