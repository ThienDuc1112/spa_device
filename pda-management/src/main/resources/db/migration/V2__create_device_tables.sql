CREATE TABLE devices (
 id BIGSERIAL PRIMARY KEY, device_code VARCHAR(100) NOT NULL UNIQUE, device_name VARCHAR(255) NOT NULL,
 store_id BIGINT NOT NULL REFERENCES stores, registered_by BIGINT NOT NULL, credential_hash CHAR(64) NOT NULL,
 fcm_token TEXT, last_active_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 FOREIGN KEY(registered_by,store_id) REFERENCES users(id,store_id), UNIQUE(id,store_id)
);
CREATE UNIQUE INDEX ON devices(fcm_token) WHERE fcm_token IS NOT NULL;
CREATE INDEX ON devices(store_id,last_active_at);
