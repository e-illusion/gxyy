package com.gxyy.controller;

import com.gxyy.common.Result;
import com.gxyy.dto.AiPolishDTO;
import com.gxyy.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/polish")
    public Result<Map<String, String>> polishDescription(@Valid @RequestBody AiPolishDTO dto) {
        String polished = aiService.polishDescription(dto.getDescription());
        return Result.ok(Map.of("polished", polished));
    }
}
