package com.arthas.demo;

import com.ryan.business.entity.user.User;
import com.ryan.business.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ArthasBatchQueryDemo {

    @Autowired
    private UserMapper userMapper;

    public List<User> submitOrder(int userCount) {
        System.out.println("开始批量查询用户信息，用户数量: " + userCount);
        
        // 模拟先获取基础信息
        getBaseInfo();
        
        // 然后批量查询详细信息
        return queryProducts(userCount);
    }

    public void getBaseInfo() {
        System.out.println("获取基础配置信息...");
        try {
            Thread.sleep(50); // 模拟基础查询耗时
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<User> queryProducts(int userCount) {
        System.out.println("开始循环查询用户详细信息...");
        List<User> users = new ArrayList<>();
        
        // ========== 添加大量干扰代码 ==========
        
        // 1. 无意义的字符串操作
        StringBuilder logBuffer = new StringBuilder();
        for (int x = 0; x < 50; x++) {
            logBuffer.append("log_entry_").append(x).append("_");
            if (x % 10 == 0) {
                logBuffer.append("checkpoint_");
            }
        }
        String logData = logBuffer.toString();
        System.out.println("日志缓冲区长度: " + logData.length());
        
        // 2. 复杂的Map操作
        Map<String, Object> contextMap = new HashMap<>();
        List<String> contextKeys = Arrays.asList(
            "request.id", "session.timeout", "cache.enabled", 
            "retry.count", "batch.size", "connection.timeout"
        );
        
        for (String key : contextKeys) {
            String value = "ctx_" + key.hashCode();
            contextMap.put(key, value);
            
            // 更多无意义处理
            if (value.contains("timeout")) {
                contextMap.put(key + "_processed", value.toUpperCase());
            } else if (value.contains("cache")) {
                contextMap.put(key + "_processed", value.toLowerCase());
            }
        }
        
        // 3. 数字计算干扰
        double calculationSum = 0.0;
        for (int calc = 1; calc <= 200; calc++) {
            calculationSum += Math.sin(calc) * Math.cos(calc);
            if (calc % 50 == 0) {
                calculationSum = calculationSum / 1.5;
                System.out.println("计算检查点 " + calc + ": " + calculationSum);
            }
        }
        contextMap.put("calculation_result", calculationSum);
        
        // 4. 集合操作干扰
        Set<Integer> processedIds = new HashSet<>();
        List<String> statusList = new ArrayList<>();
        
        for (int prep = 1; prep <= userCount * 3; prep++) {
            processedIds.add(prep);
            String status = prep % 3 == 0 ? "active" : (prep % 3 == 1 ? "pending" : "inactive");
            statusList.add(status + "_" + prep);
            
            // 排序和筛选操作
            if (prep % 10 == 0) {
                Collections.shuffle(statusList);
                statusList.sort(String::compareTo);
                System.out.println("状态列表处理: " + statusList.size() + " 项");
            }
        }
        
        // 5. 模拟权限检查
        List<String> permissions = Arrays.asList(
            "user.read", "user.write", "data.export", "admin.access"
        );
        
        Map<String, Boolean> permissionResults = new HashMap<>();
        for (String permission : permissions) {
            boolean hasPermission = ThreadLocalRandom.current().nextBoolean();
            permissionResults.put(permission, hasPermission);
            
            if (hasPermission) {
                System.out.println("权限检查通过: " + permission);
                contextMap.put("perm_" + permission, "granted");
            } else {
                System.out.println("权限检查失败: " + permission);
                contextMap.put("perm_" + permission, "denied");
            }
        }
        
        // ========== 真正的核心逻辑在这里，但被大量代码包围 ==========
        
        // 模拟循环查询多个用户的详细信息
        for (int i = 1; i <= userCount; i++) {
            System.out.println("正在查询第 " + i + " 个用户...");
            
            // 更多干扰代码
            String userContext = "user_context_" + i;
            Map<String, String> userMeta = new HashMap<>();
            userMeta.put("index", String.valueOf(i));
            userMeta.put("context", userContext);
            userMeta.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            // 无意义的条件判断
            if (i % 2 == 0) {
                userMeta.put("type", "even");
                System.out.println("处理偶数用户: " + i);
            } else {
                userMeta.put("type", "odd");
                System.out.println("处理奇数用户: " + i);
            }
            
            // *** 这里是真正的慢操作！但被隐藏在大量代码中 ***
            User user = getDetailInfo((long) i);
            
            if (user != null) {
                users.add(user);
                
                // 更多后处理干扰代码
                if (user.getAge() != null) {
                    userMeta.put("age_category", 
                        user.getAge() < 30 ? "young" : 
                        user.getAge() < 50 ? "middle" : "senior");
                }
                
                if (user.getEmail() != null && user.getEmail().contains("@")) {
                    String domain = user.getEmail().split("@")[1];
                    userMeta.put("email_domain", domain);
                    System.out.println("用户邮箱域名: " + domain);
                }
                
                // 添加到上下文
                contextMap.put("user_" + i + "_meta", userMeta);
            }
            
            // 更多无意义的处理
            for (int j = 0; j < 5; j++) {
                String metaKey = "meta_" + i + "_" + j;
                String metaValue = userContext + "_value_" + j;
                contextMap.put(metaKey, metaValue);
            }
        }
        
        // ========== 更多后处理干扰代码 ==========
        
        // 结果验证和统计
        Map<String, Integer> statistics = new HashMap<>();
        statistics.put("total_users", users.size());
        statistics.put("context_entries", contextMap.size());
        statistics.put("processed_ids", processedIds.size());
        
        for (User user : users) {
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                System.out.println("发现空用户名: " + user.getId());
                statistics.put("empty_names", statistics.getOrDefault("empty_names", 0) + 1);
            }
            
            if (user.getAge() != null) {
                String ageGroup = user.getAge() < 30 ? "young" : 
                                user.getAge() < 50 ? "middle" : "senior";
                statistics.put("age_" + ageGroup, statistics.getOrDefault("age_" + ageGroup, 0) + 1);
            }
        }
        
        // 清理临时数据
        contextMap.clear();
        processedIds.clear();
        statusList.clear();
        permissionResults.clear();
        
        System.out.println("批量查询完成，共查询到 " + users.size() + " 个用户");
        System.out.println("统计信息: " + statistics);
        
        return users;
    }

    public User getDetailInfo(Long userId) {
        // 这里会调用UserMapper.getUserDetail，其中包含慢SQL
        return userMapper.getUserDetail(userId);
    }
}