package com.imooc.order.pojo.bo;

import lombok.Data;

/**
 * 集成Stream测试: 延迟队列实现关闭超时订单, 用于检查订单属性
 */
@Data
public class OrderStatusCheckBo {

    /**
     * 订单ID
     */
    private String orderId;

    // 可继续添加其他属性
}
