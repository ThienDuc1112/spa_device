-- An unauthenticated website request has no employee requester. Keep the store/device FKs.
ALTER TABLE pda_find_requests ALTER COLUMN requester_id DROP NOT NULL;
