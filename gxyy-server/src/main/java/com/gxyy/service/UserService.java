package com.gxyy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxyy.dto.LoginDTO;
import com.gxyy.dto.RegisterDTO;
import com.gxyy.entity.User;
import com.gxyy.vo.LoginVO;
import com.gxyy.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {

    UserVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserVO getProfile(Long userId);

    UserVO getProfileById(Long userId);

    UserVO updateProfile(Long userId, UserVO vo);

    String uploadAvatar(Long userId, MultipartFile file);
}
