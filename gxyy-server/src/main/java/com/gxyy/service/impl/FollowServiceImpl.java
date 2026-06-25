package com.gxyy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxyy.common.BusinessException;
import com.gxyy.entity.Follow;
import com.gxyy.entity.User;
import com.gxyy.mapper.FollowMapper;
import com.gxyy.mapper.UserMapper;
import com.gxyy.service.FollowService;
import com.gxyy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow>
        implements FollowService {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BusinessException("不能关注自己");
        }
        User followee = userMapper.selectById(followeeId);
        if (followee == null) throw new BusinessException("用户不存在");
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFolloweeId, followeeId)
        );
        if (count > 0) throw new BusinessException("已关注");
        Follow f = new Follow();
        f.setFollowerId(followerId);
        f.setFolloweeId(followeeId);
        baseMapper.insert(f);
    }

    @Override
    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        baseMapper.delete(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId)
            .eq(Follow::getFolloweeId, followeeId));
    }

    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId)
            .eq(Follow::getFolloweeId, followeeId)) > 0;
    }

    @Override
    public List<UserVO> getFollowers(Long userId) {
        List<Follow> list = baseMapper.selectList(
            new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFolloweeId, userId)
                .orderByDesc(Follow::getCreateTime));
        return list.stream().map(f -> toVO(userMapper.selectById(f.getFollowerId())))
                .filter(v -> v != null).toList();
    }

    @Override
    public List<UserVO> getFollowing(Long userId) {
        List<Follow> list = baseMapper.selectList(
            new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, userId)
                .orderByDesc(Follow::getCreateTime));
        return list.stream().map(f -> toVO(userMapper.selectById(f.getFolloweeId())))
                .filter(v -> v != null).toList();
    }

    @Override
    public long getFollowerCount(Long userId) {
        return baseMapper.selectCount(
            new LambdaQueryWrapper<Follow>().eq(Follow::getFolloweeId, userId));
    }

    @Override
    public long getFollowingCount(Long userId) {
        return baseMapper.selectCount(
            new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId));
    }

    @Override
    public List<Long> getFollowerIds(Long userId) {
        return baseMapper.selectList(
            new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFolloweeId, userId)
                .select(Follow::getFollowerId))
            .stream().map(Follow::getFollowerId).toList();
    }

    private UserVO toVO(User user) {
        if (user == null) return null;
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        return vo;
    }
}
