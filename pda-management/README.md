# PDA Management

Tài liệu tiếng Việt: [Giải thích dự án PDA Management](PROJECT_GUIDE.vi.md).

User registration: `POST /auth/register` requires a manager access token and creates an employee in that manager's store. For optional development seed data and example requests, see [đăng ký tài khoản](PROJECT_GUIDE.vi.md#đăng-ký-tài-khoản-nhân-viên) and [seed-dữ-liệu-mẫu](PROJECT_GUIDE.vi.md#seed-dữ-liệu-mẫu).

Spring Boot backend for authentication, device registration, PDA finder, product lookup, inventory and disposal workflows.

This directory is the backend Maven project root. The complete file-by-file layout, including the feature packages from the requested architecture, is in [STRUCTURE.md](STRUCTURE.md).

## Project structure

```text
pda-management/
├── pom.xml
├── Dockerfile
├── .env.example
└── src/
    ├── main/
    │   ├── java/com/company/pda/
    │   │   ├── PdaApplication.java
    │   │   ├── domain/{auth,device,pdafinder,product,inventory,disposal,shared}/
    │   │   ├── application/{auth,device,pdafinder,product,inventory,disposal,port}/
    │   │   ├── infrastructure/{persistence/mybatis,security,firebase,integration,config}/
    │   │   └── presentation/{rest,exception,filter}/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       ├── db/migration/   # V1 through V7, including anonymous web finder requests
    │       └── mapper/
    └── test/java/com/company/pda/
```

`domain` owns business models, repository contracts and domain errors. `application` owns use cases, services, commands/results and output ports. `infrastructure` implements persistence and external integrations. `presentation` adapts HTTP requests to application use cases. MyBatis SQL is in `src/main/resources/mapper`; Flyway migrations are in `src/main/resources/db/migration`.

## Database migrations

The SQL scripts now target **Oracle 19c**. JDBC configuration, MyBatis queries and integration tests still target PostgreSQL; this is a schema-script conversion, not a completed backend database migration. See [Oracle schema instructions](src/main/resources/db/README.md).

New Oracle schemas can use `classpath:db/migration` and apply these scripts in order after the runtime has been adapted:

1. `V1__create_auth_tables.sql`: stores, users, roles, refresh tokens and audit logs.
2. `V2__create_device_tables.sql`: registered devices.
3. `V3__create_pda_finder_tables.sql`: search requests, alert logs and the durable outbox.
4. `V4__create_product_tables.sql`: products and image synchronization history.
5. `V5__create_inventory_tables.sql`: inventory, adjustments and transaction history.
6. `V6__create_disposal_tables.sql`: disposal documents, items, history and the inventory-to-disposal foreign key.
7. `V7__allow_web_finder_requests.sql`: nullable requester for the unauthenticated React finder website; included in the legacy history too.

The `db/legacy` scripts have also been converted to Oracle. They are an alternative combined schema for a new Oracle database, not the original PostgreSQL Flyway history. Do not apply these edited scripts to an existing PostgreSQL history or combine both locations. Preserve the previous application version's migrations for existing PostgreSQL deployments.

## Layer boundaries

REST controllers use application use cases and DTOs. Application services depend on domain repositories and output ports. MyBatis entities, mapper interfaces/XML, converters and repository implementations are in `infrastructure/persistence/mybatis`.

Authentication uses `spring-boot-starter-security` and JJWT 0.11.5 (`jjwt-api`, with `jjwt-impl` and `jjwt-jackson` at runtime). `JwtAuthenticationFilter` is registered once by `SecurityConfig`, before `UsernamePasswordAuthenticationFilter`. `JwtTokenProvider` validates HS256 signature, issuer, expiry, token type and audience with JJWT; the filter validates the session and maps roles to `ROLE_` authorities. The principal is JJWT `Claims`. No Spring OAuth2 Resource Server is used.

`ErpProductClient` and `ProductImageClient` are optional HTTP adapters. They are created only when `app.integration.erp.product-url` or `app.integration.image.product-url` is configured. URLs must contain `{barcode}` or `{productCode}`, respectively. ERP lookup expects the JSON fields in `ProductResult`; image lookup expects image content. Both use connection/read timeouts, represent HTTP 404 as an empty result and propagate other remote failures. They are available for integration wiring; the existing product API continues to read the local product database and Android continues to load image URLs directly. Provider-specific authentication and contracts must be configured for the actual external services.

Additional files beyond the example tree support existing endpoints, device credentials, refresh tokens, outbox delivery and audit logging. The four test packages are present; currently empty packages contain `.gitkeep` so Git retains their directories.

For local FCM startup on Windows, after building use `powershell -NoProfile -ExecutionPolicy Bypass -File ./run-fcm.ps1 -CredentialPath 'C:\secrets\service-account.json'`. This requires the Firebase Admin key for the Android project, not `google-services.json`, and enables both FCM and scheduler. See [finder troubleshooting](../docs/22-delete-device-and-finder-troubleshooting.md).

## Build

The backend uses Java 8, Spring Boot 2.7.0 and executable WAR packaging. See [migration notes and dependency decisions](JAVA8_MIGRATION.vi.md).

Run these commands from this directory:

```sh
mvn verify
```

Or from the workspace root:

```sh
mvn -f pda-management/pom.xml verify
```

The project requires JDK 8. To build the Docker image or start the whole stack, run `docker compose up --build -d` from the workspace root. Copy the workspace `.env.example` to `.env` and configure its required secrets first. Firebase credentials are optional for startup and required only when FCM push is enabled.
