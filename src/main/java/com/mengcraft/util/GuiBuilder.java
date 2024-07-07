package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class GuiBuilder {

    public static final Button EMPTY_BUTTON = new Button(new ItemStack(Material.AIR));
    private static Plugin plugin;

    private final Map<Character, Supplier<Button>> buttons = Maps.newHashMap();
    private String name = "";
    private char[] contents;
    private Consumer<InventoryClickEvent> onClick;
    private Consumer<InventoryCloseEvent> onClose;

    public GuiBuilder name(String name) {
        checkNotNull(name, "name must not be null");
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
        this.contents = b.toString().toCharArray();
        return this;
    }

    public GuiBuilder contents(String contents) {
        checkArgument(contents.length() % 9 == 0, "content length must be multiple of 9");
        this.contents = contents.toCharArray();
        return this;
    }

    public GuiBuilder setSymbol(String symbol, Button binding) {
        return setSymbol(symbol, () -> binding);
    }

    public GuiBuilder setSymbol(String symbol, Supplier<Button> factory) {
        checkArgument(symbol.length() == 1, "symbol must single char");
        buttons.put(symbol.charAt(0), factory);
        return this;
    }

    public GuiBuilder symbol(Character symbol, Button binding) {
        return symbol(symbol, () -> binding);
    }

    public GuiBuilder symbol(Character symbol, Supplier<Button> factory) {
        buttons.put(symbol, factory);
        return this;
    }

    public Context build() {
        Context gui = new Context(name);
        gui.click = onClick;
        gui.close = onClose;
        for (Character symbol : contents) {
            if (buttons.containsKey(symbol)) {
                gui.buttons.add(buttons.get(symbol).get());
            } else {
                gui.buttons.add(EMPTY_BUTTON);
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

    public static boolean isEnabled() {
        return plugin != null;
    }

    @AllArgsConstructor
    public static class Button {

        private final ItemStack icon;
        private Consumer<InventoryClickEvent> onClick;

        public Button(ItemStack icon) {
            this(icon, null);
        }

        public Button onClick(Consumer<InventoryClickEvent> onClick) {
            this.onClick = onClick;
            return this;
        }

        void onClick(InventoryClickEvent e) {
            if (onClick != null) {
                onClick.accept(e);
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Context implements InventoryHolder {

        private final String name;
        private List<Button> buttons = new ArrayList<>();
        private Inventory inventory;
        private Consumer<InventoryCloseEvent> close;
        private Consumer<InventoryClickEvent> click;
        private boolean locked;
        private boolean closed;

        public void lock() {
            locked = true;
        }

        public void unlock() {
            locked = false;
        }

        void copy(Context from) {
            buttons = from.buttons;
            click = from.click;
            close = from.close;
        }

        @Override
        public Inventory getInventory() {
            if (inventory == null) {
                inventory = Bukkit.createInventory(this, buttons.size(), name);
                fills();
            }
            return inventory;
        }

        void fills() {
            int size = buttons.size();
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, buttons.get(i).icon);
            }
        }

        void onClick(InventoryClickEvent event) {
            event.setCancelled(true);
            if (locked) {
                return;
            }
            int slot = event.getRawSlot();
            if (slot >= 0) {
                if (slot < buttons.size()) {
                    buttons.get(slot).onClick(event);
                } else if (click != null) {
                    click.accept(event);
                }
            }
        }

        void onClose(InventoryCloseEvent e) {
            closed = true;
            if (close != null) {
                close.accept(e);
            }
        }
    }

    public static abstract class Gui {

        protected Context context;

        protected abstract void setup(Player player, GuiBuilder builder);

        public void open(Player player) {
            if (context == null) {
                GuiBuilder builder = builder();
                setup(player, builder);
                context = builder.build();
            }
            player.openInventory(context.getInventory());
        }

        protected Button emptyButton() {
            return EMPTY_BUTTON;
        }

        protected Button button(Material type, String name) {
            ItemStack item = new ItemStack(type);
            if (!StringUtils.isEmpty(name)) {
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(name);
                item.setItemMeta(meta);
            }
            return button(item);
        }

        protected Button button(ItemStack item) {
            return new Button(item);
        }

        protected Button button(ItemStack item, Consumer<InventoryClickEvent> callback) {
            return new Button(item, callback);
        }

        protected void lock() {
            context.lock();
        }

        protected void unlock() {
            context.unlock();
        }

        protected boolean isLocked() {
            return context.locked;
        }

        protected boolean isClosed() {
            return context.closed;
        }

        protected void update(Player player) {
            Preconditions.checkNotNull(context);
            GuiBuilder builder = builder();
            setup(player, builder);
            Context from = builder.build();
            context.copy(from);
            context.fills();
        }

        protected void clear() {
            context = null;
        }
    }

    public static class Listeners implements Listener {

        @EventHandler
        public void on(InventoryClickEvent event) {
            Inventory inv = event.getInventory();
            if (inv != null) {
                InventoryHolder gui = inv.getHolder();
                if (gui instanceof Context) {
                    ((Context) gui).onClick(event);
                }
            }
        }

        @EventHandler
        public void on(InventoryCloseEvent event) {
            Inventory inv = event.getInventory();
            if (inv != null) {
                InventoryHolder gui = inv.getHolder();
                if (gui instanceof Context) {
                    ((Context) gui).onClose(event);
                }
            }
        }
    }
}
