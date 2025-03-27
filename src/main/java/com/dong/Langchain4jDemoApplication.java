package com.dong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.dong.entity") // 扫描实体类
@EnableJpaRepositories(basePackages = "com.dong.repository") // 扫描JPA仓库
public class Langchain4jDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(Langchain4jDemoApplication.class, args);
    }
}