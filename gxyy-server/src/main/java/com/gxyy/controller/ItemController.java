package com.gxyy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxyy.common.Result;
import com.gxyy.dto.ItemDTO;
import com.gxyy.service.ItemService;
import com.gxyy.vo.ItemVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public Result<ItemVO> create(HttpServletRequest request,
                                  @Valid @ModelAttribute ItemDTO dto,
                                  @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(itemService.createItem(userId, dto, images));
    }

    @PutMapping("/{id}")
    public Result<ItemVO> update(HttpServletRequest request,
                                  @PathVariable Long id,
                                  @Valid @RequestBody ItemDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(itemService.updateItem(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        itemService.deleteItem(userId, id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<ItemVO> detail(@PathVariable Long id) {
        return Result.ok(itemService.getItemDetail(id));
    }

    @GetMapping
    public Result<Page<ItemVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer categoryId, @RequestParam(required = false) Long ownerId, @RequestParam(required = false) String condition, @RequestParam(required = false) String sortOrder) {
        return Result.ok(itemService.getItemList(page, size, keyword, categoryId, ownerId, condition, sortOrder));
    }

    @GetMapping("/my")
    public Result<List<ItemVO>> myItems(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(itemService.getMyItems(userId));
    }

    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        itemService.offShelfItem(userId, id);
        return Result.ok();
    }
}
