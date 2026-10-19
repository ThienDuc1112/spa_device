-- Oracle Database 19c; use this migration history only with a new Oracle schema.
-- An unauthenticated website request has no employee requester. Keep the store/device FKs.
ALTER TABLE pda_find_requests MODIFY (requester_id NULL);
