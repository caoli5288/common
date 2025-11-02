package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class GuiBuilder {

    public static final int INVENTORY_MAX_SIZE = 54;
    public static final int BAG_MAX_SIZE = 36;
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
        build(gui);
        return gui;
    }

    void build(Context gui) {
        gui.builder = this;
        gui.click = onClick;
        gui.close = onClose;
        gui.buttons = Lists.newArrayList();
        for (Character symbol : contents) {
            if (buttons.containsKey(symbol)) {
                gui.buttons.add(buttons.get(symbol).get());
            } else {
                gui.buttons.add(EMPTY_BUTTON);
            }
        }
    }

    public static GuiBuilder builder() {
        return new GuiBuilder();
    }

    public static void enable(Plugin plugin) {
        Preconditions.checkState(GuiBuilder.plugin == null);
        GuiBuilder.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new Events(), plugin);
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
        private GuiBuilder builder;
        private List<Button> buttons;
        private Inventory inventory;
        private SelfContents selfContents;
        private Consumer<InventoryCloseEvent> close;
        private Consumer<InventoryClickEvent> click;
        private boolean clicking;
        private boolean locked;
        private long lockMillis;
        private boolean opened;

        public void lock() {
            locked = true;
        }

        public void lock(long millis) {
            locked = true;
            lockMillis = System.currentTimeMillis() + millis;
        }

        public void unlock() {
            locked = false;
        }

        public boolean locked() {
            if (locked) {
                if (lockMillis == 0) {
                    return true;
                }
                long millis = System.currentTimeMillis();
                if (millis < lockMillis) {
                    return true;
                }
                lockMillis = 0;
                locked = false;
            }
            return false;
        }

//        void copy(Context from) {
//            buttons = from.buttons;
//            click = from.click;
//            close = from.close;
//        }

        @Override
        public Inventory getInventory() {
            if (inventory == null) {
                int bSize = buttons.size();
                if (bSize > INVENTORY_MAX_SIZE) {
                    inventory = Bukkit.createInventory(this, INVENTORY_MAX_SIZE, name);
                    selfContents = new SelfContents();
                } else {
                    inventory = Bukkit.createInventory(this, bSize, name);
                }
                fillAll();
            }
            return inventory;
        }

        void fillAll() {
            int size = buttons.size();
            for (int slot = 0; slot < size; slot++) {
//                ItemStack old = inventory.getItem(i);
//                ItemStack item = buttons.get(i).icon;
//                if (!Objects.equals(old, item)) {
//                    inventory.setItem(i, item);
//                }
                // Comp and set items is meaningless because of server just send all items in cancelled clicks
                // Simply set item without comp
                fill(slot);
            }
        }

        void fill(int slot) {
            if (slot < INVENTORY_MAX_SIZE) {
                inventory.setItem(slot, buttons.get(slot).icon);
            } else {
                selfContents.setItem(slot - INVENTORY_MAX_SIZE, buttons.get(slot).icon);
            }
        }

        void onClick(InventoryClickEvent event) {
            event.setCancelled(true);
            if (locked()) {
                return;
            }
            int slot = event.getRawSlot();
            if (slot >= 0) {
                if (slot < buttons.size()) {
                    buttons.get(slot).onClick(event);
                } else if (click != null) {
                    try {
                        clicking = true;
                        click.accept(event);
                    } finally {
                        clicking = false;
                    }
                }
            }
        }

        void closed(InventoryCloseEvent e) {
            opened = false;
            if (selfContents != null) {
                e.getPlayer().getInventory().setStorageContents(selfContents.getOldItems());
            }
            if (close != null) {
                close.accept(e);
            }
        }

        void opened(InventoryOpenEvent event) {
            opened = true;
            if (selfContents != null) {
                SelfContents.setContents(event.getPlayer(), selfContents);
            }
        }

        void refill(int slot) {
            char content = builder.contents[slot];
            Supplier<Button> supplier = builder.buttons.get(content);
            Button fillButton = supplier == null ?
                    EMPTY_BUTTON :
                    supplier.get();
            buttons.set(slot, fillButton);
            fill(slot);
        }
    }

    public static abstract class Gui {

        protected Context context;

        protected abstract void onBuild(Player player, GuiBuilder builder);

        public void open(Player player) {
            if (context == null) {
                GuiBuilder builder = builder();
                onBuild(player, builder);
                context = builder.build();
            }
            player.openInventory(context.getInventory());
        }

        protected Button emptyButton() {
            return EMPTY_BUTTON;
        }

        protected Button button(Material type, String lines) {
            ItemStack item = new ItemStack(type);
            if (!StringUtils.isEmpty(lines)) {
                ItemMeta meta = item.getItemMeta();
                String[] split = StringUtils.split(lines, '\n');
                meta.setDisplayName(split[0]);
                if (split.length > 1) {
                    meta.setLore(Arrays.asList(Arrays.copyOfRange(split, 1, split.length)));
                }
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

        protected void lock(long millis) {
            context.lock(millis);
        }

        protected void unlock() {
            context.unlock();
        }

        protected boolean isLocked() {
            return context.locked();
        }

        protected boolean isClosed() {
            return !context.opened;
        }

        protected void refill(Player player) {
            reload(player);
            context.fillAll();
        }

        protected void refill(int slot) {
            context.refill(slot);
        }

        protected void reload(Player player) {
            Preconditions.checkNotNull(context);
            GuiBuilder builder = builder();
            onBuild(player, builder);
            builder.build(context);
        }

        protected void clear() {
            context = null;
        }

        protected void close(Player player) {
            if (context.clicking) {
                Bukkit.getScheduler().runTask(plugin, player::closeInventory);
            } else {
                player.closeInventory();
            }
        }
    }

    @Data
    static class SelfContents {

        private final ItemStack[] items = new ItemStack[BAG_MAX_SIZE];
        private ItemStack[] oldItems;
        private HumanEntity entity;

        public void setItem(int slot, ItemStack item) {
            int vSlot = slot < 27 ?
                    slot + 9 :
                    slot - 27;
            items[vSlot] = item;
            if (entity != null) {
                entity.getInventory().setItem(vSlot, item);
            }
        }

        static void setContents(HumanEntity entity, SelfContents contents) {
            contents.entity = entity;
            PlayerInventory content = entity.getInventory();
            if (contents.oldItems == null) {
                contents.oldItems = content.getStorageContents();
            }
            content.setStorageContents(contents.items);
        }
    }

    public static class Events implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            InventoryHolder gui = event.getInventory().getHolder();// Use getHolder(false) if exists
            if (gui instanceof Context) {
                ((Context) gui).onClick(event);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            InventoryHolder gui = event.getInventory().getHolder();
            if (gui instanceof Context) {
                ((Context) gui).closed(event);
            }
        }

        @EventHandler
        public void onInventoryOpen(InventoryOpenEvent event) {
            InventoryHolder gui = event.getInventory().getHolder();
            if (gui instanceof Context) {
                ((Context) gui).opened(event);
            }
        }
    }
}
