# PDA Management Backend

Spring Boot REST API for authentication, device registration, PDA finder notifications, product lookup, inventory and disposal workflows.

## Technology

Java 8, Spring Boot 2.7.0, Spring Security OAuth2 resource server, MyBatis, PostgreSQL, Flyway and Firebase Cloud Messaging.

## Build

```sh
mvn -f pda-management/pom.xml verify
```

Set `JAVA_HOME` to JDK 8 for the backend; Android Gradle builds still use JDK 21. `pda-management/.env.example` documents the backend variables used by Docker Compose. The initial administrator is created only when bootstrap is explicitly enabled and a suitable password is supplied.

## Run with Docker Compose

Copy the root `.env.example` to `.env`, set strong secrets and `DATABASE_PASSWORD`, then run:

```sh
docker compose up --build -d
```

Firebase push requires credentials available to the backend and `FCM_ENABLED=true`. The backend does not include a service-account key.

## Source layout

The backend source is under `pda-management/` and follows feature-based domain, application, infrastructure and presentation layers. See the [complete directory tree](pda-management/STRUCTURE.md) and [backend README](pda-management/README.md) for the package map, dependency rules and database notes. Fresh databases use six feature migrations; existing databases using the original V1 must select the preserved `db/legacy` history as explained in the backend README.

Other project references are in [API contracts](docs/07-api-contracts.md), [deployment](docs/11-deployment.md), and [verification results](docs/verification.md).
