package com.loopers.interfaces.event.user;

import com.loopers.domain.user.UserActivityEvent;

public interface UserActivityEventPublisher {
    void publish(UserActivityEvent event);
}
