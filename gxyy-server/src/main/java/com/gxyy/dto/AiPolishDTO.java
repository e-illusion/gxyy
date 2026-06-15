package com.gxyy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiPolishDTO {
    @NotBlank(message = "描述不能为空")
    private String description;
}
