package com.mengcraft.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class CommandRouterTest {

    String test;

    CommandRouter commandRouter = new CommandRouter()
            .addDefined("give $p $item", definition -> {
                definition.completion("p", (sender, context) -> Arrays.asList("alex", "bob"))
                        .completion("item", (sender, context) -> Arrays.asList("apple", "stick"))
                        .execution((sender, context) -> {
                            test = context.tag("p") + context.tag("item");
                            return true;
                        });
            });

    @Test
    public void testComplete() {
        Assert.assertEquals(Arrays.asList("give"), commandRouter.complete(null, new String[]{""}));
        Assert.assertEquals(Arrays.asList("give"), commandRouter.complete(null, new String[]{"giv"}));
        Assert.assertEquals(Arrays.asList("alex", "bob"), commandRouter.complete(null, new String[]{"give", ""}));
        Assert.assertEquals(Arrays.asList("apple", "stick"), commandRouter.complete(null, new String[]{"give", "alex", ""}));
        Assert.assertEquals(Arrays.asList("alex", "bob"), commandRouter.complete(null, new String[]{"give", "alex"}));
        commandRouter.execute(null, new String[]{"give", "alex", "apple"});
        Assert.assertEquals("alexapple", test);
    }
}
