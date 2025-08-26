package com.loopers.domain.product.like;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_like_stat")
public class ProductLikeStat extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long productId;
    private Long likeCount;

    public static ProductLikeStat createLike(Long productId) {
        ProductLikeStat productLikeStat = new ProductLikeStat();
        productLikeStat.productId = productId;
        productLikeStat.likeCount = 1L;
        return productLikeStat;
    }

    public static ProductLikeStat create(Long productId) {
        ProductLikeStat productLikeStat = new ProductLikeStat();
        productLikeStat.productId = productId;
        productLikeStat.likeCount = 0L;
        return productLikeStat;
    }

    public void like() {
        this.likeCount++;
    }

    public void unLike() {
        if (this.likeCount < 0) {
            this.likeCount--;
        }
    }
}
