package com.ryan.generic;

import java.util.ArrayList;

public class WhyUseGeneric {
    
    public static void main(String[] args) {
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Hello");
//        stringList.add(123);

        Box<String> box = new Box<>();
        box.set("Hello");
        testBoxGet(box);
    }

    private static void testBoxGet(Box<String> box) {

        String s = box.get();
    }
}
