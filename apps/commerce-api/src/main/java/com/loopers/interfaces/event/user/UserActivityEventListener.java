package com.loopers.interfaces.event.user;

import com.loopers.domain.user.UserActivityCommand;
import com.loopers.domain.user.UserActivityEvent;
import com.loopers.domain.user.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserActivityEventListener {
    private final UserActivityService userActivityService;

    @Async
    @EventListener
    public void onUserActivityEvent(UserActivityEvent e) {
        log.info("API_ACCESS traceId={} userId={} method={} uri={} route={} status={}",
                e.traceId(), e.userId(), e.method(), e.uri(), e.route(), e.status());
        userActivityService.save(UserActivityCommand.create(e.traceId(), e.userId(), e.method(), e.uri(), e.route(), e.status()));
    }
}
