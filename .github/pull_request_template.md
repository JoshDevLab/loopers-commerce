### 상품 조회 성능 최적화: 인덱스 추가 및 EXPLAIN 전/후 성능 비교

## 1) 인덱스 설계

```sql
-- UseCase1: 브랜드 + 카테고리 + 좋아요순 + 최신순
CREATE INDEX idx_brand_category_like_id
  ON products (brand_id, product_category, like_count DESC, id DESC);

-- UseCase2: 브랜드 + 카테고리 + 가격순 + 최신순
CREATE INDEX idx_brand_category_price_id
  ON products (brand_id, product_category, basic_price ASC, id DESC);

-- UseCase3: 브랜드 + 좋아요순 + 최신순
CREATE INDEX idx_brand_like_id
  ON products (brand_id, like_count DESC, id DESC);

-- UseCase4: 브랜드 + 가격순 + 최신순
CREATE INDEX idx_brand_price_id
  ON products (brand_id, basic_price ASC, id DESC);
```

## 2) 유즈케이스별 쿼리 & 성능 비교

| UC  | 쿼리 | Before – key / rows / Extra | After – key / rows / Extra | 개선 포인트 |
|---|---|---|---|---|
| UC1 | `WHERE brand_id = ? AND product_category = 'CLOTHING' ORDER BY like_count DESC, id DESC LIMIT 20` | `idx_brand_price_id` / 200 / `Using where; Using filesort` | `idx_brand_like_id` / 200 / `Using where` | Filesort 제거 → 인덱스 순차 스캔 활용 |
| UC2 | `WHERE brand_id = ? AND product_category = 'CLOTHING' ORDER BY basic_price, id DESC LIMIT 20` | `idx_brand_price_id` / 200 / `Using where; Using filesort` | `idx_brand_price_id` / 200 / `Using where` | Filesort 제거 |
| UC3 | `WHERE brand_id = ? ORDER BY like_count DESC, id DESC LIMIT 20` | `idx_brand_price_id` / 200 / `Using filesort` | `idx_brand_like_id` / 200 / *(null)* | Filesort 제거, 순차 인덱스 스캔 |
| UC4 | `WHERE brand_id = ? ORDER BY basic_price, id DESC LIMIT 20` | `idx_brand_price_id` / 200 / `Using filesort` | `idx_brand_price_id` / 200 / *(null)* | Filesort 제거, 순차 인덱스 스캔 |


## 3) k6 성능 결과 (Before → After)

| Metric (Scenario) | Before Avg | After Avg | Before p(95) | After p(95) | Change Avg | Change p(95) |
|-------------------|-----------:|----------:|-------------:|------------:|-----------:|-------------:|
| UC1 Duration      | 16.5883 ms | 16.4664 ms | 25.2784 ms | 24.9821 ms | **-0.1219 ms** | **-0.2963 ms** |
| UC2 Duration      | 16.4360 ms | 16.2733 ms | 26.2548 ms | 24.4146 ms | **-0.1627 ms** | **-1.8402 ms** |
| UC3 Duration      | 16.5549 ms | 16.4149 ms | 26.1010 ms | 25.2722 ms | **-0.1399 ms** | **-0.8288 ms** |
| UC4 Duration      | 16.0937 ms | 16.4854 ms | 25.8263 ms | 25.1156 ms | **+0.3918 ms** | **-0.7108 ms** |
| 전체 HTTP Req     | 16.4182 ms | 16.4100 ms | 25.9909 ms | 25.0012 ms | **-0.0082 ms** | **-0.9897 ms** |

## 4) 개선 요약
- UC1/UC3: `brand_id, like_count DESC, id DESC` 복합 인덱스로 **정렬 시 Filesort 제거** → 인덱스 순차 스캔.
- UC2/UC4: `brand_id, basic_price ASC, id DESC`로 **가격 정렬 최적화** → Filesort 제거.
