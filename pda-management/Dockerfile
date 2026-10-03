FROM maven:3.9.11-eclipse-temurin-8 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:8-jre
RUN groupadd --system retail && useradd --system --gid retail retail
WORKDIR /app
COPY --from=build /src/target/pda-management-1.0.0.war app.war
USER retail
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.war"]
