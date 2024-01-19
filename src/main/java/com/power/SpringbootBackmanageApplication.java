package com.power;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringbootBackmanageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootBackmanageApplication.class, args);
    }

}
