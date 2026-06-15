package com.gxyy.controller;

import com.gxyy.common.Result;
import com.gxyy.dto.LoginDTO;
import com.gxyy.dto.RegisterDTO;
import com.gxyy.service.UserService;
import com.gxyy.vo.LoginVO;
import com.gxyy.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(userService.register(dto));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }
}
