package com.imooc.order.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * 集成Stream测试: 延迟队列实现关闭超时订单
 */
public interface CheckOrderTopic {

    /**
     * 延迟队列实现关闭超时订单, 消费者名称
     */
    String INPUT = "orderstatus-consumer";

    /**
     * 延迟队列实现关闭超时订单, 生产者名称
     */
    String OUTPUT = "orderstatus-producer";

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
