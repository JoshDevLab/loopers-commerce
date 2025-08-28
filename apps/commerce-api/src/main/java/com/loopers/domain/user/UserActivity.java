package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_activity")
public class UserActivity extends BaseEntity {
    private String traceId;
    private String userId;
    private String method;
    private String uri;
    private String route;
    private int status;

    public static UserActivity create(UserActivityCommand command) {
        UserActivity userActivity = new UserActivity();
        userActivity.traceId = command.traceId();
        userActivity.userId = command.userId();
        userActivity.method = command.method();
        userActivity.uri = command.uri();
        userActivity.route = command.route();
        userActivity.status = command.status();
        return userActivity;
    }
}
