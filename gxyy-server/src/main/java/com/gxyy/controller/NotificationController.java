package com.gxyy.controller;

import com.gxyy.common.Result;
import com.gxyy.entity.Notification;
import com.gxyy.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<List<Notification>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(notificationService.getMyNotifications(userId));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAsRead(userId, id);
        return Result.ok();
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }
}
