plugins {
    id("chirp.spring-boot-service")
}

dependencies {
    implementation(projects.common)

    api(libs.kotlin.reflect)
    api(libs.jackson.module.kotlin)
    api(libs.jackson.datatype.jsr310)
    implementation(libs.spring.boot.starter.amqp)
}