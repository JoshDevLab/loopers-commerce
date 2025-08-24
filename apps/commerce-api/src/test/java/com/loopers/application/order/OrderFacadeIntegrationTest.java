package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.inventory.InventoryHistory;
import com.loopers.domain.inventory.InventoryRepository;
import com.loopers.domain.order.*;
import com.loopers.domain.point.*;
import com.loopers.domain.product.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.inventory.InventoryHistoryJpaRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.support.IntegrationTestSupport;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.util.ConcurrentTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

class OrderFacadeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    OrderFacade orderFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductOptionRepository productOptionRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    OrderJpaRepository orderRepository;

    @Autowired
    InventoryHistoryJpaRepository inventoryHistoryJpaRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @Transactional
    @DisplayName("유효한 주문을 생성할 수 있다.")
    @Test
    void order() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product1 = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        Product product2 = productRepository.save(Product.create("상품2", "설명2", BigDecimal.valueOf(30000), ProductCategory.CLOTHING, brand, "img"));

        ProductOption productOption1 = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product1));
        ProductOption productOption2 = productOptionRepository.save(ProductOption.create("옵션2", "M", "Blue", ProductStatus.ON_SALE, BigDecimal.valueOf(30000), product2));

        inventoryRepository.save(Inventory.create(productOption1, 10));
        inventoryRepository.save(Inventory.create(productOption2, 5));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption1.getId(), 2),
                new OrderCommand.OrderItemCommand(productOption2.getId(), 1)
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null,
                BigDecimal.valueOf(1000));

        // Act
        OrderInfo result = orderFacade.order(register, user.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(49000));

        Order savedOrder = orderRepository.findAll().getFirst(); // 테스트라면 1건만 저장되었을 것이므로

        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(savedOrder.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedOrder.getOrderItems()).hasSize(2);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedOrder.getUsedPointAmount()).isEqualByComparingTo("1000");

        assertThat(savedOrder.getShippingAddress()).isNotNull();
        assertThat(savedOrder.getShippingAddress().getZipcode()).isEqualTo("zipcode");
        assertThat(savedOrder.getShippingAddress().getRoadAddress()).isEqualTo("roadAddress");
        assertThat(savedOrder.getShippingAddress().getDetailAddress()).isEqualTo("detailAddress");

        OrderItem item1 = savedOrder.getOrderItems().get(0);
        OrderItem item2 = savedOrder.getOrderItems().get(1);

        assertThat(item1.getProductOption()).isNotNull();
        assertThat(item1.getQuantity()).isEqualTo(2);
        assertThat(item1.getOrderPrice()).isEqualByComparingTo(item1.getProductOption().getPrice());
        assertThat(item1.calculateTotalPrice()).isEqualByComparingTo(
                item1.getProductOption().getPrice().multiply(BigDecimal.valueOf(item1.getQuantity()))
        );

        assertThat(item2.getProductOption()).isNotNull();
        assertThat(item2.getQuantity()).isEqualTo(1);
        assertThat(item2.getOrderPrice()).isEqualByComparingTo(item2.getProductOption().getPrice());
        assertThat(item2.calculateTotalPrice()).isEqualByComparingTo(
                item2.getProductOption().getPrice().multiply(BigDecimal.valueOf(item2.getQuantity()))
        );

        // 🔄 이벤트 처리로 인한 재고 차감 검증 (이 부분이 이벤트 기반으로 처리됨)
        List<InventoryHistory> inventoryHistories = inventoryHistoryJpaRepository.findAll();
        assertThat(inventoryHistories)
                .extracting(InventoryHistory::getQuantityBefore, InventoryHistory::getQuantityAfter)
                .containsExactlyInAnyOrder(
                        tuple(10, 8),
                        tuple(5, 4)
                );

        // 🔄 실제 재고 수량도 감소했는지 검증 (이벤트 처리 결과)
        Inventory updatedInventory1 = inventoryRepository.findByProductOption(productOption1).orElseThrow();
        Inventory updatedInventory2 = inventoryRepository.findByProductOption(productOption2).orElseThrow();

        assertThat(updatedInventory1.getQuantity()).isEqualTo(8);  // 10 - 2 = 8
        assertThat(updatedInventory2.getQuantity()).isEqualTo(4);  // 5 - 1 = 4

        // 🔄 포인트 사용 내역 검증 (이벤트 처리 결과)
        List<PointHistory> pointHistories = pointHistoryRepository.findAll();
        assertThat(pointHistories)
                .hasSize(1)
                .extracting(PointHistory::getPoint, PointHistory::getType)
                .containsExactly(tuple(BigDecimal.valueOf(1000), PointHistoryType.USE));

        // 🔄 포인트 잔액 확인 (이벤트 처리 결과)
        Point updatedPoint = pointRepository.findByUserPk(user.getId()).orElseThrow();
        assertThat(updatedPoint.getPointBalance()).isEqualByComparingTo(BigDecimal.valueOf(199000)); // 200000 - 1000 = 199000
    }

    @DisplayName("정액 할인 쿠폰을 사용하여 주문을 생성할 수 있다.")
    @Test
    void orderWithFixedAmountCoupon() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 10));

        // 정액 할인 쿠폰 생성 (5000원 할인)
        Coupon coupon = couponRepository.save(Coupon.create("정액 할인 쿠폰", Coupon.CouponType.FIXED_AMOUNT, BigDecimal.valueOf(5000)));
        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(user, coupon, ZonedDateTime.now().plusDays(1)));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 2) // 20000원
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                userCoupon.getId(),
                BigDecimal.ZERO);

        // Act
        OrderInfo result = orderFacade.order(register, user.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(15000)); // 20000 - 5000
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));

        Order savedOrder = orderRepository.findAll().get(0);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(savedOrder.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

        // 쿠폰이 사용되었는지 확인
        UserCoupon usedCoupon = userCouponRepository.findByIdAndUserId(userCoupon.getId(), user.getId()).orElseThrow();
        assertThat(usedCoupon.isUsed()).isTrue();
    }

    @DisplayName("정률 할인 쿠폰을 사용하여 주문을 생성할 수 있다.")
    @Test
    void orderWithRateCoupon() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 10));

        // 정률 할인 쿠폰 생성 (20% 할인)
        Coupon coupon = couponRepository.save(Coupon.create("정률 할인 쿠폰", Coupon.CouponType.RATE, BigDecimal.valueOf(20)));
        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(user, coupon, ZonedDateTime.now().plusDays(1)));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 2) // 20000원
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                userCoupon.getId(),
                BigDecimal.ZERO);

        // Act
        OrderInfo result = orderFacade.order(register, user.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(16000)); // 20000 - 4000 (20% 할인)
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(4000));

        Order savedOrder = orderRepository.findAll().get(0);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(savedOrder.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(4000));
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

        // 쿠폰이 사용되었는지 확인
        UserCoupon usedCoupon = userCouponRepository.findByIdAndUserId(userCoupon.getId(), user.getId()).orElseThrow();
        assertThat(usedCoupon.isUsed()).isTrue();
    }

    @DisplayName("할인 금액이 주문 금액보다 클 경우 INVALID_COUPON 예외가 발생한다.")
    @Test
    void orderWithFixedAmountCouponExceedingTotalAmount() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(3000), product));
        inventoryRepository.save(Inventory.create(productOption, 10));

        // 정액 할인 쿠폰 생성 (5000원 할인, 주문 금액 3000원보다 큼)
        Coupon coupon = couponRepository.save(Coupon.create("정액 할인 쿠폰", Coupon.CouponType.FIXED_AMOUNT, BigDecimal.valueOf(5000)));
        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(user, coupon, ZonedDateTime.now().plusDays(1)));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 1) // 3000원
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                userCoupon.getId(),
                BigDecimal.ZERO);

        // Act & Assert
        assertThatThrownBy(() -> orderFacade.order(register, user.getId()))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_COUPON);
    }

    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다.")
    @Test
    void raceConditionCheckOrder() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 10));

        // 정률 할인 쿠폰 생성 (20% 할인)
        Coupon coupon = couponRepository.save(Coupon.create("정률 할인 쿠폰", Coupon.CouponType.RATE, BigDecimal.valueOf(20)));
        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(user, coupon, ZonedDateTime.now().plusDays(1)));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 2) // 20000원
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                userCoupon.getId(),
                BigDecimal.ZERO);

        // Act
        ConcurrentTestUtils.Result result = ConcurrentTestUtils.runConcurrent(2, () -> orderFacade.order(register, user.getId()));
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failedCount()).isEqualTo(1);
    }

    @DisplayName("동일한 유저가 여러 기기에서 동시에 주문해도, 포인트는 정상적으로 차감되어야 한다.")
    @Test
    void raceConditionPointShouldBeDeductedOnce() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200_000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 10));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 2) // 2 x 10000 = 20000
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null, // 쿠폰 없이 포인트만 사용하는 테스트
                BigDecimal.valueOf(4000)
        );

        // Act
        ConcurrentTestUtils.Result result = ConcurrentTestUtils.runConcurrent(2, () -> orderFacade.order(register, user.getId()));

        // Assert
        assertThat(result.successCount()).isEqualTo(2);

        Point point = pointRepository.findByUserPk(user.getId()).orElseThrow();
        assertThat(point.getPointBalance()).isEqualByComparingTo("192000");
    }

    @DisplayName("동일한 상품에 대해 여러 기기에서 동시에 주문해도, 재고는 정확히 차감되어야 한다.")
    @Test
    void raceConditionCheckInventory() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(100000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(
                ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product)
        );
        inventoryRepository.save(Inventory.create(productOption, 10));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 2)
        );
        OrderCommand.Register register = new OrderCommand.Register(
                itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null, // 쿠폰 없음
                BigDecimal.valueOf(1000)
        );

        // Act
        ConcurrentTestUtils.Result result = ConcurrentTestUtils.runConcurrent(10, () -> {
            orderFacade.order(register, user.getId());
        });

        // Assert
        assertThat(result.successCount()).isEqualTo(5);
        assertThat(result.failedCount()).isEqualTo(5);
        Inventory inventory = inventoryRepository.findByProductOption(productOption).orElseThrow();
        assertThat(inventory.getQuantity()).isEqualTo(0);
    }

    @DisplayName("존재하지 않는 쿠폰으로 주문하면 COUPON_NOT_FOUND 예외가 발생한다.")
    @Test
    void orderWithNonExistentCoupon() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 10));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 1)
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                999L,
                BigDecimal.ZERO); // 존재하지 않는 쿠폰 ID

        // Act & Assert
        assertThatThrownBy(() -> orderFacade.order(register, user.getId()))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.COUPON_NOT_FOUND);
    }

    @DisplayName("이미 사용한 쿠폰으로 주문하면 ALREADY_USING_COUPON 예외가 발생한다.")
    @Test
    void orderWithAlreadyUsedCoupon() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 10));

        Coupon coupon = couponRepository.save(Coupon.create("정액 할인 쿠폰", Coupon.CouponType.FIXED_AMOUNT, BigDecimal.valueOf(1000)));
        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(user, coupon, ZonedDateTime.now().plusDays(1)));
        userCoupon.use(); // 쿠폰 사용 처리
        userCouponRepository.save(userCoupon);

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 1)
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                userCoupon.getId(),
                BigDecimal.ZERO);

        // Act & Assert
        assertThatThrownBy(() -> orderFacade.order(register, user.getId()))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ALREADY_USING_COUPON);
    }

    @DisplayName("만료된 쿠폰으로 주문하면 EXPIRED_COUPON 예외가 발생한다.")
    @Test
    void orderWithExpiredCoupon() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 10));

        Coupon coupon = couponRepository.save(Coupon.create("정액 할인 쿠폰", Coupon.CouponType.FIXED_AMOUNT, BigDecimal.valueOf(1000)));
        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(user, coupon, ZonedDateTime.now().minusDays(1))); // 만료된 쿠폰

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 1)
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                userCoupon.getId(),
                BigDecimal.ZERO);

        // Act & Assert
        assertThatThrownBy(() -> orderFacade.order(register, user.getId()))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.EXPIRED_COUPON);
    }

    @DisplayName("재고가 없는 상품을 주문하면 INSUFFICIENT_STOCK 예외가 발생한다.")
    @Test
    void orderWithNoStock() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 0)); // 재고 0

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 1)
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null,
                BigDecimal.ZERO);

        // Act & Assert
        assertThatThrownBy(() -> orderFacade.order(register, user.getId()))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INSUFFICIENT_STOCK);
    }

    @DisplayName("재고보다 많은 수량을 주문하면 INSUFFICIENT_STOCK 예외가 발생한다.")
    @Test
    void orderWithInsufficientStock() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 5)); // 재고 5

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 10) // 10개 주문
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null,
                BigDecimal.ZERO);

        // Act & Assert
        assertThatThrownBy(() -> orderFacade.order(register, user.getId()))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INSUFFICIENT_STOCK);
    }

    @DisplayName("포인트가 부족하면 INSUFFICIENT_POINTS 예외가 발생한다.")
    @Test
    void orderWithInsufficientPoints() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(5000), user.getId())); // 5000 포인트 보유

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));
        inventoryRepository.save(Inventory.create(productOption, 10));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 1) // 주문 금액 10000
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null,
                BigDecimal.valueOf(10000));

        // Act & Assert
        assertThatThrownBy(() -> orderFacade.order(register, user.getId()))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INSUFFICIENT_POINT);
    }

    @DisplayName("주문 실패 시 쿠폰, 재고, 포인트가 롤백되어야 한다.")
    @Test
    void orderFailureShouldRollback() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        Point initialPoint = pointRepository.save(Point.create(BigDecimal.valueOf(10000), user.getId())); // 10000 포인트

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(20000), product));
        Inventory initialInventory = inventoryRepository.save(Inventory.create(productOption, 10)); // 재고 10

        Coupon coupon = couponRepository.save(Coupon.create("정액 할인 쿠폰", Coupon.CouponType.FIXED_AMOUNT, BigDecimal.valueOf(1000)));
        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(user, coupon, ZonedDateTime.now().plusDays(1)));

        // 주문 금액(20000) > 쿠폰 할인(1000) + 보유 포인트(10000) 이므로 실패해야 함
        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption.getId(), 1)
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands,
                new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                userCoupon.getId(),
                BigDecimal.valueOf(11000));

        // Act & Assert
        assertThatThrownBy(() -> orderFacade.order(register, user.getId()))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INSUFFICIENT_POINT);

        // Assert Rollback
        // 1. 주문이 생성되지 않았는지 확인
        assertThat(orderRepository.findAll()).isEmpty();

        // 2. 포인트가 차감되지 않았는지 확인
        Point pointAfter = pointRepository.findByUserPk(user.getId()).orElseThrow();
        assertThat(pointAfter.getPointBalance()).isEqualByComparingTo(initialPoint.getPointBalance());

        // 3. 재고가 감소하지 않았는지 확인
        Inventory inventoryAfter = inventoryRepository.findByProductOption(productOption).orElseThrow();
        assertThat(inventoryAfter.getQuantity()).isEqualTo(initialInventory.getQuantity());

        // 4. 쿠폰이 사용 처리되지 않았는지 확인
        UserCoupon couponAfter = userCouponRepository.findByIdAndUserId(userCoupon.getId(), user.getId()).orElseThrow();
        assertThat(couponAfter.isUsed()).isFalse();
    }
}

