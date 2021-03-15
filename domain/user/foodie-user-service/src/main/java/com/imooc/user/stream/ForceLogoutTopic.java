package com.imooc.user.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * 集成Stream测试: 测试强制登出Topic
 */
public interface ForceLogoutTopic {

    /**
     * 测试强制登出, 消费者名称
     */
    String INPUT = "force-logout-consumer";

    /**
     * 测试强制登出, 生产者名称
     */
    String OUTPUT = "force-logout-producer";

    /**
     * 消费者信道
     * @return
     */
    @Input(INPUT)
    SubscribableChannel input();

    /**
     * 生产者信道
     * @return
     */
    @Output(OUTPUT)
    MessageChannel output();

}
