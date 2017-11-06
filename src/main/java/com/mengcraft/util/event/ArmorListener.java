package com.mengcraft.util.event;

import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY;
import static org.bukkit.event.inventory.InventoryType.SlotType.ARMOR;
import static org.bukkit.event.inventory.InventoryType.SlotType.CONTAINER;
import static org.bukkit.event.inventory.InventoryType.SlotType.QUICKBAR;

/**
 * Created by on 11月4日.
 */
public class ArmorListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void handle(InventoryClickEvent event) {
        val action = event.getAction();
        if (action == InventoryAction.NOTHING) {
            return;
        }

        val inv = event.getClickedInventory();
        if (inv == null || !(inv.getType() == InventoryType.PLAYER)) {
            return;
        }

        val click = event.getClick();
        if (click == ClickType.DOUBLE_CLICK) {
            return;
        }

        if (event.getAction() == MOVE_TO_OTHER_INVENTORY) {
            val slot = event.getSlotType();
            if (slot == ARMOR) {
                ArmorEquipEvent.call(event.getWhoClicked(), null);
            } else if (slot == CONTAINER || slot == QUICKBAR) {
                val item = event.getCurrentItem();
                val type = ArmorType.typeFor(item);
                if (type == null || !(type.itemFor(event.getWhoClicked()) == null)) {
                    return;
                }

                ArmorEquipEvent.call(event.getWhoClicked(), item);
            }
        } else if (event.getSlotType() == ARMOR) {
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) {
                int button = event.getHotbarButton();
                item = (button == -1) ? event.getCursor() : inv.getItem(button);
                if (item == null || item.getType() == Material.AIR) {
                    return;
                }

                val type = ArmorType.typeFor(item);
                if (type == null || !(type.getValue() == event.getSlot())) {
                    return;
                }

                ArmorEquipEvent.call(event.getWhoClicked(), item);
            } else {
                int button = event.getHotbarButton();
                item = (button == -1) ? event.getCursor() : inv.getItem(button);
                if (item == null || item.getType() == Material.AIR) {
                    ArmorEquipEvent.call(event.getWhoClicked(), null);
                } else {
                    val type = ArmorType.typeFor(item);
                    if (type == null || !(type.getValue() == event.getSlot())) {
                        return;
                    }

                    ArmorEquipEvent.call(event.getWhoClicked(), item);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void handle(PlayerInteractEvent event) {
        val action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        val item = event.getItem();
        if (item == null) {
            return;
        }

        val type = ArmorType.typeFor(item);
        if (type == null) {
            return;
        }

        val slot = type.itemFor(event.getPlayer());
        if (!(slot == null)) {
            return;
        }

        ArmorEquipEvent.call(event.getPlayer(), item);
    }

}
