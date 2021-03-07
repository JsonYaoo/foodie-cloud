package com.imooc;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 服务网关Gateway平台
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PlatformGatewayApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PlatformGatewayApplication.class)
                .web(WebApplicationType.REACTIVE)
                .run(args);
    }
}
