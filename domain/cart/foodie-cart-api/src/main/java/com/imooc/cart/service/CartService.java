package com.imooc.cart.service;

import com.imooc.bo.ShopcartBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 购物车域: API服务接口抽取
 */
@FeignClient("foodie-cart-service")
@RequestMapping("cart-api")
public interface CartService {

    @PostMapping("addItem")
    public boolean addItemToCart(@RequestParam("userId") String userId,
                                 @RequestBody ShopcartBO shopcartBO);

    @PostMapping("removeItem")
    public boolean removeItemFromCart(@RequestParam("userId") String userId,
                                      @RequestParam("itemSpecId") String itemSpecId);

    @PostMapping("clearCart")
    public boolean clearCart(@RequestParam("userId") String userId);

}
