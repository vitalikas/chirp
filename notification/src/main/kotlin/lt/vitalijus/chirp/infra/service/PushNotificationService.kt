package lt.vitalijus.chirp.infra.service

import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.UserId
import lt.vitalijus.chirp.domain.exception.InvalidDeviceTokenException
import lt.vitalijus.chirp.domain.model.DeviceToken
import lt.vitalijus.chirp.domain.model.PushNotification
import lt.vitalijus.chirp.infra.database.DeviceTokenEntity
import lt.vitalijus.chirp.infra.database.DeviceTokenRepository
import lt.vitalijus.chirp.infra.mappers.toDeviceToken
import lt.vitalijus.chirp.infra.mappers.toPlatformEntity
import lt.vitalijus.chirp.infra.push_notification.FirebasePushNotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushNotificationService(
    private val firebasePushNotificationService: FirebasePushNotificationService,
    private val deviceTokenRepository: DeviceTokenRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun registerDevice(
        userId: UserId,
        token: String,
        platform: DeviceToken.Platform
    ): DeviceToken {
        val existing = deviceTokenRepository.findByToken(token = token)

        val trimmedToken = token.trim()
        if (existing == null && !firebasePushNotificationService.isValidToken(token = trimmedToken)) {
            throw InvalidDeviceTokenException()
        }

        val deviceTokenEntity = if (existing != null) {
            deviceTokenRepository.save(
                existing.apply {
                    this.userId = userId
                }
            )
        } else {
            deviceTokenRepository.save(
                DeviceTokenEntity(
                    userId = userId,
                    token = trimmedToken,
                    platform = platform.toPlatformEntity()
                )
            )
        }

        return deviceTokenEntity.toDeviceToken()
    }

    @Transactional
    fun unregisterDevice(token: String) {
        val deviceTokenEntity = deviceTokenRepository.findByToken(token = token.trim())
        deviceTokenEntity?.let {
            deviceTokenRepository.delete(it)
        }
    }

    fun sendNewMessageNotification(
        recipientUserIds: List<UserId>,
        senderUserId: UserId,
        senderUsername: String,
        message: String,
        chatId: ChatId
    ) {
        val deviceTokens = deviceTokenRepository.findByUserIdIn(userIds = recipientUserIds)
        if (deviceTokens.isEmpty()) {
            logger.info("No device tokens found for users: $recipientUserIds")
            return
        }

        val recipients = deviceTokens
            .filter { it.userId != senderUserId }
            .map { it.toDeviceToken() }

        val notification = PushNotification(
            title = "New message from $senderUsername",
            recipients = recipients,
            message = message,
            chatId = chatId,
            data = mapOf(
                "chatId" to chatId.toString(),
                "type" to "new_message"
            )
        )

        firebasePushNotificationService.sendNotification(
            notification = notification
        )
    }
}
