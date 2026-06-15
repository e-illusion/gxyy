package com.gxyy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * EXCHANGE_REQUEST: 收到交换请求
     * EXCHANGE_ACCEPTED: 交换请求被同意
     * EXCHANGE_REJECTED: 交换请求被拒绝
     */
    private String type;

    private String content;
    private Long relatedId;  // 关联的 exchange_request id
    private Boolean isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
