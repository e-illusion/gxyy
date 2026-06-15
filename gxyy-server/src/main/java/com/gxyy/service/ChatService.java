package com.gxyy.service;

import com.gxyy.entity.ChatMessage;
import com.gxyy.vo.ChatMessageVO;

import java.util.List;

public interface ChatService {
    List<ChatMessageVO> getMessages(Long userId, Long exchangeId);
    ChatMessageVO sendMessage(Long userId, Long exchangeId, String content);
}
