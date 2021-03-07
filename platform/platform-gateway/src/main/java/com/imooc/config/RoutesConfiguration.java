package com.imooc.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway网关路由规则配置类
 */
@Configuration
public class RoutesConfiguration {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder){
        return builder.routes()
                // 主搜模块还没整理 <= 由于有/items/search和/items/catItems, 所以需要排在item的路径断言之前
                .route(r -> r.path("/search/**", "/index/**", "/items/search", "/items/catItems")
                        .uri("lb://FOODIE-SEARCH-SERVICE")
                )
                .route(r -> r.path("/address/**", "/passport/**", "/userInfo/**", "/center/**")
                                .uri("lb://FOODIE-USER-SERVICE")
                )
                .route(r -> r.path("/items/**")
                        .uri("lb://FOODIE-ITEM-SERVICE")
                )
                .route(r -> r.path("/shopcart/**")
                        .uri("lb://FOODIE-CART-SERVICE")
                )
                .route(r -> r.path("/orders/**", "/myorders/**", "/mycomments/**")
                        .uri("lb://FOODIE-ORDER-SERVICE")
                )
                .build();
    }
}
