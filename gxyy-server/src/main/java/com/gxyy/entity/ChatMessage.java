package com.gxyy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long exchangeId;
    private Long senderId;
    private Long receiverId;
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
