# Verification results

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
mvn -f backend/pom.xml verify
python tools/check_android_structure.py
cd pda-android
./gradlew assembleDebug assembleRelease testDebugUnitTest lintDebug
./gradlew :app:connectedDebugAndroidTest
./gradlew :device-android:testDebugUnitTest
```

Reports are under `backend/target/surefire-reports`, `backend/target/failsafe-reports`, each Android module's `build/test-results`, `pda-android/app/build/outputs/androidTest-results`, and `pda-android/app/build/reports`. Generated build outputs and caches are excluded from version control.

Not verified here: live FCM delivery (no project credentials provided), real Zebra/Urovo/Honeywell hardware, authenticated app-to-deployed-backend journeys, OEM background/DND policies, ERP-specific adapters, TLS/certificate deployment, release signing, PostgreSQL 16 container startup, load testing or a security audit. The Docker daemon was unavailable; backend tests used isolated native PostgreSQL instead. Follow the deployment guide's device acceptance checks before store rollout.
