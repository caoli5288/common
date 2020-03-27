package com.mengcraft.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

public class GuiBuilder {

    private final Map<String, Supplier<ElementBinding>> factories = new HashMap<>();
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

    public GuiBuilder setSymbol(String symbol, ElementBinding binding) {
        return setSymbol(symbol, () -> binding);
    }

    public GuiBuilder setSymbol(String symbol, Supplier<ElementBinding> factory) {
        checkArgument(symbol.length() == 1, "symbol must single char");
        factories.put(symbol, factory);
        return this;
    }

    public InventoryBinding build() {
        InventoryBinding binding = new InventoryBinding(name);
        for (String symbol : contents.split("")) {
            if (factories.containsKey(symbol)) {
                binding.bindings.add(factories.get(symbol).get());
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
    public static class ElementBinding {

        private final ItemStack item;
        private final Consumer<InventoryClickEvent> consumer;

        public ElementBinding(ItemStack item) {
            this(item, null);
        }

        public void apply(InventoryClickEvent e) {
            if (consumer != null) consumer.accept(e);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InventoryBinding implements InventoryHolder {

        private final List<ElementBinding> bindings = new ArrayList<>();
        private final String name;
        private Inventory inventory;
        private Consumer<InventoryClickEvent> onGeneric;

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

        public Consumer<InventoryClickEvent> openInventory(Player player) {
            player.openInventory(getInventory());
            return (e) -> {
                if (e.getInventory().getHolder() == this) {
                    e.setCancelled(true);
                    int slot = e.getRawSlot();
                    if (slot >= 0) {
                        if (slot < bindings.size()) {
                            bindings.get(slot).apply(e);
                        } else {
                            onGeneric(e);
                        }
                    }
                }
            };
        }

        public void onGeneric(Consumer<InventoryClickEvent> onGeneric) {
            this.onGeneric = onGeneric;
        }

        private void onGeneric(InventoryClickEvent e) {
            if (onGeneric != null) onGeneric.accept(e);
        }
    }
}
