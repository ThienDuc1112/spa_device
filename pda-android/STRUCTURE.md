# Android source structure

Generated from the reorganized project. Build outputs and local configuration are omitted.

```text
pda-android/
|-- app/
|   |-- src/
|   |   |-- androidTest/
|   |   |   `-- java/
|   |   |       `-- com/
|   |   |           `-- company/
|   |   |               `-- pda/
|   |   |                   |-- FinderSoundTest.java
|   |   |                   `-- NavigationTest.java
|   |   |-- main/
|   |   |   |-- java/
|   |   |   |   `-- com/
|   |   |   |       `-- company/
|   |   |   |           `-- pda/
|   |   |   |               |-- common/
|   |   |   |               |   |-- exception/
|   |   |   |               |   |   `-- ApiException.java
|   |   |   |               |   |-- logging/
|   |   |   |               |   |   `-- AppLogger.java
|   |   |   |               |   |-- result/
|   |   |   |               |   |   `-- Result.java
|   |   |   |               |   `-- util/
|   |   |   |               |       |-- AlarmAwareActivity.java
|   |   |   |               |       |-- ApiCalls.java
|   |   |   |               |       |-- AsyncViewModel.java
|   |   |   |               |       |-- ScreenSupport.java
|   |   |   |               |       `-- ScreenViews.java
|   |   |   |               |-- data/
|   |   |   |               |   |-- local/
|   |   |   |               |   |   |-- dao/
|   |   |   |               |   |   |   |-- PendingEventDao.java
|   |   |   |               |   |   |   `-- ProductDao.java
|   |   |   |               |   |   |-- entity/
|   |   |   |               |   |   |   |-- PendingEvent.java
|   |   |   |               |   |   |   `-- ProductCache.java
|   |   |   |               |   |   |-- preferences/
|   |   |   |               |   |   |   `-- TokenStorage.java
|   |   |   |               |   |   `-- AppDatabase.java
|   |   |   |               |   |-- mapper/
|   |   |   |               |   |   |-- InventoryMapper.java
|   |   |   |               |   |   `-- ProductMapper.java
|   |   |   |               |   |-- remote/
|   |   |   |               |   |   |-- api/
|   |   |   |               |   |   |   |-- AuthApi.java
|   |   |   |               |   |   |   |-- DisposalApi.java
|   |   |   |               |   |   |   |-- InventoryApi.java
|   |   |   |               |   |   |   |-- PdaFinderApi.java
|   |   |   |               |   |   |   `-- ProductApi.java
|   |   |   |               |   |   |-- dto/
|   |   |   |               |   |   |   |-- auth/
|   |   |   |               |   |   |   |   `-- AuthDto.java
|   |   |   |               |   |   |   |-- disposal/
|   |   |   |               |   |   |   |   `-- DisposalDto.java
|   |   |   |               |   |   |   |-- inventory/
|   |   |   |               |   |   |   |   `-- InventoryDto.java
|   |   |   |               |   |   |   |-- pdafinder/
|   |   |   |               |   |   |   |   `-- PdaFinderDto.java
|   |   |   |               |   |   |   `-- product/
|   |   |   |               |   |   |       `-- ProductDto.java
|   |   |   |               |   |   `-- interceptor/
|   |   |   |               |   |       |-- AuthInterceptor.java
|   |   |   |               |   |       `-- TokenAuthenticator.java
|   |   |   |               |   `-- repository/
|   |   |   |               |       |-- AuthRepositoryImpl.java
|   |   |   |               |       |-- DisposalRepositoryImpl.java
|   |   |   |               |       |-- InventoryRepositoryImpl.java
|   |   |   |               |       |-- PdaFinderRepositoryImpl.java
|   |   |   |               |       |-- ProductRepositoryImpl.java
|   |   |   |               |       `-- ScannerRepositoryImpl.java
|   |   |   |               |-- di/
|   |   |   |               |   |-- AppModule.java
|   |   |   |               |   |-- DatabaseModule.java
|   |   |   |               |   |-- DeviceModule.java
|   |   |   |               |   |-- NetworkModule.java
|   |   |   |               |   `-- ScannerModule.java
|   |   |   |               |-- domain/
|   |   |   |               |   |-- model/
|   |   |   |               |   |   |-- Device.java
|   |   |   |               |   |   |-- Disposal.java
|   |   |   |               |   |   |-- DisposalDetail.java
|   |   |   |               |   |   |-- DisposalItem.java
|   |   |   |               |   |   |-- Inventory.java
|   |   |   |               |   |   |-- PdaFindRequest.java
|   |   |   |               |   |   |-- Product.java
|   |   |   |               |   |   `-- ScanResult.java
|   |   |   |               |   |-- repository/
|   |   |   |               |   |   |-- AuthRepository.java
|   |   |   |               |   |   |-- DisposalRepository.java
|   |   |   |               |   |   |-- InventoryRepository.java
|   |   |   |               |   |   |-- PdaFinderRepository.java
|   |   |   |               |   |   `-- ProductRepository.java
|   |   |   |               |   |-- scanner/
|   |   |   |               |   |   `-- ScannerRepository.java
|   |   |   |               |   `-- usecase/
|   |   |   |               |       |-- AdjustInventoryUseCase.java
|   |   |   |               |       |-- ConfirmDisposalUseCase.java
|   |   |   |               |       |-- GetProductUseCase.java
|   |   |   |               |       |-- LoginUseCase.java
|   |   |   |               |       |-- ScanBarcodeUseCase.java
|   |   |   |               |       |-- StartPdaAlarmUseCase.java
|   |   |   |               |       `-- StopPdaAlarmUseCase.java
|   |   |   |               |-- infrastructure/
|   |   |   |               |   |-- alarm/
|   |   |   |               |   |   |-- AlarmController.java
|   |   |   |               |   |   |-- AlarmTimeoutManager.java
|   |   |   |               |   |   `-- PdaAlarmService.java
|   |   |   |               |   |-- device/
|   |   |   |               |   |   `-- DeviceManager.java
|   |   |   |               |   `-- firebase/
|   |   |   |               |       |-- FcmTokenManager.java
|   |   |   |               |       |-- FcmWorkerFactory.java
|   |   |   |               |       |-- PdaFirebaseMessagingService.java
|   |   |   |               |       `-- SyncWorker.java
|   |   |   |               |-- presentation/
|   |   |   |               |   |-- auth/
|   |   |   |               |   |   |-- LoginActivity.java
|   |   |   |               |   |   |-- LoginUiState.java
|   |   |   |               |   |   `-- LoginViewModel.java
|   |   |   |               |   |-- disposal/
|   |   |   |               |   |   |-- DisposalFragment.java
|   |   |   |               |   |   |-- DisposalUiState.java
|   |   |   |               |   |   `-- DisposalViewModel.java
|   |   |   |               |   |-- home/
|   |   |   |               |   |   |-- HomeActivity.java
|   |   |   |               |   |   `-- HomeViewModel.java
|   |   |   |               |   |-- inventory/
|   |   |   |               |   |   |-- InventoryFragment.java
|   |   |   |               |   |   |-- InventoryUiState.java
|   |   |   |               |   |   `-- InventoryViewModel.java
|   |   |   |               |   |-- pdafinder/
|   |   |   |               |   |   |-- FinderSoundActivity.java
|   |   |   |               |   |   |-- PdaFinderActivity.java
|   |   |   |               |   |   |-- PdaFinderUiState.java
|   |   |   |               |   |   `-- PdaFinderViewModel.java
|   |   |   |               |   |-- product/
|   |   |   |               |   |   |-- ProductFragment.java
|   |   |   |               |   |   |-- ProductImageViewerFragment.java
|   |   |   |               |   |   |-- ProductUiState.java
|   |   |   |               |   |   |-- ProductViewModel.java
|   |   |   |               |   |   `-- ZoomImageView.java
|   |   |   |               |   `-- scanner/
|   |   |   |               |       |-- ScannerUiState.java
|   |   |   |               |       `-- ScannerViewModel.java
|   |   |   |               `-- PdaApplication.java
|   |   |   |-- res/
|   |   |   |   |-- drawable/
|   |   |   |   |   `-- product_placeholder.xml
|   |   |   |   |-- layout/
|   |   |   |   |   |-- activity_home.xml
|   |   |   |   |   |-- activity_login.xml
|   |   |   |   |   `-- activity_pda_finder.xml
|   |   |   |   |-- raw/
|   |   |   |   |   `-- pda_alarm.mp3
|   |   |   |   |-- values/
|   |   |   |   |   |-- ids.xml
|   |   |   |   |   `-- styles.xml
|   |   |   |   `-- xml/
|   |   |   |       `-- data_extraction_rules.xml
|   |   |   `-- AndroidManifest.xml
|   |   `-- test/
|   |       `-- java/
|   |           `-- com/
|   |               `-- company/
|   |                   `-- pda/
|   |                       `-- domain/
|   |                           `-- ScanBarcodeUseCaseTest.java
|   |-- build.gradle
|   `-- proguard-rules.pro
|-- device-android/
|   |-- src/
|   |   |-- main/
|   |   |   |-- java/
|   |   |   |   `-- com/
|   |   |   |       `-- company/
|   |   |   |           `-- device/
|   |   |   |               `-- android/
|   |   |   |                   |-- AlarmAudioPolicy.java
|   |   |   |                   |-- AndroidAlarmAdapter.java
|   |   |   |                   |-- AndroidAlarmPlayer.java
|   |   |   |                   |-- AndroidAudioModeController.java
|   |   |   |                   |-- AndroidDeviceInfoProvider.java
|   |   |   |                   `-- AndroidVolumeController.java
|   |   |   `-- AndroidManifest.xml
|   |   `-- test/
|   |       `-- java/
|   |           `-- com/
|   |               `-- company/
|   |                   `-- device/
|   |                       `-- android/
|   |                           |-- AlarmAudioPolicyTest.java
|   |                           |-- AlarmHealthTest.java
|   |                           |-- AndroidAlarmAdapterTest.java
|   |                           |-- AndroidAudioModeControllerTest.java
|   |                           `-- AndroidVolumeControllerTest.java
|   `-- build.gradle
|-- device-api/
|   |-- src/
|   |   `-- main/
|   |       `-- java/
|   |           `-- com/
|   |               `-- company/
|   |                   `-- device/
|   |                       `-- api/
|   |                           |-- AlarmPlayer.java
|   |                           |-- AlarmPolicy.java
|   |                           |-- AlarmResult.java
|   |                           |-- AlarmStatus.java
|   |                           |-- AudioModeController.java
|   |                           |-- DeviceAlarmAdapter.java
|   |                           |-- DeviceCapability.java
|   |                           |-- DeviceInfo.java
|   |                           |-- DeviceInfoProvider.java
|   |                           `-- VolumeController.java
|   `-- build.gradle
|-- device-factory/
|   |-- src/
|   |   `-- main/
|   |       |-- java/
|   |       |   `-- com/
|   |       |       `-- company/
|   |       |           `-- device/
|   |       |               `-- factory/
|   |       |                   |-- DeviceAudioSetup.java
|   |       |                   |-- DeviceAlarmFactory.java
|   |       |                   `-- DeviceAlarmProvider.java
|   |       `-- AndroidManifest.xml
|   `-- build.gradle
|-- device-urovo/
|   |-- src/
|   |   `-- main/
|   |       |-- java/
|   |       |   `-- com/
|   |       |       `-- company/
|   |       |           `-- device/
|   |       |               `-- urovo/
|   |       |                   |-- UrovoAlarmAdapter.java
|   |       |                   `-- UrovoDeviceAdapter.java
|   |       `-- AndroidManifest.xml
|   `-- build.gradle
|-- device-zebra/
|   |-- src/
|   |   `-- main/
|   |       |-- java/
|   |       |   `-- com/
|   |       |       `-- company/
|   |       |           `-- device/
|   |       |               `-- zebra/
|   |       |                   |-- ZebraAlarmAdapter.java
|   |       |                   `-- ZebraDeviceAdapter.java
|   |       `-- AndroidManifest.xml
|   `-- build.gradle
|-- gradle/
|   `-- wrapper/
|       |-- gradle-wrapper.jar
|       `-- gradle-wrapper.properties
|-- scanner-api/
|   |-- src/
|   |   `-- main/
|   |       `-- java/
|   |           `-- com/
|   |               `-- company/
|   |                   `-- scanner/
|   |                       `-- api/
|   |                           |-- ScanCallback.java
|   |                           |-- ScanResult.java
|   |                           |-- ScannerCapability.java
|   |                           |-- ScannerConfig.java
|   |                           |-- ScannerManager.java
|   |                           |-- ScannerProvider.java
|   |                           `-- ScannerType.java
|   `-- build.gradle
|-- scanner-factory/
|   |-- src/
|   |   |-- main/
|   |   |   |-- java/
|   |   |   |   `-- com/
|   |   |   |       `-- company/
|   |   |   |           `-- scanner/
|   |   |   |               `-- factory/
|   |   |   |                   `-- ScannerFactory.java
|   |   |   `-- AndroidManifest.xml
|   |   `-- test/
|   |       `-- java/
|   |           `-- com/
|   |               `-- company/
|   |                   `-- scanner/
|   |                       `-- factory/
|   |                           `-- ScannerFactoryTest.java
|   `-- build.gradle
|-- scanner-generic/
|   |-- src/
|   |   `-- main/
|   |       |-- java/
|   |       |   `-- com/
|   |       |       `-- company/
|   |       |           `-- scanner/
|   |       |               `-- generic/
|   |       |                   |-- CameraScannerManager.java
|   |       |                   |-- GenericScannerProvider.java
|   |       |                   |-- IntentScannerManager.java
|   |       |                   `-- KeyboardScannerManager.java
|   |       `-- AndroidManifest.xml
|   `-- build.gradle
|-- scanner-urovo/
|   |-- src/
|   |   `-- main/
|   |       |-- java/
|   |       |   `-- com/
|   |       |       `-- company/
|   |       |           `-- scanner/
|   |       |               `-- urovo/
|   |       |                   |-- UrovoScannerConfig.java
|   |       |                   |-- UrovoScannerManager.java
|   |       |                   `-- UrovoScannerProvider.java
|   |       `-- AndroidManifest.xml
|   `-- build.gradle
|-- scanner-zebra/
|   |-- src/
|   |   `-- main/
|   |       |-- java/
|   |       |   `-- com/
|   |       |       `-- company/
|   |       |           `-- scanner/
|   |       |               `-- zebra/
|   |       |                   |-- ZebraScannerConfig.java
|   |       |                   |-- ZebraScannerManager.java
|   |       |                   `-- ZebraScannerProvider.java
|   |       `-- AndroidManifest.xml
|   `-- build.gradle
|-- .gitignore
|-- README.md
|-- build.gradle
|-- gradle.properties
|-- gradlew
|-- gradlew.bat
`-- settings.gradle
```
