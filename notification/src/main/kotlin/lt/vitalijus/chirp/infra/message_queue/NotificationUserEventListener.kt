package lt.vitalijus.chirp.infra.message_queue

import jakarta.transaction.Transactional
import lt.vitalijus.chirp.domain.events.user.UserEvent
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class NotificationUserEventListener {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> {
                println("User created!")
            }

            is UserEvent.RequestResendVerification -> {
                println("Request resend verification!")
            }

            is UserEvent.RequestResetPassword -> {
                println("Request reset password!")
            }

            is UserEvent.Verified -> Unit
        }
    }
}
