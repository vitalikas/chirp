package lt.vitalijus.chirp.infra.database.entities

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.ChatMessageId
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
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
    @Size(max = 5000)
    var content: String,

    @Column(
        name = "chat_id",
        nullable = false,
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    var chat: ChatEntity? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "sender_id",
        nullable = false
    )
    var sender: ChatParticipantEntity,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)
