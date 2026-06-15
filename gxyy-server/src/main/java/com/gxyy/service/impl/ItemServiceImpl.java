package com.gxyy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxyy.common.BusinessException;
import com.gxyy.dto.ItemDTO;
import com.gxyy.entity.Category;
import com.gxyy.entity.Item;
import com.gxyy.entity.User;
import com.gxyy.mapper.CategoryMapper;
import com.gxyy.mapper.ItemMapper;
import com.gxyy.mapper.UserMapper;
import com.gxyy.service.ItemService;
import com.gxyy.vo.ItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Value("${gxyy.upload.path}")
    private String uploadPath;

    @Override
    public ItemVO createItem(Long userId, ItemDTO dto, List<MultipartFile> images) {
        Item item = new Item();
        item.setOwnerId(userId);
        item.setCategoryId(dto.getCategoryId());
        item.setTitle(dto.getTitle());
        item.setCondition(dto.getCondition());
        item.setDescription(dto.getDescription());
        item.setWantDescription(dto.getWantDescription());
        item.setStatus("ACTIVE");

        // 处理图片上传
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String url = saveImage(file);
                    imageUrls.add(url);
                }
            }
            item.setImages(JSONUtil.toJsonStr(imageUrls));
        } else {
            item.setImages("[]");
        }

        baseMapper.insert(item);
        return buildItemVO(item);
    }

    @Override
    public ItemVO updateItem(Long userId, Long itemId, ItemDTO dto) {
        Item item = baseMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物品不存在");
        }
        if (!item.getOwnerId().equals(userId)) {
            throw new BusinessException(403, "只能编辑自己的物品");
        }

        item.setCategoryId(dto.getCategoryId());
        item.setTitle(dto.getTitle());
        item.setCondition(dto.getCondition());
        item.setDescription(dto.getDescription());
        item.setWantDescription(dto.getWantDescription());
        baseMapper.updateById(item);

        return buildItemVO(item);
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        Item item = baseMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物品不存在");
        }
        if (!item.getOwnerId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的物品");
        }
        baseMapper.deleteById(itemId);
    }

    @Override
    public ItemVO getItemDetail(Long itemId) {
        Item item = baseMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物品不存在");
        }
        return buildItemVO(item);
    }

    @Override
    public Page<ItemVO> getItemList(Integer page, Integer size, String keyword, Integer categoryId) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "ACTIVE")
                .orderByDesc(Item::getCreateTime);

        if (categoryId != null) {
            wrapper.eq(Item::getCategoryId, categoryId);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Item::getTitle, keyword)
                    .or().like(Item::getDescription, keyword));
        }

        Page<Item> itemPage = baseMapper.selectPage(new Page<>(page, size), wrapper);

        // 转换为 ItemVO 列表
        List<ItemVO> voList = itemPage.getRecords().stream()
                .map(this::buildItemVO)
                .toList();

        Page<ItemVO> voPage = new Page<>(page, size, itemPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<ItemVO> getMyItems(Long userId) {
        List<Item> items = baseMapper.selectList(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getOwnerId, userId)
                        .orderByDesc(Item::getCreateTime));
        return items.stream().map(this::buildItemVO).toList();
    }

    @Override
    public void offShelfItem(Long userId, Long itemId) {
        Item item = baseMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物品不存在");
        }
        if (!item.getOwnerId().equals(userId)) {
            throw new BusinessException(403, "只能操作自己的物品");
        }
        item.setStatus("OFF_SHELF");
        baseMapper.updateById(item);
    }

    /**
     * 构建 ItemVO（含用户和分类信息）
     */
    private ItemVO buildItemVO(Item item) {
        ItemVO vo = BeanUtil.copyProperties(item, ItemVO.class);

        // 解析图片 JSON 数组
        if (StrUtil.isNotBlank(item.getImages())) {
            List<String> images = JSONUtil.toList(item.getImages(), String.class);
            vo.setImages(images);
            // 生成缩略图 URL 列表（若文件不存在则回退到原图）
            vo.setThumbImages(images.stream()
                    .map(url -> {
                        String thumbUrl = url.replaceAll("(\\.[^.]+)$", "_thumb$1");
                        String thumbPath = uploadPath + thumbUrl.substring("/uploads/".length());
                        return new java.io.File(thumbPath).exists() ? thumbUrl : url;
                    })
                    .toList());
        }

        // 填充用户信息
        User owner = userMapper.selectById(item.getOwnerId());
        if (owner != null) {
            vo.setOwnerName(owner.getUsername());
            vo.setOwnerAvatar(owner.getAvatar());
        }

        // 填充分类信息
        Category category = categoryMapper.selectById(item.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        return vo;
    }

    /**
     * 保存单张图片到本地，同时生成缩略图
     */
    private String saveImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String filename = "item_" + uuid + ext;

        File dest = new File(uploadPath + filename);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
            // 生成缩略图
            generateThumb(dest, uploadPath + "item_" + uuid + "_thumb.jpg", 300);
        } catch (IOException e) {
            throw new BusinessException("图片上传失败");
        }
        return "/uploads/" + filename;
    }

    /**
     * 生成缩略图（使用 javax.imageio，无需额外依赖）
     */
    private void generateThumb(File source, String thumbPath, int maxWidth) {
        try {
            java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(source);
            if (original == null) return;
            int w = original.getWidth();
            int h = original.getHeight();
            if (w <= maxWidth) {
                // 原图已经很小，直接复制
                org.springframework.util.FileCopyUtils.copy(source, new java.io.File(thumbPath));
                return;
            }
            int newH = (int) ((double) h / w * maxWidth);
            java.awt.Image scaled = original.getScaledInstance(maxWidth, newH, java.awt.Image.SCALE_SMOOTH);
            java.awt.image.BufferedImage thumb = new java.awt.image.BufferedImage(maxWidth, newH, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = thumb.createGraphics();
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();
            javax.imageio.ImageIO.write(thumb, "jpg", new java.io.File(thumbPath));
        } catch (Exception e) {
            // 缩略图生成失败不影响主流程
            log.warn("缩略图生成失败: {}", e.getMessage());
        }
    }
}
