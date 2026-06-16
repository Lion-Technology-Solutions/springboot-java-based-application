package com.liontech.resorts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class LiontechResortsApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(LiontechResortsApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(LiontechResortsApplication.class);
    }
}
