package com.gxyy.service;

import com.gxyy.vo.MatchResult;

import java.util.List;

public interface AiService {

    String polishDescription(String description);

    /** AI 推荐交换匹配，返回最匹配的3个物品ID和理由 */
    List<MatchResult> matchItems(Long itemId, Long userId);
}
