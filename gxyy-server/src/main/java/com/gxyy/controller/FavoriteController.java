package com.gxyy.controller;

import com.gxyy.common.Result;
import com.gxyy.service.FavoriteService;
import com.gxyy.vo.ItemVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 添加收藏 */
    @PostMapping("/{itemId}")
    public Result<Map<String, Object>> add(HttpServletRequest request,
                                            @PathVariable Long itemId) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.addFavorite(userId, itemId);
        Long count = favoriteService.getFavoriteCount(itemId);
        return Result.ok(Map.of("favoriteCount", count));
    }

    /** 取消收藏 */
    @DeleteMapping("/{itemId}")
    public Result<Map<String, Object>> remove(HttpServletRequest request,
                                               @PathVariable Long itemId) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.removeFavorite(userId, itemId);
        Long count = favoriteService.getFavoriteCount(itemId);
        return Result.ok(Map.of("favoriteCount", count));
    }

    /** 检查是否已收藏 */
    @GetMapping("/{itemId}/check")
    public Result<Map<String, Object>> check(HttpServletRequest request,
                                              @PathVariable Long itemId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean favorited = favoriteService.isFavorited(userId, itemId);
        return Result.ok(Map.of("isFavorited", favorited));
    }

    /** 我的收藏列表 */
    @GetMapping
    public Result<List<ItemVO>> myFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(favoriteService.getMyFavorites(userId));
    }
}
