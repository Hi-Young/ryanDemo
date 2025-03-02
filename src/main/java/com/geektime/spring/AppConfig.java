// 方式2：在配置类中使用 @Bean
package com.geektime.spring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public MyBean myBean() {
        System.out.println("MyBean 实例化 via @Bean 方法");
        return new MyBean();
    }
}
