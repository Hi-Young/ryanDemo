package com.arthas.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ArthasDemoStarter implements CommandLineRunner {

    @Autowired
    private ArthasPerformanceDemo performanceDemo;

    @Autowired
    private ArthasThreadDemo threadDemo;

    @Autowired
    private ArthasFieldDemo fieldDemo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Arthas Demo Starter: Initializing demo scenarios...");

        // Start some background activities for demonstration
        threadDemo.startCpuIntensiveThread();
        threadDemo.startWaitingThread();
        threadDemo.startDeadlockThreads();

        // Run a few tasks that are not on timers
        performanceDemo.cpuIntensiveTask();
        performanceDemo.memoryAllocationTask();

        // Increment the static counter in FieldDemo a few times
        for (int i = 0; i < 5; i++) {
            ArthasFieldDemo.incrementCounter();
            Thread.sleep(1000);
        }
        
        System.out.println("Arthas Demo scenarios are now running in the background.");
        System.out.println("Use Arthas to attach to this application and start monitoring.");
    }
}
