# 2. Database ERD

```mermaid
erDiagram
 stores ||--o{ users : employs
 users ||--o{ user_roles : assigned
 roles ||--o{ user_roles : grants
 users ||--o{ refresh_tokens : owns
 stores ||--o{ devices : contains
 users ||--o{ devices : registers
 devices ||--o{ pda_find_requests : target
 users ||--o{ pda_find_requests : requests
 pda_find_requests ||--o{ pda_alert_logs : records
 products ||--o{ product_image_sync_logs : synchronizes
 stores ||--o{ inventories : holds
 products ||--o{ inventories : stocked
 inventories ||--o{ inventory_adjustments : adjusted
 inventories ||--o{ inventory_transactions : ledger
 inventory_adjustments o|--|| inventory_transactions : produces
 stores ||--o{ disposals : owns
 users ||--o{ disposals : creates
 disposals ||--|{ disposal_items : contains
 products ||--o{ disposal_items : disposed
 disposals ||--o{ disposal_histories : transitions
 disposals o|--o{ inventory_transactions : deducts
 users ||--o{ audit_logs : acts
 stores ||--o{ audit_logs : scopes
 stores {
  bigint id PK
  varchar store_code UK
 }
 users {
  bigint id PK
  varchar username UK
  bigint store_id FK
  varchar password_hash
  boolean active
 }
 roles {
  bigint id PK
  varchar role_name UK
 }
 user_roles {
  bigint user_id PK,FK
  bigint role_id PK,FK
 }
 refresh_tokens {
  uuid id PK
  uuid family_id
  bigint user_id FK
  char token_hash UK
  timestamptz consumed_at
  timestamptz expires_at
  boolean revoked
 }
 devices {
  bigint id PK
  varchar device_code UK
  bigint store_id FK
  char credential_hash
  text fcm_token UK
 }
 pda_find_requests {
  uuid id PK
  bigint device_id FK
  bigint store_id FK
  bigint requester_id FK
  varchar status
  timestamptz expires_at
 }
 pda_alert_logs {
  bigint id PK
  uuid request_id FK
  bigint device_id FK
  varchar event
 }
 products {
  bigint id PK
  varchar product_code UK
  varchar barcode UK
  varchar image_url
  bigint image_version
 }
 product_image_sync_logs {
  bigint id PK
  bigint product_id FK
  bigint source_version
  varchar sync_status
 }
 inventories {
  bigint id PK
  bigint store_id FK
  bigint product_id FK
  numeric quantity
  bigint version
 }
 inventory_adjustments {
  uuid id PK
  bigint product_id FK
  bigint store_id FK
  numeric old_qty
  numeric new_qty
  bigint expected_version
 }
 inventory_transactions {
  bigint id PK
  bigint product_id FK
  bigint store_id FK
  uuid adjustment_id FK
  uuid disposal_id FK
  numeric qty_before
  numeric qty_change
  numeric qty_after
 }
 disposals {
  uuid id PK
  bigint store_id FK
  bigint version
  varchar status
 }
 disposal_items {
  bigint id PK
  uuid disposal_id FK
  bigint product_id FK
  numeric quantity
 }
 disposal_histories {
  bigint id PK
  uuid disposal_id FK
  varchar old_status
  varchar new_status
 }
 outbox_events {
  bigint id PK
  varchar aggregate_id
  varchar event_type
  jsonb payload
  integer attempts
  timestamptz processed_at
 }
 audit_logs {
  bigint id PK
  bigint actor_id FK
  bigint store_id FK
  varchar operation
 }
```

The authoritative schema is defined by V1 through V6 in `pda-management/src/main/resources/db/migration/`. The former single V1 is retained under `db/legacy` for existing databases; see the backend README for selecting the migration history. All timestamps use UTC-capable `TIMESTAMPTZ`. Quantities use `NUMERIC(18,2)` and Java `BigDecimal`. Composite foreign keys prevent cross-store user/device associations. Stock is unique per `(store_id, product_id)`; disposal products are unique per header. A partial unique index permits only one active finder request per PDA.

The ledger checks `before + change = after`, nonnegative stock and the correct reference type. Completed financial/stock history is retained instead of cascading deletes. The outbox deliberately holds an opaque aggregate ID; business creation and enqueue occur within one transaction. Database numeric constraints complement API precision validation.
