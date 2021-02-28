package com.imooc.user.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 集成配置中心: 拉取配置中心中的User服务配置: 实现动态拉取业务开关控制配置
 */
@Configuration
@RefreshScope
@Data
public class UserApplicationProperties {

    @Value("${userservice.registration.disabled}")
    private boolean disabledRegistration;

}
