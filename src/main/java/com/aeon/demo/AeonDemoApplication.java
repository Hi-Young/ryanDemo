package com.aeon.demo;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

/**
 * AEON(永旺)「促销 + 券」最小化可运行 Demo。
 *
 * <p>注意：本 Demo 为了不依赖当前工程的 DB/Redis 等外部资源，显式排除相关自动配置。</p>
 *
 * @author codex
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        DynamicDataSourceAutoConfiguration.class,
        RedisAutoConfiguration.class
})
public class AeonDemoApplication {

    public static void main(String[] args) {
        // 使用独立配置文件，避免读取工程现有 application.yml 导致需要 MySQL/Redis 等外部依赖
        System.setProperty("spring.config.name", "aeon-demo");
        // 通过 Profile 隔离：避免当前工程原有应用启动时误加载本 Demo 的 Controller/Service
        System.setProperty("spring.profiles.active", "aeon-demo");
        SpringApplication.run(AeonDemoApplication.class, args);
    }
}
