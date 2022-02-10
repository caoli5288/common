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