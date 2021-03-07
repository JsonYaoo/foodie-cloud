package com.imooc.auth.service;

import com.imooc.auth.pojo.Account;
import com.imooc.auth.pojo.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 鉴权服务接口
 */
@FeignClient("foodie-auth-service")
@RequestMapping("auth-service")
public interface AuthService {

    @PostMapping("token")
    @ResponseBody
    public AuthResponse tokenize(@RequestParam("userId") String userId);

    @PostMapping("verify")
    @ResponseBody
    public AuthResponse verify(@RequestBody Account account);

    @PostMapping("refresh")
    @ResponseBody
    public AuthResponse refresh(@RequestParam("refresh") String refresh);

    @DeleteMapping("delete")
    @ResponseBody
    public AuthResponse delete(@RequestBody Account account);

}
