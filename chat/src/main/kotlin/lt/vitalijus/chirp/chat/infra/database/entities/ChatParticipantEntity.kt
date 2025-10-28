package lt.vitalijus.chirp.chat.infra.database.entities

import jakarta.persistence.*
import lt.vitalijus.chirp.domain.events.type.UserId
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "chat_participants",
    schema = "chat_service",
    indexes = [
        Index(
            name = "idx_chat_participants_username",
            columnList = "username"
        ),
        Index(
            name = "idx_chat_participants_email",
            columnList = "email"
        )
    ]
)
class ChatParticipantEntity(
    @Id
    var userId: UserId,

    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = true)
    var profilePictureUrl: String? = null,

    @CreationTimestamp
    var createdAt: Instant = Instant.now()
)
