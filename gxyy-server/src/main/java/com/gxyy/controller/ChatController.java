package com.gxyy.controller;

import com.gxyy.common.Result;
import com.gxyy.service.ChatService;
import com.gxyy.vo.ChatMessageVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{exchangeId}")
    public Result<List<ChatMessageVO>> getMessages(HttpServletRequest request,
                                                    @PathVariable Long exchangeId) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(chatService.getMessages(userId, exchangeId));
    }

    @PostMapping
    public Result<ChatMessageVO> sendMessage(HttpServletRequest request,
                                              @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        Long exchangeId = ((Number) body.get("exchangeId")).longValue();
        String content = (String) body.get("content");
        return Result.ok(chatService.sendMessage(userId, exchangeId, content));
    }
}
