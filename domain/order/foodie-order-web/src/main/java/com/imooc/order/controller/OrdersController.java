package com.imooc.order.controller;

import com.imooc.bo.ShopcartBO;
import com.imooc.controller.BaseController;
import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.PayMethod;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.bo.OrderStatusCheckBo;
import com.imooc.order.pojo.bo.PlaceOrderBO;
import com.imooc.order.pojo.bo.SubmitOrderBO;
import com.imooc.order.pojo.vo.MerchantOrdersVO;
import com.imooc.order.pojo.vo.OrderVO;
import com.imooc.order.service.OrderService;
import com.imooc.order.stream.CheckOrderTopic;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@Api(value = "订单相关", tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController
public class OrdersController extends BaseController {

    final static Logger logger = LoggerFactory.getLogger(OrdersController.class);

    /**
     * 分布式接口幂等性: 订单Token前缀
     */
    public static final String REDIS_ORDER_TOKEN_KEY_PREFIX = "ORDER_TOKEN_";
    /**
     * 分布式接口幂等性: 订单Lock前缀
     */
    public static final String REDIS_ORDER_LOCK_KEY_PREFIX = "ORDER_LOCK_";

    @Autowired
    private OrderService orderService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisOperator redisOperator;

    // TODO 整理完微服务再打开
//    @Autowired
//    private RedissonClient redissonClient;

    @Autowired
    private CheckOrderTopic checkOrderTopic;

    @ApiOperation(value = "获取订单Token", notes = "获取订单Token", httpMethod = "POST")
    @PostMapping("/getOrderToken")
    // 这里为了方便用了httpSession, 而实际中要用分布式会话中的id
    public IMOOCJSONResult getOrderToken(HttpSession httpSession){
        String token = UUID.randomUUID().toString();

        // 分布式接口幂等性: 存到Redis中, 用于校验, 过期时间10分钟
        redisOperator.set(REDIS_ORDER_TOKEN_KEY_PREFIX + httpSession.getId(), token, 600);

        // 返回到订单提交页
        return IMOOCJSONResult.ok(token);
    }

    @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
    @PostMapping("/create")
    public IMOOCJSONResult create(
            @RequestBody SubmitOrderBO submitOrderBO,
            HttpServletRequest request,
            HttpServletResponse response) {

        // !!4、分布式接口幂等性: 获取Redis分布式锁, 防止并发情况
        // TODO 整理完微服务再打开
//        RLock lock = redissonClient.getLock(REDIS_ORDER_LOCK_KEY_PREFIX + request.getSession().getId());
//        lock.lock(5, TimeUnit.SECONDS);
        try {
            // !!4、分布式接口幂等性: 从Redis中获取orderToken, 校验通过的则获取分布式锁
            String redisOrderTokenKey = REDIS_ORDER_TOKEN_KEY_PREFIX + request.getSession().getId();
            String redisOrderToken = redisOperator.get(redisOrderTokenKey);
            if(StringUtils.isBlank(redisOrderToken)){
                throw new RuntimeException("orderToken不存在");
            }
            if(!redisOrderToken.equals(submitOrderBO.getToken())){
                throw new RuntimeException("orderToken不正确");
            }

            // !!4、分布式接口幂等性: 校验通过则删除RedisKey
            redisOperator.del(redisOrderTokenKey);
        }finally {
            // TODO 整理完微服务再打开
//            try {
//                lock.unlock();
//            } catch (Exception e) {
//                // 锁过期, do nothing...
//            }
        }

        // 校验支付方式
        if (submitOrderBO.getPayMethod() != PayMethod.WEIXIN.type
            && submitOrderBO.getPayMethod() != PayMethod.ALIPAY.type ) {
            return IMOOCJSONResult.errorMsg("支付方式不支持！");
        }

//        System.out.println(submitOrderBO.toString());

        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId());
        if (StringUtils.isBlank(shopcartJson)) {
            return IMOOCJSONResult.errorMsg("购物数据不正确");
        }

        // TODO 购物车模块妥协
        List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);

        // 1. 创建订单
        OrderVO orderVO = orderService.createOrder(new PlaceOrderBO(submitOrderBO, shopcartList));
        String orderId = orderVO.getOrderId();

        // 2. 创建订单以后，移除购物车中已结算（已提交）的商品
        /**
         * 1001
         * 2002 -> 用户购买
         * 3003 -> 用户购买
         * 4004
         */
        // 清理覆盖现有的redis汇总的购物数据
        shopcartList.removeAll(orderVO.getToBeRemovedShopcatdList());
        redisOperator.set(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId(), JsonUtils.objectToJson(shopcartList));

        // 整合redis之后，完善购物车中的已结算商品清除，并且同步到前端的cookie
        CookieUtils.setCookie(request, response, FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartList), true);

        // 集成Stream测试: 利用延迟队列实现关闭超时订单
        OrderStatusCheckBo orderStatusCheckBo = new OrderStatusCheckBo();
        orderStatusCheckBo.setOrderId(orderId);
        checkOrderTopic.output().send(MessageBuilder
                .withPayload(orderStatusCheckBo)
                // 为了确保关闭的一定时24小时的, 所以再加了5分钟缓冲时间
                .setHeader("x-delay", 24 * 3600 * 1000 + 300 * 1000)
                .build());

        // 3. 向支付中心发送当前订单，用于保存支付中心的订单数据
        MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(payReturnUrl);

        // 为了方便测试购买，所以所有的支付金额都统一改为1分钱
        merchantOrdersVO.setAmount(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("imoocUserId","imooc");
        headers.add("password","imooc");

        HttpEntity<MerchantOrdersVO> entity =
                new HttpEntity<>(merchantOrdersVO, headers);

        ResponseEntity<IMOOCJSONResult> responseEntity =
                restTemplate.postForEntity(paymentUrl,
                                            entity,
                                            IMOOCJSONResult.class);
        IMOOCJSONResult paymentResult = responseEntity.getBody();
        if (paymentResult.getStatus() != 200) {
            logger.error("发送错误：{}", paymentResult.getMsg());
            return IMOOCJSONResult.errorMsg("支付中心订单创建失败，请联系管理员！");
        }

        return IMOOCJSONResult.ok(orderId);
    }

    @PostMapping("notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId) {
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();
    }

    @PostMapping("getPaidOrderInfo")
    public IMOOCJSONResult getPaidOrderInfo(String orderId) {

        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
        return IMOOCJSONResult.ok(orderStatus);
    }
}
