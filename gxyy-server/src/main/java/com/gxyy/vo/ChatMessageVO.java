package com.gxyy.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageVO {
    private Long id;
    private Long exchangeId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private Boolean isMine;
    private LocalDateTime createTime;
}
