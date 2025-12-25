package com.geektime.designpattern;

public class SinglePattern {

    private static volatile SinglePattern singlePattern = null;


    private SinglePattern() {

    }

    public static SinglePattern getSingle() {
        if (singlePattern == null) {
            synchronized (SinglePattern.class) {
                if (singlePattern == null) {
                    singlePattern = new SinglePattern();
                }
            }
        }

        return singlePattern;
    }

}
