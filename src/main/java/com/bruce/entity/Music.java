package com.bruce.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heyyon
 * @date 2024-06-18 22:24
 */
public class Music {
    public static void main(String[] args) {
        List<Instrument> list = new ArrayList<>();
        list.add(new Piano());
        list.add(new Violin());
        for (Instrument instrument : list) {
            instrument.play();
        }
    }
}
