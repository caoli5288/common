package com.mengcraft.util.event;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Material.*;

/**
 * Created by on 11月4日.
 */
public enum ArmorType {

    HELMET(39),
    CHESTPLATE(38),
    LEGGINGS(37),
    BOOTS(36);

    private final int value;

    ArmorType(int value) {
        this.value = value;
    }

    public ItemStack itemFor(HumanEntity p) {
        return p.getInventory().getItem(value);
    }

    public int getValue() {
        return value;
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
