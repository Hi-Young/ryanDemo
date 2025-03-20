package com.bruce.ioc;

import org.springframework.context.annotation.Bean;

public class ZooConfig {

    @Bean
    public Tiger tiger() {
        return new Tiger();
    }

}

class Tiger {

}
