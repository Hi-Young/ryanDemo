package com.ryan.common.config;

import com.ryan.common.interceptor.AutoFillAndQueryCheckInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 * 注册自定义拦截器
 */
@Configuration
public class MyBatisConfig {

    /**
     * 注册自动填充和查询检查拦截器
     */
    @Bean
    public AutoFillAndQueryCheckInterceptor autoFillAndQueryCheckInterceptor() {
        return new AutoFillAndQueryCheckInterceptor();
    }
}