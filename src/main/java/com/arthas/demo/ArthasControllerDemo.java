package com.arthas.demo;

import com.ryan.business.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/arthas/demo")
public class ArthasControllerDemo {

    private Random random = new Random();
    
    @Autowired
    private ArthasMethodDemo arthasMethodDemo;
    
    @Autowired
    private ArthasBatchQueryDemo arthasBatchQueryDemo;
    
    @Autowired
    private ArthasComplexDemo arthasComplexDemo;
    
    @Autowired
    private ArthasLargeMethodDemo arthasLargeMethodDemo;

    @GetMapping("/hello")
    public String hello(@RequestParam(name = "name", defaultValue = "World") String name) {
        return "Hello, " + name + "! Welcome to Arthas demo.";
    }

    @GetMapping("/user/1")
    public Map<String, Object> getUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1);
        user.put("name", "TestUser");
        user.put("email", "testuser@example.com");
        return user;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", 100L);
        stats.put("successCount", 98L);
        stats.put("errorCount", 2L);
        return stats;
    }

    @GetMapping("/slow")
    public String slowApi(@RequestParam(name = "level", defaultValue = "1") int level) {
        try {
            Thread.sleep(level * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Slow API call finished after " + level + " seconds.";
    }

    @GetMapping("/random")
    public String randomResponse() {
        int value = arthasMethodDemo.randomMethod();
        return "Random value: " + value;
    }

    @GetMapping("/error")
    public String triggerError(@RequestParam(name = "errorType", defaultValue = "1") int errorType) {
        if (errorType == 1) {
            throw new IllegalArgumentException("Invalid parameter provided.");
        } else {
            throw new RuntimeException("A generic runtime error occurred.");
        }
    }
    
    @GetMapping("/batch-query")
    public Map<String, Object> batchQuery(@RequestParam(name = "count", defaultValue = "5") int count) {
        long startTime = System.currentTimeMillis();
        
        List<User> users = arthasBatchQueryDemo.submitOrder(count);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", users.size());
        result.put("totalTimeMs", totalTime);
        result.put("averageTimeMs", users.size() > 0 ? totalTime / users.size() : 0);
        result.put("users", users);
        
        return result;
    }
    
    @GetMapping("/complex-query")
    public Map<String, Object> complexQuery(@RequestParam(name = "count", defaultValue = "3") int count) {
        long startTime = System.currentTimeMillis();
        
        List<User> users = arthasComplexDemo.processComplexOrder(count);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "复杂业务处理完成");
        result.put("totalUsers", users.size());
        result.put("totalTimeMs", totalTime);
        result.put("layerCount", "7层嵌套调用");
        result.put("users", users);
        
        return result;
    }
    
    @GetMapping("/large-method")
    public Map<String, Object> largeMethod(@RequestParam(name = "count", defaultValue = "3") int count) {
        long startTime = System.currentTimeMillis();
        
        List<User> users = arthasLargeMethodDemo.processLargeMethod(count);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "大方法处理完成 - 性能瓶颈隐藏在大量代码中");
        result.put("description", "真正的慢操作(slowDatabaseQuery)被隐藏在processLargeMethod的大量代码中");
        result.put("totalUsers", users.size());
        result.put("totalTimeMs", totalTime);
        result.put("codeLines", "约400行代码");
        result.put("hiddenBottleneck", "slowDatabaseQuery方法调用慢SQL");
        result.put("analysisChallenge", "如何在大量代码中快速定位真正的性能瓶颈？");
        result.put("users", users);
        
        return result;
    }
}
