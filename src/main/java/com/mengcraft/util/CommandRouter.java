package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandRouter {

    private final String label;
    private final boolean tag;
    private Map<String, CommandRouter> routers;
    private BiFunction<CommandSender, Context, List<String>> completion;
    private BiFunction<CommandSender, Context, Boolean> execution;
    private BiPredicate<CommandSender, Context> validation;

    public CommandRouter() {
        this(null, false);
    }

    private CommandRouter(String label, boolean tag) {
        this.label = label;
        this.tag = tag;
    }

    public List<String> complete(CommandSender console, String[] commands) {
        Context context = new Context(Mode.COMPLETION, commands);
        CommandRouter look = look(console, context);
        if (look == null) {
            return Collections.emptyList();
        }
        return look.complete(console, context);
    }

    private CommandRouter look(CommandSender console, Context context) {
        CommandRouter let = this;
        int length = context.getCommands().length;
        for (int i = 0; i < length; i++) {
            context.index = i;
            CommandRouter look = let.lookup(console, context);
            if (look == null) {
                return let;
            }
            let = look;
        }
        return let;
    }

    public boolean execute(CommandSender console, String[] commands) {
        Context context = new Context(Mode.EXECUTION, commands);
        CommandRouter look = look(console, context);
        if (look == null) {
            return false;
        }
        return look.execute(console, context);
    }

    private boolean execute(CommandSender console, Context context) {
        if (execution == null) {
            return false;
        }
        return execution.apply(console, context);
    }

    private List<String> complete(CommandSender console, Context context) {
        if (completion == null) {
            if (routers == null || routers.isEmpty()) {
                return Collections.emptyList();
            }
            return routers.values()
                    .stream()
                    .filter(lt -> !lt.tag)
                    .map(lt -> lt.label)
                    .collect(Collectors.toList());
        }
        return completion.apply(console, context);
    }

    private CommandRouter lookup(CommandSender console, Context context) {
        String seg = context.poll();
        if (routers == null) {
            return null;
        }
        CommandRouter look = routers.get(seg);
        if (look != null && !look.tag) {
            return look;
        }
        for (CommandRouter value : routers.values()) {
            if (value.tag) {
                context.tags.put(value.label, seg);
                if (value.validate(console, context)) {
                    return value;
                }
                // Rollback tagged value
                context.tags.remove(value.label);
            }
        }
        return null;
    }

    private boolean validate(CommandSender console, Context context) {
        return validation == null || validation.test(console, context);
    }

    public CommandRouter addDefined(String define, Consumer<Definition> callback) {
        LinkedList<String> linked = Lists.newLinkedList();
        Collections.addAll(linked, StringUtils.split(define, ' '));
        Definition definition = loadDefinition(linked);
        callback.accept(definition);
        CommandRouter seg = this;
        for (CommandRouter line : definition.list) {
            seg = addDefinition(seg, line);
        }
        return this;
    }

    private static CommandRouter addDefinition(CommandRouter seg, CommandRouter line) {
        // fast path
        if (seg.routers == null) {
            seg.routers = Maps.newHashMap();
            seg.routers.put(line.label, line);
            return line;
        }
        // exists
        if (seg.routers.containsKey(line.label)) {
            CommandRouter old = seg.routers.get(line.label);
            Preconditions.checkState(old.tag == line.tag);
            return old;
        }
        // not exists
        seg.routers.put(line.label, line);
        return line;
    }

    private Definition loadDefinition(LinkedList<String> linked) {
        Definition definition = new Definition();
        while (!linked.isEmpty()) {
            String label = linked.poll();
            boolean tagged = false;
            if (label.charAt(0) == '$') {
                label = label.substring(1);
                tagged = true;
            }
            CommandRouter let = new CommandRouter(label, tagged);
            definition.list.add(let);
            if (tagged) {
                definition.tags.put(label, let);
            }
        }
        return definition;
    }

    public static class Definition {

        private final List<CommandRouter> list = Lists.newArrayList();
        private final Map<String, CommandRouter> tags = Maps.newHashMap();

        public Definition completion(String tag, BiFunction<CommandSender, Context, List<String>> completion) {
            tags.get(tag).completion = completion;
            return this;
        }

        public Definition validation(String tag, BiPredicate<CommandSender, Context> validation) {
            tags.get(tag).validation = validation;
            return this;
        }

        public Definition execution(BiFunction<CommandSender, Context, Boolean> execution) {
            list.get(list.size() - 1).execution = execution;
            return this;
        }

        public Definition execution(String tag, BiFunction<CommandSender, Context, Boolean> execution) {
            tags.get(tag).execution = execution;
            return this;
        }
    }

    public enum Mode {
        COMPLETION,
        EXECUTION;
    }

    @Data
    public static class Context {

        @Getter(AccessLevel.NONE)
        private final Map<String, String> tags = Maps.newHashMap();
        private final Mode mode;
        private final String[] commands;
        private int index;

        public String tag(String tag) {
            return tags.get(tag);
        }

        public boolean tail() {
            return index == commands.length - 1;
        }

        public String poll() {
            return commands[index];
        }
    }
}
