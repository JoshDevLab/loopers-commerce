package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCriteria;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.QOrder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.loopers.domain.brand.QBrand.brand;
import static com.loopers.domain.order.QOrder.order;
import static com.loopers.domain.product.QProduct.product;

@RequiredArgsConstructor
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Page<Order> findAllByCriteriaAndUserPk(OrderCriteria criteria, Long userPk, Pageable pageable) {
        List<Order> result = queryFactory
                .select(order)
                .from(order)
                .where(order.user.id.eq(userPk))
                .orderBy(
                        ordersOrderBy(criteria)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(order.count())
                .from(order)
                .where(
                        order.user.id.eq(userPk)
                )
                .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(result, pageable, total);
    }

    private OrderSpecifier<?> ordersOrderBy(OrderCriteria criteria) {
        return criteria.getSort().equals("createdAt_desc") ? order.createdAt.desc() : order.createdAt.asc();
    }
}
