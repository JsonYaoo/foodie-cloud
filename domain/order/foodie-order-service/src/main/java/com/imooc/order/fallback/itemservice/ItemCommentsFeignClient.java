package com.imooc.order.fallback.itemservice;

import com.imooc.item.service.ItemCommentsService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 商品中心服务Feign Client: 这里用于实现降级：
 * A. 问题:
 *      1) 讨厌的地方来了，对于需要在调用端指定降级业务的场景来说，由于@RequestMapping和@xxxMapping注解可以从原始接口上继承，因此不能配置两个完全一样的访问路径，否则启动报错。
 *      2) 在我们的实际案例中，ItemCommentsService上面定义了RequestMapping，同时ItemCommentsServiceFeign继承自ItemCommentsService，因此相当于在Spring上下文中加载了
 *         两个访问路径一样的方法，会报错"Ambiguous mapping"
 * B. 解决问题的思路就是:
 *      避免让Spring的上下文中，同时加载两个访问路径相同的方法
 * C. 解决方案：
 *      1) 在启动类扫包的时候，不要把原始Feign接口扫描进来, 具体做法：可以使用EnableFeignClients注解的clients属性，只加载需要的Feign接口(推荐)
 *          1. 优点：服务提供者和服务调用者都不需要额外的配置
 *          2. 缺点：启动的时候配置麻烦一点，要指定加载每一个用到的接口
 *      2) 原始Feign接口不要定义RequestMapping注解(不推荐)
 *          1. 优点：启动的时候直接扫包即可，不用指定加载接口
 *          2. 缺点：
 *              a, 服务提供者要额外配置路径访问的注解
 *              b, 任何情况下，即使不需要在调用端定义fallback类，服务调用者都需要声明一个
 *      3) 原始Feign接口不要定义@FeignClients注解，这样就不会被加载到上下文当中(推荐)
 *          1. 优点：启动的时候直接扫包即可，不用指定加载接口，服务提供者不用额外配置
 *          2. 缺点：任何情况下，服务调用者都需要声明一个额外@FeignCliet接口
 *      => 各有利弊, 按照喜好来选就行, 课程里用的是1), 个人认为3)方案既可以满足服务提供方不用必须引入OpenFeign的包, 还可以解决当下业务问题, 而且调用方对Feign Client管理更规范
 */
// 1. 对ItemCommentsFeignClient实现Hystrix降级: 直接配置降级类
@FeignClient(value = "foodie-item-service", fallback = ItemCommentsFallback.class)
// 2. 对ItemCommentsFeignClient实现Hystrix降级: 配置降级工厂类
//@FeignClient(value = "foodie-item-service", fallbackFactory = ItemCommentsFallbackFactory.class)
public interface ItemCommentsFeignClient extends ItemCommentsService {

}
