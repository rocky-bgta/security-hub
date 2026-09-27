# syntax=docker/dockerfile:1
# Pin amd64 so `docker build` works on ARM hosts and matches compose `platform: linux/amd64`.
FROM --platform=linux/amd64 gradle:8.8-jdk17-alpine AS build
WORKDIR /build
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY common ./common
COPY services ./services
RUN chmod +x ./gradlew \
    && ./gradlew :services:breach-detection:service:bootJar --no-daemon

# Stage 2: Extract
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine AS layers
WORKDIR /layers
COPY --from=build /build/services/breach-detection/service/build/libs/service-*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Run
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=layers /layers/dependencies/ ./
COPY --from=layers /layers/spring-boot-loader/ ./
COPY --from=layers /layers/snapshot-dependencies/ ./
COPY --from=layers /layers/application/ ./
EXPOSE 6080
# Set profile as JVM arg so it applies before config load (matches other ASAT service images).
ENTRYPOINT ["sh", "-c", "exec java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev} org.springframework.boot.loader.launch.JarLauncher"]
