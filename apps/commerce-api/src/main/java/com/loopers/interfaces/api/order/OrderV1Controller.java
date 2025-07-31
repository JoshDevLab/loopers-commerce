package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.domain.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.order.dto.OrderRequest;
import com.loopers.interfaces.api.order.dto.OrderResponse;
import com.loopers.interfaces.interceptor.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller {

    private final OrderFacade orderFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> order(@RequestBody OrderRequest orderRequest, @CurrentUser UserInfo userInfo) {
        OrderInfo orderInfo = orderFacade.order(orderRequest.toRegisterCommand(), userInfo.id());
        return ApiResponse.success(OrderResponse.of(orderInfo));
    }
}
