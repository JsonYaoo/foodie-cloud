package com.imooc.user.consumer;

import com.imooc.auth.pojo.Account;
import com.imooc.auth.pojo.AuthCode;
import com.imooc.auth.pojo.AuthResponse;
import com.imooc.auth.service.AuthService;
import com.imooc.user.stream.ForceLogoutTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * 集成Stream测试: 测试强制登出Topic Consumer
 */
@Slf4j
@EnableBinding(value = {
        ForceLogoutTopic.class
})
public class UserMessageConsumer {

    @Autowired
    private AuthService authService;

    /**
     * 消费强制登出Topic
     * @param payload
     */
    @StreamListener(ForceLogoutTopic.INPUT)
    public void consumerLogoutMessage(String payload){
        log.info("Force logout user, uid={}", payload);

        Account account = Account.builder()
                        .userId(payload)
                        .skipVerification(true)
                        .build();
        AuthResponse authResponse = authService.delete(account);
        if(!AuthCode.SUCCESS.getCode().equals(authResponse.getCode())){
            log.error("Error occurred when deleting user session, uid={}", payload);
            throw new RuntimeException("Cannot delete user session");
        }
    }

    // 1) 重试（待会配置）& requeue
    // 2) 死信队列 & 服务降级
    /**
     * 具体异常降级逻辑
     * @param message
     */
    @ServiceActivator(inputChannel = "force-logout-topic.force-logout-group.errors")
    public void fallback(Message<?> message){
        log.info("Force logout failed");
        // do nothing

        // 新零售发布库存 - 异步请求
        // 降级： 钉钉群的接口 - 通知运营

        // 强制登出 -> inactive user
        // user表 -> flag (active/inactive) -> 不让你下次登录
    }
}
