package com.gxyy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxyy.common.BusinessException;
import com.gxyy.entity.Favorite;
import com.gxyy.entity.Item;
import com.gxyy.entity.User;
import com.gxyy.entity.Category;
import com.gxyy.mapper.FavoriteMapper;
import com.gxyy.mapper.ItemMapper;
import com.gxyy.mapper.UserMapper;
import com.gxyy.mapper.CategoryMapper;
import com.gxyy.service.FavoriteService;
import com.gxyy.vo.ItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
        implements FavoriteService {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long itemId) {
        // 检查物品是否存在
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物品不存在");
        }
        // 检查是否已收藏
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId)
        );
        if (count > 0) {
            throw new BusinessException("已收藏过该物品");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setItemId(itemId);
        baseMapper.insert(fav);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long itemId) {
        baseMapper.delete(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId)
        );
    }

    @Override
    public boolean isFavorited(Long userId, Long itemId) {
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId)
        );
        return count > 0;
    }

    @Override
    public List<ItemVO> getMyFavorites(Long userId) {
        List<Favorite> favs = baseMapper.selectList(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime)
        );

        return favs.stream().map(fav -> {
            Item item = itemMapper.selectById(fav.getItemId());
            if (item == null) return null;
            return buildItemVO(item);
        }).filter(vo -> vo != null).toList();
    }

    @Override
    public Long getFavoriteCount(Long itemId) {
        return baseMapper.selectCount(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getItemId, itemId)
        );
    }

    private ItemVO buildItemVO(Item item) {
        ItemVO vo = BeanUtil.copyProperties(item, ItemVO.class);
        User owner = userMapper.selectById(item.getOwnerId());
        if (owner != null) {
            vo.setOwnerName(owner.getUsername());
            vo.setOwnerAvatar(owner.getAvatar());
        }
        Category cat = categoryMapper.selectById(item.getCategoryId());
        if (cat != null) {
            vo.setCategoryName(cat.getName());
        }
        return vo;
    }
}
