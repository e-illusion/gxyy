package com.gxyy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gxyy.dto.ItemDTO;
import com.gxyy.entity.Item;
import com.gxyy.vo.ItemVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService extends IService<Item> {

    ItemVO createItem(Long userId, ItemDTO dto, List<MultipartFile> images);

    ItemVO updateItem(Long userId, Long itemId, ItemDTO dto);

    void deleteItem(Long userId, Long itemId);

    ItemVO getItemDetail(Long itemId);

    Page<ItemVO> getItemList(Integer page, Integer size, String keyword, Integer categoryId, Long ownerId, String condition, String sortOrder);

    List<ItemVO> getMyItems(Long userId);

    void offShelfItem(Long userId, Long itemId);
}
