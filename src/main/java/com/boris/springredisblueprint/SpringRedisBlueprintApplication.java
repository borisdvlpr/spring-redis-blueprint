package com.boris.springredisblueprint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringRedisBlueprintApplication {
    static void main(String[] args) {
        SpringApplication.run(SpringRedisBlueprintApplication.class, args);
    }
}
