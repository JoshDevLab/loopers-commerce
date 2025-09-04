dependencies {
    // add-ons
    implementation(project(":modules:jpa"))
    implementation(project(":modules:redis"))
    implementation(project(":modules:feign"))
    implementation(project(":modules:scheduling"))
    implementation(project(":modules:kafka"))
    implementation(project(":supports:jackson"))
    implementation(project(":supports:logging"))
    implementation(project(":supports:monitoring"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")

    // querydsl
    annotationProcessor("com.querydsl:querydsl-apt::jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")

    implementation("org.springframework.boot:spring-boot-starter-aop")

    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
    testImplementation(testFixtures(project(":modules:redis")))

    // test dependencies for wiremock
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock")
    testImplementation ("org.testcontainers:junit-jupiter:1.20.2")
    testImplementation ("org.testcontainers:kafka:1.20.2")
    testImplementation ("org.springframework.kafka:spring-kafka-test:3.3.4")
    testImplementation ("org.awaitility:awaitility:4.2.1")
    testImplementation ("org.assertj:assertj-core:3.26.0")

}
