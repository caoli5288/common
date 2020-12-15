package com.mengcraft.util;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

public class GuiBuilder {

    private static final GuiButton EMPTY = new GuiButton(new ItemStack(Material.AIR));
    private static Plugin plugin;

    private final Map<String, Supplier<GuiButton>> factories = new HashMap<>();
    private String name;
    private String contents;
    private Consumer<InventoryClickEvent> onClick;
    private Consumer<InventoryCloseEvent> onClose;

    public GuiBuilder name(String name) {
        this.name = name;
        return this;
    }

    public GuiBuilder onClick(Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
        return this;
    }

    public GuiBuilder onClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
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

    public GuiBuilder setSymbol(String symbol, GuiButton binding) {
        return setSymbol(symbol, () -> binding);
    }

    public GuiBuilder setSymbol(String symbol, Supplier<GuiButton> factory) {
        checkArgument(symbol.length() == 1, "symbol must single char");
        factories.put(symbol, factory);
        return this;
    }

    public GuiInventory build() {
        GuiInventory gui = new GuiInventory(name);
        gui.onClick = onClick;
        gui.onClose = onClose;
        for (String symbol : contents.split("")) {
            if (factories.containsKey(symbol)) {
                gui.bindings.add(factories.get(symbol).get());
            } else {
                gui.bindings.add(EMPTY);
            }
        }
        return gui;
    }

    public static GuiBuilder builder() {
        return new GuiBuilder();
    }

    public static void setup(Plugin plugin) {
        Preconditions.checkState(GuiBuilder.plugin == null);
        GuiBuilder.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new Listeners(), plugin);
    }

    @RequiredArgsConstructor
    public static class GuiButton {

        private final ItemStack icon;
        private final Consumer<InventoryClickEvent> onClick;

        public GuiButton(ItemStack icon) {
            this(icon, null);
        }

        void onClick(InventoryClickEvent e) {
            if (onClick != null) {
                onClick.accept(e);
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GuiInventory implements InventoryHolder {

        private final List<GuiButton> bindings = new ArrayList<>();
        private final String name;
        private Inventory inventory;
        private Consumer<InventoryCloseEvent> onClose;
        private Consumer<InventoryClickEvent> onClick;
        private boolean lock;

        public void lock() {
            lock = true;
        }

        public void unlock() {
            lock = false;
        }

        @Override
        public Inventory getInventory() {
            if (inventory == null) {
                int size = bindings.size();
                inventory = Bukkit.createInventory(this, size, name);
                for (int i = 0; i < size; i++) {
                    inventory.setItem(i, bindings.get(i).icon);
                }
            }
            return inventory;
        }

        void onClick(InventoryClickEvent event) {
            event.setCancelled(true);
            if (lock) {
                return;
            }
            int slot = event.getRawSlot();
            if (slot >= 0) {
                if (slot < bindings.size()) {
                    bindings.get(slot).onClick(event);
                } else if (onClick != null) {
                    onClick.accept(event);
                }
            }
        }

        void onClose(InventoryCloseEvent e) {
            if (onClose == null) {
                return;
            }
            onClose.accept(e);
        }
    }

    public static class Listeners implements Listener {

        @EventHandler
        public void on(InventoryClickEvent event) {
            Inventory inventory = event.getClickedInventory();
            if (inventory != null) {
                InventoryHolder b = inventory.getHolder();
                if (b instanceof GuiInventory) {
                    ((GuiInventory) b).onClick(event);
                }
            }
        }

        @EventHandler
        public void on(InventoryCloseEvent event) {
            Inventory inventory = event.getInventory();
            if (inventory != null) {
                InventoryHolder b = inventory.getHolder();
                if (b instanceof GuiInventory) {
                    ((GuiInventory) b).onClose(event);
                }
            }
        }
    }
}
