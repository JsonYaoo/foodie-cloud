package com.imooc;

import com.imooc.auth.service.AuthService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 服务网关Gateway平台
 */
@SpringBootApplication
@EnableDiscoveryClient
// 扫描鉴权服务FeignClient
@EnableFeignClients(basePackageClasses = {AuthService.class})
public class PlatformGatewayApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PlatformGatewayApplication.class)
                .web(WebApplicationType.REACTIVE)
                .run(args);
    }
}
