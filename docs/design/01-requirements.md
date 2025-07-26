# 1. 상품 목록 조회

---

## 유저 스토리

* 사용자는 전체 상품 목록을 조회할 수 있습니다.
* 사용자는 브랜드별로 상품을 필터링할 수 있습니다.
* 사용자는 상품을 좋아요 수(인기순), 등록일자(최신순), 가격순으로 정렬할 수 있습니다.
* 사용자는 상품 이름으로 검색할 수 있습니다.
* 사용자는 상품 목록을 페이지 단위로 나누어 조회할 수 있습니다.
* 사용자는 각 상품의 좋아요 수를 확인할 수 있습니다.
* 사용자는 특정 카테고리의 상품만 필터링하여 조회할 수 있습니다.

---

## 기능 흐름

### Main Flow

1.  사용자가 상품 목록 페이지에 접근합니다.
2.  시스템은 기본적으로 전체 상품을 최신순(`sort=latest`)으로 정렬하여, 첫 페이지(`page=0`, `size=20`)를 반환합니다.
3.  사용자가 원하는 필터/정렬/검색/페이징 옵션을 입력합니다.
4.  상품상태가 판매중인 상품만 반환합니다.
5.  시스템은 해당 조건에 맞는 상품 목록을 반환합니다.

### Alternate Flow

* 정렬 조건 지정: 사용자가 `sort=latest`, `price_asc`, `likes_desc` 중 하나를 선택하면, 해당 기준에 따라 목록을 정렬하여 반환합니다.
* 브랜드 필터: 사용자가 특정 브랜드 ID를 전달하면, 해당 브랜드의 상품만 반환됩니다.
* 상품명 검색: 사용자가 검색어를 입력하면, 상품명에 해당 문자열이 포함된 상품만 반환됩니다.
* 페이지네이션: 사용자가 `page`, `size` 파라미터를 입력하면, 해당 위치의 상품 목록을 반환합니다.
* 카테고리 필터: 사용자가 특정 카테고리 ID를 전달하면, 해당 카테고리의 상품만 반환됩니다.

### Exception Flow

* 검색/필터/정렬 결과 없음: 조건에 해당하는 상품이 없으면 빈 목록을 반환하며, 상태 코드는 `200 OK`입니다.
* 잘못된 필터/정렬 파라미터: 정의되지 않은 정렬 조건이나 존재하지 않는 브랜드 ID 또는 카테고리 ID가 들어오면, `400 Bad Request`와 함께 오류 메시지를 반환합니다.

---

## 고려사항

* 필터, 정렬, 검색, 페이징은 조합 가능해야 합니다. (예: "Nike 브랜드 + 인기순 정렬 + '운동화' 검색 + 2페이지 조회 + 신발 카테고리")
* 로그인 여부와 무관하게 누구나 접근 가능합니다.
* 각 상품 객체에는 좋아요 수(`likeCount`)가 포함되어야 합니다.
* 검색은 상품명에 대한 부분 검색(LIKE) 또는 Full-text Search 기반으로 성능 최적화가 필요합니다.
* 전체 상품 수 및 현재 페이지 정보(`totalCount`, `currentPage`, `totalPages` 등)를 함께 응답할 수 있어야 합니다.
* 각 상품 객체에는 카테고리 정보(`categoryId`, `categoryName`)가 포함되어야 합니다.
* 각 상품 목록 항목에는 대표 옵션 정보(예: 가장 저렴한 옵션의 가격, 사용 가능한 사이즈 범위)를 포함할 수 있습니다.

---

## 요청 파라미터 명세

| 파라미터   | 예시                                  | 설명                           |
| :--------- | :------------------------------------ | :----------------------------- |
| `brandId`  | `1`                                   | 특정 브랜드의 상품만 필터링    |
| `sort`     | `latest` / `price_asc` / `likes_desc` | 정렬 기준 (기본값: `latest`) |
| `search`   | `운동화`                              | 상품명에 해당 문자열이 포함된 상품 검색 |
| `page`     | `0`                                   | 페이지 번호 (기본값: `0`)      |
| `size`     | `20`                                  | 페이지당 상품 수 (기본값: `20`) |
| `categoryId` | `10`                                  | 특정 카테고리의 상품만 필터링  |

---

## 응답 필드 예시

```json
{
  "page": 0,
  "size": 20,
  "totalCount": 120,
  "totalPages": 6,
  "contents": [
    {
      "productId": 42,
      "name": "에어맥스 2023",
      "price": 129000,
      "brandName": "Nike",
      "likeCount": 101,
      "thumbnail": "[https://cdn.example.com/products/42/thumb.jpg](https://cdn.example.com/products/42/thumb.jpg)",
      "categoryId": 1,
      "categoryName": "신발",
      "minOptionPrice": 129000,
      "availableSizes": ["260", "270", "280"]
    },
    {
      "productId": 43,
      "name": "러닝화 울트라",
      "price": 99000,
      "brandName": "Adidas",
      "likeCount": 85,
      "thumbnail": "[https://cdn.example.com/products/43/thumb.jpg](https://cdn.example.com/products/43/thumb.jpg)",
      "categoryId": 1,
      "categoryName": "신발",
      "minOptionPrice": 99000,
      "availableSizes": ["S", "M", "L"]
    }
  ]
}
```

---

# 2. 상품 상세 정보 조회

---

## 유저 스토리

* 사용자는 특정 상품의 상세 정보를 조회할 수 있습니다.

---

## 기능 흐름

### Main Flow

1.  사용자가 특정 상품의 상세 정보를 요청합니다.
2.  시스템은 요청된 상품 ID에 해당하는 상품을 조회합니다.
3.  시스템은 상품의 상세 정보를 응답으로 반환합니다.
4.  로그인 여부와 관계없이 누구나 접근 가능합니다.

### Alternate Flow

* 존재하는 상품 ID를 요청한 경우: 상품명, 가격, 옵션(사이즈, 색상, 재고) 정보, 재고 수량, 상품 설명, 이미지, 브랜드 정보, 좋아요 수 등의 정보를 포함하여 반환합니다.
* 로그인한 경우: 본인이 좋아요를 누른 상품인지 여부(`liked`)도 함께 포함하여 반환합니다. 비로그인 시 `liked` 필드는 `false`로 반환됩니다.
* 상품의 판매 상태가 'ON_SALE'이 아닌 경우: 상품이 `SOLD_OUT` 또는 `DISCONTINUED` 상태일 경우에도 상세 정보는 조회 가능하나, 해당 상태 정보(`saleStatus`)를 명확히 포함하여 반환합니다.

### Exception Flow

* 존재하지 않는 상품 ID: 시스템은 `404 Not Found` 와 함께 `"해당 상품을 찾을 수 없습니다."` 메시지를 반환합니다.
* 삭제된 상품: 시스템은 마찬가지로 `404 Not Found`로 응답합니다.
* 잘못된 형식의 상품 ID (예: 음수, 문자 등): 시스템은 `400 Bad Request`를 반환합니다.

---

## 고려사항

* 상품 ID는 URL 경로 파라미터(`/products/{id}`)를 통해 전달됩니다.
* 좋아요 여부(`liked`)는 항상 응답에 포함되며, 인증된 사용자의 요청일 경우 해당 사용자의 좋아요 여부를, 비인증 사용자의 요청일 경우 `false`를 반환합니다.
* 상품 상세 조회 시, 해당 상품에 연결된 모든 `ProductOption` 정보를 함께 반환하여 사용자가 구매 가능한 사이즈 및 재고를 확인할 수 있도록 합니다.
* `categoryId`를 통해 해당 카테고리의 `sizeUnitType` (예: `MM`, `ALPHA_SIZE`, `INCH` 등)을 함께 제공하여 프론트엔드가 적절한 사이즈 단위로 표시할 수 있도록 합니다.

---

## 응답 필드 예시

| 필드명        | 설명                               |
| :------------ | :--------------------------------- |
| `id`          | 상품 고유 ID                       |
| `name`        | 상품명                             |
| `price`       | 상품의 기본 가격 (옵션에 따른 추가 가격은 각 옵션에 포함) |
| `description` | 상품 설명                          |
| `images`      | 이미지 URL 리스트                  |
| `brandName`   | 브랜드명                           |
| `likeCount`   | 현재 좋아요 수                     |
| `liked`       | 사용자가 이 상품을 좋아요했는지 여부 (로그인 여부와 관계없이 항상 포함) |
| `createdAt`   | 등록일                             |
| `thumbnail`   | 대표 썸네일 이미지 URL             |
| `categoryId`  | 상품 카테고리 고유 ID              |
| `categoryName` | 상품 카테고리명                    |
| `sizeUnitType` | 해당 카테고리의 사이즈 단위 유형 (예: "MM", "ALPHA_SIZE", "INCH") |
| `saleStatus`  | 상품 판매 상태 (ON_SALE, SOLD_OUT, DISCONTINUED 등) |
| `options`     | 상품 옵션 리스트                   |
| &nbsp;&nbsp;&nbsp;&nbsp;`- optionId` | &nbsp;&nbsp;&nbsp;&nbsp;옵션 고유 ID |
| &nbsp;&nbsp;&nbsp;&nbsp;`- sizeValue` | &nbsp;&nbsp;&nbsp;&nbsp;사이즈 값 (예: "260", "M", "30") |
| &nbsp;&nbsp;&nbsp;&nbsp;`- colorValue` | &nbsp;&nbsp;&nbsp;&nbsp;색상 값 (예: "Red", "Black") |
| &nbsp;&nbsp;&nbsp;&nbsp;`- stock` | &nbsp;&nbsp;&nbsp;&nbsp;해당 옵션의 재고 수량 |
| &nbsp;&nbsp;&nbsp;&nbsp;`- additionalPrice` | &nbsp;&nbsp;&nbsp;&nbsp;해당 옵션의 추가 가격 (0원일 수 있음) |
| &nbsp;&nbsp;&nbsp;&nbsp;`- optionStatus` | &nbsp;&nbsp;&nbsp;&nbsp;옵션 판매 상태 (ON_SALE, SOLD_OUT 등) |

```json
{
  "id": 42,
  "name": "에어맥스 2023",
  "price": 120000,
  "description": "편안한 착용감을 제공하는 에어맥스 2023...",
  "images": ["[https://cdn.example.com/products/42/img1.jpg](https://cdn.example.com/products/42/img1.jpg)", "[https://cdn.example.com/products/42/img2.jpg](https://cdn.example.com/products/42/img2.jpg)"],
  "brandName": "Nike",
  "likeCount": 101,
  "liked": true,
  "createdAt": "2024-07-20T10:00:00",
  "thumbnail": "[https://cdn.example.com/products/42/thumb.jpg](https://cdn.example.com/products/42/thumb.jpg)",
  "categoryId": 1,
  "categoryName": "신발",
  "sizeUnitType": "MM",
  "saleStatus": "ON_SALE",
  "options": [
    {
      "optionId": 101,
      "sizeValue": "260",
      "colorValue": "Black",
      "stock": 50,
      "additionalPrice": 9000,
      "optionStatus": "ON_SALE"
    },
    {
      "optionId": 102,
      "sizeValue": "270",
      "colorValue": "Black",
      "stock": 30,
      "additionalPrice": 9000,
      "optionStatus": "ON_SALE"
    },
    {
      "optionId": 103,
      "sizeValue": "280",
      "colorValue": "Black",
      "stock": 0,
      "additionalPrice": 9000,
      "optionStatus": "SOLD_OUT"
    },
    {
      "optionId": 104,
      "sizeValue": "270",
      "colorValue": "White",
      "stock": 10,
      "additionalPrice": 9000,
      "optionStatus": "ON_SALE"
    }
  ]
}
```

-----

# 3. 브랜드 조회

---

## 유저 스토리

* 사용자는 전체 브랜드 목록을 조회할 수 있습니다.
* 사용자는 특정 브랜드의 상세 정보를 조회할 수 있습니다.

---

## 기능 흐름

### Main Flow

* [브랜드 목록 조회]

    1.  사용자가 브랜드 목록을 요청합니다.
    2.  시스템은 등록된 전체 브랜드 목록을 반환합니다.
    3.  로그인 여부와 관계없이 누구나 접근 가능합니다.

* [브랜드 상세 조회]

    1.  사용자가 특정 브랜드 ID로 브랜드 정보를 요청합니다.
    2.  시스템은 해당 브랜드가 존재하는 경우 상세 정보를 반환합니다.

### Alternate Flow

* [브랜드 목록 조회]: 시스템은 기본적으로 브랜드명을 오름차순 정렬하여 반환합니다.
* [브랜드 상세 조회]: 반환 시 브랜드명, 로고 URL, 등록일 등 기본 정보를 포함합니다.

### Exception Flow

* 브랜드가 존재하지 않는 ID로 상세 조회 요청한 경우: 시스템은 `404 Not Found` 와 함께 `"해당 브랜드를 찾을 수 없습니다."` 메시지를 반환합니다.
* 브랜드가 하나도 없는 경우 (목록 조회): 시스템은 빈 배열을 반환하며, 상태 코드는 `200 OK`입니다.
* 잘못된 `brandId` 형식 (예: 음수, 문자 등): 시스템은 `400 Bad Request`를 반환합니다.

---

## 고려사항

* 브랜드 목록은 주로 드롭다운/필터에 사용되므로 전체 조회가 기본입니다.
* 브랜드 상세 조회는 상품 상세에 연결되거나 브랜드 소개 페이지 등에서 사용될 수 있습니다.
* 향후 `비활성 브랜드`, `브랜드 삭제 여부` 등을 고려해 `isActive` 등의 필드를 포함시킬 수 있습니다.

---

## 요청 파라미터 명세

### 목록 조회 (`GET /api/v1/brands`)

* 인증 필요 없음
* 별도 파라미터 없음 (정렬, 검색은 추후 확장 고려)

### 상세 조회 (`GET /api/v1/brands/{brandId}`)

| 파라미터  | 예시 | 설명                       |
| :-------- | :--- | :------------------------- |
| `brandId` | `1`  | 브랜드 고유 ID (경로 변수) |

---

## 응답 필드 예시

| 필드명    | 설명             |
| :-------- | :--------------- |
| `id`      | 브랜드 고유 ID   |
| `name`    | 브랜드명         |
| `logoUrl` | 브랜드 로고 이미지 URL |
| `createdAt` | 브랜드 등록일    |
| `updatedAt` | 브랜드 최종 수정일 |

-----

# 4. 상품 좋아요 기능

---

## 유저 스토리

* 사용자는 상품에 좋아요를 등록할 수 있습니다.
* 사용자는 이미 좋아요한 상품을 취소할 수 있습니다.
* 사용자는 자신이 좋아요한 상품 목록을 조회할 수 있습니다.

---

### 1) 좋아요 등록

* Method: `POST`
* URI: `/api/v1/products/{productId}/likes`

#### Main Flow

1.  로그인한 사용자가 특정 상품에 좋아요를 등록합니다.
2.  시스템은 해당 상품 ID가 존재하는지 확인합니다.
3.  이미 좋아요가 등록되어 있다면: `200 OK` 상태 코드와 함께 현재 상태를 반환합니다. (멱등 처리)
4.  좋아요가 등록되지 않은 경우:
    * `likes` 테이블에 새로운 row를 생성합니다.
    * `products.like_count` 값을 +1 증가시킵니다.
    * `201 Created` 상태 코드와 함께 현재 상태(`liked=true`)와 좋아요 수(`likeCount`)를 반환합니다.

#### Exception Flow

* 비로그인 사용자가 요청할 경우: `401 Unauthorized`
* 존재하지 않는 상품 ID: `404 Not Found`
* 잘못된 상품 ID 형식: `400 Bad Request`

---

### 2) 좋아요 취소

* Method: `DELETE`
* URI: `/api/v1/products/{productId}/likes`

#### Main Flow

1.  로그인한 사용자가 특정 상품의 좋아요를 취소 요청합니다.
2.  시스템은 해당 좋아요가 존재하는지 확인합니다.
3.  좋아요가 존재한다면:
    * `likes` 테이블에서 row를 삭제합니다.
    * `products.like_count` 값을 -1 감소시킵니다.
    * `200 OK` 상태 코드와 함께 현재 상태(`liked=false`)와 좋아요 수(`likeCount`)를 반환합니다.
4.  좋아요가 존재하지 않아도: `200 OK` 상태 코드와 함께 현재 상태(`liked=false`)와 좋아요 수(`likeCount`)를 반환합니다. (멱등 처리)

#### Exception Flow

* 비로그인 사용자가 요청할 경우: `401 Unauthorized`
* 존재하지 않는 상품 ID: `404 Not Found`
* 잘못된 상품 ID 형식: `400 Bad Request`

---

### 3) 좋아요한 상품 목록 조회

* Method: `GET`
* URI: `/api/v1/users/{userId}/likes`

#### Main Flow

1.  로그인한 사용자가 본인의 좋아요 목록을 요청합니다.
2.  시스템은 해당 사용자가 좋아요한 상품 리스트를 조회하여 반환합니다.
3.  각 상품에는 다음 정보가 포함되어야 합니다: `productId`, `name`, `price`, `brandName`, `likeCount`, `thumbnail`, `categoryId`, `categoryName`, `minOptionPrice`, `availableSizes`.
4.  페이징 및 정렬 옵션은 기본 적용됩니다 (예: `page=0`, `size=20`, `sort=latest`).

#### Exception Flow

* 요청한 사용자 ID가 로그인 사용자와 다를 경우: `403 Forbidden` (본인만 조회 가능)

---

## 고려사항

* `userId + productId` 조합은 `likes` 테이블에서 복합 유니크 인덱스로 제한되어야 합니다.
* 좋아요 등록 및 취소 요청은 모두 멱등하게 동작해야 하며, 같은 요청을 여러 번 보내더라도 결과는 동일해야 합니다.
* 좋아요 수(`likeCount`)는 `products` 테이블에 별도 컬럼으로 관리되며, 등록/취소 시 함께 증가 또는 감소되어야 합니다.
* 좋아요 수는 다음 API에서 응답에 반드시 포함되어야 합니다:
    * 상품 목록 조회
    * 상품 상세 조회
    * 좋아요한 상품 목록 조회
* 실시간 동시성 충돌(예: 같은 상품에 여러 사용자가 동시에 좋아요 요청)은 본 요구사항 정의에서는 고려하지 않습니다.

---

# 5. 주문 / 결제

---

## 유저 스토리

* 사용자는 장바구니에 담은 상품을 주문할 수 있습니다.
* 사용자는 주문할 때 보유 포인트를 일부 또는 전액 사용할 수 있습니다.
* 주문이 성공적으로 완료되면 상품 옵션의 재고가 차감되고, 사용한 포인트도 차감됩니다.
* 사용자는 본인의 주문 내역 및 주문 상세 정보를 조회할 수 있습니다.

---

## API 명세

| METHOD | URI                          | 설명           |
| :----- | :--------------------------- | :------------- |
| `POST` | `/api/v1/orders`             | 주문 요청      |
| `GET`  | `/api/v1/users/{userId}/orders` | 유저의 주문 목록 조회 |
| `GET`  | `/api/v1/orders/{orderId}`   | 단일 주문 상세 조회 |

---

### 1) 주문 생성

* Method: `POST`
* URI: `/api/v1/orders`

#### 요청 예시

```json
{
  "items": [
    { "productOptionId": 101, "quantity": 2 },
    { "productOptionId": 203, "quantity": 1 }
  ],
  "usedPoint": 1000 
}
```

#### Main Flow

1. 로그인한 사용자가 상품 ID 및 수량 정보를 포함한 주문을 요청합니다.
2. 시스템은 각 상품 옵션의 판매 상태가 'ON_SALE'이고, 해당 상품이 'ON_SALE' 상태인지 확인합니다.
3. 시스템은 각 상품 옵션의 재고를 확인하고, 요청 수량만큼 재고를 차감합니다.
4. 사용자가 포인트를 사용할 경우:
    * 보유 포인트를 확인하고 사용 가능 여부를 검증합니다.
    * 사용한 포인트만큼 차감합니다.
5. 주문 정보를 저장하고, 결제 외부 시스템에 주문 정보를 전송합니다. (Mock 처리 가능)
6. 주문 생성 완료 응답(201 Created)을 반환합니다.

#### Alternate Flow

* 일부 상품 옵션만 재고가 부족한 경우 전체 주문은 실패합니다.
* 포인트가 부족한 경우, 주문은 실패합니다.

#### Exception Flow

* 비로그인 사용자가 요청한 경우: 401 Unauthorized
* 존재하지 않는 상품 옵션 ID가 포함된 경우: 400 Bad Request
* 판매 중이 아닌 상품 옵션이 포함된 경우: 400 Bad Request (메시지: "판매 중이 아닌 상품 옵션이 포함되어 있습니다.")
* 상품 옵션 재고가 부족한 경우: 409 Conflict (메시지: "상품 옵션 재고가 부족합니다.")
* 사용 가능한 포인트보다 많은 포인트 사용 시: 400 Bad Request (메시지: "포인트가 부족합니다.")

-----

### 2) 주문 목록 조회

* Method: `GET`
* URI: `/api/v1/users/{userId}/orders`

#### 요청 파라미터 명세

| 파라미터 | 예시                     | 설명                           |
| :------- | :----------------------- | :----------------------------- |
| `page`   | `0`                      | 페이지 번호 (기본값: `0`)      |
| `size`   | `20`                     | 페이지당 주문 수 (기본값: `20`) |
| `sort`   | `orderedAt_desc` / `totalPrice_asc` | 정렬 기준 (기본값: `orderedAt_desc`) |

#### Main Flow

* 로그인한 사용자가 본인의 주문 목록을 요청합니다.
* 시스템은 해당 사용자의 주문 리스트를 orderedAt(주문일시) 기준 내림차순으로 페이징하여 반환합니다.
* 각 주문 항목에는 다음 정보가 포함됩니다: orderId, totalPrice, usedPoint, paidAmount, status, orderedAt.
* 응답에는 페이징 정보(page, size, totalCount, totalPages)가 포함됩니다.

#### Exception Flow

* 다른 사용자의 주문 목록을 요청할 경우: `403 Forbidden`

-----

### 3) 주문 상세 조회

* Method: `GET`
* URI: `/api/v1/orders/{orderId}`

#### Main Flow

* 로그인한 사용자가 특정 주문 ID를 요청합니다.
* 시스템은 해당 주문 정보를 반환합니다.

#### Exception Flow

* **본인이 아닌 사용자의 주문을 조회할 경우**: `403 Forbidden`
* **존재하지 않는 주문 ID**: `404 Not Found`
* **잘못된 주문 ID 형식**: `400 Bad Request`

-----

## 고려사항

* 주문 생성 시 상품 옵션 재고 차감, 포인트 차감, 주문 저장은 하나의 논리적 단위로 처리되어야 하며, 이 중 하나라도 실패할 경우 전체 주문 프로세스는 실패합니다.
* 현 시점에서는 동시성 제어는 고려하지 않습니다.
* 사용자 포인트는 적립 및 사용 내역이 관리되어야 합니다.
* 상품의 재고 유무의 따라 상품상태가 관리되어야 합니다.
* 주문 상품 항목에는 productId 외에 optionId, sizeValue, colorValue 등 주문한 특정 옵션의 정보가 포함되어야 합니다.

-----

### 주문 응답 예시

```json
{
  "orderId": 10001,
  "userId": "user123",
  "totalPrice": 87000,
  "usedPoint": 1000,
  "paidAmount": 86000,
  "status": "ORDERED",
  "orderedAt": "2025-07-21T14:55:00",
  "items": [
    {
      "productId": 1,
      "optionId": 101,
      "productName": "에어맥스 2023",
      "price": 43000,
      "quantity": 2,
      "sizeValue": "260",
      "colorValue": "Black"
    },
    {
      "productId": 3,
      "optionId": 203,
      "productName": "런닝화 X",
      "price": 10000,
      "quantity": 1,
      "sizeValue": "M",
      "colorValue": "Blue"
    }
  ]
}
```