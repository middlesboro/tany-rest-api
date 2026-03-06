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

# Crypt shared library pro MongoDB
RUN apt-get update && apt-get install -y curl && \
    curl -L "https://downloads.mongodb.com/linux/mongo_crypt_shared_v1-linux-x86_64-enterprise-ubuntu2204-8.0.4.tgz" \
    -o /tmp/crypt.tgz && \
    tar -xzf /tmp/crypt.tgz -C /tmp && \
    find /tmp -name "mongo_crypt_v1.so" -exec mv {} /usr/local/lib/ \; && \
    rm -rf /tmp/crypt.tgz /tmp/mongo_crypt_shared_v1-* && \
    apt-get clean

RUN groupadd -r spring && useradd -r -g spring spring \
    && mkdir -p /data \
    && chown -R spring:spring /data \
    && chmod 700 /data

USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]