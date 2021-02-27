package com.imooc.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Item服务应用
 */
@SpringBootApplication
//@MapperScan(basePackages = "com.imooc.cart.mapper")
@ComponentScan(basePackages = {"com.imooc", "org.n3r.idworker"})
@EnableDiscoveryClient
// 没有Feign接口调用, 不需要开启@EnableFeignClients
// 开启Hystrix使用Hystrix和Hystrix Dashboard
@EnableCircuitBreaker
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}
