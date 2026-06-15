package com.gxyy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxyy.dto.ExchangeRequestDTO;
import com.gxyy.entity.ExchangeRequest;
import com.gxyy.vo.ExchangeRequestVO;

import java.util.List;

public interface ExchangeRequestService extends IService<ExchangeRequest> {

    ExchangeRequestVO createRequest(Long fromUserId, ExchangeRequestDTO dto);

    ExchangeRequestVO acceptRequest(Long userId, Long requestId);

    ExchangeRequestVO rejectRequest(Long userId, Long requestId);

    void cancelRequest(Long userId, Long requestId);

    List<ExchangeRequestVO> getMyRequests(Long userId);
}
