plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.jaranas"
version = "0.3.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Web + Actuator
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Persistence
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Messaging — Spring Boot 4 modular starter (replaces direct org.springframework.kafka:spring-kafka)
    // The starter brings spring-kafka transitively AND the relocated Kafka auto-configuration
    // (org.springframework.boot.kafka.autoconfigure), which is what registers the KafkaTemplate bean.
    implementation("org.springframework.boot:spring-boot-starter-kafka")

    // Kotlin support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.springframework.security:spring-security-test")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
