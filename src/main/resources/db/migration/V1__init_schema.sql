CREATE TABLE companies (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    tax_number   VARCHAR(50)  NOT NULL UNIQUE,
    sector       VARCHAR(150),
    city         VARCHAR(100),
    address      VARCHAR(500),
    description  VARCHAR(2000),
    verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(255) NOT NULL,
    role         VARCHAR(30)  NOT NULL,
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    company_id   BIGINT       NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_company_id ON users (company_id);
