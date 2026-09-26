# PDA Android

The app follows the requested feature, scanner and device layout. Open **this directory** in Android Studio. The application ID remains `com.company.pda`. See [STRUCTURE.md](STRUCTURE.md) for the full source tree.

| Module | Responsibility |
|---|---|
| `app` | DI composition, feature screens/ViewModels, domain use cases, repositories, Firebase and foreground-service lifecycle |
| `scanner-api` | Plain Java scanner contracts, providers and capabilities |
| `scanner-zebra` | Zebra DataWedge broadcasts, software trigger and foreground receiver lifecycle |
| `scanner-urovo` | Urovo ScanWedge broadcasts, software trigger and configuration |
| `scanner-generic` | Keyboard input, configurable intents (including Honeywell), and Google code-scanner camera UI |
| `scanner-factory` | Selects an injected scanner provider |
| `device-api` | Plain Java device/audio contracts, policy and results |
| `device-android` | Android playback, alarm volume, audio focus and device information |
| `device-zebra` | Zebra adapter boundary using public Android APIs |
| `device-urovo` | Urovo adapter boundary using public Android APIs |
| `device-factory` | Selects by manufacturer, with Android fallback |

~~~mermaid
flowchart TD
 app --> scannerFactory[scanner-factory]
 scannerFactory --> scannerZebra[scanner-zebra]
 scannerFactory --> scannerUrovo[scanner-urovo]
 scannerFactory --> scannerGeneric[scanner-generic]
 scannerZebra --> scannerGeneric
 scannerUrovo --> scannerGeneric
 scannerGeneric --> scannerApi[scanner-api]
 app --> deviceFactory[device-factory]
 deviceFactory --> deviceZebra[device-zebra]
 deviceFactory --> deviceUrovo[device-urovo]
 deviceFactory --> deviceAndroid[device-android]
 deviceZebra --> deviceAndroid
 deviceUrovo --> deviceAndroid
 deviceAndroid --> deviceApi[device-api]
~~~

Manual dependency injection lives in `di/AppModule`; no Hilt/Dagger setup is required. Domain code has no Android, Retrofit or Room dependencies. Data repositories implement domain interfaces and map transport/cache objects into domain models. Hardware modules never depend on `app`.

`LoginActivity` opens `HomeActivity`. Products, image viewing, inventory and disposal use separate fragments and ViewModels. `HomeViewModel` retains Scan Order state across fragment changes. Finder management has its own activity/ViewModel. Firebase and alarm services live under `infrastructure`.

## Build and test

Use JDK 21 and Android SDK 35. Set `ANDROID_HOME` or an untracked `local.properties`.

~~~sh
./gradlew :app:assembleDebug :app:assembleRelease
./gradlew :app:testDebugUnitTest :scanner-factory:testDebugUnitTest :device-android:testDebugUnitTest :app:lintDebug
# With an emulator or test device connected:
./gradlew :app:connectedDebugAndroidTest
~~~

On Windows, use `gradlew.bat`. Release APKs are unsigned until enterprise signing is configured.

Set your HTTPS origin, including a trailing slash:

~~~sh
./gradlew :app:assembleDebug -PapiUrl=https://pda.example.com/
~~~

Put Firebase `google-services.json` in `app/` for live push delivery. Compilation and local UI tests do not require Firebase credentials.

## Device integrations

Giải thích bằng tiếng Việt: [Luồng scanner, vòng đời receiver và vai trò các module](../docs/13-scanner-flow.md).

Chức năng tìm PDA: [Luồng gửi lệnh, phát chuông và báo trạng thái (tiếng Việt)](../docs/14-device-finder-flow.md).

Choose scanner type in **Scanner settings**, then **Use selected scanner defaults**, adjust values to match the device's wedge profile and save. Zebra, Urovo, Honeywell and other configurable intent scanners all use the shared `BarcodeReceiver`. See [broadcast scanner setup and device checks](scanner-generic/README.md) and [Zebra DataWedge setup](scanner-zebra/README.md).

Choose **CAMERA**, then **Trigger configured scanner** for the Google scanner UI. Compatible Google Play services are required; initial module download may need connectivity. Results are retained while the external scanner activity pauses the app. See [Google code scanner documentation](https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner).

The bundled `app/src/main/res/raw/pda_alarm.mp3` is an original synthesized two-tone alert. `PdaAlarmService` owns notifications/deadlines; the selected adapter handles playback, focus and volume restoration. Vendor adapters use standard Android audio behavior, including OS/DND restrictions.

Open **Finder sound settings** on each target PDA to configure DND alarm access and run the local speaker test. Silent/vibrate use the alarm stream; DND that blocks alarms returns a failure. See [finder audio setup and acceptance checks](../docs/12-finder-audio.md).

## Upgrade compatibility

Application ID, `retail.db` schema/version, encrypted preferences, Keystore alias and scanner settings remain unchanged. `FcmWorkerFactory` maps old `com.company.pda.data.SyncWorker` jobs to the moved worker. The old aggregate activity, ViewModel, API, repository and model classes were replaced rather than duplicated.

See the workspace [deployment guide](../docs/11-deployment.md) and [verification results](../docs/verification.md).
