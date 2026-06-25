package com.gxyy.controller;

import com.gxyy.common.Result;
import com.gxyy.service.FollowService;
import com.gxyy.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public Result<Void> follow(HttpServletRequest request, @PathVariable Long userId) {
        Long followerId = (Long) request.getAttribute("userId");
        followService.follow(followerId, userId);
        return Result.ok();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(HttpServletRequest request, @PathVariable Long userId) {
        Long followerId = (Long) request.getAttribute("userId");
        followService.unfollow(followerId, userId);
        return Result.ok();
    }

    @GetMapping("/{userId}/check")
    public Result<Map<String, Boolean>> check(HttpServletRequest request, @PathVariable Long userId) {
        Long followerId = (Long) request.getAttribute("userId");
        return Result.ok(Map.of("isFollowing", followService.isFollowing(followerId, userId)));
    }

    @GetMapping("/{userId}/followers")
    public Result<List<UserVO>> followers(@PathVariable Long userId) {
        return Result.ok(followService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public Result<List<UserVO>> following(@PathVariable Long userId) {
        return Result.ok(followService.getFollowing(userId));
    }

    @GetMapping("/{userId}/counts")
    public Result<Map<String, Long>> counts(@PathVariable Long userId) {
        return Result.ok(Map.of(
            "followers", followService.getFollowerCount(userId),
            "following", followService.getFollowingCount(userId)
        ));
    }
}
