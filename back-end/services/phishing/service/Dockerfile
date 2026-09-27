# Stage 1: Build
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /build
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY common ./common
COPY services ./services
RUN gradle :services:phishing:service:bootJar --no-daemon

# Stage 2: Extract
FROM eclipse-temurin:17-jre-alpine AS layers
WORKDIR /layers
COPY --from=build /build/services/phishing/service/build/libs/service-*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Run  (Debian-based for Playwright/Chromium support)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libglib2.0-0 libnss3 libnspr4 libdbus-1-3 libatk1.0-0 \
    libatk-bridge2.0-0 libcups2 libdrm2 libxcb1 libxkbcommon0 \
    libatspi2.0-0 libx11-6 libxcomposite1 libxdamage1 libxext6 \
    libxfixes3 libxrandr2 libgbm1 libpango-1.0-0 libcairo2 \
    libasound2 libwayland-client0 fonts-liberation fonts-noto-color-emoji \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appgroup && useradd -r -g appgroup -m appuser

COPY --from=layers /layers/dependencies/ ./
COPY --from=layers /layers/spring-boot-loader/ ./
COPY --from=layers /layers/snapshot-dependencies/ ./
COPY --from=layers /layers/application/ ./

RUN java -cp "BOOT-INF/lib/*:BOOT-INF/classes" \
    com.microsoft.playwright.CLI install chromium && \
    chown -R appuser:appgroup /home/appuser

USER appuser
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
