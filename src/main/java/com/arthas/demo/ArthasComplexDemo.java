package com.arthas.demo;

import com.ryan.business.entity.user.User;
import com.ryan.business.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArthasComplexDemo {

    @Autowired
    private UserMapper userMapper;

    // 第1层：订单处理入口
    public List<User> processComplexOrder(int userCount) {
        System.out.println("=== 开始处理复杂订单 ===");
        
        // 预处理阶段
        preProcessOrder();
        
        // 核心业务处理
        List<User> users = executeBusinessLogic(userCount);
        
        // 后处理阶段
        postProcessOrder();
        
        return users;
    }

    // 第2层：预处理
    private void preProcessOrder() {
        validatePermission();
        initializeCache();
        loadConfiguration();
    }

    // 第2层：核心业务逻辑
    private List<User> executeBusinessLogic(int userCount) {
        // 获取用户列表
        List<Long> userIds = getUserIdList(userCount);
        
        // 批量处理用户数据
        return batchProcessUsers(userIds);
    }

    // 第2层：后处理
    private void postProcessOrder() {
        sendNotification();
        updateStatistics();
        cleanupResources();
    }

    // 第3层：权限验证
    private void validatePermission() {
        System.out.println("验证用户权限...");
        simulateDelay(100);
    }

    // 第3层：初始化缓存
    private void initializeCache() {
        System.out.println("初始化缓存...");
        simulateDelay(200);
    }

    // 第3层：加载配置
    private void loadConfiguration() {
        System.out.println("加载业务配置...");
        simulateDelay(150);
    }

    // 第3层：获取用户ID列表
    private List<Long> getUserIdList(int count) {
        System.out.println("构建用户ID列表...");
        simulateDelay(50);
        
        List<Long> userIds = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            userIds.add((long) i);
        }
        return userIds;
    }

    // 第3层：批量处理用户
    private List<User> batchProcessUsers(List<Long> userIds) {
        List<User> users = new ArrayList<>();
        
        for (Long userId : userIds) {
            System.out.println("处理用户ID: " + userId);
            
            // 多层嵌套的用户处理
            User user = processIndividualUser(userId);
            if (user != null) {
                users.add(user);
            }
        }
        
        return users;
    }

    // 第4层：处理单个用户
    private User processIndividualUser(Long userId) {
        // 获取用户基础信息
        User basicInfo = getUserBasicInfo(userId);
        
        if (basicInfo != null) {
            // 增强用户信息
            enrichUserInfo(basicInfo);
            
            // 验证用户数据
            validateUserData(basicInfo);
        }
        
        return basicInfo;
    }

    // 第5层：获取用户基础信息
    private User getUserBasicInfo(Long userId) {
        System.out.println("  查询用户基础信息: " + userId);
        
        // 这里有多层数据库调用
        return executeDeepDatabaseQuery(userId);
    }

    // 第5层：增强用户信息
    private void enrichUserInfo(User user) {
        System.out.println("  增强用户信息: " + user.getId());
        
        // 模拟调用外部服务获取额外信息
        callExternalService(user);
        
        // 从缓存获取历史数据
        loadHistoryData(user);
    }

    // 第5层：验证用户数据
    private void validateUserData(User user) {
        System.out.println("  验证用户数据: " + user.getId());
        simulateDelay(30);
    }

    // 第6层：深层数据库查询
    private User executeDeepDatabaseQuery(Long userId) {
        // 第一次查询：基础信息
        User user = userMapper.getUserDetail(userId); // 这里是慢SQL
        
        if (user != null) {
            // 第二次查询：关联数据（模拟）
            queryRelatedData(userId);
            
            // 第三次查询：扩展信息（模拟）
            queryExtendedInfo(userId);
        }
        
        return user;
    }

    // 第6层：调用外部服务
    private void callExternalService(User user) {
        System.out.println("    调用外部服务...");
        simulateDelay(200); // 模拟网络调用
    }

    // 第6层：加载历史数据
    private void loadHistoryData(User user) {
        System.out.println("    加载历史数据...");
        simulateDelay(100);
    }

    // 第7层：查询关联数据
    private void queryRelatedData(Long userId) {
        System.out.println("    查询关联数据...");
        simulateDelay(150);
    }

    // 第7层：查询扩展信息
    private void queryExtendedInfo(Long userId) {
        System.out.println("    查询扩展信息...");
        simulateDelay(100);
    }

    // 通知相关方法
    private void sendNotification() {
        System.out.println("发送通知...");
        simulateDelay(80);
    }

    private void updateStatistics() {
        System.out.println("更新统计信息...");
        simulateDelay(120);
    }

    private void cleanupResources() {
        System.out.println("清理资源...");
        simulateDelay(50);
    }

    // 工具方法：模拟延迟
    private void simulateDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}