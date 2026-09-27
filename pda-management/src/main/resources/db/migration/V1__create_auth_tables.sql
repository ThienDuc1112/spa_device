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

CREATE TABLE audit_logs (
 id BIGSERIAL PRIMARY KEY, actor_id BIGINT REFERENCES users, store_id BIGINT REFERENCES stores,
 operation VARCHAR(80) NOT NULL, reference VARCHAR(100) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ON audit_logs(store_id,created_at DESC);
