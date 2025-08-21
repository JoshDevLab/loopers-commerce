plugins {
    `java-library`
}

dependencies {
    // feign client
    api("org.springframework.cloud:spring-cloud-starter-openfeign")
    
    // circuit breaker
    api("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    api("io.github.resilience4j:resilience4j-spring-boot3")
}
