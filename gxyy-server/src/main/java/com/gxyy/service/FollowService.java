package com.gxyy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxyy.entity.Follow;
import com.gxyy.vo.UserVO;

import java.util.List;

public interface FollowService extends IService<Follow> {
    void follow(Long followerId, Long followeeId);
    void unfollow(Long followerId, Long followeeId);
    boolean isFollowing(Long followerId, Long followeeId);
    List<UserVO> getFollowers(Long userId);
    List<UserVO> getFollowing(Long userId);
    long getFollowerCount(Long userId);
    long getFollowingCount(Long userId);
    List<Long> getFollowerIds(Long userId);
}
