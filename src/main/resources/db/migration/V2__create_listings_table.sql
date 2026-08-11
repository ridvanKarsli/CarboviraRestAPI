CREATE TABLE listings (
    id           BIGSERIAL PRIMARY KEY,
    type         VARCHAR(20)   NOT NULL,
    title        VARCHAR(255)  NOT NULL,
    category     VARCHAR(150)  NOT NULL,
    description  VARCHAR(2000),
    quantity     NUMERIC(19,3) NOT NULL,
    unit         VARCHAR(30)   NOT NULL,
    city         VARCHAR(100)  NOT NULL,
    price        NUMERIC(19,2),
    status       VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    company_id   BIGINT        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    created_at   TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT now()
);

-- Arama uçlarındaki filtrelerle birebir eşleşen bileşik index: durum her sorguda
-- sabit olduğundan (ACTIVE) en solda, ardından en sık kullanılan tip/şehir filtreleri.
CREATE INDEX idx_listings_status_type_city ON listings (status, type, city);
CREATE INDEX idx_listings_company_id ON listings (company_id);
