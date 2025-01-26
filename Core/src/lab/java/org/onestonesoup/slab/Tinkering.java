package org.onestonesoup.slab;

public class Tinkering {

    public static void main(String[] args) {

        new Tinkering().test();

    }

    public void test() {
        System.out.println( getValue()!=null && getValue() > 0 );
    }

    private Integer getValue() {
        return null;
    }
}
