# 1. Fáza: Build (JDK)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests

# 2. Fáza: Runtime (JRE)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /data \
    && chown -R spring:spring /data \
    && chmod 700 /data

USER spring:spring

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]