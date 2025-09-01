# common
License [GPLv2](http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).

## Duck typing for Java

This is a duck typing implementation based on reflection and dynamic proxies with force methods access. Perhaps it can be used as a wrapper for reflect.

### Examples

Call with parameter contravariant.

```groovy
class Foo {
    void run() {
        println("I'm running")
    }
    void print(String msg) {
        println(msg)
    }
}

Foo foo = new Foo()
Runnable runnable = Types.asType(foo, Runnable.class)
runnable.run()// I'm running
```

Call with method name map.

```groovy
class Foo {
    void run() {
        println("I'm running")
    }
}

Foo foo = new Foo()
Types.desc(Foo.class).map("run", Closeable.class, "close")
Types.asType(foo, Closeable.class).close()// I'm running

```

## Compiled PlaceholderAPI string

10x faster than `PlaceholderAPI.setPlaceholder(Player, String)`

### Examples

```groovy
import com.mengcraft.util.CompiledStr
import org.bukkit.Bukkit

def compiled = new CompiledStr("My name is %player_name%")

println compiled.apply(Bukkit.getPlayerExact("HIM")) // My name is HIM
```

## Command Router

A fluent API for routing complex Bukkit/Spigot command trees with built-in support for tab-completion and argument validation. It simplifies the creation of multi-level commands (e.g., `/mycmd user <name> set <value>`).

### Features

-   **Declarative Syntax**: Define entire command structures with a single string like `"user $player set $property $value"`.
-   **Placeholder Arguments**: Use `$`-prefixed words (e.g., `$player`) to capture arguments.
-   **Context-Aware Tab Completion**: Easily attach tab-completion logic to any argument.
-   **Argument Validation**: Add validation rules to placeholder arguments.
-   **Clean Execution Logic**: Assign an execution block to a valid command path.
-   **Automatic Routing**: The router handles matching subcommands and arguments, delegating to the correct logic.

### Usage Example

Here is how you can implement a command like `/mycmd <subcommand> ...` using the router.

```java
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyCommand implements CommandExecutor, TabCompleter {

    private final CommandRouter router = new CommandRouter();

    public MyCommand() {
        // Define '/mycmd user $user_name set $property $value'
        router.addDefined("user $user_name set $property $value", def -> {
            def.completion("user_name", (sender, context) ->
                // Suggest online players for the $user_name argument
                Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(context.poll().toLowerCase()))
                    .collect(Collectors.toList())
            );
            def.validation("user_name", (sender, context) -> {
                // Optional: validate that the player is online
                return Bukkit.getPlayer(context.tag("user_name")) != null;
            });
            def.completion("property", (sender, context) ->
                // Suggest properties for the $property argument
                Arrays.asList("health", "food", "level")
            );
            def.execution((sender, context) -> {
                // This code runs when the full command is entered correctly
                String userName = context.tag("user_name");
                String property = context.tag("property");
                String value = context.tag("value");
              
                Player target = Bukkit.getPlayer(userName);
                if (target == null) {
                    sender.sendMessage("Player " + userName + " not found.");
                    return true;
                }

                sender.sendMessage(String.format("Set %s's %s to %s.", userName, property, value));
                // ... add logic to actually set the property
                return true;
            });
        });

        // Define '/mycmd reload'
        router.addDefined("reload", def -> {
            def.execution((sender, context) -> {
                sender.sendMessage("Configuration reloaded.");
                // ... add reload logic
                return true;
            });
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Delegate command execution to the router
        if (!router.execute(sender, args)) {
            sender.sendMessage("Unknown command. Use /mycmd help for help.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Delegate tab completion to the router
        return router.complete(sender, args);
    }
}

// In your onEnable() method:
// this.getCommand("mycmd").setExecutor(new MyCommand());
```