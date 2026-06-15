package com.gxyy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxyy.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    List<Notification> getMyNotifications(Long userId);

    void markAsRead(Long userId, Long notificationId);

    Long getUnreadCount(Long userId);
}
