package com.arthas.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;

@Component
public class ArthasTimerDemo {

    private Random random = new Random();

    @Scheduled(fixedRate = 5000) // Runs every 5 seconds
    public void fastTimer() {
        System.out.println("Fast timer executed at: " + new Date());
    }

    @Scheduled(fixedRate = 15000) // Runs every 15 seconds
    public void processDataBatch() {
        System.out.println("Processing data batch...");
        try {
            // Simulate work
            Thread.sleep(random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Data batch processing finished.");
    }

    @Scheduled(cron = "0 * * * * ?") // Runs every minute
    public void healthCheck() {
        System.out.println("Performing health check...");
    }

    @Scheduled(fixedRate = 20000) // Runs every 20 seconds
    public void slowTask() {
        System.out.println("Starting slow task...");
        try {
            // Simulate a slow task
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Slow task finished.");
    }
}
