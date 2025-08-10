package com.loopers.interfaces.api.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.inventory.InventoryRepository;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.order.dto.OrderItemResponse;
import com.loopers.interfaces.api.order.dto.OrderRequest;
import com.loopers.interfaces.api.order.dto.OrderResponse;
import com.loopers.support.E2ETestSupport;
import com.loopers.support.fixture.brand.BrandFixture;
import com.loopers.support.fixture.product.ProductFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class OrderV1ApiE2ETest extends E2ETestSupport {

    private static final String BASE_URL = "/api/v1/orders";

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductOptionRepository productOptionRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    OrderRepository orderRepository;

    @BeforeEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 수량이 재고보다 많으면 409 Conflict 응답을 반환한다.")
    @Test
    void createOrder_withInsufficientInventory_shouldReturnConflict() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));

        pointRepository.save(Point.create(BigDecimal.valueOf(10000000), user.getId()));

        Brand brand = brandRepository.save(BrandFixture.createBrand("Brand", "Brand description", "https://image.com"));
        Product product = productRepository.save(ProductFixture.createProduct(
                "testProduct",
                "Description for product",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand,
                "https://example.com/image.jpg"
        ));

        ProductOption productOption = productOptionRepository.save(ProductOption.create(
                "optionName", "M", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product
        ));

        inventoryRepository.save(Inventory.create(productOption, 2)); // 재고: 2개

        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderRequest.OrderItemRequest(productOption.getId(), 5)), // 주문 수량: 5개 → 초과
                new OrderRequest.AddressRequest("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null,
                BigDecimal.ZERO
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<OrderRequest> httpEntity = new HttpEntity<>(orderRequest, headers);

        // Act
        var response = client.exchange(
                BASE_URL,
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<ApiResponse<Object>>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @DisplayName("사용자 포인트가 부족하면 400 BAD_REQUEST 응답을 반환한다.")
    @Test
    void createOrder_withInsufficientPoint_shouldReturnConflict() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));

        // 포인트: 5,000원만 보유 (부족)
        pointRepository.save(Point.create(BigDecimal.valueOf(5000), user.getId()));

        Brand brand = brandRepository.save(BrandFixture.createBrand("Brand", "Brand description", "https://image.com"));
        Product product = productRepository.save(ProductFixture.createProduct(
                "testProduct",
                "Description for product",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand,
                "https://example.com/image.jpg"
        ));

        ProductOption productOption = productOptionRepository.save(ProductOption.create(
                "optionName", "M", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product
        ));

        inventoryRepository.save(Inventory.create(productOption, 10)); // 재고는 충분

        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderRequest.OrderItemRequest(productOption.getId(), 1)), // 주문 금액: 10,000
                new OrderRequest.AddressRequest("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null,
                BigDecimal.valueOf(10000)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<OrderRequest> httpEntity = new HttpEntity<>(orderRequest, headers);

        // Act
        var response = client.exchange(
                BASE_URL,
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<ApiResponse<Object>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("로그인 한 사용자가 주문을 할 수 있고 주문 생성시 201 Created 응답을 반환한다.")
    @Test
    void createOrder() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));

        pointRepository.save(Point.create(BigDecimal.valueOf(10000000), user.getId()));

        Brand brand1 = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1", "https://image1.com"));
        Brand brand2 = brandRepository.save(BrandFixture.createBrand("Brand2", "Brand description 2", "https://image2.com"));
        Product testProduct1 = productRepository.save(ProductFixture.createProduct(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand1,
                "https://example.com/image1.jpg"
        ));
        Product testProduct2 = productRepository.save(ProductFixture.createProduct(
                "testProduct2",
                "Description for product 2",
                BigDecimal.valueOf(50000),
                ProductCategory.CLOTHING,
                brand2,
                "https://example.com/image2.jpg"
        ));

        ProductOption productOption1 = productOptionRepository.save(ProductOption.create("name1","L", "Blue", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), testProduct1));
        ProductOption productOption2 = productOptionRepository.save(ProductOption.create("name2","XL", "Green", ProductStatus.ON_SALE, BigDecimal.valueOf(50000), testProduct2));

        inventoryRepository.save(Inventory.create(productOption1, 10));
        inventoryRepository.save(Inventory.create(productOption2, 20));

        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderRequest.OrderItemRequest(productOption1.getId(), 1),
                        new OrderRequest.OrderItemRequest(productOption2.getId(), 2)
                ), new OrderRequest.AddressRequest("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null,
                BigDecimal.ZERO
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<OrderRequest> httpEntityWithHeaders = new HttpEntity<>(orderRequest, headers);

        // Act
        var response = client.exchange(
                BASE_URL,
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<OrderResponse>>() {
                }
        );

        // Assert
        OrderResponse result = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(110000));
    }

    @DisplayName("주문 목록 조회 API - 로그인 사용자는 본인의 주문 목록을 페이지 단위로 조회할 수 있다. 응답 200 Ok")
    @Test
    void getOrderList() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));

        pointRepository.save(Point.create(BigDecimal.valueOf(10000000), user.getId()));

        Brand brand1 = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1", "https://image1.com"));
        Brand brand2 = brandRepository.save(BrandFixture.createBrand("Brand2", "Brand description 2", "https://image2.com"));
        Product testProduct1 = productRepository.save(ProductFixture.createProduct(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand1,
                "https://example.com/image1.jpg"
        ));
        Product testProduct2 = productRepository.save(ProductFixture.createProduct(
                "testProduct2",
                "Description for product 2",
                BigDecimal.valueOf(50000),
                ProductCategory.CLOTHING,
                brand2,
                "https://example.com/image2.jpg"
        ));

        ProductOption productOption1 = productOptionRepository.save(ProductOption.create("name1","L", "Blue", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), testProduct1));
        ProductOption productOption2 = productOptionRepository.save(ProductOption.create("name2","XL", "Green", ProductStatus.ON_SALE, BigDecimal.valueOf(50000), testProduct2));

        inventoryRepository.save(Inventory.create(productOption1, 10));
        inventoryRepository.save(Inventory.create(productOption2, 20));

        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderRequest.OrderItemRequest(productOption1.getId(), 1),
                        new OrderRequest.OrderItemRequest(productOption2.getId(), 2)
                ), new OrderRequest.AddressRequest("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null,
                BigDecimal.ZERO
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<OrderRequest> httpEntityWithHeaders = new HttpEntity<>(orderRequest, headers);

        client.exchange(
                BASE_URL,
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<OrderResponse>>() {
                }
        );

        client.exchange(
                BASE_URL,
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<OrderResponse>>() {
                }
        );

        // Act
        var response = client.exchange(
                BASE_URL + "?page=0&size=20&sort=createdAt_desc",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<PageResponse<OrderResponse.Summary>>>() {
                }
        );

        // Assert
        ApiResponse<PageResponse<OrderResponse.Summary>> body = response.getBody();
        List<OrderResponse.Summary> result = Objects.requireNonNull(body).data().getContent();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(
                OrderResponse.Summary::getTotalAmount,
                OrderResponse.Summary::getStatus
        ).containsExactlyInAnyOrder(
                tuple(BigDecimal.valueOf(110000).setScale(2), OrderStatus.PENDING),
                tuple(BigDecimal.valueOf(110000).setScale(2), OrderStatus.PENDING)
        );

    }


    @DisplayName("주문 상세 조회 API - 로그인 사용자는 본인의 주문을 상세조회 할 수 있다. 응답 200 Ok")
    @Test
    void getOrderDetail() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));

        pointRepository.save(Point.create(BigDecimal.valueOf(10000000), user.getId()));

        Brand brand1 = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1", "https://image1.com"));
        Brand brand2 = brandRepository.save(BrandFixture.createBrand("Brand2", "Brand description 2", "https://image2.com"));
        Product testProduct1 = productRepository.save(ProductFixture.createProduct(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand1,
                "https://example.com/image1.jpg"
        ));
        Product testProduct2 = productRepository.save(ProductFixture.createProduct(
                "testProduct2",
                "Description for product 2",
                BigDecimal.valueOf(50000),
                ProductCategory.CLOTHING,
                brand2,
                "https://example.com/image2.jpg"
        ));

        ProductOption productOption1 = productOptionRepository.save(ProductOption.create("name1","L", "Blue", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), testProduct1));
        ProductOption productOption2 = productOptionRepository.save(ProductOption.create("name2","XL", "Green", ProductStatus.ON_SALE, BigDecimal.valueOf(50000), testProduct2));

        inventoryRepository.save(Inventory.create(productOption1, 10));
        inventoryRepository.save(Inventory.create(productOption2, 20));

        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderRequest.OrderItemRequest(productOption1.getId(), 1),
                        new OrderRequest.OrderItemRequest(productOption2.getId(), 2)
                ), new OrderRequest.AddressRequest("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"),
                null,
                BigDecimal.ZERO
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<OrderRequest> httpEntityWithHeaders = new HttpEntity<>(orderRequest, headers);

        ResponseEntity<ApiResponse<OrderResponse>> orderCreateResponse = client.exchange(
                BASE_URL,
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<OrderResponse>>() {
                }
        );

        Long orderId = Objects.requireNonNull(orderCreateResponse.getBody()).data().getId();

        // Act
        var response = client.exchange(
                BASE_URL + "/" + orderId,
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<OrderResponse.Detail>>() {
                }
        );

        // Assert
        ApiResponse<OrderResponse.Detail> body = response.getBody();
        OrderResponse.Detail result = Objects.requireNonNull(body).data();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(110000));
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getOrderItemInfos()).hasSize(2);
        assertThat(result.getOrderItemInfos())
                .extracting(
                        OrderItemResponse::getProductId,
                        OrderItemResponse::getProductOptionId,
                        OrderItemResponse::getProductOptionName,
                        OrderItemResponse::getPrice,
                        OrderItemResponse::getQuantity,
                        OrderItemResponse::getSize,
                        OrderItemResponse::getColor
                ).containsExactlyInAnyOrder(
                        tuple(testProduct1.getId(), productOption1.getId(), "name1", BigDecimal.valueOf(10000).setScale(2), 1, "L", "Blue"),
                        tuple(testProduct2.getId(), productOption2.getId(), "name2", BigDecimal.valueOf(50000).setScale(2), 2, "XL", "Green")
                );
    }

    @DisplayName("주문 상세 조회 API - 존재하지 않는 orderId 로 조회시에 404 Not Found 응답을 반환한다.")
    @Test
    void getOrderDetail_whenNonExistOrderId() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<OrderRequest> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "/" + 999,
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<OrderResponse.Detail>>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
