plugins {
    `java-library`
}

dependencies {
    // Spring Boot Starter
    api("org.springframework.boot:spring-boot-starter")
    
    // Spring Context for @EnableScheduling
    api("org.springframework:spring-context")
    
    // Configuration Properties
    api("org.springframework.boot:spring-boot-configuration-processor")
    
    // Validation
    api("org.springframework.boot:spring-boot-starter-validation")
    
    // Micrometer for metrics
    api("io.micrometer:micrometer-core")
    
    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
