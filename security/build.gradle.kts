plugins {
    id("chirp.spring-boot-service")
}

dependencies {
    implementation(projects.common)
    implementation(projects.user)

    implementation(libs.spring.boot.starter.security)
}