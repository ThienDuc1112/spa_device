# Retail PDA system

Java 21 / Spring Boot backend and Java Android application for PDA finder alerts, product images, inventory adjustments and disposal management. This repository includes implementation, PostgreSQL migrations, automated tests, API contracts and deployment configuration.

Read the deliverables in the requested order:

1. [System architecture](docs/01-architecture.md)
2. [Database ERD](docs/02-erd.md)
3. [Backend project structure](docs/03-projects-and-models.md#3-backend-project-structure)
4. [Android project structure](docs/03-projects-and-models.md#4-android-project-structure)
5. [Flyway migration](backend/src/main/resources/db/migration/V1__retail_schema.sql)
6. [Entity models](backend/src/main/java/com/company/domain/Models.java)
7. [API contracts and JSON examples](docs/07-api-contracts.md) · [OpenAPI](docs/openapi.json)
8. [Backend source](backend/src/main/java/com/company)
9. [Android project and modules](pda-android/README.md) · [Complete folder structure](pda-android/STRUCTURE.md)
10. [Sequence diagrams](docs/10-sequences.md)
11. [Deployment guide](docs/11-deployment.md)
12. [Future improvements and rollout boundaries](docs/12-future-improvements.md)
13. [Luồng scanner và vai trò các module (tiếng Việt)](docs/13-scanner-flow.md)
14. [Luồng tìm thiết bị PDA (tiếng Việt)](docs/14-device-finder-flow.md)

```sh
# JDK 21, Maven 3.9+
mvn -f backend/pom.xml verify

# Android SDK 35; on Windows use gradlew.bat
cd pda-android
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Configure `.env` from `.env.example`, then start with `docker compose up --build -d`. The first admin must be explicitly bootstrapped; there are no default credentials. See the deployment guide for TLS, Firebase, ERP data and PDA registration. The app compiles without Firebase configuration, but live push requires your project's `google-services.json` and backend credentials.

Implementation phases 1–8 are represented by the schema, authentication, finder, products, inventory, disposals, Android app and test suite. See [verification results](docs/verification.md) for what was actually run and what still requires device testing. Treat this as a tested implementation to configure and validate for your environment, not a claim of production certification.
