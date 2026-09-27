CREATE TABLE products (
 id BIGSERIAL PRIMARY KEY, product_code VARCHAR(50) NOT NULL UNIQUE, barcode VARCHAR(50) NOT NULL UNIQUE,
 product_name VARCHAR(255) NOT NULL, image_url VARCHAR(1000), image_version BIGINT NOT NULL DEFAULT 0,
 unit_price NUMERIC(18,2) CHECK(unit_price >= 0), active BOOLEAN NOT NULL DEFAULT true,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE product_image_sync_logs (
 id BIGSERIAL PRIMARY KEY, product_id BIGINT NOT NULL REFERENCES products, image_url VARCHAR(1000), source_version BIGINT NOT NULL,
 sync_status VARCHAR(20) NOT NULL CHECK(sync_status IN ('APPLIED','STALE')), created_by BIGINT NOT NULL REFERENCES users, synced_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ON product_image_sync_logs(product_id,synced_at DESC);
