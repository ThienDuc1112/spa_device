# Verification results

## User registration and development seed — 2026-09-26

Ran Maven `verify` on Java 21: **BUILD SUCCESS**, 6 unit tests and 21 integration tests passed, with no failures, errors or skips. The added checks cover manager-only employee registration, anonymous/employee denial, store and role isolation, duplicate usernames, password validation (including BCrypt UTF-8 length), password hashing and new-account login. Development seed checks verify repeat execution preserves existing credentials, inventory and disposal state; profile checks exclude production, including simultaneous `dev,prod` activation.

The OpenAPI contract now includes `POST /auth/register` with bearer authentication and HTTP 201. Seed was exercised only against the temporary test database, not a running application database. No Android registration screen was added.

## Backend restructure — 2026-09-26

Verified the `pda-management` layout against the supplied folder specification: all 191 required file/directory paths exist. Removed the four empty legacy package trees outside `com/company/pda`. Java package declarations and MyBatis XML namespaces match their source paths.

Ran `mvn -f pda-management/pom.xml verify` with Java 21 and the local dependency cache: **BUILD SUCCESS**, 5 unit tests and 17 integration tests passed, with no failures, errors or skips. The integration suite started an isolated PostgreSQL 14.22 instance, applied all six feature migrations (V1–V6), and exercised the configured JWT filter and existing business endpoints.

The old `V1__retail_schema.sql` is preserved byte-for-byte under `db/legacy` (SHA-256 `CFF8D9A32523F8BECB43A17C4D7CA635BB51C1619CAA87C3C307EDCBBFC51134`). The legacy deployment selection is documented in the backend README. Live ERP/image services, live FCM and Android were not re-tested during this backend restructure.

## Earlier full-system verification

Executed in this workspace on 2026-09-24.

| Check | Result |
|---|---|
| Java 21 backend compilation and executable JAR packaging | Passed |
| Inventory unit tests | 5 passed; zero failures/errors/skips |
| Real PostgreSQL integration tests | 17 passed; zero failures/errors/skips |
| Flyway migration against an empty PostgreSQL 14.22 database | Passed as part of integration tests |
| Android debug APK | Built successfully |
| Minified Android release APK | Built successfully; unsigned |
| Android JVM unit tests | 17 audio tests passed; 3 app tests passed; scanner-factory's 1 test passed during the prior structure verification |
| Android requested folder structure | All 185 requested paths present; package and dependency-boundary checks passed |
| Android debug lint | Passed: 0 errors, 29 warnings |
| Release vital lint / R8 shrinking | Passed |
| OpenAPI structural checks | JSON parses; 19 operations; all schema references resolve |
| Android emulator instrumentation | 3 finder sound tests passed; 2 navigation tests also passed in the preceding full run |

Backend integration tests exercise JWT access/refresh separation, refresh rotation/reuse revocation, disabled accounts, permissions, cross-store isolation, optimistic version checks, idempotent inventory adjustments, negative/over-precision quantities, required version fields, disposal rollback across multiple products, repeated/concurrent confirmation, cancellation invariants, finder store isolation, device authentication, token refresh, invalid-token removal, delivery retries, expiry, successful acknowledgements, remote stop and image source-version ordering/HTTPS validation.

The reorganized Android project has 11 Gradle modules under `pda-android`. The finder update adds policy, playback-health, volume/mute restoration, process-restart recovery, and concurrent-session tests. Robolectric tests run against API 28; emulator tests run on the existing Pixel_10a profile (API 37), headlessly with `-read-only -no-snapshot`. Finder instrumentation verifies playback start/stop, restoration when leaving the screen, and DND total silence rejecting playback without increasing volume. A routing initialization failure found by the first emulator run was fixed and all three finder tests passed on rerun. These software tests do not establish physical speaker audibility. The emulator is disposable and is shut down without saving changes. Backend results above are from the earlier implementation verification; the backend was unchanged by these Android changes.

Robolectric's runtime downloader stalled while fetching its Android 15 resource runtime. The runtime was downloaded directly from Maven Central, SHA-512 verified, and cached before the successful test run. The test repository is explicitly set to Maven Central. See [finder audio setup and device acceptance](12-finder-audio.md).

Lint warnings include pinned dependency update notices, hardcoded UI text/localization, and synchronous encrypted-preference persistence. Tokens are committed synchronously so a process exit cannot silently lose a freshly rotated refresh token; token-refresh network calls do not hold the secure-store lock. These warnings are recorded, not hidden with a baseline.

Commands used (with local JDK/SDK and dependency-cache paths configured):

```sh
mvn -f pda-management/pom.xml verify
python tools/check_android_structure.py
cd pda-android
./gradlew assembleDebug assembleRelease testDebugUnitTest lintDebug
./gradlew :app:connectedDebugAndroidTest
./gradlew :device-android:testDebugUnitTest
```

Reports are under `pda-management/target/surefire-reports`, `pda-management/target/failsafe-reports`, each Android module's `build/test-results`, `pda-android/app/build/outputs/androidTest-results`, and `pda-android/app/build/reports`. Generated build outputs and caches are excluded from version control.

Not verified here: live FCM delivery (no project credentials provided), real Zebra/Urovo/Honeywell hardware, authenticated app-to-deployed-backend journeys, OEM background/DND policies, ERP-specific adapters, TLS/certificate deployment, release signing, PostgreSQL 16 container startup, load testing or a security audit. The Docker daemon was unavailable; backend tests used isolated native PostgreSQL instead. Follow the deployment guide's device acceptance checks before store rollout.
