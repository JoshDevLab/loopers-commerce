package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "birth_day", nullable = false)
    private LocalDate birthDay;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private User(String userId, String email, LocalDate birthDay, Gender gender) {
        this.userId = userId;
        this.email = email;
        this.birthDay = birthDay;
        this.gender = gender;
    }

    public static User create(String userId, String email, String birthday, String gender) {
        validate(userId, email, birthday);
        return new User(userId, email, LocalDate.parse(birthday), Gender.validate(gender));
    }

    private static void validate(String userId, String email, String birthday) {
        validateUserId(userId);
        validateEmail(email);
        validateBirthday(birthday);
    }

    private static void validateUserId(String userId) {
        String USERID_REGEX = "^[a-zA-Z0-9]{1,10}$";
        if (!Pattern.matches(USERID_REGEX, userId)) {
            throw new CoreException(ErrorType.USER_ID_ERROR, "Invalid userId: " + userId);
        }
    }

    private static void validateEmail(String email) {
        String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            throw new CoreException(ErrorType.USER_EMAIL_ERROR, "Invalid email: " + email);
        }
    }

    private static void validateBirthday(String birthday) {
        LocalDate parsedDate = parseDateOrThrow(birthday);
        if (parsedDate.isAfter(LocalDate.now())) {
            throw new CoreException(ErrorType.USER_BIRTHDAY_ERROR, "생년월일은 현재보다 미래일 수 없습니다.");
        }
    }

    private static LocalDate parseDateOrThrow(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new CoreException(ErrorType.USER_BIRTHDAY_ERROR, "Invalid birthday: " + dateStr);
        }
    }

}
