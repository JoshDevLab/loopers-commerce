package com.loopers.domain.payment;

import com.loopers.interfaces.event.payment.DataPlatformService;

public interface DataPlatformGateway {
    void send(DataPlatformService.DataPlatformRequest request);
}
