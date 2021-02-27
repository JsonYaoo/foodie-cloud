package com.imooc.order;

import com.imooc.item.service.ItemService;
import com.imooc.order.fallback.itemservice.ItemCommentsFeignClient;
import com.imooc.user.service.AddressService;
import com.imooc.user.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * User服务应用
 */
@SpringBootApplication
@MapperScan(basePackages = "com.imooc.order.mapper")
@ComponentScan(basePackages = {"com.imooc", "org.n3r.idworker"})
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(
        // 所以使用指定扫类的方式来加载: 这里不加载ItemCommentsService以解决"Ambiguous mapping"的报错问题
        clients = {
                ItemCommentsFeignClient.class,
                ItemService.class,
                UserService.class,
                AddressService.class
        }
        // 扫包方式, 不能解决指定降级的Feign Client的问题
//        basePackages = {
//            // 解决Hystrix降级时, 不能把item.service的扫包去掉, 因为还有其他商品中心的服务
//            "com.imooc.item.service",
//            "com.imooc.user.service",
//            // 指定降级的Feign Client在哪个包, 不配是扫不进来的, 即是都是com.imooc.order, 但会报错"Ambiguous mapping"
//            "com.imooc.order.fallback.itemservice"
//        }
)
// 开启Hystrix使用Hystrix和Hystrix Dashboard
@EnableCircuitBreaker
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
