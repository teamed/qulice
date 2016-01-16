/**
 * This project has a license.
 */
package com.qulice.foo;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * This is just a test class.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
public final class Main {

    /**
     * List of some URLs.
     */
    private final transient Set<URL> list;

    /**
     * Test something.
     */
    public Main() {
        this.list = new HashSet<URL>();
    }

    /**
     * Get size of list.
     * @return The size
     */
    public int size() {
        return this.list.size();
    }

}
