package com.imooc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * Gateway网关路由规则配置类
 */
@Configuration
public class RoutesConfiguration {

    /**
     * 注入基于请求HostAddress的限流特征量
     */
    @Autowired
    @Qualifier("hostAddressKeyResolver")
    private KeyResolver hostAddressKeyResolver;

    /**
     * 注入User服务的限流规则
     */
    @Autowired
    @Qualifier("redisRateLimiterUser")
    private RedisRateLimiter redisRateLimiterUser;

    /**
     * 注入Item服务的限流规则
     */
    @Autowired
    @Qualifier("redisRateLimiterItem")
    private RedisRateLimiter redisRateLimiterItem;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder){
        return builder.routes()
                // 主搜模块还没整理 <= 由于有/items/search和/items/catItems, 所以需要排在item的路径断言之前
                .route(r -> r.path("/search/**", "/index/**", "/items/search", "/items/catItems")
                        .uri("lb://FOODIE-SEARCH-SERVICE")
                )
                .route(r -> r.path("/address/**", "/passport/**", "/userInfo/**", "/center/**")
                                .filters(f -> f.requestRateLimiter(c -> {
                                    c.setKeyResolver(hostAddressKeyResolver);
                                    c.setRateLimiter(redisRateLimiterUser);
                                    // 自定义限流后的响应代码
//                                    c.setStatusCode(HttpStatus.BAD_GATEWAY);
                                }))
                                .uri("lb://FOODIE-USER-SERVICE")
                )
                .route(r -> r.path("/items/**")
                                .filters(f -> f.requestRateLimiter(c -> {
                                    c.setKeyResolver(hostAddressKeyResolver);
                                    c.setRateLimiter(redisRateLimiterItem);
                                    // 自定义限流后的响应代码
//                                    c.setStatusCode(HttpStatus.BAD_GATEWAY);
                                }))
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
