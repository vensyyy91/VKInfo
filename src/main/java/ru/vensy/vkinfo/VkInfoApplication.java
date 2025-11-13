package ru.vensy.vkinfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VkInfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VkInfoApplication.class, args);
    }

}
