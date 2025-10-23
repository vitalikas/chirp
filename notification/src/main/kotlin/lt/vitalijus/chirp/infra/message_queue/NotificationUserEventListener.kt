package lt.vitalijus.chirp.infra.message_queue

import lt.vitalijus.chirp.domain.events.user.UserEvent
import lt.vitalijus.chirp.infra.service.EmailService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class NotificationUserEventListener(
    private val emailService: EmailService
) {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> {
                println("User created!")
                emailService.sendVerificationEmail(
                    to = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.verificationToken
                )
            }

            is UserEvent.RequestResendVerification -> {
                println("Request resend verification!")
                emailService.sendVerificationEmail(
                    to = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.verificationToken
                )
            }

            is UserEvent.RequestResetPassword -> {
                println("Request reset password!")
                emailService.sendPasswordResetEmail(
                    to = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.passwordResetToken,
                    expiresIn = Duration.of(event.expiresInMinutes, ChronoUnit.MINUTES)
                )
            }

            is UserEvent.Verified -> Unit
        }
    }
}
