CREATE TABLE disposals (
 id UUID PRIMARY KEY, store_id BIGINT NOT NULL REFERENCES stores, status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','CONFIRMED','CANCELLED')),
 remarks VARCHAR(1000) NOT NULL, created_by BIGINT NOT NULL, version BIGINT NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), FOREIGN KEY(created_by,store_id) REFERENCES users(id,store_id)
);
CREATE INDEX ON disposals(store_id,created_at DESC);

CREATE TABLE disposal_items (
 id BIGSERIAL PRIMARY KEY, disposal_id UUID NOT NULL REFERENCES disposals, product_id BIGINT NOT NULL REFERENCES products,
 quantity NUMERIC(18,2) NOT NULL CHECK(quantity > 0), reason VARCHAR(500) NOT NULL, UNIQUE(disposal_id,product_id)
);
CREATE INDEX ON disposal_items(product_id);

CREATE TABLE disposal_histories (
 id BIGSERIAL PRIMARY KEY, disposal_id UUID NOT NULL REFERENCES disposals, old_status VARCHAR(20), new_status VARCHAR(20) NOT NULL,
 changed_by BIGINT NOT NULL REFERENCES users, changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ON disposal_histories(disposal_id,changed_at);

-- V5 is applied before disposals exists; complete the cross-feature foreign key here.
ALTER TABLE inventory_transactions
 ADD CONSTRAINT inventory_transactions_disposal_id_fkey
 FOREIGN KEY (disposal_id) REFERENCES disposals(id);
