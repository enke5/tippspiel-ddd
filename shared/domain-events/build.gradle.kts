plugins {
    java
}

// Shared library: no Spring Boot plugin, no fat jar
dependencies {
    // Jackson for event serialisation (shared with both services)
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.3")
}
