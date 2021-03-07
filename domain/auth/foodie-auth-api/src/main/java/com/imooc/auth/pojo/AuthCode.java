package com.imooc.auth.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 鉴权服务: 响应编码
 */
@AllArgsConstructor
public enum AuthCode {

    /**
     * 成功
     */
    SUCCESS(1L),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1000L),

    /**
     * 验签非法
     */
    INVALID_CREDENTIAL(2000L);

    /**
     * 响应编码
     */
    @Getter
    private Long code;

}
