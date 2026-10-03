# 7. API contracts

All paths are relative to the HTTPS API origin. Authenticated employee calls send `Authorization: Bearer <accessToken>`. Store scope comes from the user record. Device calls send both `X-Device-Id` and `X-Device-Secret`, with no employee bearer token. Body validation failures return 400, authentication 401, permissions 403, missing/store-invisible resources 404, stale versions or invalid state transitions 409. Business errors use RFC Problem Details; infrastructure errors return an opaque reference. Spring Security may return an empty 401/403 response at the filter boundary.

| Method | Path | Permission | Body / response |
|---|---|---|---|
| POST | `/auth/register` | MANAGER | RegisterUser -> RegisteredUser, HTTP 201 |
| POST | `/auth/login` | Public, rate-limited at ingress | Login → Tokens |
| POST | `/auth/refresh` | Refresh JWT | RefreshBody → Tokens |
| GET | `/devices` | MANAGER | Device summary array, maximum 500 |
| POST | `/devices/register` | MANAGER | Register → Registration |
| PUT | `/devices/token` | Device credential | Token → empty 200 |
| POST | `/pda/find` | MANAGER | Find → FindRequest |
| GET | `/pda/find/{id}` | MANAGER | FindRequest |
| POST | `/pda/stop` | MANAGER | Stop → empty 200 |
| POST | `/pda/events` | Target device credential | AlertEvent → empty 200 |
| GET | `/products/barcode/{barcode}` | Signed-in active user | ProductDto |
| PUT | `/products/image-sync` | ERP | ImageSync → empty 200 |
| GET | `/inventories/{productCode}` | Signed-in active user | Inventory |
| POST | `/inventory-adjustments` | MANAGER | Adjustment → latest Inventory |
| GET | `/inventory-transactions?afterId=0` | MANAGER / ERP | Up to 200 immutable rows, ID ascending |
| GET | `/disposals?page=0` | Signed-in active user | Up to 50 headers, newest first |
| GET | `/disposals/{id}` | Signed-in active user | `{disposal, items, history}` |
| POST | `/disposals` | MANAGER / EMPLOYEE | DisposalCreate → Disposal |
| POST | `/disposals/{id}/confirm` | MANAGER | Transition → Disposal |
| POST | `/disposals/{id}/cancel` | MANAGER | Transition → Disposal |

## Authentication

Managers can create employees with `POST /auth/register` and a bearer access token:

```json
{"username":"employee01","password":"Employee-demo-2026!","fullName":"New Employee","email":"employee01@example.test"}
```

Returns HTTP 201 with `{id, username, storeId, role}`. The role is always `EMPLOYEE`; store ID comes from the manager, not the request. Username is unique, 3–100 ASCII letters/digits/dot/underscore/hyphen, starting with a letter or digit. Password is at least 12 characters and at most 72 UTF-8 bytes. Full name is required; email is optional. Anonymous requests return 401, non-managers 403, invalid data 400 and duplicate usernames 409. No credentials are returned. The new employee signs in using the login endpoint.

```json
{"username":"admin","password":"your-provisioned-password"}
```

```json
{"accessToken":"<15-minute JWT>","refreshToken":"<14-day JWT>","expiresIn":900}
```

Refresh with `{"refreshToken":"<current refresh JWT>"}`. Each refresh is single use and creates a new pair. Reuse revokes the entire refresh family and requires login. Both token types validate issuer and expiry; access tokens also validate the API audience/type. Server storage contains hashes of refresh tokens. Android serializes refresh requests to avoid racing its own rotations. An ambiguous failed refresh response may require signing in again.

## Device and finder

```json
{"deviceCode":"PDA-001","deviceName":"Receiving PDA","fcmToken":"<Firebase registration token>"}
```

Registration returns `{"deviceId":1,"deviceSecret":"<one-time random secret>"}`. Device code is unique, max 100 characters; name max 255; push token optional (polling-only registration), max 4096. Store is derived from the registering manager. Persist the device secret immediately. Updating `/devices/token` sends `{"fcmToken":"<latest token>"}` and refreshes the last-active timestamp. Last activity is observational, not proof of current reachability.

Find: `{"deviceId":1}` →

```json
{"id":"f827e8d8-f9a7-45ca-9a54-dc7235fe26fa","requesterId":1,"storeId":1,"deviceId":1,"status":"QUEUED","expiresAt":"2026-09-24T12:00:00Z"}
```

Stop: `{"requestId":"f827e8d8-f9a7-45ca-9a54-dc7235fe26fa"}`.
Event: `{"requestId":"f827e8d8-f9a7-45ca-9a54-dc7235fe26fa","status":"RINGING"}`. Allowed device statuses: RINGING, STOPPED, FAILED. Terminal requests cannot return to an active state. Timeout is server-configurable and clamped to 10–300 seconds. Missing/invalid push tokens and exhausted push retries leave the request available to HTTP polling until expiry. GET /pda/commands authenticates X-Device-Id and X-Device-Secret and returns unexpired commands for that device only; see [polling](15-finder-polling.md). An offline device can remain SENT without RINGING and then expire.

`GET /pda/fcm-health` uses `X-Device-Id` and `X-Device-Secret` and returns `{"fallbackRequired":false,"reason":"READY"}` with `Cache-Control: no-store`. Other reasons are `FCM_DISABLED`, `NO_TOKEN`, and `PUSH_FAILED`. State is scoped to the authenticated device/store; READY means no known transport failure, not proof of delivery. Android checks health every 15 seconds and fetches `/pda/commands` only when a local or backend FCM failure is known.

## Products and image publication

`GET /products/barcode/8850000000012` →

```json
{"barcode":"8850000000012","productCode":"SKU-001","productName":"Mineral Water","imageUrl":"https://images.example.com/SKU-001/v4.jpg"}
```

`imageUrl` can be null. An inactive or unknown barcode returns 404.

```json
{"productCode":"SKU-001","imageUrl":"https://images.example.com/SKU-001/v4.jpg","sourceVersion":4}
```

Image publication requires a pre-imported active product, a positive monotonically increasing sourceVersion and HTTPS URL (max 1000), or null to remove the image. Stale messages succeed without overwriting newer data; both outcomes are logged. The ERP owns image upload, validation and availability before publishing the URL. Product master import is an external ERP provisioning step; no proprietary ERP connector is fabricated here.

## Inventory

```json
{"id":1,"productId":1,"storeId":1,"quantity":12.00,"version":3}
```

```json
{"requestId":"fe033e89-9840-4b61-b592-e70243de7973","productCode":"SKU-001","quantity":10.00,"version":3,"reason":"Cycle count"}
```

Quantity is an absolute new count, nonnegative, at most 16 integer and 2 fractional digits. Version is required and must be nonnegative, and reason must contain 1–1000 characters. Reuse the identical request ID/body on transport retries. Changed content with the same ID returns 409. The response returns current inventory, which may include subsequent changes. Reload after a stale-version conflict; do not blindly overwrite with a new version.

ERP consumers poll the ledger by `afterId`, apply transactions idempotently using `(store_id,id)`, and advance their checkpoint only after their local transaction commits. This is pull-based inventory synchronization. IDs can have gaps. Only the ERP service account's own store is visible.

## Disposal

```json
{"requestId":"468f6138-984b-478a-ad5d-4e582e79e654","remarks":"Damaged in transit","items":[{"productCode":"SKU-001","quantity":2.00,"reason":"Broken packaging"}]}
```

Disposals contain 1–100 distinct products, strictly positive quantities with 2 decimal places, required per-item reasons up to 500 characters and remarks up to 1000. Creation is idempotent for the same request ID/body. Confirmation/cancellation body: `{"version":0}`. Confirmation atomically deducts every item; a failed line rolls back all deductions. Repeating the same terminal operation succeeds without repeating stock effects. A confirmed disposal cannot be cancelled; corrections require an independently audited adjustment. The Android creation dialog creates a single selected-product line; the API supports multi-line submissions.

Response example:

```json
{"id":"468f6138-984b-478a-ad5d-4e582e79e654","storeId":1,"status":"CONFIRMED","remarks":"Damaged in transit","createdBy":1,"version":1,"createdAt":"2026-09-24T12:00:00Z"}
```

## Source of truth

Validated DTO definitions: `pda-management/src/main/java/com/company/pda/presentation/rest/*/dto/`.
The OpenAPI document is `docs/openapi.json`. Never log passwords, authorization headers, refresh tokens, device secrets or FCM tokens.
