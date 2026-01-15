package lt.vitalijus.chirp.domain.model

import lt.vitalijus.chirp.domain.events.type.ChatId
import java.util.*

data class PushNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val recipients: List<DeviceToken>,
    val message: String,
    val chatId: ChatId,
    val data: Map<String, String>
)
