plugins {
    java
    id("org.springframework.boot") version "3.4.5"
}

group = "vn.tienlo"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/openapi/public/src/main/java"))
            srcDir(layout.buildDirectory.dir("generated/openapi/internal/src/main/java"))
            srcDir(layout.buildDirectory.dir("generated/openapi/admin/src/main/java"))
        }
    }
}

tasks.register<Exec>("openApiGenerate") {
    group = "code generation"
    description = "Generate TypeScript clients and Java API interfaces from OpenAPI contracts."
    val repositoryRoot = rootProject.projectDir.parentFile
    workingDir(repositoryRoot)
    commandLine("node", "tools/contracts/generate.mjs")
    inputs.files(repositoryRoot.resolve("shared/contracts"))
    inputs.file(repositoryRoot.resolve("tools/contracts/generate.mjs"))
    outputs.dir(layout.buildDirectory.dir("generated/openapi"))
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.5"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
    implementation("org.bouncycastle:bcprov-jdk18on:1.86")
    implementation("com.google.crypto.tink:tink:1.24.0")
    implementation("org.flywaydb:flyway-core:10.20.1")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:10.20.1")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
