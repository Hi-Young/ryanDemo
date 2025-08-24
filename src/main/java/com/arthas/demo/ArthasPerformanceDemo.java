package com.arthas.demo;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ArthasPerformanceDemo {

    // For memory leak simulation
    private static List<byte[]> memoryLeakList = new ArrayList<>();

    public void cpuIntensiveTask() {
        long startTime = System.nanoTime();
        int count = 0;
        // Simulate a CPU-intensive task
        for (int i = 0; i < 1000000; i++) {
            count += Math.sin(i) * Math.cos(i);
        }
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        System.out.println("CPU intensive task finished in " + duration + " ms.");
    }

    public void memoryAllocationTask() {
        System.out.println("Allocating memory...");
        // Allocate 10MB of memory
        for (int i = 0; i < 10; i++) {
            byte[] buffer = new byte[1024 * 1024]; // 1MB
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Memory allocation finished.");
    }

    public void memoryLeakSimulation() {
        System.out.println("Adding 1MB to the static list to simulate a leak...");
        memoryLeakList.add(new byte[1024 * 1024]); // 1MB leak
    }

    public void highGCTrigger() {
        System.out.println("Triggering high GC activity...");
        for (int i = 0; i < 500; i++) {
            List<String> tempList = new ArrayList<>();
            for (int j = 0; j < 10000; j++) {
                tempList.add("String-" + j);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("High GC activity finished.");
    }
}
