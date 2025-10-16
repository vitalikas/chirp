plugins {
    id("chirp.spring-boot-service")
}

dependencies {
    implementation(projects.common)

    api(libs.kotlin.reflect)
    api(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.amqp)
}