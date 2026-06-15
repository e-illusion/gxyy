package com.gxyy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxyy.common.BusinessException;
import com.gxyy.entity.Notification;
import com.gxyy.mapper.NotificationMapper;
import com.gxyy.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public List<Notification> getMyNotifications(Long userId) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime));
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = baseMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能操作自己的通知");
        }
        notification.setIsRead(true);
        baseMapper.updateById(notification);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return baseMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false));
    }
}
