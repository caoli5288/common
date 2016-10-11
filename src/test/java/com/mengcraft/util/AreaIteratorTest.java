package com.mengcraft.util;

import org.bukkit.Location;
import org.junit.Test;

/**
 * Created on 16-2-23.
 */
public class AreaIteratorTest {

    @Test
    public void testNext() throws Exception {
        AreaIterator itr = new AreaIterator(new Location(null, 1, -3, -1), new Location(null, 2, -1, 0));
        while (itr.hasNext()) {
            System.out.println(itr.next());
        }
    }

}