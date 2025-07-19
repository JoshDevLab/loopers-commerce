package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserTest {

    @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, CoreException USERID_ERROR 에러타입 이 발생한다.")
    @ParameterizedTest
    @ValueSource(
            strings = {
                    "toolonguserid1",    // 10자 초과
                    "invalid!@#",        // 특수문자 포함
                    "한글아이디",         // 한글 포함
                    "space id",          // 공백 포함
                    "",                  // 빈 문자열
            }
    )
    void idValidate(String userId) {
        // Arrange
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";

        // Act
        // Assert
        assertThatThrownBy(() -> User.create(userId, email, birthday, gender))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException)e).getErrorType())
                .isEqualTo(ErrorType.USER_ID_ERROR);
    }


    @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, CoreException USER_EMAIL_ERROR 이 발생한다.")
    @ParameterizedTest
    @ValueSource(
            strings = {
                    "plainaddress",         // '@' 없음
                    "missingatsign.com",    // '@' 없음
                    "user@.com",            // 도메인 이름 없음
                    "user@domain",          // TLD 없음 (예: '.com')
                    "@no-local-part.com"    // 로컬 파트 없음
            }
    )
    void emailValidate(String email) {
        // Arrange
        String userId = "userid12";
        String birthday = "1996-11-27";
        String gender = "MALE";

        // Act
        // Assert
        assertThatThrownBy(() -> User.create(userId, email, birthday, gender))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException)e).getErrorType())
                .isEqualTo(ErrorType.USER_EMAIL_ERROR);
    }

    @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, CoreException USER_BIRTHDAY_ERROR 에러타입이 발생한다.")
    @ParameterizedTest
    @ValueSource(
            strings = {
                    "19950619",      // 하이픈 없음
                    "95-06-19",      // 연도 4자리가 아님
                    "1995-6-9",      // 월/일이 2자리가 아님
                    "1995-13-01",    // 13월 없음
                    "1995-02-30"     // 2월에 30일 없음 (정규식은 통과, 실제 파싱 시 실패)
            }
    )
    void brithDayValidate(String birthday) {
        // Arrange
        String userId = "userid12";
        String email = "email@email.com";
        String gender = "MALE";


        // Act
        // Assert
        assertThatThrownBy(() -> User.create(userId, email, birthday, gender))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException)e).getErrorType())
                .isEqualTo(ErrorType.USER_BIRTHDAY_ERROR);
    }

}
