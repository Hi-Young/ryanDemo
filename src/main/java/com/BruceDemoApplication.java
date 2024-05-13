package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author heyyon
 * @date 2023-04-22 16:52
 */
@SpringBootApplication//去掉数据源
@MapperScan("com.**.mapper")
public class BruceDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(BruceDemoApplication.class,args);
    }

}
