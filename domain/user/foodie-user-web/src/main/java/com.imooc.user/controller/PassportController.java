package com.imooc.user.controller;

import com.imooc.bo.ShopcartBO;
import com.imooc.controller.BaseController;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.UserBO;
import com.imooc.user.service.UserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.RedisOperator;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Api(value = "注册登录", tags = {"用于注册登录的相关接口"})
@RestController
@RequestMapping("passport")
public class PassportController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET")
    @GetMapping("/usernameIsExist")
    public IMOOCJSONResult usernameIsExist(@RequestParam String username) {

        // 1. 判断用户名不能为空
        if (StringUtils.isBlank(username)) {
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }

        // 2. 查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }

        // 3. 请求成功，用户名没有重复
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户注册", notes = "用户注册", httpMethod = "POST")
    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO,// Json对象用@RequestBody
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPwd = userBO.getConfirmPassword();

        // 0. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(password) ||
                StringUtils.isBlank(confirmPwd)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1. 查询用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }

        // 2. 密码长度不能少于6位
        if (password.length() < 6) {
            return IMOOCJSONResult.errorMsg("密码长度不能少于6");
        }

        // 3. 判断两次密码是否一致
        if (!password.equals(confirmPwd)) {
            return IMOOCJSONResult.errorMsg("两次密码输入不一致");
        }

        // 4. 实现注册
        Users userResult = userService.createUser(userBO);

        // TODO 拆分完微服务再打开
        // 改用UsersVO => 忽略隐私信息, 生成用户token, 存入redis会话
//        UsersVO usersVO = conventUsersVO(userResult);
//
//        // 设置Cookie
//        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(usersVO), true);

        // 同步购物车数据
        synchShopcartData(userResult.getId(), request, response);

        return IMOOCJSONResult.ok();
    }

    /**
     * 注册登录成功后，同步cookie和redis中的购物车数据
     */
    private void synchShopcartData(String userId, HttpServletRequest request,
                                   HttpServletResponse response) {

        /**
         * 1. redis中无数据，如果cookie中的购物车为空，那么这个时候不做任何处理
         *                 如果cookie中的购物车不为空，此时直接放入redis中
         * 2. redis中有数据，如果cookie中的购物车为空，那么直接把redis的购物车覆盖本地cookie
         *                 如果cookie中的购物车不为空，
         *                      如果cookie中的某个商品在redis中存在，
         *                      则以cookie为主，删除redis中的，
         *                      把cookie中的商品直接覆盖redis中（参考京东）
         * 3. 同步到redis中去了以后，覆盖本地cookie购物车的数据，保证本地购物车的数据是同步最新的
         */

        // 从redis中获取购物车
        String shopcartJsonRedis = redisOperator.get(FOODIE_SHOPCART + ":" + userId);

        // 从cookie中获取购物车
        String shopcartStrCookie = CookieUtils.getCookieValue(request, FOODIE_SHOPCART, true);

        if (StringUtils.isBlank(shopcartJsonRedis)) {
            // redis为空，cookie不为空，直接把cookie中的数据放入redis
            if (StringUtils.isNotBlank(shopcartStrCookie)) {
                redisOperator.set(FOODIE_SHOPCART + ":" + userId, shopcartStrCookie);
            }
        } else {
            // redis不为空，cookie不为空，合并cookie和redis中购物车的商品数据（同一商品则覆盖redis）
            if (StringUtils.isNotBlank(shopcartStrCookie)) {

                /**
                 * 1. 已经存在的，把cookie中对应的数量，覆盖redis（参考京东）
                 * 2. 该项商品标记为待删除，统一放入一个待删除的list
                 * 3. 从cookie中清理所有的待删除list
                 * 4. 合并redis和cookie中的数据
                 * 5. 更新到redis和cookie中
                 */

                List<ShopcartBO> shopcartListRedis = JsonUtils.jsonToList(shopcartJsonRedis, ShopcartBO.class);
                List<ShopcartBO> shopcartListCookie = JsonUtils.jsonToList(shopcartStrCookie, ShopcartBO.class);

                // 定义一个待删除list
                List<ShopcartBO> pendingDeleteList = new ArrayList<>();

                for (ShopcartBO redisShopcart : shopcartListRedis) {
                    String redisSpecId = redisShopcart.getSpecId();

                    for (ShopcartBO cookieShopcart : shopcartListCookie) {
                        String cookieSpecId = cookieShopcart.getSpecId();

                        if (redisSpecId.equals(cookieSpecId)) {
                            // 覆盖购买数量，不累加，参考京东
                            redisShopcart.setBuyCounts(cookieShopcart.getBuyCounts());
                            // 把cookieShopcart放入待删除列表，用于最后的删除与合并
                            pendingDeleteList.add(cookieShopcart);
                        }

                    }
                }

                // 从现有cookie中删除对应的覆盖过的商品数据
                shopcartListCookie.removeAll(pendingDeleteList);

                // 合并两个list
                shopcartListRedis.addAll(shopcartListCookie);
                // 更新到redis和cookie
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartListRedis), true);
                redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartListRedis));
            } else {
                // redis不为空，cookie为空，直接把redis覆盖cookie
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, shopcartJsonRedis, true);
            }

        }
    }

    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @PostMapping("/login")
    @HystrixCommand(
            // 全局唯一的标识符号, 默认为函数名称
            commandKey = "loginFail",
            // 全局服务分组标识符, 用于组织Dashboard仪表盘统计信息, 默认为类名
            groupKey = "password",
            // 降级方法名(同一个类中, public | private)
            fallbackMethod = "loginFail",
            // 配置忽略抛出的异常: 当抛出列表中的异常, 不会触发降级
            ignoreExceptions = {IllegalArgumentException.class},
            // 线程池/线程组名称, 多个服务可以共用一个线程池/线程组
            threadPoolKey = "threadPoolA",
            threadPoolProperties = {
                    // 线程池核心线程数
                    @HystrixProperty(name = "coreSize", value = "10"),
                    // 线程池阻塞等待队列长度
                    @HystrixProperty(name = "maxQueueSize", value = "40"),
                    // 线程池阻塞等待队列拒绝策略(队列长度=-1时不生效)
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "15"),
                    // 线程池统计窗口持续时间
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "2024"),
                    // 线程池统计窗口内的桶子数量
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "18")
            }
//            ,
//            commandProperties = {
//                    // 还可以在这里配置@HystrixCommand熔断降级相关的属性, 但这里的这些属性配置在了配置文件中
//            }
    )
    public IMOOCJSONResult login(@RequestBody UserBO userBO,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        // 测试Hystrix超时降级
        Thread.sleep(40000);

        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 0. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(password)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1. 实现登录
        Users userResult = userService.queryUserForLogin(username,
                    MD5Utils.getMD5Str(password));

        if (userResult == null) {
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }

        // 改用UsersVO => 忽略隐私信息, 生成用户token, 存入redis会话
        // TODO 拆分完微服务再打开
//        UsersVO usersVO = conventUsersVO(userResult);

        // 设置Cookie
//        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(usersVO), true);

        // 同步购物车数据
        synchShopcartData(userResult.getId(), request, response);

        return IMOOCJSONResult.ok(userResult);
    }

    /**
     * Hystrix降级方法
     * @param userBO
     * @param request
     * @param response
     * @param throwable
     * @return
     * @throws Exception
     */
    public IMOOCJSONResult loginFail(UserBO userBO,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     Throwable throwable) throws Exception {
        return IMOOCJSONResult.errorMap("验证码输错了(模仿12306)" + throwable.getLocalizedMessage());
    }

    @ApiOperation(value = "用户退出登录", notes = "用户退出登录", httpMethod = "POST")
    @PostMapping("/logout")
    public IMOOCJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        // 清除用户的相关信息的cookie
        CookieUtils.deleteCookie(request, response, "user");

        // 用户退出登录, 清除Redis分布式会话中用户数据
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);

        // 用户退出登录，需要清空购物车
        CookieUtils.deleteCookie(request, response, FOODIE_SHOPCART);

        return IMOOCJSONResult.ok();
    }

}
