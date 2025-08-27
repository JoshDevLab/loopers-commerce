package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserActivityEvent;
import com.loopers.interfaces.event.user.UserActivityEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserActivityPublisherImpl implements UserActivityEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(UserActivityEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
