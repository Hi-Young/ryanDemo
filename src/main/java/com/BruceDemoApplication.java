package com;

import com.bruce.demo.service.UserService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author heyyon
 * @date 2023-04-22 16:52
 */
@SpringBootApplication//change name1
@MapperScan("com.**.mapper")
public class BruceDemoApplication implements CommandLineRunner {


    public final UserService userService;

    public BruceDemoApplication(UserService userService) {
        this.userService = userService;
    }
    public static void main(String[] args) {
        SpringApplication.run(BruceDemoApplication.class,args);
    }

    public void run(String... args) {
        userService.addUser("Bruce");
    }

}
