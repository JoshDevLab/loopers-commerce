CREATE TABLE IF NOT EXISTS brands (
                                      id   BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      name VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_brands_name (name)
    ) ENGINE=InnoDB;

INSERT IGNORE INTO brands(name, created_at, updated_at)
SELECT CONCAT('Brand ', t.n), NOW(), NOW()
FROM (
         SELECT ones.n + tens.n*10 + hundreds.n*100 + 1 AS n
         FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
               UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
                  CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                              UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
                  CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                              UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) hundreds
     ) t
WHERE t.n <= 500;
-- 1) 존재하는 브랜드 id에 순번(seq) 부여 (임시테이블)
CREATE TEMPORARY TABLE brand_seq AS
SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS seq
FROM brands;

SELECT COUNT(*) INTO @brand_cnt FROM brand_seq;

-- 2) products 10만건: n을 seq로 치환해 '실존 brand_id'만 사용
INSERT INTO products
(name, description, basic_price, product_category, like_count, product_status, brand_id, image_url, created_at, updated_at)
SELECT
    CONCAT('Product ', t.n),
    CONCAT('loopers description for product #', t.n),
    CAST(10000 + FLOOR(RAND(t.n) * 990001) AS DECIMAL(15,2)),
    CASE (t.n % 5)
        WHEN 0 THEN 'SHOES'
        WHEN 1 THEN 'CLOTHING'
        WHEN 2 THEN 'BAG'
        WHEN 3 THEN 'ACCESSORY'
        ELSE 'CLOTHING'
        END,
    CAST(FLOOR(POW(RAND(t.n), 2) * 5001) AS UNSIGNED),
    CASE
        WHEN RAND(t.n * 7)  < 0.85 THEN 'ON_SALE'
        WHEN RAND(t.n * 11) < 0.10 THEN 'SOLD_OUT'
        ELSE 'END_OF_SALE'
        END,
    bs.id,
    CONCAT('https://picsum.photos/seed/', t.n, '/600/600'),
    NOW(), NOW()
FROM (
         SELECT a.n
         FROM (
                  SELECT ones.n + tens.n*10 + hundreds.n*100 + thousands.n*1000 + tenthousands.n*10000 + 1 AS n
                  FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
                           CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                                       UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
                           CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                                       UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) hundreds
                           CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                                       UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) thousands
                           CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                                       UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tenthousands
              ) a
         WHERE a.n <= 100000
     ) t
         JOIN brand_seq bs
              ON bs.seq = (t.n % @brand_cnt) + 1;