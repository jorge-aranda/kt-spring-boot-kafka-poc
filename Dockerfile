# syntax=docker/dockerfile:1.7
# ---- Build stage ----------------------------------------------------------
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy the Gradle wrapper and build scripts first to leverage Docker layer cache.
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts ./

RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

# Now copy the sources and build the executable jar.
COPY src src
RUN ./gradlew --no-daemon clean bootJar

# ---- Runtime stage --------------------------------------------------------
FROM eclipse-temurin:17-jre AS runtime

# Create a dedicated non-root user.
RUN groupadd -r appuser && \
    useradd -r -g appuser -d /app -s /sbin/nologin appuser && \
    mkdir -p /app && chown -R appuser:appuser /app

WORKDIR /app

# Copy the built jar (version-agnostic glob so we don't couple to a version).
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown appuser:appuser app.jar

# Default connection settings — point to Docker Compose service names.
ENV SPRING_MONGODB_URI=mongodb://mongodb:27017/kafkapoc \
    SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
