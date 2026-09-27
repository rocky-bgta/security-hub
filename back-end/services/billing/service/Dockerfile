# --- Stage 1: Build app ---
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /build
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY common ./common
COPY services ./services
RUN chmod +x ./gradlew
RUN ./gradlew :services:billing:service:bootJar --no-daemon

# --- Stage 2: Build truststore (needs keytool) ---
FROM eclipse-temurin:17-jdk-jammy AS certs
WORKDIR /opt/certs
RUN apt-get update && apt-get install -y curl ca-certificates && rm -rf /var/lib/apt/lists/*

# Step 1: Create truststore and import system CA certificates from JDK's default truststore
# This ensures standard CA certificates (DigiCert, Let's Encrypt, etc.) are included
# which are needed for external HTTPS connections like Stripe API
RUN keytool -importkeystore \
    -srckeystore $JAVA_HOME/lib/security/cacerts \
    -srcstorepass changeit \
    -destkeystore /opt/certs/rds-truststore.jks \
    -deststorepass changeit \
    -noprompt

# Step 2: Download AWS DocDB/RDS global CA bundle
RUN curl -sS https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem -o global-bundle.pem

# Step 3: Split bundle to individual certs
RUN awk 'split_after==1{n++;split_after=0} /-----END CERTIFICATE-----/{split_after=1} {print > sprintf("rds-%03d.pem", n)}' global-bundle.pem

# Step 4: Import AWS RDS certificates into the existing truststore (which already has system CAs)
RUN i=0; for c in /opt/certs/rds-*.pem; do \
    i=$((i+1)); \
    keytool -importcert -noprompt -file "$c" \
        -alias aws-docdb-$i \
        -keystore /opt/certs/rds-truststore.jks \
        -storepass changeit; \
done

# Cleanup temporary files
RUN rm -f /opt/certs/global-bundle.pem /opt/certs/rds-*.pem

# --- Stage 3: Final runtime image ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# App user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
# Ensure certs dir exists
RUN mkdir -p /opt/certs

# App JAR
COPY --from=build /build/services/billing/service/build/libs/service-1.0-SNAPSHOT.jar /app/app.jar
# Truststore from certs stage
COPY --from=certs /opt/certs/rds-truststore.jks /opt/certs/rds-truststore.jks

# Perms so appuser can read the truststore
RUN chown -R appuser:appgroup /opt/certs && chmod 640 /opt/certs/rds-truststore.jks

# Tell Java to use the truststore (no code changes needed)
ENV JAVA_TOOL_OPTIONS="-Djavax.net.ssl.trustStore=/opt/certs/rds-truststore.jks -Djavax.net.ssl.trustStorePassword=changeit"

USER appuser
EXPOSE 6060
# Always pass profile as JVM arg so Spring uses it before loading config (avoids prod showing but using local config)
ENTRYPOINT ["sh", "-c", "exec java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local} -jar /app/app.jar"]

