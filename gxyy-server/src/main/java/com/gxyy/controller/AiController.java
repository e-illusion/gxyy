package com.gxyy.controller;

import com.gxyy.common.Result;
import com.gxyy.dto.AiPolishDTO;
import com.gxyy.service.AiService;
import com.gxyy.service.ItemService;
import com.gxyy.vo.ItemVO;
import com.gxyy.vo.MatchResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final ItemService itemService;

    @PostMapping("/polish")
    public Result<Map<String, String>> polishDescription(@Valid @RequestBody AiPolishDTO dto) {
        String polished = aiService.polishDescription(dto.getDescription());
        return Result.ok(Map.of("polished", polished));
    }

    /** AI 推荐交换匹配 */
    @PostMapping("/match/{itemId}")
    public Result<List<Map<String, Object>>> matchItems(HttpServletRequest request,
                                                         @PathVariable Long itemId) {
        Long userId = (Long) request.getAttribute("userId");
        List<MatchResult> matches = aiService.matchItems(itemId, userId);
        List<Map<String, Object>> result = matches.stream().map(m -> {
            ItemVO vo = itemService.getItemDetail(m.getItemId());
            Map<String, Object> map = new HashMap<>();
            map.put("item", vo);
            map.put("reason", m.getReason());
            return map;
        }).toList();
        return Result.ok(result);
    }
}
