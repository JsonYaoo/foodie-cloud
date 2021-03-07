package com.imooc.auth.service.impl;

import com.imooc.auth.pojo.Account;
import com.imooc.auth.pojo.AuthResponse;
import com.imooc.auth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.imooc.auth.pojo.AuthCode.*;

/**
 * 鉴权服务: 鉴权服务接口实现类
 */
@RestController
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * UserToken前缀
     */
    private static final String USER_TOKEN = "USER_TOKEN-";

    @Override
    public AuthResponse tokenize(String userId) {
        Account account = Account.builder()
                .userId(userId)
                .build();

        String token = jwtService.token(account);
        account.setToken(token);
        account.setRefreshToken(UUID.randomUUID().toString());

        // USER_TOKEN-userId->account
        redisTemplate.opsForValue().set(USER_TOKEN + userId, account);
        // UUID->userId
        redisTemplate.opsForValue().set(account.getRefreshToken(), userId);

        return AuthResponse.builder()
                .account(account)
                .code(SUCCESS.getCode())
                .build();
    }

    // 生成Token是交由服务方实现, 验签最好交由Gateway自己验签
    @Override
    public AuthResponse verify(Account account) {
        boolean success = jwtService.verify(account.getToken(), account.getUserId());
        return AuthResponse.builder()
                .code(success ? SUCCESS.getCode() : USER_NOT_FOUND.getCode())
                .build();
    }

    // 有很多种方法实现自动刷新，比如前端主动调用(可以在AuthResponse里将过期时间返回给前端口)
    @Override
    public AuthResponse refresh(String refreshToken) {
        String userId = (String) redisTemplate.opsForValue().get(refreshToken);
        if (StringUtils.isBlank(userId)) {
            return AuthResponse.builder()
                    .code(INVALID_CREDENTIAL.getCode())
                    .build();
        }

        redisTemplate.delete(refreshToken);
        return tokenize(userId);
    }

    @Override
    public AuthResponse delete(@RequestBody Account account) {
        AuthResponse token = verify(account);
        AuthResponse resp = new AuthResponse();
        if (SUCCESS.getCode().equals(token.getCode())) {
            redisTemplate.delete(USER_TOKEN + account.getUserId());
            redisTemplate.delete(account.getRefreshToken());
            resp.setCode(SUCCESS.getCode());
        } else {
            resp.setCode(USER_NOT_FOUND.getCode());
        }
        return resp;
    }
}
