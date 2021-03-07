package com.imooc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Gateway网关Redis限流规则配置类
 */
@Configuration
public class RedisLimiterConfiguration {

    /**
     * 配置基于请求HostAddress的限流特征量
     * @return
     */
    @Bean("hostAddressKeyResolver")
    @Primary
    public KeyResolver hostAddressKeyResolver(){
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress()
        );
    }

    /**
     * 配置User服务的限流规则
     * @return
     */
    @Bean("redisRateLimiterUser")
    @Primary
    public RedisRateLimiter redisRateLimiterUser(){
        // 令牌桶令牌生成速率, 令牌桶的总容量
        return new RedisRateLimiter(1, 2);
    }

    /**
     * 配置Item服务的限流规则
     * @return
     */
    @Bean("redisRateLimiterItem")
    public RedisRateLimiter redisRateLimiterItem(){
        // 令牌桶令牌生成速率, 令牌桶的总容量
        return new RedisRateLimiter(20, 50);
    }
}
