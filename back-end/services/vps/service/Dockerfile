# Stage 1: Build
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /build
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY common ./common
COPY services ./services
RUN gradle :services:vps:service:bootJar --no-daemon

# Stage 2: Extract
FROM eclipse-temurin:17-jre-alpine AS layers
WORKDIR /layers
COPY --from=build /build/services/vps/service/build/libs/service-*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install FFmpeg and CA certificates for SSL
RUN apt-get update && \
    apt-get install -y ffmpeg ca-certificates && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Create output directory for ffmpeg processing and set ownership
RUN mkdir -p /app/output && chown -R appuser:appgroup /app

# Copy extracted layers (as root, then change ownership)
COPY --from=layers /layers/dependencies/ ./
COPY --from=layers /layers/spring-boot-loader/ ./
COPY --from=layers /layers/snapshot-dependencies/ ./
COPY --from=layers /layers/application/ ./

# Ensure appuser owns all files in /app
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Always pass profile as JVM arg so Spring uses it before loading config (avoids prod showing but using local config)
ENTRYPOINT ["sh", "-c", "exec java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local} org.springframework.boot.loader.launch.JarLauncher"]