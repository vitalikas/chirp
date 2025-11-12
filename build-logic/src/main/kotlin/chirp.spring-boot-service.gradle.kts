import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    id("chirp.kotlin-common")

    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libraries.findVersion("spring-boot").get()}")
    }
}

dependencies {
    "implementation"(libraries.findLibrary("kotlin-reflect").get())
    "implementation"(libraries.findLibrary("spring-boot-starter-web").get())

    "implementation"(libraries.findLibrary("spring-boot-starter-data-jpa").get())
    "runtimeOnly"(libraries.findLibrary("postgresql").get())

    "testImplementation"(libraries.findLibrary("spring-boot-starter-test").get())
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
