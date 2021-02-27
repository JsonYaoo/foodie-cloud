package com.imooc.order.fallback.itemservice;

import com.google.common.collect.Lists;
import com.imooc.item.pojo.vo.MyCommentVO;
import com.imooc.pojo.PagedGridResult;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 对ItemCommentsFeignClient实现Hystrix降级: 配置降级工厂类
 */
@Component
public class ItemCommentsFallbackFactory implements FallbackFactory<ItemCommentsFeignClient> {

    @Override
    public ItemCommentsFeignClient create(Throwable cause) {
        return new ItemCommentsFeignClient(){
            @Override
            // => 测试结果: ItemCommentsFallbackFactory配置方式的降级, 不能触发多级降级, 但一级降级还是可以保证的
//            @HystrixCommand(fallbackMethod = "fallback2")
            public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
                MyCommentVO commentVO = new MyCommentVO();
                commentVO.setContent("正在加载中1...");

                PagedGridResult result = new PagedGridResult();
                result.setRows(Lists.newArrayList(commentVO));
                result.setTotal(2);
                result.setRecords(2);
                return result;
            }

            // 最后一级降级不用配置@HystrixCommand
            // => 测试结果: ItemCommentsFallbackFactory配置方式的降级, 不能触发多级降级, 但一级降级还是可以保证的
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
        };
    }

}
