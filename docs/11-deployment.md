# 11. Deployment guide

## Build and verify

Prerequisites: JDK 8 and Maven 3.9+ for the backend; JDK 21 for Android, Android SDK 35 and an installed/licensed SDK toolchain. The Gradle 8.11.1 wrapper is included. Backend integration tests start an isolated real PostgreSQL 14 instance using [Zonky Embedded Postgres](https://github.com/zonkyio/embedded-postgres), including on Windows; no Docker daemon is required for the test suite. Run tests as a non-root user on Linux. The application deployment uses PostgreSQL 16.

```sh
# JAVA_HOME must point to JDK 8 for Maven
mvn -f pda-management/pom.xml verify
# Switch JAVA_HOME to JDK 21 for Gradle
cd pda-android
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Windows: use `gradlew.bat`; set JAVA_HOME to JDK 21 and ANDROID_HOME to your SDK, or set `sdk.dir` in an untracked `pda-android/local.properties`. Build the Android app with your HTTPS origin, including a trailing slash:

```sh
./gradlew assembleDebug -PapiUrl=https://pda.your-company.example/
```

Outputs: `pda-management/target/pda-management-1.0.0.war` and `pda-android/app/build/outputs/apk/debug/app-debug.apk`. Firebase configuration is optional for compilation, so CI does not need secrets. It is required for push functionality. Release APKs need your enterprise signing configuration; no private signing key is committed.

## First backend installation

Fresh databases use the six migrations in `db/migration`. For an existing database that already applied `V1__retail_schema.sql`, set `SPRING_FLYWAY_LOCATIONS=classpath:db/legacy` in `.env` before starting this version. The legacy directory contains the unchanged original migration. Do not combine the two locations or remove Flyway history to bypass validation.

1. Copy `.env.example` to `.env`. Set a random database password and JWT secret. Generate the latter from 32 or more random bytes and Base64 encode it (e.g. `openssl rand -base64 32`); keep it in your secret manager. Do not reuse the test-only all-zero key.
2. For initial provisioning only, set `APP_BOOTSTRAP_ENABLED=true` and a random `BOOTSTRAP_PASSWORD` of at least 16 characters. Bootstrap creates STORE-001 and the `admin` manager only when the user table is empty. It never overwrites existing users.
3. Run `docker compose up --build -d`. Flyway installs the schema. Check `http://localhost:8080/actuator/health`. The backend port is bound to localhost and the database port is not exposed.
4. Sign in as admin. Set bootstrap false and remove its password from the deployment environment; recreate the backend service. Provision further stores, users and roles through your controlled identity/ERP administration process, using BCrypt cost 12 password hashes.
5. Place a TLS reverse proxy in front of localhost:8080. `deploy/nginx.conf` provides TLS and login/API rate limits for a host-installed Nginx. Replace its hostname and certificate paths. Do not expose an unthrottled direct backend port. If Nginx runs in a container, change its upstream to `backend:8080` on the private Compose network.

Production environments should separate the migration owner from the runtime database role. Grant runtime SELECT/INSERT/UPDATE only on the necessary business tables and sequences; remove DELETE/UPDATE on immutable audit/history/ledger tables. Run migrations as a separate deployment job before disabling Flyway in runtime (`SPRING_FLYWAY_ENABLED=false`). Back up the database with tested restores and protect audit exports from application writers.

## Firebase and managed PDAs

Finder also supports HTTP polling without Firebase. For background/locked-screen operation, provision the managed-device power policy and start the persistent service from Home as described in [finder polling](15-finder-polling.md). The Firebase steps below configure the additional push transport.

1. Create/register Android package `com.company.pda` in your Firebase project. Put its client `google-services.json` in `pda-android/app/`; it is excluded from version control. Enable FCM HTTP v1.
2. Enable `FCM_ENABLED=true` on the backend. Prefer workload identity/application default credentials with only messaging permission. For local credentials, mount the service-account JSON read-only and set `GOOGLE_APPLICATION_CREDENTIALS` to its container path. Do not put JSON private keys in the repository or APK. The base Compose file does not mount any credential automatically.
3. Install the APK on a Google Play services-enabled PDA, allow notifications and launch once. Force-stopped apps cannot receive normal FCM until relaunched. Validate OEM battery/device policy behavior in your fleet.
4. A manager signs in and selects **Register this PDA** once using a unique asset code. The one-time device secret is stored encrypted on that installation. A fresh installation has no device secret; re-enrollment requires revoking/replacing the old device registration through the operator's database administration procedure. Never assign one token to multiple device records.
5. Configure **Finder sound settings** on the target PDA and run its speaker test in silent/vibrate and the store's usual DND mode. Follow the [finder audio acceptance checks](12-finder-audio.md). Open **PDA management** on another signed-in manager device. Select the target, then refresh status. Observe QUEUED → SENT → RINGING (software playback checks passed; physical audibility must be tested). Check local Stop, remote Stop, and timeout; the previous alarm volume/mute state is restored during normal service cleanup. DND that blocks alarms must result in FAILED.

FCM is an external delivery channel and cannot guarantee delivery to disconnected or force-stopped devices. Android policy can prevent background service starts, notifications or volume changes. The app reports failures and uses notification presentation rather than bypassing OS rules. Test these states before an operational rollout.

## Scanner setup

The default flow accepts keyboard-wedge scans terminated by Enter, as well as manual barcode entry. This works without vendor SDK redistribution. Intent adapters exist for Zebra DataWedge, Urovo and Honeywell profile-based output; they share a configurable receiver and are active only while the screen is resumed.

Configure the manufacturer's scanner tool to broadcast an explicit action/payload key to this package. Set the matching `ScannerConfig` through **Scanner settings** in the app. For Zebra, a typical DataWedge payload key is `com.symbol.datawedge.data_string`; configure an app-specific action such as `com.company.pda.SCAN`. Urovo/Honeywell keys and sender permissions vary with firmware: use the actual profile settings, not invented constants. If supported, require a vendor-defined sender permission to restrict broadcast injection. Every scanned string is untrusted input, and scans only trigger product lookup, never stock mutations.

These are real intent adapters; proprietary EMDK/Urovo/Honeywell SDK binaries are not included. Devices requiring SDK-only scanning need the licensed vendor SDK implementation behind ScannerManager. Confirm firmware capabilities before choosing that deployment path.

## ERP and image/inventory synchronization

Import master products and initial store inventories through your controlled ERP ingestion process, preserving product-code/barcode uniqueness and recording opening stock according to your accounting policy. The application intentionally does not invent credentials, endpoints or opening balances for your ERP.

Publish an immutable image object to your HTTPS image server/CDN. After successful publication, use an ERP-role account to call `/products/image-sync` with its source version. Use publicly readable product images or a CDN access policy suitable for Glide; image access does not reuse the backend JWT. Avoid expiring signed URLs unless your publisher refreshes them before expiry.

For inventory synchronization, poll `/inventory-transactions?afterId=<checkpoint>` as a per-store ERP account. Apply each record once and transactionally advance the ERP checkpoint. Poll until fewer than 200 rows remain. Preserve the store writer lock protocol if you add future inventory write paths, or replace cursor polling with a database CDC connector.

## Operations

Monitor database availability, HTTP error rates/latency, outbox backlog age, invalid-token events, finder expiry rate and refresh-reuse failures. An expired finder does not mean the physical device is lost; it means no successful completion was observed before its deadline. Outbox attempts are durable and may be replayed, so never remove request-ID deduplication in the Android service.

Readiness uses Actuator health. Deploy multiple backend instances behind the proxy; PostgreSQL outbox row locks prevent simultaneous processing of one event. A send can succeed before a worker transaction fails, hence Android deduplication is mandatory. Keep device/server clocks synchronized. Rotate JWT keys by deploying a coordinated login reset with this symmetric-key configuration; use asymmetric rotating keys for larger multi-service installations.

Do not log request bodies, authorization headers, device secrets or Firebase credentials. API access logs include method, route family, status, duration and a generated request ID. Business changes have database audit records and inventory/disposal histories. Retain these according to store policy; archive before implementing cleanup.

## Device acceptance checks

Run on each supported OS/OEM combination: foreground/background/locked-screen finder, high-priority downgrade, offline expiry, duplicate delivery, STOP arriving before FIND, notification denial, DND policy, token refresh, app process death, timeout volume restoration, scan after rotation, cached image offline, failed image placeholder, pinch zoom, stale inventory conflict, network loss after save, multi-item disposal rollback and repeated confirmation. The backend tests mock the FCM gateway; they do not prove physical push delivery.
