package lt.vitalijus.chirp.service.auth

import lt.vitalijus.chirp.domain.exception.PasswordEncodingException
import lt.vitalijus.chirp.domain.exception.UserAlreadyExistsException
import lt.vitalijus.chirp.domain.model.User
import lt.vitalijus.chirp.infra.database.entities.UserEntity
import lt.vitalijus.chirp.infra.database.mappers.toUser
import lt.vitalijus.chirp.infra.database.repositories.UserRepository
import lt.vitalijus.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(
        email: String,
        username: String,
        password: String
    ): User {
        val user = userRepository.findByEmailOrUsername(
            email = email.trim(),
            username = username.trim()
        )

        if (user != null) {
            throw UserAlreadyExistsException()
        }

        val savedUser = userRepository.save(
            UserEntity(
                email = email.trim(),
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(rawPassword = password) ?: throw PasswordEncodingException()
            )
        ).toUser()

        return savedUser
    }
}
