package com.mengcraft.util.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class ArmorEquipEvent extends Event {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final HumanEntity player;
    private final ItemStack armor;

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    static ArmorEquipEvent call(HumanEntity p, ItemStack armor) {
        val event = new ArmorEquipEvent(p, armor);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

}
