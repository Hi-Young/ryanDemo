package com;
import com.ryan.business.service.UserService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author heyyon
 * @date 2023-04-22 16:52
 */
@SpringBootApplication//change name1
@EnableScheduling
@MapperScan("com.**.mapper")
public class RyanDemoApplication implements CommandLineRunner {


    public final UserService userService;

    public RyanDemoApplication(UserService userService) {
        this.userService = userService;
    }
    public static void main(String[] args) {
        // 添加启动参数以激活dev环境配置
        System.setProperty("spring.profiles.active", "dev");
        SpringApplication.run(RyanDemoApplication.class, args);
    }

    public void run(String... args) {
        userService.addUser("Bruce");
    }

}
