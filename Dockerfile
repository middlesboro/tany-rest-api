# Phase 1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests

# Phase 2
FROM bellsoft/liberica-openjdk-alpine-musl:21-jre
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir /data && chown spring:spring /data

USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]