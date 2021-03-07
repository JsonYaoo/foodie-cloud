package com.imooc.auth.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 鉴权服务: 账户实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * Token
     */
    private String token;

    /**
     * 用于刷新Token的UUID
     */
    private String refreshToken;

}
