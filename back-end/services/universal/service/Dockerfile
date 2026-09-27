# Stage 1: Build
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /build
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY common ./common
COPY services ./services
RUN gradle :services:universal:service:bootJar --no-daemon

# Stage 2: Extract
FROM eclipse-temurin:17-jre-alpine AS layers
WORKDIR /layers
COPY --from=build /build/services/universal/service/build/libs/service-*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=layers /layers/dependencies/ ./
COPY --from=layers /layers/spring-boot-loader/ ./
COPY --from=layers /layers/snapshot-dependencies/ ./
COPY --from=layers /layers/application/ ./
# Always pass profile as JVM arg so Spring uses it before loading config (avoids prod showing but using local config)
ENTRYPOINT ["sh", "-c", "exec java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local} org.springframework.boot.loader.launch.JarLauncher"]
