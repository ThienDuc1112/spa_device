# Verification results

## Android direct service calls — 2026-10-03

- Activities/fragments now call Retrofit API clients directly. Removed Android business ViewModels, UseCases and Repository interfaces/implementations; Firebase/polling call AlarmController directly. Home state uses Bundle; UI callbacks wait until the screen is started and are dropped after destruction.
- JDK 21: `:app:assembleDebug :app:testDebugUnitTest :device-android:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintDebug` passed. App: 7 unit tests; device-android: 17 unit tests. Lint: 0 errors, 20 warnings (including hardcoded UI text and layout overdraw).
- Four instrumentation tests passed on the connected emulator: login UI, image navigation/rotation retaining scanned products, work off the main thread with UI callback on main, and dropping callbacks after screen destruction. Used a separate `com.company.pda.refactortest` package and unreachable local API URL; preserved the installed production-package app and its registration. Temporary test packages were removed by the test runner.
- Source structure check passed for 322 paths; local Android documentation links checked. See [direct service code guide](24-android-direct-services.md). Real server login/inventory/disposal mutations and live FCM sound were not exercised by these isolated instrumentation tests.
- Environment notes: native unit tests initially failed because C: had no space for temporary files; rerun passed with process-local temp directory on D:. The broad all-module `assembleDebugAndroidTest` also exposed a pre-existing Kotlin duplicate dependency in scanner-factory's test APK; the app instrumentation APK builds successfully. No scanner dependency changes were made for that unrelated task.

## Device deletion and finder delivery diagnosis — 2026-10-03

- Read-only inspection of the running dev database found the latest web request had QUEUED/STOP_REQUESTED logs, no PUSH_FIND, and unprocessed outbox rows with attempts=0. Dev configuration disabled the scheduler. No real device/request rows were deleted or changed during diagnosis.
- Java 8 Maven `verify`: 12 unit + 36 integration tests passed. New tests cover deletion of only the selected PDA and its finder logs/requests/outbox, retention of other devices and general audit, 409 for an active unexpired request, deletion after expiry with scheduler disabled, 404 for a missing device, rejection of old credentials, reusing asset code/token on registration, runtime configuration and latest delivery log projection.
- Android build/unit tests/lint passed: 7 unit tests, 0 lint errors/16 warnings. Instrumentation APK includes a test that stale credential rejection cannot clear a newer registration; compiled, not run on the emulator in this change.
- React production build and Chrome mocked-API smoke tests passed: scheduler/FCM warnings, Find/Stop, disabled delete during active request, cancel/confirm delete, correct DELETE path, refresh, error recovery and mobile layout. No real DELETE or FCM was sent.
- The FCM PowerShell launcher parsed and rejected Android google-services.json before starting a backend. Successful Admin authentication/live FCM delivery remains unverified without a supplied service-account path. See [deletion and troubleshooting guide](22-delete-device-and-finder-troubleshooting.md).

## React finder website and Android enrollment — 2026-10-03

- Java 8 Maven `verify` passed: 12 unit tests and 33 integration tests. Verified both Flyway histories on isolated PostgreSQL: legacy V1 + V7, and fresh V1–V7. Public web tests cover all stores including empty stores, exclusion of device secrets/tokens, find/outbox/mock push/ACK/stop, nullable requester/system audit, duplicate 409, missing IDs 404, employee enrollment and anonymous enrollment denial. Existing manager store isolation still passes. No application database was migrated during these tests.
- React `npm run build` passed. Chrome headless with mocked API passed grouping/filtering stores, an empty store/system, Find/Stop request paths and button states, lack of Authorization headers, API error/recovery and a mobile viewport; no browser JavaScript errors. This is separate from the backend integration tests, not a live FCM end-to-end test.
- Android `assembleDebug`, `assembleDebugAndroidTest`, `testDebugUnitTest`, `lintDebug` passed after removing remote finder screens and prompting enrollment at Home after login. 7 unit tests passed; lint 0 errors, 16 warnings. Device instrumentation was compiled, not executed in this change; the new registration dialog and actual speaker/FCM still need device acceptance checks.
- Android structure checker passed for 182 required paths; OpenAPI regenerated and local documentation links/whitespace checked. Run instructions and migration details: [React finder guide](21-react-device-finder.md).

## Explicit finder transport, FCM by default — 2026-10-03

With JDK 21 / Gradle 8.11.1, both default FCM and `-PfinderTransport=polling` passed `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`. Each unit-test run passed all 7 tests, including 4 transport tests covering no command HTTP in FCM mode, explicit polling without FCM health, network recovery without changing mode, and propagation of authentication errors. Lint: 0 errors, 18 warnings. An invalid transport value was rejected at Gradle configuration time. Final generated `BuildConfig.FINDER_TRANSPORT` and debug APK use `fcm`.

Instrumentation tests were updated for the two build modes and compiled. Execution on the connected emulator was not completed: installing the polling APK returned `INSTALL_FAILED_UPDATE_INCOMPATIBLE` because the installed app has a different signing key. The existing app/data were preserved; no uninstall was performed. Live FCM delivery, service lifecycle on-device and physical audio remain unverified for this change. No backend logic, dependency versions or database schema changed. Setup: [configured finder transport](16-fcm-to-polling-fallback.md).

## Finder conflict and stale expiry — 2026-10-03

Java 8 `mvn verify`: **BUILD SUCCESS**, 12 unit tests and 30 integration tests passed. New checks cover replacing expired QUEUED/SENT/RINGING requests with the scheduler disabled, store isolation during expiry, expiry audit logs, skipping obsolete outbox messages, and concurrent Find calls producing exactly one request/outbox event. An unexpired duplicate returns a specific HTTP 409 message. Existing database migrations and unique indexes are unchanged; tests used an isolated embedded database.

## Android AAR metadata compatibility — 2026-10-03

Aligned Activity dependencies to 1.10.1, Lifecycle to 2.8.7, and app/instrumentation Compose BOM to 2025.03.00 for compileSdk 35 / AGP 8.9.1. Removed a dangling `SummaryActivity` manifest entry: neither that class nor its title resource exists in the source tree.

With JDK 21 and Gradle 8.11.1, `:app:checkDebugAarMetadata :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest` completed with **BUILD SUCCESSFUL**. Both app and instrumentation AAR metadata checks passed, and debug APKs were generated. Instrumentation tests were compiled, not executed on a device during this check.

## Java 8 / Spring Boot 2.7 / WAR — 2026-10-03

Ran `mvn -f pda-management/pom.xml clean verify` with Oracle JDK 1.8.0_202 and the workspace Maven cache: **BUILD SUCCESS**, 12 unit tests and 26 integration tests passed, with no failures, errors or skips. Integration tests used an isolated embedded PostgreSQL database; no application database was changed.

Additional compatibility checks cover immutable DTO JSON round trips and MapStruct mapping, SHA-256 output, ERP/image HTTP adapters (success, 404, remote error and invalid content type), offline Firebase initialization, and the HTTP problem-response contract. Firebase delivery to an actual device was not exercised.

The generated `pda-management/target/pda-management-1.0.0.war` declares Spring Boot 2.7.0 and `WarLauncher`. Application classes use bytecode version 52 (Java 8); scanning dependency base classes found none requiring a newer JVM, excluding module descriptors and multi-release variants. Tomcat core/websocket are in `WEB-INF/lib-provided`. Docker image build, PostgreSQL 16 deployment and deployment into an external Tomcat were not run.

Dependency changes and run instructions: [Java 8 migration notes](../pda-management/JAVA8_MIGRATION.vi.md).

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
