package com.rednuo.core.starter;

/**
 * @author nz.zou 2021/7/14
 * @since avery 1.0.0
 */

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ CoreProperties.class})
@ComponentScan(basePackages = {"com.rednuo.core"})
@MapperScan(basePackages = {"com.rednuo.core.mapper"})
public class CoreAutoConfiguration {
}
