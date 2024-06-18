package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author heyyon
 * @date 2023-04-22 16:52
 */
@SpringBootApplication//change name1
@MapperScan("com.**.mapper")
public class BruceDemoApplication {
    public static void main(String[] args) {
        Integer integer = Integer.valueOf(0);
        SpringApplication.run(BruceDemoApplication.class,args);
    }

}
