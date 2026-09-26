CREATE TABLE stores (
 id BIGSERIAL PRIMARY KEY, store_code VARCHAR(50) NOT NULL UNIQUE, store_name VARCHAR(255) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE users (
 id BIGSERIAL PRIMARY KEY, username VARCHAR(100) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL,
 full_name VARCHAR(255), email VARCHAR(255), store_id BIGINT NOT NULL REFERENCES stores,
 active BOOLEAN NOT NULL DEFAULT true, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE(id, store_id)
);
CREATE INDEX ON users(store_id);
CREATE TABLE roles (id BIGSERIAL PRIMARY KEY, role_name VARCHAR(50) NOT NULL UNIQUE, description VARCHAR(255));
CREATE TABLE user_roles (user_id BIGINT REFERENCES users NOT NULL, role_id BIGINT REFERENCES roles NOT NULL, PRIMARY KEY(user_id,role_id));
CREATE INDEX ON user_roles(role_id);
INSERT INTO roles(role_name) VALUES ('MANAGER'),('EMPLOYEE'),('ERP');
CREATE TABLE refresh_tokens (
 id UUID PRIMARY KEY, family_id UUID NOT NULL, user_id BIGINT NOT NULL REFERENCES users, token_hash CHAR(64) NOT NULL UNIQUE,
 expires_at TIMESTAMPTZ NOT NULL, consumed_at TIMESTAMPTZ, revoked BOOLEAN NOT NULL DEFAULT false, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ON refresh_tokens(user_id);
CREATE INDEX ON refresh_tokens(family_id);
CREATE TABLE devices (
 id BIGSERIAL PRIMARY KEY, device_code VARCHAR(100) NOT NULL UNIQUE, device_name VARCHAR(255) NOT NULL,
 store_id BIGINT NOT NULL REFERENCES stores, registered_by BIGINT NOT NULL, credential_hash CHAR(64) NOT NULL,
 fcm_token TEXT, last_active_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 FOREIGN KEY(registered_by,store_id) REFERENCES users(id,store_id), UNIQUE(id,store_id)
);
CREATE UNIQUE INDEX ON devices(fcm_token) WHERE fcm_token IS NOT NULL;
CREATE INDEX ON devices(store_id,last_active_at);
CREATE TABLE pda_find_requests (
 id UUID PRIMARY KEY, requester_id BIGINT NOT NULL, store_id BIGINT NOT NULL, device_id BIGINT NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('QUEUED','SENT','RINGING','STOPPED','EXPIRED','FAILED')),
 expires_at TIMESTAMPTZ NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 FOREIGN KEY(requester_id,store_id) REFERENCES users(id,store_id), FOREIGN KEY(device_id,store_id) REFERENCES devices(id,store_id), UNIQUE(id,device_id)
);
CREATE INDEX ON pda_find_requests(store_id,created_at DESC);
CREATE INDEX ON pda_find_requests(device_id);
CREATE UNIQUE INDEX one_active_find ON pda_find_requests(device_id) WHERE status IN ('QUEUED','SENT','RINGING');
CREATE TABLE pda_alert_logs (
 id BIGSERIAL PRIMARY KEY, request_id UUID NOT NULL, device_id BIGINT NOT NULL, event VARCHAR(30) NOT NULL,
 message VARCHAR(500), created_at TIMESTAMPTZ NOT NULL DEFAULT now(), FOREIGN KEY(request_id,device_id) REFERENCES pda_find_requests(id,device_id)
);
CREATE INDEX ON pda_alert_logs(request_id);
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
CREATE TABLE inventory_transactions (
 id BIGSERIAL PRIMARY KEY, product_id BIGINT NOT NULL, store_id BIGINT NOT NULL,
 transaction_type VARCHAR(20) NOT NULL CHECK(transaction_type IN ('ADJUSTMENT','DISPOSAL')),
 qty_before NUMERIC(18,2) NOT NULL CHECK(qty_before >= 0), qty_change NUMERIC(18,2) NOT NULL, qty_after NUMERIC(18,2) NOT NULL CHECK(qty_after >= 0),
 adjustment_id UUID REFERENCES inventory_adjustments, disposal_id UUID REFERENCES disposals, created_by BIGINT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), CHECK(qty_before + qty_change = qty_after),
 CHECK((transaction_type='ADJUSTMENT' AND adjustment_id IS NOT NULL AND disposal_id IS NULL) OR (transaction_type='DISPOSAL' AND disposal_id IS NOT NULL AND adjustment_id IS NULL)),
 FOREIGN KEY(store_id,product_id) REFERENCES inventories(store_id,product_id), FOREIGN KEY(created_by,store_id) REFERENCES users(id,store_id),
 UNIQUE(adjustment_id), UNIQUE(disposal_id,product_id)
);
CREATE INDEX ON inventory_transactions(store_id,id);
CREATE TABLE outbox_events (
 id BIGSERIAL PRIMARY KEY, event_type VARCHAR(30) NOT NULL, aggregate_id VARCHAR(100) NOT NULL, payload JSONB NOT NULL,
 attempts INTEGER NOT NULL DEFAULT 0, available_at TIMESTAMPTZ NOT NULL DEFAULT now(), processed_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX outbox_pending ON outbox_events(available_at,id) WHERE processed_at IS NULL;
CREATE TABLE audit_logs (
 id BIGSERIAL PRIMARY KEY, actor_id BIGINT REFERENCES users, store_id BIGINT REFERENCES stores,
 operation VARCHAR(80) NOT NULL, reference VARCHAR(100) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ON audit_logs(store_id,created_at DESC);
