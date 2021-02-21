package com.imooc.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // FIXME 文件上传配置类: 未配置
//    @Autowired
//    private FileUpload fileUpload;

    // 实现静态资源的映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/")  // 映射swagger2 -> 保持原有的资源映射
                .addResourceLocations("file:/workspaces/images/");  // 映射本地静态资源 -> 相当于用域名替换调这些路径, 就是写死, 因为要存到数据库中的, 哪个环境都一样
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    // FIXME 自定义Token拦截器未配置
//    @Bean
//    public UserTokenInterceptor userTokenInterceptor() {
//        return new UserTokenInterceptor();
//    }
    // FIXME 自定义Token拦截器未配置
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(userTokenInterceptor()).addPathPatterns("/hello/hello");
//         TODO 继续添加待拦截的路由
//    }
}
