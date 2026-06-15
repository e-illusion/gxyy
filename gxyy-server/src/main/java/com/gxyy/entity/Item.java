package com.gxyy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("item")
public class Item {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerId;
    private Integer categoryId;

    private String title;

    @TableField("`condition`")
    private String condition;  // NEW, LIKE_NEW, SLIGHTLY_USED, NORMAL_USE

    private String description;
    private String wantDescription;
    private String images;  // JSON 数组字符串

    private String status;  // ACTIVE, EXCHANGED, OFF_SHELF

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
