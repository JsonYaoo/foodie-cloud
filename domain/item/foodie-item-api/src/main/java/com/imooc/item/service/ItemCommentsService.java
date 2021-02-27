package com.imooc.item.service;

import com.imooc.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ItemCommentsService的Hystrix降级:
 * 1. 内部的降级(商品中心): 放到foodie-item-service自己的服务里面来实现, 不用暴露给调用方, 只要降级后把结果返回就好
 * 2. 调用方降级(订单中心, 即商品中心服务的调用方): 交由订单中心来定义降级逻辑, 是订单中心自己的降级逻辑, 与商品中心无关
 */
@FeignClient("foodie-item-service")
@RequestMapping("item-comments-api")
public interface ItemCommentsService {

    /**
     * 我的评价查询 分页
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("myComments")
    public PagedGridResult queryMyComments(@RequestParam("userId") String userId,
                                           @RequestParam(value = "page", required = false) Integer page,
                                           @RequestParam(value = "pageSize", required = false) Integer pageSize);

    /**
     * TODO 保存用户的评论: 解耦订单域接口
     * @param map
     */
    @PostMapping("saveComments")
    public void saveComments(@RequestBody Map<String, Object> map);
}
