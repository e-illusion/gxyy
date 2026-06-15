package com.gxyy.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExchangeRequestVO {
    private Long id;

    // 发起方信息
    private Long fromUserId;
    private String fromUserName;
    private String fromUserAvatar;

    // 发起方拿出的物品
    private Long fromItemId;
    private String fromItemTitle;
    private String fromItemImages;

    // 接收方信息
    private Long toUserId;
    private String toUserName;

    // 目标物品
    private Long toItemId;
    private String toItemTitle;
    private String toItemImages;

    private String message;
    private String status;
    private LocalDateTime createTime;
}
