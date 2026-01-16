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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap

@Service
class PushNotificationService(
    private val firebasePushNotificationService: FirebasePushNotificationService,
    private val deviceTokenRepository: DeviceTokenRepository
) {

    companion object {
        private val RETRY_DELAYS_SECONDS = listOf(
            30L,
            60L,
            120L,
            300L,
            600L
        )
        const val MAX_RETRY_AGE_MINUTES = 30L
    }

    private val retryQueue = ConcurrentSkipListMap<Long, MutableList<RetryData>>()

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun registerDevice(
        userId: UserId,
        token: String,
        platform: DeviceToken.Platform
    ): DeviceToken {
        val trimmedToken = token.trim()

        val existing = deviceTokenRepository.findByToken(token = trimmedToken)

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
        val trimmedToken = token.trim()

        val deviceTokenEntity = deviceTokenRepository.findByToken(token = trimmedToken)
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

        sendWithRetry(pushNotification = notification)
    }

    fun sendWithRetry(
        pushNotification: PushNotification,
        attempt: Int = 0
    ) {
        val result = firebasePushNotificationService.sendNotification(
            notification = pushNotification
        )

        result.permanentFailures.forEach { deviceToken ->
            deviceTokenRepository.deleteByToken(token = deviceToken.token)
        }

        if (result.temporaryFailures.isNotEmpty() && attempt < RETRY_DELAYS_SECONDS.size) {
            val retryNotification = pushNotification.copy(
                recipients = result.temporaryFailures
            )
            scheduleRetry(retryNotification, attempt + 1)
        }

        if (result.succeeded.isNotEmpty()) {
            logger.info("Successfully sent notification to ${result.succeeded.size} devices")
        }
    }

    private fun scheduleRetry(
        pushNotification: PushNotification,
        attempt: Int
    ) {
        val delay = RETRY_DELAYS_SECONDS.getOrElse(attempt - 1) {
            RETRY_DELAYS_SECONDS.last()
        }
        val executeAt = Instant.now().plusSeconds(delay)
        val executeAtMillis = executeAt.toEpochMilli()

        val retryData = RetryData(
            pushNotification = pushNotification,
            attempt = attempt,
            createdAt = Instant.now()
        )

        retryQueue.compute(executeAtMillis) { _, retries ->
            (retries ?: mutableListOf()).apply { add(retryData) }
        }

        logger.info("Scheduled retry $attempt for notification ${pushNotification.id} in $delay seconds")
    }

    @Scheduled(fixedDelay = 15_000L)
    fun processRetries() {
        val now = Instant.now()
        val nowMillis = now.toEpochMilli()

        val toProcess = retryQueue.headMap(nowMillis, true)

        if (toProcess.isEmpty()) {
            return
        }

        val entries = toProcess.entries.toList()
        entries.forEach { (timeMillis, retries) ->
            retryQueue.remove(timeMillis)

            retries.forEach { retry ->
                try {
                    val age = Duration.between(retry.createdAt, now)
                    if (age.toMinutes() > MAX_RETRY_AGE_MINUTES) {
                        logger.info("Dropping retry for notification ${retry.pushNotification.id} as it is too old")
                        return@forEach
                    }

                    sendWithRetry(
                        pushNotification = retry.pushNotification,
                        attempt = retry.attempt
                    )
                } catch (e: Exception) {
                    logger.warn("Error processing retry for notification ${retry.pushNotification.id}", e)
                }
            }
        }
    }

    private data class RetryData(
        val pushNotification: PushNotification,
        val attempt: Int,
        val createdAt: Instant
    )
}
