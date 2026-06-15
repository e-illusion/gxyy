package com.gxyy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxyy.common.BusinessException;
import com.gxyy.common.JwtUtils;
import com.gxyy.dto.LoginDTO;
import com.gxyy.dto.RegisterDTO;
import com.gxyy.entity.User;
import com.gxyy.mapper.UserMapper;
import com.gxyy.service.UserService;
import com.gxyy.vo.LoginVO;
import com.gxyy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${gxyy.upload.path}")
    private String uploadPath;

    @Override
    public UserVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已被注册");
        }

        // 检查手机号是否已存在
        if (StrUtil.isNotBlank(dto.getPhone())) {
            count = baseMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
            if (count > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        baseMapper.insert(user);

        return fillThumb(BeanUtil.copyProperties(user, UserVO.class));
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = baseMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        UserVO userVO = fillThumb(BeanUtil.copyProperties(user, UserVO.class));

        return LoginVO.builder().token(token).user(userVO).build();
    }

    @Override
    public UserVO getProfile(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return fillThumb(BeanUtil.copyProperties(user, UserVO.class));
    }

    private UserVO fillThumb(UserVO vo) {
        if (vo.getAvatar() != null) {
            String thumbUrl = vo.getAvatar().replaceAll("(\\.[^.]+)$", "_thumb$1");
            String thumbPath = uploadPath + thumbUrl.substring("/uploads/".length());
            vo.setAvatarThumb(new java.io.File(thumbPath).exists() ? thumbUrl : vo.getAvatar());
        }
        return vo;
    }

    @Override
    public UserVO getProfileById(Long userId) {
        return getProfile(userId);
    }

    @Override
    public UserVO updateProfile(Long userId, UserVO vo) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (StrUtil.isNotBlank(vo.getBio())) {
            user.setBio(vo.getBio());
        }
        if (StrUtil.isNotBlank(vo.getPhone())) {
            user.setPhone(vo.getPhone());
        }
        if (StrUtil.isNotBlank(vo.getEmail())) {
            user.setEmail(vo.getEmail());
        }
        if (vo.getAddress() != null) {
            user.setAddress(vo.getAddress());
        }
        baseMapper.updateById(user);

        return fillThumb(BeanUtil.copyProperties(user, UserVO.class));
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String filename = "avatar_" + uuid + ext;

        // 保存文件
        File dest = new File(uploadPath + filename);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
            // 生成缩略图
            generateThumb(dest, uploadPath + "avatar_" + uuid + "_thumb.jpg", 150);
        } catch (IOException e) {
            throw new BusinessException("头像上传失败");
        }

        // 更新用户头像
        String avatarUrl = "/uploads/" + filename;
        User user = baseMapper.selectById(userId);
        user.setAvatar(avatarUrl);
        baseMapper.updateById(user);

        return avatarUrl;
    }

    private void generateThumb(File source, String thumbPath, int maxWidth) {
        try {
            java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(source);
            if (original == null) return;
            int w = original.getWidth();
            int h = original.getHeight();
            if (w <= maxWidth) {
                org.springframework.util.FileCopyUtils.copy(source, new java.io.File(thumbPath));
                return;
            }
            int newH = (int) ((double) h / w * maxWidth);
            java.awt.Image scaled = original.getScaledInstance(maxWidth, newH, java.awt.Image.SCALE_SMOOTH);
            java.awt.image.BufferedImage thumb = new java.awt.image.BufferedImage(maxWidth, newH, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = thumb.createGraphics();
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();
            javax.imageio.ImageIO.write(thumb, "jpg", new java.io.File(thumbPath));
        } catch (Exception e) {
            log.warn("缩略图生成失败: {}", e.getMessage());
        }
    }
}
