import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    id("chirp.kotlin-common")

    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    implementation(libs.spring.boot.starter.security)
}


configure<DependencyManagementExtension> {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libraries.findVersion("spring-boot").get()}")
    }
}
