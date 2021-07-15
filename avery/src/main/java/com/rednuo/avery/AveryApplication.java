package com.rednuo.avery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author redNuo----2020/12/16
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling //开启定时的注解
@EntityScan("com.rednuo.**")
@ComponentScan(basePackages = {"com.rednuo.**", "com.rednuo.avery.config"})
@MapperScan(basePackages = {"com.rednuo.**"})
public class AveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(AveryApplication.class, args);
    }
}
