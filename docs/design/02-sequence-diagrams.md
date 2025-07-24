# 1) 상품 목록 조회
```mermaid
sequenceDiagram
    participant User
    participant ProductController
    participant ProductFacade
    participant ProductService
    participant BrandService
    participant CategoryService

    User->>ProductController: GET /products?sort=POPULAR&category=SHOES
    ProductController->>ProductFacade: 상품 목록 조회 요청

    ProductFacade->>ProductService: 판매중 상품 목록 조회 (필터, 정렬)
    ProductService-->>ProductFacade: 상품 리스트 반환(판매중인 상품)

    ProductFacade->>BrandService: 각 상품의 브랜드 정보 조회
    BrandService-->>ProductFacade: 브랜드 정보 반환

    ProductFacade->>CategoryService: 카테고리 정보 조회
    CategoryService-->>ProductFacade: 카테고리 정보 반환

    ProductFacade-->>ProductController: DTO 변환 후 반환
    ProductController-->>User: 200 OK, 상품 목록 응답
```
---

# 2) 상품 상세 조회

```mermaid
sequenceDiagram
    participant User
    participant ProductController
    participant ProductFacade
    participant ProductService
    participant LikeService

    User->>ProductController: GET /products/{productId} (선택적으로 X-USER-ID 포함)
    ProductController->>ProductFacade: 상품 상세 조회 요청

    ProductFacade->>ProductService: 상품 및 옵션 조회 by productId
    alt 상품이 존재하지 않는 경우
        ProductService--xProductFacade: CoreException(PRODUCT_NOT_FOUND)
        ProductFacade--xProductController: 예외 전달
        ProductController-->>User: 404 Not Found
    else 상품 존재
        ProductService-->>ProductFacade: 상품 및 옵션 정보 반환

        alt X-USER-ID 존재 (로그인 사용자)
            ProductFacade->>LikeService: 좋아요 여부 조회 (userId, productId)
            LikeService-->>ProductFacade: 좋아요 여부 반환
        end

        ProductFacade-->>ProductController: DTO 반환 (좋아요 여부 포함 가능)
        ProductController-->>User: 200 OK, 상품 상세 정보 반환
    end
```

---

# 3) 브랜드 조회

## 3-1) 전체 브랜드 목록 조회
```mermaid
sequenceDiagram
    participant User
    participant BrandController
    participant BrandFacade
    participant BrandService

    User->>BrandController: GET /brands
    BrandController->>BrandFacade: 브랜드 목록 조회
    BrandFacade->>BrandService: 브랜드 목록 조회
    BrandService-->>BrandFacade: 브랜드 목록 반환
    BrandFacade-->>BrandController: DTO 반환
    BrandController-->>User: 200 OK, 브랜드 목록 응답
```

## 3-2) 특정 브랜드 상세 조회
```mermaid
sequenceDiagram
    participant User
    participant BrandController
    participant BrandFacade
    participant BrandService

    User->>BrandController: GET /brands/{brandId}
    BrandController->>BrandFacade: 브랜드 상세 조회 요청
    BrandFacade->>BrandService: 브랜드 ID로 조회
    alt 브랜드 없음
        BrandService--xBrandFacade: throw CoreException(BRAND_NOT_FOUND)
        BrandFacade--xBrandController: 예외 전달
        BrandController-->>User: 404 Not Found
    else 브랜드 존재
        BrandService-->>BrandFacade: 브랜드 정보 반환
        BrandFacade-->>BrandController: DTO 반환
        BrandController-->>User: 200 OK, 브랜드 상세 정보 응답
    end
```

---

# 4) 상품 좋아요 기능
## 4-1) 상품 좋아요 등록
```mermaid
sequenceDiagram
    participant User
    participant LikeController
    participant LikeFacade
    participant ProductService
    participant LikeService

    User->>LikeController: POST /products/{productId}/like (X-USER-ID)
    alt X-USER-ID 없음
        LikeController-->>User: 401 Unauthorized
    else
        LikeController->>LikeFacade: 좋아요 등록 요청
        LikeFacade->>ProductService: 상품 존재 확인
        alt 상품 없음
            ProductService--xLikeFacade: CoreException(PRODUCT_NOT_FOUND)
            LikeFacade--xLikeController: 예외 전달
            LikeController-->>User: 404 Not Found
        else 상품 있음
            ProductService-->>LikeFacade: 확인 완료
            LikeFacade->>LikeService: 좋아요 저장
            LikeService-->>LikeFacade: 저장 완료
            LikeFacade-->>LikeController: DTO 반환
            LikeController-->>User: 201 Created, 좋아요 등록 완료
        end
    end
```

## 4-2) 상품 좋아요 취소
```mermaid
sequenceDiagram
    participant User
    participant LikeController
    participant LikeFacade
    participant ProductService
    participant LikeService

    User->>LikeController: DELETE /products/{productId}/like (X-USER-ID)
    alt X-USER-ID 없음
        LikeController-->>User: 401 Unauthorized
    else
        LikeController->>LikeFacade: 좋아요 취소 요청
        LikeFacade->>ProductService: 상품 존재 확인
        alt 상품 없음
            ProductService--xLikeFacade: CoreException(PRODUCT_NOT_FOUND)
            LikeFacade--xLikeController: 예외 전달
            LikeController-->>User: 404 Not Found
        else 상품 있음
            ProductService-->>LikeFacade: 확인 완료
            LikeFacade->>LikeService: 좋아요 삭제
            LikeService-->>LikeFacade: 삭제 완료
            LikeFacade-->>LikeController: DTO 반환
            LikeController-->>User: 200 OK, 좋아요 취소 완료
        end
    end
```

## 4-3) 좋아요한 상품 목록 조회
```mermaid
sequenceDiagram
    participant User
    participant LikeController
    participant LikeFacade
    participant LikeService
    participant ProductService
    participant BrandService
    participant CategoryService

    User->>LikeController: GET /users/{userId}/likes (X-USER-ID)
    alt X-USER-ID 없음
        LikeController-->>User: 401 Unauthorized
    else ID 불일치
        LikeController-->>User: 403 Forbidden
    else
        LikeController->>LikeFacade: 좋아요 상품 목록 요청
        LikeFacade->>LikeService: 좋아요 데이터 조회
        LikeService-->>LikeFacade: 상품 ID 목록 반환
        LikeFacade->>ProductService: 상품 상세 정보 조회
        ProductService-->>LikeFacade: 상품 목록 반환
        LikeFacade->>BrandService: 브랜드 정보 조회
        LikeFacade->>CategoryService: 카테고리 정보 조회
        BrandService-->>LikeFacade: 브랜드 정보 반환
        CategoryService-->>LikeFacade: 카테고리 정보 반환
        LikeFacade-->>LikeController: DTO 반환
        LikeController-->>User: 200 OK, 좋아요 상품 목록 반환
    end
```

---

# 5) 주문 / 결제

## 5-1) 주문 생성
```mermaid
sequenceDiagram
    participant User
    participant OrderController
    participant OrderFacade
    participant ProductService
    participant InventoryService
    participant PointService
    participant OrderService
    participant PaymentService

    User->>OrderController: POST /orders (X-USER-ID 포함)
    alt X-USER-ID 누락
        OrderController-->>User: 401 Unauthorized
    else 로그인 상태
        OrderController->>OrderFacade: 주문 생성 요청

        loop 상품 항목 반복
            OrderFacade->>ProductService: 상품 옵션 조회 및 검증
            alt 상품 옵션이 유효하지 않음
                ProductService--xOrderFacade: CoreException(INVALID_PRODUCT)
                OrderFacade--xOrderController: 예외 전달
                OrderController-->>User: 400 Bad Request
            else
                ProductService-->>OrderFacade: 옵션 정보 반환
                OrderFacade->>InventoryService: 재고 차감 요청
                alt 재고 부족
                    InventoryService--xOrderFacade: CoreException(INSUFFICIENT_STOCK)
                    OrderFacade--xOrderController: 예외 전달
                    OrderController-->>User: 400 Bad Request
                else
                    InventoryService-->>OrderFacade: 차감 완료
                end
            end
        end

        OrderFacade->>PointService: 포인트 차감 요청
        alt 포인트 부족
            PointService--xOrderFacade: CoreException(INSUFFICIENT_POINT)
            OrderFacade--xOrderController: 예외 전달
            OrderController-->>User: 400 Bad Request
        else
            PointService-->>OrderFacade: 차감 완료
            alt 결제 실패
                PaymentService-->>OrderFacade: 결제 실패 응답 HTTP 502 (Bad Gateway)
                OrderFacade-->>OrderController: 예외 전달
            else 결제 성공
                PaymentService-->>OrderFacade: 결제 승인 완료
                OrderFacade->>OrderService: 주문 및 주문항목 저장
                OrderService-->>OrderFacade: 저장 완료

                OrderFacade-->>OrderController: HTTP 201 Created + 응답 DTO
            end
        end
        
    end

```        

## 5-2) 주문 목록 조회
```mermaid
sequenceDiagram
    participant User
    participant OrderController
    participant OrderFacade
    participant OrderService

    User->>OrderController: GET /users/{userId}/orders (X-USER-ID 포함)
    alt X-USER-ID 누락
        OrderController-->>User: 401 Unauthorized
    else 로그인 상태
        alt URL userId ≠ X-USER-ID
            OrderController-->>User: 403 Forbidden
        else 일치
            OrderController->>OrderFacade: 주문 목록 조회 요청
            OrderFacade->>OrderService: 사용자 주문 목록 조회
            OrderService-->>OrderFacade: 주문 목록 반환
            OrderFacade-->>OrderController: 응답 DTO 반환
            OrderController-->>User: 200 OK
        end
    end

```

## 5-3) 주문 상세 조회
```mermaid
sequenceDiagram
    participant User
    participant OrderController
    participant OrderFacade
    participant OrderService
    participant ProductService

    User->>OrderController: GET /orders/{orderId} (X-USER-ID 포함)
    alt X-USER-ID 누락
        OrderController-->>User: 401 Unauthorized
    else 로그인 상태
        OrderController->>OrderFacade: 주문 상세 조회 요청
        OrderFacade->>OrderService: 주문 조회 (orderId 포함)
        alt 주문 없음
            OrderService--xOrderFacade: CoreException(ORDER_NOT_FOUND)
            OrderFacade--xOrderController: 예외 전달
            OrderController-->>User: 404 Not Found
        else 주문 존재
            OrderService-->>OrderFacade: 주문 및 소유자 ID 반환
            alt 요청자 ≠ 주문 소유자
                OrderFacade-->>OrderController: 403 Forbidden
                OrderController-->>User: 403 Forbidden
            else
                loop 각 주문 항목
                    OrderFacade->>ProductService: 상품 정보 조회 (상품 ID)
                    ProductService-->>OrderFacade: 상품 정보 반환
                end
                OrderFacade-->>OrderController: 주문 상세 DTO 반환
                OrderController-->>User: 200 OK
            end
        end
    end

```