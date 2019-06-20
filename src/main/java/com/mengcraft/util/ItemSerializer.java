package com.mengcraft.util;


import lombok.SneakyThrows;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagByte;
import net.minecraft.server.v1_12_R1.NBTTagByteArray;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagDouble;
import net.minecraft.server.v1_12_R1.NBTTagFloat;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagIntArray;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagLong;
import net.minecraft.server.v1_12_R1.NBTTagLongArray;
import net.minecraft.server.v1_12_R1.NBTTagShort;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemSerializer {

    private static final Field FIELD_TAG_LIST = _FIELD_TAG_LIST();
    private static final Field FIELD_TAG_LONG_LIST = _FIELD_TAG_LONG_LIST();

    private final TypeFunctionRegistry<Object> registry = new TypeFunctionRegistry<>();

    public ItemSerializer() {
        registry.register(NBTTagCompound.class, this::asCompound);
        registry.register(NBTTagList.class, this::asList);
        registry.register(NBTTagFloat.class, NBTTagFloat::i);
        registry.register(NBTTagDouble.class, NBTTagDouble::asDouble);
        registry.register(NBTTagByte.class, NBTTagByte::g);
        registry.register(NBTTagShort.class, NBTTagShort::f);
        registry.register(NBTTagInt.class, NBTTagInt::e);
        registry.register(NBTTagLong.class, NBTTagLong::d);
        registry.register(NBTTagString.class, NBTTagString::c_);
        registry.register(NBTTagByteArray.class, NBTTagByteArray::c);
        registry.register(NBTTagIntArray.class, NBTTagIntArray::d);
        registry.register(NBTTagLongArray.class, ItemSerializer::_FIELD_TAG_LONG_LIST);
    }

    private Object asList(NBTTagList list) {
        List<Object> container = new ArrayList<>();
        for (NBTBase sub : _FIELD_TAG_LIST(list)) {
            Maybe.of(registry.handle(sub)).then(container::add);
        }
        return container;
    }

    private Object asCompound(NBTTagCompound compound) {
        HashMap<String, Object> container = new HashMap<>();
        for (String key : compound.c()) {
            NBTBase sub = compound.get(key);
            Maybe.of(registry.handle(sub)).then(obj -> container.put(key, obj));
        }
        return container;
    }

    public Map<String, Object> serialize(ItemStack itemStack) {
        net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound serializer = new NBTTagCompound();
        stack.save(serializer);
        return (Map<String, Object>) registry.handle(serializer);
    }

    @SneakyThrows
    private static long[] _FIELD_TAG_LONG_LIST(NBTTagLongArray longArray) {
        return (long[]) FIELD_TAG_LONG_LIST.get(longArray);
    }

    @SneakyThrows
    private static Field _FIELD_TAG_LONG_LIST() {
        Field field = NBTTagLongArray.class.getDeclaredField("b");
        field.setAccessible(true);
        return field;
    }

    @SneakyThrows
    private static Field _FIELD_TAG_LIST() {
        Field field = NBTTagList.class.getDeclaredField("list");
        field.setAccessible(true);
        return field;
    }

    @SneakyThrows
    private static List<NBTBase> _FIELD_TAG_LIST(NBTTagList list) {
        return (List<NBTBase>) FIELD_TAG_LIST.get(list);
    }
}
