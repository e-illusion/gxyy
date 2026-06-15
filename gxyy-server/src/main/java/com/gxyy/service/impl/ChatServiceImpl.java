package com.gxyy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxyy.common.BusinessException;
import com.gxyy.entity.ChatMessage;
import com.gxyy.entity.ExchangeRequest;
import com.gxyy.entity.User;
import com.gxyy.mapper.ChatMessageMapper;
import com.gxyy.mapper.ExchangeRequestMapper;
import com.gxyy.mapper.UserMapper;
import com.gxyy.service.ChatService;
import com.gxyy.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final ExchangeRequestMapper exchangeRequestMapper;
    private final UserMapper userMapper;

    @Override
    public List<ChatMessageVO> getMessages(Long userId, Long exchangeId) {
        // 验证用户是这个交换的参与者
        ExchangeRequest exchange = exchangeRequestMapper.selectById(exchangeId);
        if (exchange == null) {
            throw new BusinessException("交换请求不存在");
        }
        if (!exchange.getFromUserId().equals(userId) && !exchange.getToUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看");
        }

        List<ChatMessage> messages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getExchangeId, exchangeId)
                        .orderByAsc(ChatMessage::getCreateTime));

        return messages.stream().map(msg -> {
            User sender = userMapper.selectById(msg.getSenderId());
            return ChatMessageVO.builder()
                    .id(msg.getId())
                    .exchangeId(msg.getExchangeId())
                    .senderId(msg.getSenderId())
                    .senderName(sender != null ? sender.getUsername() : "")
                    .receiverId(msg.getReceiverId())
                    .content(msg.getContent())
                    .isMine(msg.getSenderId().equals(userId))
                    .createTime(msg.getCreateTime())
                    .build();
        }).toList();
    }

    @Override
    public ChatMessageVO sendMessage(Long userId, Long exchangeId, String content) {
        // 验证用户是这个交换的参与者
        ExchangeRequest exchange = exchangeRequestMapper.selectById(exchangeId);
        if (exchange == null) {
            throw new BusinessException("交换请求不存在");
        }

        Long receiverId;
        if (exchange.getFromUserId().equals(userId)) {
            receiverId = exchange.getToUserId();
        } else if (exchange.getToUserId().equals(userId)) {
            receiverId = exchange.getFromUserId();
        } else {
            throw new BusinessException(403, "无权操作");
        }

        ChatMessage msg = new ChatMessage();
        msg.setExchangeId(exchangeId);
        msg.setSenderId(userId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        chatMessageMapper.insert(msg);

        User sender = userMapper.selectById(userId);
        return ChatMessageVO.builder()
                .id(msg.getId())
                .exchangeId(msg.getExchangeId())
                .senderId(msg.getSenderId())
                .senderName(sender != null ? sender.getUsername() : "")
                .receiverId(msg.getReceiverId())
                .content(msg.getContent())
                .isMine(true)
                .createTime(msg.getCreateTime())
                .build();
    }
}
