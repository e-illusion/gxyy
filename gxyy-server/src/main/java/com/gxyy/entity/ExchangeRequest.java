package com.gxyy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exchange_request")
public class ExchangeRequest {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromUserId;
    private Long toUserId;
    private Long fromItemId;
    private Long toItemId;

    private String message;

    /**
     * PENDING: 待确认
     * ACCEPTED: 已同意
     * REJECTED: 已拒绝
     * CANCELLED: 已取消
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
