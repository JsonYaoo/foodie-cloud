package com.imooc.auth.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 鉴权服务: 响应体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * 账户实体
     */
    private Account account;

    /**
     * 响应编码
     */
    private Long code;

}
