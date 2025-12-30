package com.flashsaleshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.flashsaleshop.mapper")
public class FlashsaleShopBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashsaleShopBackendApplication.class, args);
    }
}
