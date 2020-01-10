package com.mengcraft.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

public class GuiBuilder {

    private final Map<String, Function<Player, ElementBinding>> factories = new HashMap<>();
    private String name;
    private String contents;

    private GuiBuilder() {
    }

    public GuiBuilder name(String name) {
        this.name = name;
        return this;
    }

    public GuiBuilder contents(List<String> contents) {
        StringBuilder b = new StringBuilder();
        for (String content : contents) {
            checkArgument(content.length() == 9, "content length must equals 9");
            b.append(content);
        }
        this.contents = b.toString();
        return this;
    }

    public GuiBuilder contents(String contents) {
        checkArgument(contents.length() % 9 == 0, "content length must be multiple of 9");
        this.contents = contents;
        return this;
    }

    public GuiBuilder setSymbol(String symbol, Function<Player, ElementBinding> factory) {
        checkArgument(symbol.length() == 1, "symbol must single char");
        factories.put(symbol, factory);
        return this;
    }

    public InventoryBinding build(Player player) {
        InventoryBinding binding = new InventoryBinding(PlaceholderAPI.setPlaceholders(player, name));
        for (String symbol : contents.split("")) {
            if (factories.containsKey(symbol)) {
                binding.bindings.add(factories.get(symbol).apply(player));
            } else {
                binding.bindings.add(new ElementBinding(new ItemStack(0), null));
            }
        }
        return binding;
    }

    public static GuiBuilder builder() {
        return new GuiBuilder();
    }

    @RequiredArgsConstructor
    @Getter
    public static class ElementBinding {

        private final ItemStack item;
        private final BiConsumer<Player, ClickType> consumer;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InventoryBinding implements InventoryHolder {

        private final List<ElementBinding> bindings = new ArrayList<>();
        private final String name;
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            if (inventory == null) {
                inventory = Bukkit.createInventory(this, bindings.size(), name);
                for (int i = 0; i < bindings.size(); i++) {
                    inventory.setItem(i, bindings.get(i).item);
                }
            }
            return inventory;
        }

        public void onClick(Player p, ClickType click, int slot) {
            ElementBinding binding = bindings.get(slot);
            if (binding.consumer != null) {
                binding.consumer.accept(p, click);
            }
        }

        public Consumer<InventoryClickEvent> openInventory(Player player) {
            player.openInventory(getInventory());
            return (e) -> {
                if (e.getInventory().getHolder() == this) {
                    e.setCancelled(true);
                    int slot = e.getRawSlot();
                    if (slot >= 0 && slot < bindings.size()) {
                        onClick(player, e.getClick(), slot);
                    }
                }
            };
        }
    }
}
