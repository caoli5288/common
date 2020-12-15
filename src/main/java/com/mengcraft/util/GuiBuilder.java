package com.mengcraft.util;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
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

    public static final Button EMPTY_BUTTON = new Button(new ItemStack(0));
    private static Plugin plugin;

    private final Map<String, Supplier<Button>> buttons = new HashMap<>();
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

    public GuiBuilder setSymbol(String symbol, Button binding) {
        return setSymbol(symbol, () -> binding);
    }

    public GuiBuilder setSymbol(String symbol, Supplier<Button> factory) {
        checkArgument(symbol.length() == 1, "symbol must single char");
        buttons.put(symbol, factory);
        return this;
    }

    public Context build() {
        Context gui = new Context(name);
        gui.click = onClick;
        gui.close = onClose;
        for (String symbol : contents.split("")) {
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

    @RequiredArgsConstructor
    public static class Button {

        private final ItemStack icon;
        private final Consumer<InventoryClickEvent> onClick;

        public Button(ItemStack icon) {
            this(icon, null);
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
        private boolean lock;

        public void lock() {
            lock = true;
        }

        public void unlock() {
            lock = false;
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
            if (lock) {
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
            if (close != null) {
                close.accept(e);
            }
        }
    }

    public static abstract class Gui {

        protected final HumanEntity player;
        protected Context context;

        protected Gui(HumanEntity player) {
            this.player = player;
        }

        protected abstract void setup(GuiBuilder builder);

        public void open() {
            if (context == null) {
                GuiBuilder builder = builder();
                setup(builder);
                context = builder.build();
            }
            player.openInventory(context.getInventory());
        }

        protected void lock() {
            context.lock();
        }

        protected void unlock() {
            context.unlock();
        }

        protected void update() {
            Preconditions.checkNotNull(context);
            GuiBuilder builder = builder();
            setup(builder);
            Context from = builder.build();
            context.copy(from);
            context.fills();
        }

        protected void close() {
            Bukkit.getScheduler().runTask(plugin, player::closeInventory);
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
