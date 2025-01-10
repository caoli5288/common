package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandRouter {

    private final Map<String, CommandRouter> childMap = Maps.newHashMap();
    private Callable<List<String>> completion;
    private Callable<Boolean> execution;

    @FunctionalInterface
    public interface Callable<T> {
        T call(CommandSender console, CallInfo info);
    }

    public boolean execute(CommandSender console, String[] commands) {
        CallInfo info = new CallInfo(commands);
        return executeNode(console, info);
    }

    private boolean executeNode(CommandSender console, CallInfo info) {
        // Example: give me apple
        info.pollNode();
        CommandRouter child = childMap.get(info.nextNode);
        if (child == null) {
            child = childMap.get("*");
        }
        // execute self
        if (child == null) {
            return executeAll(console, info);
        }
        if (executeAll(console, info)) {
            return child.executeNode(console, info);
        }
        return false;
    }

    /**
     * @param console the console
     * @param info    the command call info
     * @return false only if the execution explicitly false, otherwise true
     */
    private boolean executeAll(CommandSender console, CallInfo info) {
        if (execution == null) {
            return true;
        }
        Boolean bool = execution.call(console, info);
        return bool == null || bool;
    }

    public List<String> complete(CommandSender console, String[] commands) {
        CallInfo info = new CallInfo(commands);
        return completeNode(console, info);
    }

    private List<String> completeNode(CommandSender console, CallInfo info) {
        // Example: give me apple [amount]
        info.pollNode();
        String pollNode = info.nextNode;
        if (info.empty()) {
            List<String> list = completeAll(console, info);
            if (list == null) {
                if (!childMap.isEmpty()) {
                    list = childMap.keySet()
                            .stream()
                            .filter(it -> it.startsWith(pollNode))
                            .filter(it -> !it.equals("*"))
                            .collect(Collectors.toList());
                    if (childMap.containsKey("*")) {
                        // guessing * means players
                        list.addAll(filerOnlinePlayers(pollNode));
                    }
                }
            }
            return list;
        }
        CommandRouter child = childMap.get(pollNode);
        if (child == null) {
            child = childMap.get("*");
        }
        // not empty but no child node
        if (child == null) {
            return Collections.emptyList();
        }
        // Optional
        completeAll(console, info);
        // Call child
        return child.completeNode(console, info);
    }

    public static Collection<String> filerOnlinePlayers(String filter) {
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .filter(it -> it.startsWith(filter))
                .collect(Collectors.toSet());
    }

    private List<String> completeAll(CommandSender console, CallInfo info) {
        if (completion == null) {
            return null;
        }
        return completion.call(console, info);
    }

    public CommandRouter execution(Callable<Boolean> execution) {
        this.execution = execution;
        return this;
    }

    public CommandRouter completion(Callable<List<String>> completion) {
        this.completion = completion;
        return this;
    }

    public CommandRouter child(String name) {
        Preconditions.checkArgument(name.indexOf(' ') == -1, "Child must not contain space");
        return childMap.computeIfAbsent(name, __ -> new CommandRouter());
    }

    public CommandRouter child(String name, CommandRouter child) {
        Preconditions.checkArgument(name.indexOf(' ') == -1, "Child must not contain space");
        childMap.put(name, child);
        return this;
    }

    public CommandRouter child(String name, Consumer<CommandRouter> let) {
        CommandRouter child = new CommandRouter();
        let.accept(child);
        return child(name, child);
    }

    public static class CallInfo {

        private final Map<Object, Object> options = Maps.newHashMap();
        private final @Getter String[] commands;
        private int pollIndex;
        @Getter
        private String node;
        @Getter
        private String nextNode;

        CallInfo(String[] commands) {
            this.commands = commands;
        }

        void pollNode() {
            node = nextNode;
            if (empty()) {
                nextNode = null;
            } else {
                nextNode = commands[pollIndex++];
            }
        }

        public boolean empty() {
            return pollIndex >= commands.length;
        }

        public void bakeAllNext() {
            if (!empty()) {
                StringBuilder line = new StringBuilder(nextNode);
                while (!empty()) {
                    line.append(' ');
                    line.append(commands[pollIndex++]);
                }
                nextNode = line.toString();
            }
        }

        @SuppressWarnings("all")
        public <T> T option(Object key) {
            return (T) options.get(key);
        }

        @SuppressWarnings("all")
        public <T> T option(Object key, Object value) {
            return (T) options.put(key, value);
        }
    }
}
