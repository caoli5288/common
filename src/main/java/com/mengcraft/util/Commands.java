package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class Commands<T extends CommandSender> {

    private final Map<String, Commands<T>> ops = Maps.newHashMap();
    private final String permission;
    private final BiConsumer<T, List<String>> function;

    public Commands() {
        this(null, null);
    }

    public Commands(BiConsumer<T, List<String>> function) {
        this(null, function);
    }

    public Commands(String permission, BiConsumer<T, List<String>> function) {
        this.permission = permission;
        this.function = function;
    }

    /**
     * @return this object
     */
    public Commands<T> addSubject(String name, BiConsumer<T, List<String>> consumer) {
        Preconditions.checkArgument(name.indexOf(' ') == -1);
        Commands<T> commands = new Commands<>(consumer);
        ops.put(name, commands);
        return this;
    }

    /**
     * @return this object
     */
    public Commands<T> addSubject(String name, Commands<T> commands) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(commands);
        ops.put(name, commands);
        return this;
    }

    public void onCommand(T sender, List<String> params) {
        onCommand(0, sender, params);
    }

    private void onCommand(int step, T sender, List<String> params) {
        if (permission == null || permission.isEmpty() || sender.hasPermission(permission)) {
            if (step < params.size()) {
                String s = params.get(step);
                if (ops.containsKey(s)) {
                    ops.get(s).onCommand(++step, sender, params);
                    return;
                } else if (ops.containsKey("")) {
                    ops.get("").onCommand(++step, sender, params);
                    return;
                }
            }
            if (function == null) {
                sender.sendMessage("Usable commands is " + String.join(", ", ops.keySet()));
            } else {
                function.accept(sender, params);
            }
        }
    }
}
