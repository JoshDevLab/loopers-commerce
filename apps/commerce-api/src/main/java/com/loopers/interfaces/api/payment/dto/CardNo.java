package com.loopers.interfaces.api.payment.dto;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardNo {

    private static final Pattern PATTERN = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$");

    private String value;

    private CardNo(String value) {
        this.value = value;
    }

    public static CardNo valueOfName(String cardNo) {
        if (cardNo == null || !PATTERN.matcher(cardNo).matches()) {
            throw new IllegalArgumentException("잘못된 카드번호 형식입니다. 형식: xxxx-xxxx-xxxx-xxxx");
        }
        return new CardNo(cardNo);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardNo cardNo)) return false;
        return Objects.equals(value, cardNo.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
