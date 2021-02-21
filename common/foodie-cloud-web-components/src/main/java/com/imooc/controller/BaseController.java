package com.imooc.controller;

import org.springframework.stereotype.Controller;

import java.io.File;

@Controller
public class BaseController {

    public static final String FOODIE_SHOPCART = "shopcart";

    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;

    public static final String REDIS_USER_TOKEN = "redis_user_token";

    // 支付中心的调用地址
    public String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";		// produce

    // 微信支付成功 -> 支付中心 -> 天天吃货平台
    //                       |-> 回调通知的url
//    String payReturnUrl = "http://api.z.mukewang.com/foodie-dev-api/orders/notifyMerchantOrderPaid";
    // 本地地址
//    String payReturnUrl = "http://38vf4v.natappfree.cc/orders/notifyMerchantOrderPaid";
    // 虚拟机地址
    public String payReturnUrl = "http://b85hwu.natappfree.cc/foodie-dev-api/orders/notifyMerchantOrderPaid";

    // 用户上传头像的位置
    public static final String IMAGE_USER_FACE_LOCATION = File.separator + "workspaces" +
                                                            File.separator + "images" +
                                                            File.separator + "foodie" +
                                                            File.separator + "faces";
//    public static final String IMAGE_USER_FACE_LOCATION = "/workspaces/images/foodie/faces";

    // FIXME Redis未引用
//    @Autowired
//    private RedisOperator redisOperator;

    // FIXME 用户域服务未抽取
//    // 改用UsersVO => 忽略隐私信息, 生成用户token, 存入redis会话
//    public UsersVO conventUsersVO(Users userResult) {
//        // 生成用户token, 存入redis会话
//        String uniqueToken = UUID.randomUUID().toString().trim();
//        redisOperator.set(REDIS_USER_TOKEN + ":" + userResult.getId(), uniqueToken);
//
//        // 复制Users属性
//        UsersVO usersVO = new UsersVO();
//        BeanUtils.copyProperties(userResult, usersVO);
//        usersVO.setUserUniqueToken(uniqueToken);
//
//        return usersVO;
//    }
}
