package com.gxyy.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String address;
    private String avatar;
    private String avatarThumb;
    private String bio;
    private LocalDateTime createTime;
}
