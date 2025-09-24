plugins {
    id("chirp.spring-boot-service")
}

dependencies {
    implementation(projects.common)

    implementation(libs.spring.boot.starter.security)
}