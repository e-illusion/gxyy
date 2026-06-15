package com.gxyy.controller;

import com.gxyy.common.Result;
import com.gxyy.service.UserService;
import com.gxyy.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public Result<UserVO> updateProfile(HttpServletRequest request, @RequestBody UserVO vo) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.updateProfile(userId, vo));
    }

    @PostMapping("/me/avatar")
    public Result<String> uploadAvatar(HttpServletRequest request,
                                        @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.uploadAvatar(userId, file));
    }

    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.ok(userService.getProfileById(id));
    }
}
