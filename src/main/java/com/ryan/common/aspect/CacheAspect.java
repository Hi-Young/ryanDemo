package com.ryan.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 简单缓存切面 - 演示AOP在缓存场景的应用
 */
@Slf4j
@Aspect
@Component
public class CacheAspect {

    // 简单的内存缓存，实际项目中建议使用Redis
    private final ConcurrentMap<String, CacheData> cache = new ConcurrentHashMap<>();

    /**
     * 缓存注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Cacheable {
        String key() default ""; // 缓存key，为空时自动生成
        long expireTime() default 300; // 过期时间(秒)
        String prefix() default "cache"; // key前缀
    }

    /**
     * 清除缓存注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CacheEvict {
        String key() default ""; // 要清除的缓存key
        String prefix() default "cache"; // key前缀
        boolean allEntries() default false; // 是否清除所有缓存
    }

    /**
     * 缓存数据包装类
     */
    private static class CacheData {
        private final Object data;
        private final long expireTime;

        public CacheData(Object data, long expireTime) {
            this.data = data;
            this.expireTime = expireTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        public Object getData() {
            return data;
        }
    }

    /**
     * 缓存切面
     */
    @Around("@annotation(cacheable)")
    public Object handleCache(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String cacheKey = generateCacheKey(joinPoint, cacheable.key(), cacheable.prefix());
        
        // 检查缓存
        CacheData cacheData = cache.get(cacheKey);
        if (cacheData != null && !cacheData.isExpired()) {
            log.info("🎯 缓存命中: {} -> {}", cacheKey, cacheData.getData().getClass().getSimpleName());
            return cacheData.getData();
        }
        
        // 缓存未命中，执行方法
        log.info("🔄 缓存未命中，执行方法: {}", cacheKey);
        Object result = joinPoint.proceed();
        
        // 将结果存入缓存
        if (result != null) {
            long expireTime = System.currentTimeMillis() + cacheable.expireTime() * 1000;
            cache.put(cacheKey, new CacheData(result, expireTime));
            log.info("💾 数据已缓存: {} (过期时间: {}秒)", cacheKey, cacheable.expireTime());
        }
        
        return result;
    }

    /**
     * 清除缓存切面
     */
    @Around("@annotation(cacheEvict)")
    public Object handleCacheEvict(ProceedingJoinPoint joinPoint, CacheEvict cacheEvict) throws Throwable {
        Object result = joinPoint.proceed();
        
        if (cacheEvict.allEntries()) {
            // 清除所有缓存
            int size = cache.size();
            cache.clear();
            log.info("🗑️ 已清除所有缓存，共{}条", size);
        } else {
            // 清除指定缓存
            String cacheKey = generateCacheKey(joinPoint, cacheEvict.key(), cacheEvict.prefix());
            CacheData removed = cache.remove(cacheKey);
            if (removed != null) {
                log.info("🗑️ 已清除缓存: {}", cacheKey);
            } else {
                log.info("🤷 缓存不存在: {}", cacheKey);
            }
        }
        
        return result;
    }

    /**
     * 生成缓存key
     */
    private String generateCacheKey(ProceedingJoinPoint joinPoint, String key, String prefix) {
        if (!key.isEmpty()) {
            return prefix + ":" + key;
        }
        
        // 自动生成key：类名:方法名:参数hashCode
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(prefix).append(":")
                  .append(className).append(":")
                  .append(methodName);
        
        if (args.length > 0) {
            for (Object arg : args) {
                keyBuilder.append(":").append(arg != null ? arg.hashCode() : "null");
            }
        }
        
        return keyBuilder.toString();
    }

    /**
     * 获取当前缓存状态（用于调试）
     */
    public void printCacheStatus() {
        log.info("📊 缓存状态统计:");
        log.info("   - 总条数: {}", cache.size());
        
        int expiredCount = 0;
        for (String key : cache.keySet()) {
            CacheData data = cache.get(key);
            if (data != null && data.isExpired()) {
                expiredCount++;
            }
        }
        log.info("   - 已过期: {}", expiredCount);
        log.info("   - 有效条数: {}", cache.size() - expiredCount);
        
        // 清理过期缓存
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}

// 使用示例（在你的Service中使用）
/*
@Service
public class UserService {
    
    @Cacheable(key = "user:all", expireTime = 600)
    public List<User> getAllUsers() {
        log.info("从数据库查询所有用户...");
        return userMapper.selectList(null);
    }
    
    @Cacheable(prefix = "user", expireTime = 300)
    public User getUserById(Long id) {
        log.info("从数据库查询用户: {}", id);
        return userMapper.selectById(id);
    }
    
    @CacheEvict(key = "user:all")
    public void createUser(User user) {
        userMapper.insert(user);
        log.info("用户创建成功，已清除用户列表缓存");
    }
    
    @CacheEvict(allEntries = true)
    public void clearAllCache() {
        log.info("清除所有用户相关缓存");
    }
}
*/