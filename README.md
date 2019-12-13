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

Consumer<String> consumer = Types.asType(foo, Consumer.class)
consumer.accept("hello")// hello

```

Call with metheds name map.

```groovy
class Foo {
    void run() {
        println("I'm running")
    }
}

Foo foo = new Foo()
Types.getMethods(foo).map("run", Closeable.class, "close")
Types.asType(foo, Closeable.class).close()// I'm running

```

Call with methods mismap.

```groovy
class Foo {
    void run() {
        println("I'm running")
    }
}

interface Bar {
    void run()
    void close()
}

Bar bar = Types.asType(new Foo(), Bar.class)
bar.run()// I'm running
bar.close()// an NoSuchMethodException thrown

```
