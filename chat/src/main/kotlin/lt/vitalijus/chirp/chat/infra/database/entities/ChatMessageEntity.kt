package lt.vitalijus.chirp.chat.infra.database.entities

import jakarta.persistence.*
import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.ChatMessageId
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "chat_messages",
    schema = "chat_service",
    indexes = [
        Index(
            name = "idx_chat_messages_chat_id_created_at",
            columnList = "chat_id,created_at DESC"
        )
    ]
)
class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: ChatMessageId? = null,

    @Column(nullable = false)
    var content: String,

    @Column(
        name = "chat_id",
        nullable = false,
        insertable = false,
        updatable = false
    )
    var chatId: ChatId,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "chat_id",
        nullable = false,
        insertable = false,
        updatable = false
    )
    var chat: ChatEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "sender_id",
        nullable = false,
        insertable = false,
        updatable = false
    )
    var sender: ChatParticipantEntity? = null,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)
