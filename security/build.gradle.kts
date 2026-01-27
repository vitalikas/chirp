plugins {
    id("chirp.spring-boot-service")
}

dependencies {
    implementation(projects.common)
    implementation(projects.user)

    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.kotlin.reflect)
}
