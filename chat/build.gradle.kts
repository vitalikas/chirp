plugins {
    id("chirp.spring-boot-service")
}

dependencies {
    implementation(projects.common)

    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.websocket)
}
