import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("chirp.spring-boot-app")
}

tasks {
	named<BootJar>("bootJar") {
		from(project(":notification").projectDir.resolve("src/main/resources")) {
			into("")
		}
		from(project(":user").projectDir.resolve("src/main/resources")) {
			into("")
		}
	}
}

dependencies {
	implementation(projects.user)
	implementation(projects.chat)
	implementation(projects.notification)
	implementation(projects.common)
	implementation(projects.security)
}
