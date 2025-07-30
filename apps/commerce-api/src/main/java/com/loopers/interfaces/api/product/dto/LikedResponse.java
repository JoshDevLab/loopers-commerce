package com.loopers.interfaces.api.product.dto;

import com.loopers.application.product.ProductLikedInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LikedResponse {
    boolean liked;
    int likeCount;

    public static LikedResponse from(ProductLikedInfo productLikedInfo) {
        return new LikedResponse(productLikedInfo.isLiked(), productLikedInfo.getLikeCount());
    }
}
