# PDA Management - source tree

This is the actual project layout after matching the supplied folder specification.
Generated `target/` output is omitted. `db/legacy` preserves the original migration for existing databases.

```text
pda-management/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── company/
│   │   │           └── pda/
│   │   │               ├── application/
│   │   │               │   ├── auth/
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── LoginCommand.java
│   │   │               │   │   │   ├── RefreshTokenCommand.java
│   │   │               │   │   │   ├── RegisterUserCommand.java
│   │   │               │   │   │   ├── RegisterUserResult.java
│   │   │               │   │   │   └── TokenResult.java
│   │   │               │   │   ├── service/
│   │   │               │   │   │   ├── AuthenticationService.java
│   │   │               │   │   │   └── UserRegistrationService.java
│   │   │               │   │   └── usecase/
│   │   │               │   │       ├── AuthUseCase.java
│   │   │               │   │       ├── LoginUseCase.java
│   │   │               │   │       ├── RefreshTokenUseCase.java
│   │   │               │   │       └── RegisterUserUseCase.java
│   │   │               │   ├── device/
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── DeviceRegistrationResult.java
│   │   │               │   │   │   ├── DeviceResult.java
│   │   │               │   │   │   ├── RegisterDeviceCommand.java
│   │   │               │   │   │   └── UpdateFcmTokenCommand.java
│   │   │               │   │   ├── service/
│   │   │               │   │   │   └── DeviceService.java
│   │   │               │   │   └── usecase/
│   │   │               │   │       ├── DeviceUseCase.java
│   │   │               │   │       ├── RegisterDeviceUseCase.java
│   │   │               │   │       └── UpdateFcmTokenUseCase.java
│   │   │               │   ├── disposal/
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── CreateDisposalCommand.java
│   │   │               │   │   │   ├── DisposalLineCommand.java
│   │   │               │   │   │   └── DisposalTransitionCommand.java
│   │   │               │   │   ├── service/
│   │   │               │   │   │   └── DisposalService.java
│   │   │               │   │   └── usecase/
│   │   │               │   │       ├── CancelDisposalUseCase.java
│   │   │               │   │       ├── ConfirmDisposalUseCase.java
│   │   │               │   │       ├── CreateDisposalUseCase.java
│   │   │               │   │       └── DisposalUseCase.java
│   │   │               │   ├── inventory/
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   └── AdjustInventoryCommand.java
│   │   │               │   │   ├── service/
│   │   │               │   │   │   └── InventoryService.java
│   │   │               │   │   └── usecase/
│   │   │               │   │       ├── AdjustInventoryUseCase.java
│   │   │               │   │       ├── GetInventoryUseCase.java
│   │   │               │   │       └── InventoryUseCase.java
│   │   │               │   ├── pdafinder/
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── FindPdaCommand.java
│   │   │               │   │   │   ├── FindPdaResult.java
│   │   │               │   │   │   ├── PdaAlertEventCommand.java
│   │   │               │   │   │   └── StopPdaCommand.java
│   │   │               │   │   ├── service/
│   │   │               │   │   │   ├── OutboxProcessor.java
│   │   │               │   │   │   └── PdaFinderService.java
│   │   │               │   │   └── usecase/
│   │   │               │   │       ├── FinderUseCase.java
│   │   │               │   │       ├── FindPdaUseCase.java
│   │   │               │   │       └── StopPdaAlertUseCase.java
│   │   │               │   ├── port/
│   │   │               │   │   └── out/
│   │   │               │   │       ├── AuditLogPort.java
│   │   │               │   │       ├── CurrentActor.java
│   │   │               │   │       ├── InventoryAdjustmentPort.java
│   │   │               │   │       ├── NotificationPort.java
│   │   │               │   │       ├── OperationsRepository.java
│   │   │               │   │       ├── PasswordHasher.java
│   │   │               │   │       └── TokenProvider.java
│   │   │               │   └── product/
│   │   │               │       ├── dto/
│   │   │               │       │   ├── ImageSyncCommand.java
│   │   │               │       │   ├── ProductDtoMapper.java
│   │   │               │       │   └── ProductResult.java
│   │   │               │       ├── service/
│   │   │               │       │   └── ProductService.java
│   │   │               │       └── usecase/
│   │   │               │           ├── GetProductByBarcodeUseCase.java
│   │   │               │           └── ProductUseCase.java
│   │   │               ├── domain/
│   │   │               │   ├── auth/
│   │   │               │   │   ├── exception/
│   │   │               │   │   │   └── InvalidCredentialsException.java
│   │   │               │   │   ├── model/
│   │   │               │   │   │   ├── Permission.java
│   │   │               │   │   │   ├── Refresh.java
│   │   │               │   │   │   ├── Role.java
│   │   │               │   │   │   └── User.java
│   │   │               │   │   └── repository/
│   │   │               │   │       └── AuthRepository.java
│   │   │               │   ├── device/
│   │   │               │   │   ├── model/
│   │   │               │   │   │   ├── Device.java
│   │   │               │   │   │   └── DeviceStatus.java
│   │   │               │   │   └── repository/
│   │   │               │   │       └── DeviceRepository.java
│   │   │               │   ├── disposal/
│   │   │               │   │   ├── model/
│   │   │               │   │   │   ├── Disposal.java
│   │   │               │   │   │   ├── DisposalItem.java
│   │   │               │   │   │   └── DisposalStatus.java
│   │   │               │   │   └── repository/
│   │   │               │   │       └── DisposalRepository.java
│   │   │               │   ├── inventory/
│   │   │               │   │   ├── exception/
│   │   │               │   │   │   └── InsufficientInventoryException.java
│   │   │               │   │   ├── model/
│   │   │               │   │   │   ├── Inventory.java
│   │   │               │   │   │   ├── InventoryAdjustment.java
│   │   │               │   │   │   └── InventoryTransaction.java
│   │   │               │   │   └── repository/
│   │   │               │   │       └── InventoryRepository.java
│   │   │               │   ├── pdafinder/
│   │   │               │   │   ├── exception/
│   │   │               │   │   │   └── DeviceNotInStoreException.java
│   │   │               │   │   ├── model/
│   │   │               │   │   │   ├── PdaAlertLog.java
│   │   │               │   │   │   ├── PdaFindRequest.java
│   │   │               │   │   │   └── PdaFindStatus.java
│   │   │               │   │   └── repository/
│   │   │               │   │       └── PdaFindRepository.java
│   │   │               │   ├── product/
│   │   │               │   │   ├── model/
│   │   │               │   │   │   └── Product.java
│   │   │               │   │   └── repository/
│   │   │               │   │       └── ProductRepository.java
│   │   │               │   └── shared/
│   │   │               │       ├── exception/
│   │   │               │       │   └── DomainException.java
│   │   │               │       └── model/
│   │   │               │           ├── Actor.java
│   │   │               │           ├── AuditInfo.java
│   │   │               │           ├── Outbox.java
│   │   │               │           └── SecretHash.java
│   │   │               ├── infrastructure/
│   │   │               │   ├── config/
│   │   │               │   │   ├── MyBatisConfig.java
│   │   │               │   │   └── TransactionConfig.java
│   │   │               │   ├── firebase/
│   │   │               │   │   ├── FirebaseConfig.java
│   │   │               │   │   ├── FirebaseNotificationAdapter.java
│   │   │               │   │   └── OutboxScheduler.java
│   │   │               │   ├── integration/
│   │   │               │   │   ├── erp/
│   │   │               │   │   │   └── ErpProductClient.java
│   │   │               │   │   └── image/
│   │   │               │   │       └── ProductImageClient.java
│   │   │               │   ├── persistence/
│   │   │               │   │   ├── mybatis/
│   │   │               │   │   │   ├── converter/
│   │   │               │   │   │   │   ├── DeviceEntityMapper.java
│   │   │               │   │   │   │   ├── DisposalEntityMapper.java
│   │   │               │   │   │   │   ├── DisposalItemEntityMapper.java
│   │   │               │   │   │   │   ├── InventoryEntityMapper.java
│   │   │               │   │   │   │   ├── OutboxEntityMapper.java
│   │   │               │   │   │   │   ├── PdaFindRequestEntityMapper.java
│   │   │               │   │   │   │   ├── ProductEntityMapper.java
│   │   │               │   │   │   │   ├── RefreshEntityMapper.java
│   │   │               │   │   │   │   └── UserEntityMapper.java
│   │   │               │   │   │   ├── entity/
│   │   │               │   │   │   │   ├── DeviceEntity.java
│   │   │               │   │   │   │   ├── DisposalEntity.java
│   │   │               │   │   │   │   ├── DisposalItemEntity.java
│   │   │               │   │   │   │   ├── InventoryEntity.java
│   │   │               │   │   │   │   ├── OutboxEntity.java
│   │   │               │   │   │   │   ├── PdaFindRequestEntity.java
│   │   │               │   │   │   │   ├── ProductEntity.java
│   │   │               │   │   │   │   ├── RefreshEntity.java
│   │   │               │   │   │   │   └── UserEntity.java
│   │   │               │   │   │   ├── mapper/
│   │   │               │   │   │   │   ├── DeviceMapper.java
│   │   │               │   │   │   │   ├── DisposalMapper.java
│   │   │               │   │   │   │   ├── InventoryMapper.java
│   │   │               │   │   │   │   ├── OperationsMapper.java
│   │   │               │   │   │   │   ├── PdaFindMapper.java
│   │   │               │   │   │   │   ├── ProductMapper.java
│   │   │               │   │   │   │   └── UserMapper.java
│   │   │               │   │   │   ├── repository/
│   │   │               │   │   │   │   ├── MyBatisAuthRepository.java
│   │   │               │   │   │   │   ├── MyBatisDeviceRepository.java
│   │   │               │   │   │   │   ├── MyBatisDisposalRepository.java
│   │   │               │   │   │   │   ├── MyBatisInventoryRepository.java
│   │   │               │   │   │   │   ├── MyBatisOperationsRepository.java
│   │   │               │   │   │   │   ├── MyBatisPdaFindRepository.java
│   │   │               │   │   │   │   └── MyBatisProductRepository.java
│   │   │               │   │   │   └── UuidTypeHandler.java
│   │   │               │   │   └── seed/
│   │   │               │   │       └── DevelopmentDataSeeder.java
│   │   │               │   └── security/
│   │   │               │       ├── BootstrapAdmin.java
│   │   │               │       ├── JwtAuthenticationFilter.java
│   │   │               │       ├── JwtTokenProvider.java
│   │   │               │       ├── PasswordEncoderConfig.java
│   │   │               │       ├── SecurityConfig.java
│   │   │               │       └── SecurityCurrentActor.java
│   │   │               ├── presentation/
│   │   │               │   ├── exception/
│   │   │               │   │   ├── ErrorResponse.java
│   │   │               │   │   └── GlobalExceptionHandler.java
│   │   │               │   ├── filter/
│   │   │               │   │   └── RequestLogFilter.java
│   │   │               │   └── rest/
│   │   │               │       ├── auth/
│   │   │               │       │   ├── dto/
│   │   │               │       │   │   ├── LoginRequest.java
│   │   │               │       │   │   ├── LoginResponse.java
│   │   │               │       │   │   ├── RefreshTokenRequest.java
│   │   │               │       │   │   ├── RegisterUserRequest.java
│   │   │               │       │   │   └── RegisterUserResponse.java
│   │   │               │       │   └── AuthController.java
│   │   │               │       ├── device/
│   │   │               │       │   ├── dto/
│   │   │               │       │   │   ├── RegisterDeviceRequest.java
│   │   │               │       │   │   └── UpdateFcmTokenRequest.java
│   │   │               │       │   └── DeviceController.java
│   │   │               │       ├── disposal/
│   │   │               │       │   ├── dto/
│   │   │               │       │   │   ├── CreateDisposalRequest.java
│   │   │               │       │   │   ├── DisposalLineRequest.java
│   │   │               │       │   │   └── DisposalTransitionRequest.java
│   │   │               │       │   └── DisposalController.java
│   │   │               │       ├── inventory/
│   │   │               │       │   ├── dto/
│   │   │               │       │   │   └── AdjustInventoryRequest.java
│   │   │               │       │   └── InventoryController.java
│   │   │               │       ├── pdafinder/
│   │   │               │       │   ├── dto/
│   │   │               │       │   │   ├── FindPdaRequest.java
│   │   │               │       │   │   ├── FindPdaResponse.java
│   │   │               │       │   │   ├── PdaAlertEventRequest.java
│   │   │               │       │   │   └── StopPdaRequest.java
│   │   │               │       │   └── PdaFinderController.java
│   │   │               │       └── product/
│   │   │               │           ├── dto/
│   │   │               │           │   └── ImageSyncRequest.java
│   │   │               │           └── ProductController.java
│   │   │               └── PdaApplication.java
│   │   └── resources/
│   │       ├── db/
│   │       │   ├── legacy/
│   │       │   │   └── V1__retail_schema.sql
│   │       │   └── migration/
│   │       │       ├── V1__create_auth_tables.sql
│   │       │       ├── V2__create_device_tables.sql
│   │       │       ├── V3__create_pda_finder_tables.sql
│   │       │       ├── V4__create_product_tables.sql
│   │       │       ├── V5__create_inventory_tables.sql
│   │       │       └── V6__create_disposal_tables.sql
│   │       ├── mapper/
│   │       │   ├── DeviceMapper.xml
│   │       │   ├── DisposalMapper.xml
│   │       │   ├── InventoryMapper.xml
│   │       │   ├── OperationsMapper.xml
│   │       │   ├── PdaFindMapper.xml
│   │       │   ├── ProductMapper.xml
│   │       │   └── UserMapper.xml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/
│               └── company/
│                   └── pda/
│                       ├── application/
│                       │   └── inventory/
│                       │       └── InventoryServiceTest.java
│                       ├── domain/
│                       │   └── .gitkeep
│                       ├── infrastructure/
│                       │   └── persistence/
│                       │       └── seed/
│                       │           └── DevelopmentDataSeederTest.java
│                       └── presentation/
│                           └── StoreFlowsIT.java
├── .dockerignore
├── .env.example
├── Dockerfile
├── pom.xml
├── PROJECT_GUIDE.vi.md
├── README.md
└── STRUCTURE.md
```

All required paths from the supplied specification are present. Additional commands, DTOs, persistence adapters and outbox classes support existing application behavior.
