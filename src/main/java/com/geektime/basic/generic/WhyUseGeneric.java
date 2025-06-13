package com.geektime.basic.generic;

import java.util.ArrayList;
import java.util.List;


public class WhyUseGeneric {
    
    public static void main(String[] args) {
        try {
            ArrayList<String> stringList = new ArrayList<>();
            stringList.add("Hello");
//        stringList.add(123);
//            int i = 1 / 0;
            Box<String> box = new Box<>();
            box.set("Hello");
            testBoxGet(box);
            String str = null;
            String lowerCase = str.toLowerCase();
        } catch (Exception e) {
//            throw new RuntimeException(e);
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private static void testBoxGet(Box<String> box) {

        String s = box.get();
    }
    
    
    private static void testGeneric1() {
        List list = new ArrayList();
        list.add("xxString");
        list.add(100d);
        list.add(new Person());

        query(list);
    }

    private static void query(List list) {

        Object o = list.get(0);
    }

    private static void testGeneric2() {
        List<String> list = new ArrayList();
        list.add("xxString");
//        list.add(100d);
//        list.add(new Person());
        query1((list));
    }

    private static void query1(List<String> list) {
        String s = list.get(0);
    }
}

