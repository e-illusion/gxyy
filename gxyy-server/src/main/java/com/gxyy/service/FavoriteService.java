package com.gxyy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxyy.entity.Favorite;
import com.gxyy.vo.ItemVO;

import java.util.List;

public interface FavoriteService extends IService<Favorite> {

    /** 添加收藏 */
    void addFavorite(Long userId, Long itemId);

    /** 取消收藏 */
    void removeFavorite(Long userId, Long itemId);

    /** 是否已收藏 */
    boolean isFavorited(Long userId, Long itemId);

    /** 我的收藏列表 */
    List<ItemVO> getMyFavorites(Long userId);

    /** 物品被收藏数 */
    Long getFavoriteCount(Long itemId);
}
