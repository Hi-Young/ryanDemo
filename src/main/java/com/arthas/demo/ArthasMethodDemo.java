package com.arthas.demo;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ArthasMethodDemo {

    private Random random = new Random();

    public String normalMethod(String input) {
        return "Processed: " + input.toUpperCase();
    }

    public static String staticMethod(String input) {
        return "Processed statically: " + input.toLowerCase();
    }

    public void slowMethod() {
        try {
            nestedMethodA();
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void nestedMethodA() {
        nestedMethodB();
    }

    public void nestedMethodB() {
        // Deepest part of the call stack
    }

    public int randomMethod() {
        int value = random.nextInt(100);
        if (value > 80) {
            throw new IllegalStateException("Random value too high!");
        }
        return value;
    }

    public void exceptionMethod() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    public String overloadedMethod(String s) {
        return "String version: " + s;
    }

    public String overloadedMethod(int i) {
        return "Int version: " + i;
    }
}
