package com.gxyy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemDTO {
    @NotBlank(message = "物品名称不能为空")
    private String title;

    @NotNull(message = "分类不能为空")
    private Integer categoryId;

    @NotBlank(message = "成色不能为空")
    private String condition;

    @NotBlank(message = "描述不能为空")
    private String description;

    private String wantDescription;
}
