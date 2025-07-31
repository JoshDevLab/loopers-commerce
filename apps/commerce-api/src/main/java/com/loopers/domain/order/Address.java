package com.loopers.domain.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
public class Address {

    private String zipcode;
    private String roadAddress;
    private String detailAddress;
    private String receiverName;
    private String receiverPhone;

}
