package com.loopers.application.userpoint;

import com.loopers.domain.point.PointHistoryService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRegisterCommand;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserPointFacadeTest {

    @Mock
    UserService userService;

    @Mock
    PointService pointService;

    @Mock
    PointHistoryService pointHistoryService;

    @InjectMocks
    UserPointFacade userPointFacade;

    @DisplayName("회원 가입 시 Point 초기화에 실패하면 User 도 삭제처리한다.")
    @Test
    void whenSignUpFailCreatePointThenDeleteUser() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        Gender gender = Gender.MALE;
        UserRegisterCommand userRegisterCommand = new UserRegisterCommand(
                userId, email, birthday, gender
        );

        User mockUser = User.create(userId, email, birthday, gender);
        when(userService.signUp(userRegisterCommand)).thenReturn(mockUser);

        doThrow(new RuntimeException()).when(pointService).initPoint(userId);

        // Act
        assertThrows(CoreException.class, () -> userPointFacade.signUp(userRegisterCommand));

        // Assert
        verify(userService).deleteUser(userId);
    }

    @DisplayName("포인트 충전 실패 시 충전내역을 삭제한다.")
    @Test
    void whenChargingFailDeletePointHistory() {
        // Arrange
        String userId = "test123";
        long chargePoint = 100L;
        long historyId = 1L;
        when(userService.existByUserId(userId)).thenReturn(true);

        when(pointHistoryService.chargePointHistory(eq(userId), eq(chargePoint), any()))
                .thenReturn(historyId);

        when(pointService.chargingPoint(userId, chargePoint))
                .thenThrow(new CoreException(ErrorType.POINT_CHARGING_ERROR, "포인트 충전 실패"));

        // Act
        assertThrows(CoreException.class, () ->
                userPointFacade.existMemberChargingPoint(userId, chargePoint)
        );

        // Assert
        verify(pointHistoryService).delete(eq(historyId));
    }

}
