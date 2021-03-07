package com.imooc.config;

import com.imooc.filer.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    // 注意的是, 这里通过自动装配的方式注入的Bean有时候为null, 可能是个bug, 因为在不同包中 => 通过参数注入可以解决这个问题
//    @Autowired
//    @Qualifier("authFilter")
//    private AuthFilter authFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, AuthFilter authFilter){
        return builder.routes()
                // Auth在网关层有很多种做法, 【最常用方案】网关层或微服务自己本地校验jwt token有效性, 不向auth-service发起远程调用：
                // 1) 【路由配置最简单】可以把AuthFilter注册为global filter, 然后在AuthFilter里配置需要过滤的url pattern(也可以从config-server动态拉取)
                // 2) 【路由配置也简单】也可以采用interceptor的形式
                // 3) 【路由配置最丑(现在)】这就是我选的路, 只是为了演示下自定义过滤器, 才走了这条不归路

                // => 要注意声明URL Pattern的先后顺序, 一个URL可能匹配多个路由, 先来后到
                // 1) 将其他需要登录校验的地址添加在下面, 或者采用上面提到的其他方案改造登录检测过程
                // 2) 修改前端JS代码, 记得把login后返回的jwt-token和refresh-token带到每个请求的header里
                // 3) 前端JS依据header中的Authorization来判断是否登录
                // 指定需要做登录校验的路由(login和logout不需要执行过滤器), 其他的太多了, 先不配...
                .route(r -> r.path("/address/list",
                        "/address/add",
                        "/address/update",
                        "/address/setDefalut",
                        "/address/delete",
                        "/userInfo/**", "/center/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://FOODIE-USER-SERVICE")
                )
                // 指定刷新Token接口路由的服务
                // => 踩坑告警:
                //    1) Gateway指定路由规则时, 如果auth接口同时暴露了FeignClient接口又想暴露指定web服务, 那么必须声明为/**, 即暴露全部的服务
                //    2) 而且该接口的方法返回值上必须有@ResponseBody, 其中在实现类上的@RestController是不会认的, 只有这样才会调用得通
                .route(r -> r.path("/auth-service/**")
//                .route(r -> r.path("/auth-service/refresh")
                        .uri("lb://FOODIE-AUTH-SERVICE")
                )
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
