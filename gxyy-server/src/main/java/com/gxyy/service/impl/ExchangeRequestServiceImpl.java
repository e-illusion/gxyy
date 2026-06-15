package com.gxyy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxyy.common.BusinessException;
import com.gxyy.dto.ExchangeRequestDTO;
import com.gxyy.entity.ExchangeRequest;
import com.gxyy.entity.Item;
import com.gxyy.entity.Notification;
import com.gxyy.entity.User;
import com.gxyy.mapper.ExchangeRequestMapper;
import com.gxyy.mapper.ItemMapper;
import com.gxyy.mapper.NotificationMapper;
import com.gxyy.mapper.UserMapper;
import com.gxyy.service.ExchangeRequestService;
import com.gxyy.vo.ExchangeRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeRequestServiceImpl extends ServiceImpl<ExchangeRequestMapper, ExchangeRequest>
        implements ExchangeRequestService {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public ExchangeRequestVO createRequest(Long fromUserId, ExchangeRequestDTO dto) {
        // 获取目标物品
        Item toItem = itemMapper.selectById(dto.getToItemId());
        if (toItem == null) {
            throw new BusinessException("目标物品不存在");
        }
        if (!"ACTIVE".equals(toItem.getStatus())) {
            throw new BusinessException("该物品已被换出或下架");
        }
        if (toItem.getOwnerId().equals(fromUserId)) {
            throw new BusinessException("不能用自己的物品交换自己的物品");
        }

        // 获取发起方的物品
        Item fromItem = itemMapper.selectById(dto.getFromItemId());
        if (fromItem == null) {
            throw new BusinessException("你选择的物品不存在");
        }
        if (!fromItem.getOwnerId().equals(fromUserId)) {
            throw new BusinessException("你只能用自己的物品发起交换");
        }
        if (!"ACTIVE".equals(fromItem.getStatus())) {
            throw new BusinessException("你选择的物品已被换出或下架");
        }

        // 检查是否已有待处理的相同交换请求
        Long existingCount = baseMapper.selectCount(
                new LambdaQueryWrapper<ExchangeRequest>()
                        .eq(ExchangeRequest::getFromUserId, fromUserId)
                        .eq(ExchangeRequest::getToItemId, dto.getToItemId())
                        .eq(ExchangeRequest::getStatus, "PENDING"));
        if (existingCount > 0) {
            throw new BusinessException("你已经向该物品发起过交换请求，请等待对方处理");
        }

        // 创建交换请求
        ExchangeRequest request = new ExchangeRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toItem.getOwnerId());
        request.setFromItemId(dto.getFromItemId());
        request.setToItemId(dto.getToItemId());
        request.setMessage(dto.getMessage());
        request.setStatus("PENDING");
        baseMapper.insert(request);

        // 发送通知给目标物品的主人
        User fromUser = userMapper.selectById(fromUserId);
        Notification notification = new Notification();
        notification.setUserId(toItem.getOwnerId());
        notification.setType("EXCHANGE_REQUEST");
        notification.setContent((fromUser != null ? fromUser.getUsername() : "用户") + " 想用物品《" + fromItem.getTitle() + "》交换你的《" + toItem.getTitle() + "》");
        notification.setRelatedId(request.getId());
        notification.setIsRead(false);
        notificationMapper.insert(notification);

        return buildRequestVO(request);
    }

    @Override
    @Transactional
    public ExchangeRequestVO acceptRequest(Long userId, Long requestId) {
        ExchangeRequest request = baseMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("交换请求不存在");
        }
        if (!request.getToUserId().equals(userId)) {
            throw new BusinessException(403, "只能处理自己的交换请求");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("该请求已被处理");
        }

        request.setStatus("ACCEPTED");
        baseMapper.updateById(request);

        // 将双方物品标记为已换出
        Item toItem = itemMapper.selectById(request.getToItemId());
        if (toItem != null) {
            toItem.setStatus("EXCHANGED");
            itemMapper.updateById(toItem);
        }
        Item fromItem = itemMapper.selectById(request.getFromItemId());
        if (fromItem != null) {
            fromItem.setStatus("EXCHANGED");
            itemMapper.updateById(fromItem);
        }

        // 通知发起方
        User toUser = userMapper.selectById(userId);
        Notification notification = new Notification();
        notification.setUserId(request.getFromUserId());
        notification.setType("EXCHANGE_ACCEPTED");
        notification.setContent((toUser != null ? toUser.getUsername() : "用户") + " 同意了你的交换请求！快去联系对方完成交换吧");
        notification.setRelatedId(request.getId());
        notification.setIsRead(false);
        notificationMapper.insert(notification);

        return buildRequestVO(request);
    }

    @Override
    @Transactional
    public ExchangeRequestVO rejectRequest(Long userId, Long requestId) {
        ExchangeRequest request = baseMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("交换请求不存在");
        }
        if (!request.getToUserId().equals(userId)) {
            throw new BusinessException(403, "只能处理自己的交换请求");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("该请求已被处理");
        }

        request.setStatus("REJECTED");
        baseMapper.updateById(request);

        // 通知发起方
        User toUser = userMapper.selectById(userId);
        Notification notification = new Notification();
        notification.setUserId(request.getFromUserId());
        notification.setType("EXCHANGE_REJECTED");
        notification.setContent((toUser != null ? toUser.getUsername() : "用户") + " 拒绝了你的交换请求");
        notification.setRelatedId(request.getId());
        notification.setIsRead(false);
        notificationMapper.insert(notification);

        return buildRequestVO(request);
    }

    @Override
    public void cancelRequest(Long userId, Long requestId) {
        ExchangeRequest request = baseMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("交换请求不存在");
        }
        if (!request.getFromUserId().equals(userId)) {
            throw new BusinessException(403, "只能取消自己发起的请求");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("该请求已被处理");
        }

        request.setStatus("CANCELLED");
        baseMapper.updateById(request);
    }

    @Override
    public List<ExchangeRequestVO> getMyRequests(Long userId) {
        // 收到和发出的请求都返回
        List<ExchangeRequest> requests = baseMapper.selectList(
                new LambdaQueryWrapper<ExchangeRequest>()
                        .and(w -> w.eq(ExchangeRequest::getFromUserId, userId)
                                .or().eq(ExchangeRequest::getToUserId, userId))
                        .orderByDesc(ExchangeRequest::getCreateTime));

        return requests.stream().map(this::buildRequestVO).toList();
    }

    private ExchangeRequestVO buildRequestVO(ExchangeRequest request) {
        ExchangeRequestVO vo = BeanUtil.copyProperties(request, ExchangeRequestVO.class);

        // 填充发起方信息
        User fromUser = userMapper.selectById(request.getFromUserId());
        if (fromUser != null) {
            vo.setFromUserName(fromUser.getUsername());
            vo.setFromUserAvatar(fromUser.getAvatar());
        }

        // 填充发起方物品信息
        Item fromItem = itemMapper.selectById(request.getFromItemId());
        if (fromItem != null) {
            vo.setFromItemTitle(fromItem.getTitle());
            // 取第一张图片用于展示
            if (StrUtil.isNotBlank(fromItem.getImages())) {
                List<String> images = JSONUtil.toList(fromItem.getImages(), String.class);
                vo.setFromItemImages(images.isEmpty() ? null : images.get(0));
            }
        }

        // 填充目标物品信息
        Item toItem = itemMapper.selectById(request.getToItemId());
        if (toItem != null) {
            vo.setToItemTitle(toItem.getTitle());
            if (StrUtil.isNotBlank(toItem.getImages())) {
                List<String> images = JSONUtil.toList(toItem.getImages(), String.class);
                vo.setToItemImages(images.isEmpty() ? null : images.get(0));
            }
        }

        // 填充目标用户信息
        User toUser = userMapper.selectById(request.getToUserId());
        if (toUser != null) {
            vo.setToUserName(toUser.getUsername());
        }

        return vo;
    }
}
