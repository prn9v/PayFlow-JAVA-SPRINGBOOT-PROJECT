FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ── List what was built (helpful for debugging) ───────────────────────────
RUN echo "=== Built jars ===" && ls /app/target/*.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# ── Copy using wildcard — works regardless of jar name ───────────────────
COPY --from=build /app/target/*.jar app.jar

RUN addgroup -S payflow && adduser -S payflow -G payflow
USER payflow

EXPOSE 8761

ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]