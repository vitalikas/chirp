package lt.vitalijus.chirp

import jakarta.annotation.PostConstruct
import lt.vitalijus.chirp.infra.database.entities.UserEntity
import lt.vitalijus.chirp.infra.database.repositories.UserRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class ChirpApplication

fun main(args: Array<String>) {
    runApplication<ChirpApplication>(*args)
}

@Component
class Demo(
    private val repository: UserRepository
) {

    @PostConstruct
    fun init() {
        repository.save(
            UserEntity(
                email = "test@test.com",
                username = "test",
                hashedPassword = "123"
            )
        )
    }
}