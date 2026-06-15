package com.gxyy.controller;

import com.gxyy.common.Result;
import com.gxyy.dto.ExchangeRequestDTO;
import com.gxyy.service.ExchangeRequestService;
import com.gxyy.vo.ExchangeRequestVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-requests")
@RequiredArgsConstructor
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;

    @PostMapping
    public Result<ExchangeRequestVO> create(HttpServletRequest request,
                                             @Valid @RequestBody ExchangeRequestDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(exchangeRequestService.createRequest(userId, dto));
    }

    @PutMapping("/{id}/accept")
    public Result<ExchangeRequestVO> accept(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(exchangeRequestService.acceptRequest(userId, id));
    }

    @PutMapping("/{id}/reject")
    public Result<ExchangeRequestVO> reject(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(exchangeRequestService.rejectRequest(userId, id));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        exchangeRequestService.cancelRequest(userId, id);
        return Result.ok();
    }

    @GetMapping
    public Result<List<ExchangeRequestVO>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(exchangeRequestService.getMyRequests(userId));
    }
}
