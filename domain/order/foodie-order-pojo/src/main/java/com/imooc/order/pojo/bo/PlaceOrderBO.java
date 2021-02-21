package com.imooc.order.pojo.bo;

import com.imooc.bo.ShopcartBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 下单BO: 整合SubmitOrderBO和ShopcartBO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderBO {

    private SubmitOrderBO order;

    private List<ShopcartBO> items;
}
