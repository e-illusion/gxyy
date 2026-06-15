package com.gxyy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeRequestDTO {
    @NotNull(message = "目标物品不能为空")
    private Long toItemId;

    @NotNull(message = "你的物品不能为空")
    private Long fromItemId;

    private String message;
}
