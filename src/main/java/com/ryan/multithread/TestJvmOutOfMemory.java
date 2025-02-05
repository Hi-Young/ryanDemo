package com.ryan.multithread;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author heyyon
 * @date 2025-02-04 9:20
 */
public class TestJvmOutOfMemory {

    public static void main(String[] args) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            String str = "";
            for (int j = 0; j < 1000; j++) {
                str += UUID.randomUUID().toString();
            }
            list.add(str);
        }
        System.out.println("ok");
    }
}
