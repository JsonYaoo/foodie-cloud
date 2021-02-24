package com.imooc.item.service;

import com.imooc.item.pojo.Items;
import com.imooc.item.pojo.ItemsImg;
import com.imooc.item.pojo.ItemsParam;
import com.imooc.item.pojo.ItemsSpec;
import com.imooc.item.pojo.vo.CommentLevelCountsVO;
import com.imooc.item.pojo.vo.ShopcartVO;
import com.imooc.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// 服务提供者与服务消费者公用一个接口, 服务消费者调用时, feign接口会动态代理生成代理对象, 发起调用时自动请求foodie-item-service服务
@FeignClient("foodie-item-service")
// 注解会继承到实现类
@RequestMapping("item-api")
public interface ItemService {

    /**
     * 根据商品ID查询详情
     * @param itemId
     * @return
     */
    @GetMapping("item")
    public Items queryItemById(@RequestParam("itemId") String itemId);

    /**
     * 根据商品id查询商品图片列表
     * @param itemId
     * @return
     */
    @GetMapping("itemImages")
    public List<ItemsImg> queryItemImgList(@RequestParam("itemId") String itemId);

    /**
     * 根据商品id查询商品规格
     * @param itemId
     * @return
     */
    @GetMapping("itemSpecs")
    public List<ItemsSpec> queryItemSpecList(@RequestParam("itemId") String itemId);

    /**
     * 根据商品id查询商品参数
     * @param itemId
     * @return
     */
    @GetMapping("itemParam")
    public ItemsParam queryItemParam(@RequestParam("itemId") String itemId);

    /**
     * 根据商品id查询商品的评价等级数量
     * @param itemId
     */
    @GetMapping("countComments")
    public CommentLevelCountsVO queryCommentCounts(@RequestParam("itemId") String itemId);

    /**
     * 根据商品id查询商品的评价（分页）
     * @param itemId
     * @param level
     * @return
     */
    @GetMapping("pagedComments")
    public PagedGridResult queryPagedComments(@RequestParam("itemId") String itemId,
                                              @RequestParam(value = "level", required = false) Integer level,
                                              @RequestParam(value = "page", required = false) Integer page,
                                              @RequestParam(value = "pageSize", required = false) Integer pageSize);

    // TODO 抽取到主搜模块
//    /**
//     * 搜索商品列表
//     * @param keywords
//     * @param sort
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult searhItems(String keywords, String sort,
//                                      Integer page, Integer pageSize);
//
//    /**
//     * 根据分类id搜索商品列表
//     * @param catId
//     * @param sort
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult searhItems(Integer catId, String sort,
//                                      Integer page, Integer pageSize);

    // TODO 购物车领域妥协
    /**
     * 根据规格ids查询最新的购物车中商品数据（用于刷新渲染购物车中的商品数据）
     * @param specIds
     * @return
     */
    @GetMapping("getCartBySpecIds")
    public List<ShopcartVO> queryItemsBySpecIds(@RequestParam("specIds") String specIds);

    /**
     * 根据商品规格id获取规格对象的具体信息
     * @param specId
     * @return
     */
    @GetMapping("singleItemSpec")
    public ItemsSpec queryItemSpecById(@RequestParam("specId") String specId);

    /**
     * 根据商品id获得商品图片主图url
     * @param itemId
     * @return
     */
    @GetMapping("primaryImage")
    public String queryItemMainImgById(@RequestParam("itemId") String itemId);

    /**
     * 减少库存
     * @param specId
     * @param buyCounts
     */
    @PostMapping("decreaseStock")
    public void decreaseItemSpecStock(@RequestParam("specId") String specId,
                                      @RequestParam("buyCounts") int buyCounts);
}
