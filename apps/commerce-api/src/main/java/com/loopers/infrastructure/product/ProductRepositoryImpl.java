package com.loopers.infrastructure.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCategory;
import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.product.ProductRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.loopers.domain.brand.QBrand.brand;
import static com.loopers.domain.product.QProduct.product;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public Optional<Product> findWithBrandById(Long productId) {
        return productJpaRepository.findWithBrandById(productId);
    }

    @Override
    public List<Product> findByBrandId(Brand brand) {
        return productJpaRepository.findByBrand(brand);
    }

    @Override
    public Optional<Product> findByIdWithLock(Long productId) {
        return productJpaRepository.findByIdWithLock(productId);
    }

    @Override
    public Page<Product> findAllByCriteria(ProductCriteria criteria, Pageable pageable) {
        List<Product> result = queryFactory
                .select(product)
                .from(product)
                .join(product.brand, brand).fetchJoin()
                .where(
                        productBrandEq(criteria.brandId()),
                        productNameContains(criteria.keyword()),
                        productCategoryEq(criteria.category())
                )
                .orderBy(
                        productOrderBy(criteria.sort())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(product.count())
                .from(product)
                .join(product.brand, brand)
                .where(
                        productNameContains(criteria.keyword()),
                        productCategoryEq(criteria.category()),
                        productBrandEq(criteria.brandId())
                )
                .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(result, pageable, total);
    }

    private OrderSpecifier<?> productOrderBy(String sort) {
        if (sort == null || sort.isBlank()) {
            return product.createdAt.desc();
        }

        return switch (sort.toLowerCase()) {
            case "price_asc" -> product.basicPrice.asc();
            case "likes_desc" -> product.likeCount.desc();
            default -> product.createdAt.desc();
        };
    }

    private static BooleanExpression productBrandEq(Long brandId) {
        return brandId == null ? null : product.brand.id.eq(brandId);
    }

    private static BooleanExpression productCategoryEq(ProductCategory category) {
        return category == null ? null : product.productCategory.eq(category);
    }

    private static BooleanExpression productNameContains(String keyword) {
        return keyword == null || keyword.isBlank() ? null : product.name.containsIgnoreCase(keyword);
    }

}
