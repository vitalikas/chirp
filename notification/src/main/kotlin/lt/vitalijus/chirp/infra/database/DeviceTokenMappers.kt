package lt.vitalijus.chirp.infra.database

import lt.vitalijus.chirp.domain.model.DeviceToken

fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        id = this.id,
        userId = this.userId,
        token = this.token,
        platform = when (this.platform) {
            PlatformEntity.ANDROID -> DeviceToken.Platform.ANDROID
            PlatformEntity.IOS -> DeviceToken.Platform.IOS
        },
        createdAt = this.createdAt
    )
}

fun DeviceToken.toEntity(): DeviceTokenEntity {
    return DeviceTokenEntity(
        id = this.id,
        userId = this.userId,
        token = this.token,
        platform = when (this.platform) {
            DeviceToken.Platform.ANDROID -> PlatformEntity.ANDROID
            DeviceToken.Platform.IOS -> PlatformEntity.IOS
        },
        createdAt = this.createdAt
    )
}
