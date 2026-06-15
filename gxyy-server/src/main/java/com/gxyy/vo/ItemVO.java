package com.gxyy.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemVO {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private String condition;
    private String description;
    private String wantDescription;
    private List<String> images;
    private List<String> thumbImages;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
