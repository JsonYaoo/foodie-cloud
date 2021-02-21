package com.imooc.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Item服务应用
 */
@SpringBootApplication
//@MapperScan(basePackages = "com.imooc.cart.mapper")
@ComponentScan(basePackages = {"com.imooc", "org.n3r.idworker"})
@EnableDiscoveryClient
// TODO 添加Feign注解
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}
