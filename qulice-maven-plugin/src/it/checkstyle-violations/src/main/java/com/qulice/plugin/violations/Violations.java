package com.qulice.plugin.violations;

import java.util.ArrayList;

public class Violations {
    public void test() {
        System.setProperty("test", "test value");
    }

    /**
     * Test method.
     * @todo #123 First
     *  second
     */
    public final void foreach() {
        for (String txt : new String[] {"test"}) {
            System.out.println(txt);
        }
    }

    /**
     * Missing final in catch.
     * @todo #123 First
     *  second
     */
    public final void catchFinal() {
        try {
            Integer.parseInt("123");
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(ex);
        }
    }
    /**
     * ArrayList without initializer.
     * @todo #123 Make this
     *  a better text.
     */
    public final void arrayLists() {
        System.out.println(new ArrayList<Integer>());
        System.out.println(new java.util.ArrayList<Integer>());
        System.out.println(new ArrayList<Integer>(1));
        System.out.println(new java.util.ArrayList<Integer>(2));
    }
}
