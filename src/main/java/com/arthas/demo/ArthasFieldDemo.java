package com.arthas.demo;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ArthasFieldDemo {

    public static int staticCounter = 0;

    private String instanceField = "initial_value";

    private List<String> listField = new ArrayList<>();

    private Map<String, String> mapField = new HashMap<>();

    public ArthasFieldDemo() {
        listField.add("element1");
        listField.add("element2");

        mapField.put("key1", "value1");
        mapField.put("key2", "value2");
    }

    public static void incrementCounter() {
        staticCounter++;
    }

    public String getInstanceField() {
        return instanceField;
    }

    public void setInstanceField(String value) {
        this.instanceField = value;
        System.out.println("instanceField is now: " + this.instanceField);
    }

    public List<String> getListField() {
        return listField;
    }

    public Map<String, String> getMapField() {
        return mapField;
    }
}
