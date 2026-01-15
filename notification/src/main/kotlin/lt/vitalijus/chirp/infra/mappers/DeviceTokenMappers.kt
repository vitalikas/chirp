package lt.vitalijus.chirp.infra.mappers

import lt.vitalijus.chirp.domain.model.DeviceToken
import lt.vitalijus.chirp.infra.database.DeviceTokenEntity
import lt.vitalijus.chirp.infra.database.PlatformEntity

fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        id = this.id,
        userId = this.userId,
        token = this.token,
        platform = this.platform.toPlatform(),
        createdAt = this.createdAt
    )
}

fun DeviceToken.toEntity(): DeviceTokenEntity {
    return DeviceTokenEntity(
        id = this.id,
        userId = this.userId,
        token = this.token,
        platform = this.platform.toPlatformEntity(),
        createdAt = this.createdAt
    )
}

fun DeviceToken.Platform.toPlatformEntity(): PlatformEntity {
    return when (this) {
        DeviceToken.Platform.ANDROID -> PlatformEntity.ANDROID
        DeviceToken.Platform.IOS -> PlatformEntity.IOS
    }
}

fun PlatformEntity.toPlatform(): DeviceToken.Platform {
    return when (this) {
        PlatformEntity.ANDROID -> DeviceToken.Platform.ANDROID
        PlatformEntity.IOS -> DeviceToken.Platform.IOS
    }
}
