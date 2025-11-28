package com.arthas.demo;

import com.ryan.business.entity.user.User;
import com.ryan.business.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ArthasLargeMethodDemo {

    @Autowired
    private UserMapper userMapper;

    /**
     * 这是一个"懒惰程序员"写的1000行大方法
     * 真正的性能瓶颈(慢SQL)被隐藏在大量无意义的代码中
     */
    public List<User> processLargeMethod(int userCount) {
        System.out.println("=== 开始处理大方法 ===");
        
        // ========== 第一段：初始化和准备工作 (100行左右) ==========
        List<User> resultUsers = new ArrayList<>();
        Map<String, Object> configMap = new HashMap<>();
        List<String> logMessages = new ArrayList<>();
        Set<Integer> processedIds = new HashSet<>();
        
        // 无意义的字符串操作
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("config_").append(i).append("_");
            if (i % 10 == 0) {
                sb.append("checkpoint_");
            }
        }
        String configString = sb.toString();
        logMessages.add("配置字符串长度: " + configString.length());
        
        // 无意义的数字计算
        double calculationResult = 0.0;
        for (int i = 1; i <= 1000; i++) {
            calculationResult += Math.sin(i) * Math.cos(i);
            if (i % 100 == 0) {
                calculationResult = calculationResult / 2.0;
            }
        }
        configMap.put("calculation_result", calculationResult);
        
        // 模拟配置检查
        List<String> configKeys = Arrays.asList(
            "database.timeout", "cache.enabled", "retry.count", 
            "thread.pool.size", "batch.size", "connection.pool.max",
            "session.timeout", "request.timeout", "response.timeout"
        );
        
        for (String key : configKeys) {
            String value = "default_" + key.hashCode();
            configMap.put(key, value);
            logMessages.add("加载配置: " + key + " = " + value);
            
            // 无意义的字符串处理
            if (value.contains("timeout")) {
                value = value.toUpperCase();
            } else if (value.contains("pool")) {
                value = value.toLowerCase();
            }
        }
        
        // ========== 第二段：业务规则验证 (150行左右) ==========
        boolean isValidRequest = true;
        String validationMessage = "";
        
        // 模拟复杂的业务规则验证
        if (userCount < 1) {
            isValidRequest = false;
            validationMessage = "用户数量不能小于1";
        } else if (userCount > 100) {
            isValidRequest = false;
            validationMessage = "用户数量不能大于100";
        } else {
            // 更多复杂验证
            Random random = new Random();
            int validationCode = random.nextInt(1000);
            
            if (validationCode % 2 == 0) {
                logMessages.add("偶数验证码: " + validationCode);
                if (validationCode % 4 == 0) {
                    logMessages.add("4的倍数验证通过");
                    configMap.put("validation_level", "high");
                } else {
                    logMessages.add("2的倍数验证通过");
                    configMap.put("validation_level", "medium");
                }
            } else {
                logMessages.add("奇数验证码: " + validationCode);
                configMap.put("validation_level", "low");
            }
            
            // 模拟权限检查
            List<String> permissions = Arrays.asList(
                "user.read", "user.write", "user.delete", "admin.access",
                "report.generate", "data.export", "system.config"
            );
            
            for (String permission : permissions) {
                boolean hasPermission = random.nextBoolean();
                configMap.put("perm_" + permission, hasPermission);
                if (hasPermission) {
                    logMessages.add("权限检查通过: " + permission);
                } else {
                    logMessages.add("权限检查失败: " + permission);
                }
            }
        }
        
        if (!isValidRequest) {
            System.out.println("验证失败: " + validationMessage);
            return resultUsers;
        }
        
        // ========== 第三段：数据预处理 (200行左右) ==========
        Map<Integer, String> userStatusMap = new HashMap<>();
        List<Integer> activeUserIds = new ArrayList<>();
        Map<String, Integer> statisticsMap = new HashMap<>();
        
        // 模拟从各种数据源预加载数据
        for (int i = 1; i <= userCount * 5; i++) {
            String status = i % 3 == 0 ? "active" : (i % 3 == 1 ? "inactive" : "pending");
            userStatusMap.put(i, status);
            
            if ("active".equals(status)) {
                activeUserIds.add(i);
            }
            
            // 统计计算
            statisticsMap.put("status_" + status, 
                statisticsMap.getOrDefault("status_" + status, 0) + 1);
        }
        
        // 复杂的数据结构操作
        Map<String, List<Integer>> groupedData = new HashMap<>();
        for (Map.Entry<Integer, String> entry : userStatusMap.entrySet()) {
            String status = entry.getValue();
            groupedData.computeIfAbsent(status, k -> new ArrayList<>()).add(entry.getKey());
        }
        
        // 排序和筛选操作
        List<Integer> sortedActiveIds = new ArrayList<>(activeUserIds);
        Collections.sort(sortedActiveIds);
        
        // 模拟缓存操作
        Map<String, Object> cacheData = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            String cacheKey = "cache_key_" + i;
            Object cacheValue = "cache_value_" + (i * i);
            cacheData.put(cacheKey, cacheValue);
            
            // 模拟缓存命中率计算
            if (i % 7 == 0) {
                logMessages.add("缓存命中: " + cacheKey);
            } else {
                logMessages.add("缓存未命中: " + cacheKey);
            }
        }
        
        // ========== 第四段：核心业务逻辑 - 这里隐藏着真正的性能瓶颈！ ==========
        System.out.println("开始核心业务处理...");
        
        // 大量无意义的循环和计算
        for (int round = 1; round <= 3; round++) {
            System.out.println("处理轮次: " + round);
            
            // 模拟数据转换
            List<String> tempData = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                String data = "temp_data_" + round + "_" + i;
                tempData.add(data);
                
                if (data.length() % 2 == 0) {
                    data = data.toUpperCase();
                } else {
                    data = data.toLowerCase();
                }
            }
            
            // 更多无意义操作
            Collections.shuffle(tempData);
            tempData.sort(String::compareTo);
            
            // *** 这里是真正的性能瓶颈 - 但被隐藏在大量代码中 ***
            for (int i = 1; i <= userCount; i++) {
                System.out.println("  处理用户 " + i + " (轮次 " + round + ")");
                
                // 大量干扰代码
                String userPrefix = "user_" + round + "_" + i;
                Map<String, String> userContext = new HashMap<>();
                userContext.put("prefix", userPrefix);
                userContext.put("round", String.valueOf(round));
                userContext.put("index", String.valueOf(i));
                
                // *** 真正的慢操作在这里！***
                if (round == 2) { // 只在第2轮才真正查询数据库
                    User user = slowDatabaseQuery((long) i); // 这是真正的瓶颈！
                    if (user != null) {
                        resultUsers.add(user);
                    }
                }
                
                // 更多无意义的后处理
                for (int j = 0; j < 20; j++) {
                    String contextKey = "ctx_" + j;
                    String contextValue = userPrefix + "_value_" + j;
                    userContext.put(contextKey, contextValue);
                }
            }
        }
        
        // ========== 第五段：后处理和清理 (200行左右) ==========
        System.out.println("开始后处理...");
        
        // 结果数据验证
        for (User user : resultUsers) {
            if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
                logMessages.add("发现空用户名: " + user.getId());
            }
            
            if (user.getAge() != null && user.getAge() < 0) {
                logMessages.add("发现负年龄: " + user.getId());
            }
            
            // 数据补全
            if (user.getStatus() == null) {
                user.setStatus(1); // 默认状态
                logMessages.add("补全用户状态: " + user.getId());
            }
        }
        
        // 统计信息计算
        Map<String, Integer> finalStatistics = new HashMap<>();
        finalStatistics.put("total_users", resultUsers.size());
        finalStatistics.put("total_logs", logMessages.size());
        finalStatistics.put("cache_entries", cacheData.size());
        finalStatistics.put("config_entries", configMap.size());
        
        // 模拟报告生成
        StringBuilder report = new StringBuilder();
        report.append("=== 处理报告 ===\n");
        report.append("用户数量: ").append(resultUsers.size()).append("\n");
        report.append("日志条数: ").append(logMessages.size()).append("\n");
        report.append("缓存条目: ").append(cacheData.size()).append("\n");
        
        for (Map.Entry<String, Integer> stat : finalStatistics.entrySet()) {
            report.append(stat.getKey()).append(": ").append(stat.getValue()).append("\n");
        }
        
        // 清理临时数据
        configMap.clear();
        logMessages.clear();
        cacheData.clear();
        userStatusMap.clear();
        activeUserIds.clear();
        
        System.out.println("大方法处理完成，返回 " + resultUsers.size() + " 个用户");
        return resultUsers;
    }
    
    /**
     * 真正的慢操作 - 但方法名不明显，容易被忽略
     */
    private User slowDatabaseQuery(Long userId) {
        // 这里调用慢SQL，但在1000行代码中很难发现
        return userMapper.getUserDetail(userId);
    }
}