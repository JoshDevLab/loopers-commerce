package com.loopers.application.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductLikedInfo {
    private boolean liked;
    private int likeCount;
}
