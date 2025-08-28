package com.loopers.domain.product.like;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(
        name = "product_likes",
        uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = {"product_id", "user_id"})
)
public class ProductLike extends BaseEntity {
    private Long productId;
    private Long userPk;

    public static ProductLike create(Long productId, Long userPk) {
        ProductLike productLike = new ProductLike();
        productLike.productId = productId;
        productLike.userPk = userPk;
        return productLike;
    }
}
