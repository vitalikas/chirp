plugins {
	id("chirp.spring-boot-app")
}

dependencies {
	implementation(projects.user)
	implementation(projects.chat)
	implementation(projects.notification)
	implementation(projects.common)

	implementation(libs.spring.boot.starter.security)
	implementation(libs.spring.boot.starter.data.redis)
}