package com.ryan.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志切面 - 记录用户操作行为
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 操作日志注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface OperationLog {
        String value(); // 操作描述
        String module() default ""; // 模块名称
        boolean recordParams() default true; // 是否记录参数
        boolean recordResult() default false; // 是否记录返回值
    }

    @Around("@annotation(operationLog)")
    public Object recordOperation(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        String userAgent = request != null ? request.getHeader("User-Agent") : "Unknown";
        String ip = request != null ? getClientIp(request) : "Unknown";
        String url = request != null ? request.getRequestURL().toString() : "Unknown";
        String method = request != null ? request.getMethod() : "Unknown";
        
        // 方法信息
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // 开始时间
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        log.info("📝 操作开始 ===========================================");
        log.info("🔹 操作描述: {}", operationLog.value());
        log.info("🔹 所属模块: {}", operationLog.module().isEmpty() ? "默认模块" : operationLog.module());
        log.info("🔹 执行方法: {}#{}", className, methodName);
        log.info("🔹 请求地址: {} {}", method, url);
        log.info("🔹 客户端IP: {}", ip);
        log.info("🔹 用户代理: {}", userAgent);
        log.info("🔹 开始时间: {}", startTime);
        
        // 记录参数
        if (operationLog.recordParams() && args.length > 0) {
            try {
                String params = objectMapper.writeValueAsString(args);
                log.info("🔹 请求参数: {}", params);
            } catch (Exception e) {
                log.info("🔹 请求参数: [参数序列化失败: {}]", e.getMessage());
            }
        }
        
        long startMillis = System.currentTimeMillis();
        Object result = null;
        boolean success = false;
        String errorMsg = null;
        
        try {
            result = joinPoint.proceed();
            success = true;
            return result;
        } catch (Exception e) {
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long endMillis = System.currentTimeMillis();
            String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            if (success) {
                log.info("✅ 操作成功");
                // 记录返回值
                if (operationLog.recordResult() && result != null) {
                    try {
                        String resultStr = objectMapper.writeValueAsString(result);
                        log.info("🔹 返回结果: {}", resultStr);
                    } catch (Exception e) {
                        log.info("🔹 返回结果: [结果序列化失败: {}]", e.getMessage());
                    }
                }
            } else {
                log.error("❌ 操作失败: {}", errorMsg);
            }
            
            log.info("🔹 结束时间: {}", endTime);
            log.info("🔹 耗时: {}ms", endMillis - startMillis);
            log.info("📝 操作结束 ===========================================");
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

// 使用示例（在你的Controller中使用）
/*
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @OperationLog(value = "查询用户信息", module = "用户管理", recordResult = true)
    @GetMapping("/{id}")
    public ResultVO<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return ResultVO.success(user);
    }
    
    @OperationLog(value = "创建新用户", module = "用户管理", recordParams = true)
    @PostMapping
    public ResultVO<String> createUser(@RequestBody User user) {
        userService.save(user);
        return ResultVO.success("用户创建成功");
    }
    
    @OperationLog(value = "删除用户", module = "用户管理")
    @DeleteMapping("/{id}")
    public ResultVO<String> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return ResultVO.success("用户删除成功");
    }
}
*/