package lt.vitalijus.chirp.api.mappers

import lt.vitalijus.chirp.api.dto.AuthenticatedUserDto
import lt.vitalijus.chirp.api.dto.UserDto
import lt.vitalijus.chirp.domain.model.AuthenticatedUser
import lt.vitalijus.chirp.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto = AuthenticatedUserDto(
    user = this.user.toUserDto(),
    accessToken = this.accessToken,
    refreshToken = this.refreshToken

)

fun User.toUserDto(): UserDto = UserDto(
    id = this.id,
    email = this.email,
    username = this.username,
    hasVerifiedEmail = this.hasEmailVerified
)
