CREATE TABLE inventories (
 id BIGSERIAL PRIMARY KEY, product_id BIGINT NOT NULL REFERENCES products, store_id BIGINT NOT NULL REFERENCES stores,
 quantity NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK(quantity >= 0), version BIGINT NOT NULL DEFAULT 0 CHECK(version >= 0),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(store_id,product_id)
);
CREATE INDEX ON inventories(product_id);

CREATE TABLE inventory_adjustments (
 id UUID PRIMARY KEY, product_id BIGINT NOT NULL, store_id BIGINT NOT NULL, old_qty NUMERIC(18,2) NOT NULL CHECK(old_qty >= 0),
 new_qty NUMERIC(18,2) NOT NULL CHECK(new_qty >= 0), expected_version BIGINT NOT NULL, reason VARCHAR(1000) NOT NULL,
 created_by BIGINT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 FOREIGN KEY(store_id,product_id) REFERENCES inventories(store_id,product_id), FOREIGN KEY(created_by,store_id) REFERENCES users(id,store_id)
);
CREATE INDEX ON inventory_adjustments(store_id,product_id,created_at DESC);

CREATE TABLE inventory_transactions (
 id BIGSERIAL PRIMARY KEY, product_id BIGINT NOT NULL, store_id BIGINT NOT NULL,
 transaction_type VARCHAR(20) NOT NULL CHECK(transaction_type IN ('ADJUSTMENT','DISPOSAL')),
 qty_before NUMERIC(18,2) NOT NULL CHECK(qty_before >= 0), qty_change NUMERIC(18,2) NOT NULL, qty_after NUMERIC(18,2) NOT NULL CHECK(qty_after >= 0),
 adjustment_id UUID REFERENCES inventory_adjustments, disposal_id UUID, created_by BIGINT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), CHECK(qty_before + qty_change = qty_after),
 CHECK((transaction_type='ADJUSTMENT' AND adjustment_id IS NOT NULL AND disposal_id IS NULL) OR (transaction_type='DISPOSAL' AND disposal_id IS NOT NULL AND adjustment_id IS NULL)),
 FOREIGN KEY(store_id,product_id) REFERENCES inventories(store_id,product_id), FOREIGN KEY(created_by,store_id) REFERENCES users(id,store_id),
 UNIQUE(adjustment_id), UNIQUE(disposal_id,product_id)
);
CREATE INDEX ON inventory_transactions(store_id,id);
