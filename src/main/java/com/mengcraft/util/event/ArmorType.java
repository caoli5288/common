package com.mengcraft.util.event;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.bukkit.Material.*;

/**
 * Created by on 11月4日.
 */
public enum ArmorType {

    HELMET(PlayerInventory::getHelmet),
    CHESTPLATE(PlayerInventory::getChestplate),
    LEGGINGS(PlayerInventory::getLeggings),
    BOOTS(PlayerInventory::getBoots);

    private final Function<PlayerInventory, ItemStack> func;

    ArmorType(Function<PlayerInventory, ItemStack> func) {
        this.func = func;
    }

    public ItemStack itemFor(HumanEntity p) {
        return func.apply(p.getInventory());
    }

    private static final Map<Material, ArmorType> MAPPING = new EnumMap<>(new HashMap<Material, ArmorType>() {{
        put(LEATHER_HELMET, HELMET);
        put(CHAINMAIL_HELMET, HELMET);
        put(IRON_HELMET, HELMET);
        put(DIAMOND_HELMET, HELMET);
        put(GOLD_HELMET, HELMET);
        put(LEATHER_CHESTPLATE, CHESTPLATE);
        put(CHAINMAIL_CHESTPLATE, CHESTPLATE);
        put(IRON_CHESTPLATE, CHESTPLATE);
        put(DIAMOND_CHESTPLATE, CHESTPLATE);
        put(GOLD_CHESTPLATE, CHESTPLATE);
        put(LEATHER_LEGGINGS, LEGGINGS);
        put(CHAINMAIL_LEGGINGS, LEGGINGS);
        put(IRON_LEGGINGS, LEGGINGS);
        put(DIAMOND_LEGGINGS, LEGGINGS);
        put(GOLD_LEGGINGS, LEGGINGS);
        put(LEATHER_BOOTS, BOOTS);
        put(CHAINMAIL_BOOTS, BOOTS);
        put(IRON_BOOTS, BOOTS);
        put(DIAMOND_BOOTS, BOOTS);
        put(GOLD_BOOTS, BOOTS);
    }});

    public static ArmorType typeFor(ItemStack item) {
        return MAPPING.get(item.getType());
    }
}
