package com.imooc.order.consumer;

import com.imooc.enums.OrderStatusEnum;
import com.imooc.order.mapper.OrderStatusMapper;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.bo.OrderStatusCheckBo;
import com.imooc.order.stream.CheckOrderTopic;
import com.imooc.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 集成Stream测试: 延迟队列实现关闭超时订单
 */
@Slf4j
@EnableBinding(value = {
        CheckOrderTopic.class
})
public class CheckOrderConsumer {

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    /**
     * 延迟队列实现关闭超时订单Topic: 过了1天才会发送消息过来消费, 所以一般都是未关闭的超时订单, 但还是要做个判断校验一下
     * @param orderStatusCheckBo
     */
    @StreamListener(CheckOrderTopic.INPUT)
    public void consumerOrderStatusMessage(OrderStatusCheckBo orderStatusCheckBo){
        String orderId = orderStatusCheckBo.getOrderId();

        log.info("received order check request, orderId={}", orderId);

        // 查询所有未付款订单，判断时间是否超时（1天），超时则关闭交易
        OrderStatus queryOrder = new OrderStatus();
        queryOrder.setOrderId(orderId);
        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> list = orderStatusMapper.select(queryOrder);
        if (CollectionUtils.isEmpty(list)) {
            log.info("order paid or closed, orderId={}", orderId);
            return;
        }

        // 获得订单创建时间
        Date createdTime = list.get(0).getCreatedTime();
        // 和当前时间进行对比
        int days = DateUtil.daysBetween(createdTime, new Date());
        if (days >= 1) {
            // 超过1天，关闭订单
            OrderStatus update = new OrderStatus();
            update.setOrderId(orderId);
            update.setOrderStatus(OrderStatusEnum.CLOSE.type);
            update.setCloseTime(new Date());

            int count = orderStatusMapper.updateByPrimaryKey(update);
            log.info("Closed order, orderId={}, count={}", orderId, count);
        }
    }
}
