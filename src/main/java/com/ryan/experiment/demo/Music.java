package com.ryan.experiment.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heyyon
 * @date 2024-06-18 22:24
 */
public class Music {
//    static String initStr = "";
    
    public static void main(String[] args) {

//        Integer integer = Integer.valueOf(0);
        List<Instrument> list = new ArrayList<>();
        list.add(new Piano());
        list.add(new Violin());
        for (Instrument instrument : list) {
            instrument.play();
        }
//        String str = "world";
//        setStr(str);
//        System.out.println(str);
//        String initStr = "";
//        initStr = "hello world";
//        initStr = initStr + "1";
//        setStr(initStr);
//        printStr(initStr);
    }
    
    public static void setStr(String updateStr) {
        updateStr = updateStr + " hello";
        System.out.println(updateStr);
        System.out.println(0);
    }

    public static void printStr(String updateStr) {
        System.out.println(updateStr);
    }
    
    
}

