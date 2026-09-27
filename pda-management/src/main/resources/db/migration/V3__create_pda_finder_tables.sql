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

CREATE TABLE outbox_events (
 id BIGSERIAL PRIMARY KEY, event_type VARCHAR(30) NOT NULL, aggregate_id VARCHAR(100) NOT NULL, payload JSONB NOT NULL,
 attempts INTEGER NOT NULL DEFAULT 0, available_at TIMESTAMPTZ NOT NULL DEFAULT now(), processed_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX outbox_pending ON outbox_events(available_at,id) WHERE processed_at IS NULL;
