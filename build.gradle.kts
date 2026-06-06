plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.jaranas"
version = "0.5.0-SNAPSHOT"

extra["springAiVersion"] = "2.0.0-M8"

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

    // MCP (Model Context Protocol) server
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

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

// ---------------------------------------------------------------------------
// Test scopes
// ---------------------------------------------------------------------------
// We split the tests into three Gradle scopes to keep fast unit tests separated
// from slower, infrastructure-bound suites:
//   - `test`            -> pure unit tests (default `src/test`).
//   - `integrationTest` -> tests that boot part of the Spring context and rely on
//                          real infrastructure via Testcontainers (MongoDB, Kafka).
//                          Sources at `src/integrationTest/kotlin`.
//   - `e2eTest`         -> end-to-end tests that boot the full application against
//                          Testcontainers and exercise the HTTP API + Kafka pipeline.
//                          Sources at `src/e2eTest/kotlin`.
//
// All scopes inherit the regular test dependencies (JUnit, MockK, Spring Test, …)
// and additionally pull in Testcontainers for the integration and e2e scopes.
// ---------------------------------------------------------------------------

sourceSets {
    create("integrationTest") {
        kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += output + compileClasspath
    }
    create("e2eTest") {
        kotlin.srcDir("src/e2eTest/kotlin")
        resources.srcDir("src/e2eTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += output + compileClasspath
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

val e2eTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
configurations["e2eTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    // Testcontainers BOM keeps the versions of all testcontainers modules aligned.
    val testcontainersBom = platform("org.testcontainers:testcontainers-bom:1.20.4")

    integrationTestImplementation(testcontainersBom)
    integrationTestImplementation("org.testcontainers:junit-jupiter")
    integrationTestImplementation("org.testcontainers:mongodb")
    integrationTestImplementation("org.testcontainers:kafka")

    e2eTestImplementation(testcontainersBom)
    e2eTestImplementation("org.testcontainers:junit-jupiter")
    e2eTestImplementation("org.testcontainers:mongodb")
    e2eTestImplementation("org.testcontainers:kafka")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests (Testcontainers: MongoDB + Kafka)."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

val e2eTest = tasks.register<Test>("e2eTest") {
    description = "Runs end-to-end tests against the full app on Testcontainers."
    group = "verification"
    testClassesDirs = sourceSets["e2eTest"].output.classesDirs
    classpath = sourceSets["e2eTest"].runtimeClasspath
    shouldRunAfter(integrationTest)
    useJUnitPlatform()
}

// `check` should run unit + integration + e2e tests so CI catches everything,
// while `test` stays scoped to fast unit tests only.
tasks.named("check") {
    dependsOn(integrationTest, e2eTest)
}
