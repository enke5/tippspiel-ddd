import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.4.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "dev.tippspiel"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    // Disable BootJar for library modules that are not Spring Boot apps
    plugins.withId("org.springframework.boot") {
        tasks.named<BootJar>("bootJar") {
            enabled = true
        }
        tasks.named<Jar>("jar") {
            enabled = false
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
