package com.imooc.order.fallback.itemservice;

import com.google.common.collect.Lists;
import com.imooc.item.pojo.vo.MyCommentVO;
import com.imooc.pojo.PagedGridResult;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * 1. 对ItemCommentsFeignClient实现Hystrix降级: 直接配置降级类
 */
@Component
// 但要配置假的RequestMapping，把spring糊弄过去, 不推荐 => 因为Fallback class must implement the interface annotated by @FeignClient
@RequestMapping("JokeJoke")
public class ItemCommentsFallback implements ItemCommentsFeignClient {

    @Override
    @HystrixCommand(fallbackMethod = "fallback2")
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
        throw new RuntimeException("正在加载中1...");
    }

    // 最后一级降级不用配置@HystrixCommand
    public PagedGridResult fallback2(String userId, Integer page, Integer pageSize) {
        MyCommentVO commentVO = new MyCommentVO();
        commentVO.setContent("正在加载中2...");

        PagedGridResult result = new PagedGridResult();
        result.setRows(Lists.newArrayList(commentVO));
        result.setTotal(2);
        result.setRecords(2);
        return result;
    }

    @Override
    public void saveComments(Map<String, Object> map) {

    }
}
