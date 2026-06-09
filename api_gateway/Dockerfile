FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

RUN addgroup -S payflow && adduser -S payflow -G payflow
USER payflow

EXPOSE 8080

ENTRYPOINT ["java","-Xmx256m","-Xms128m","-jar","app.jar"]