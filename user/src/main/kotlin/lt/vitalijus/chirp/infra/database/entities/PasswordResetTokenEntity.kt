package lt.vitalijus.chirp.infra.database.entities

import jakarta.persistence.*
import lt.vitalijus.chirp.infra.security.TokenGenerator
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "password_reset_tokens",
    schema = "user_service",
    indexes = [
        Index(name = "idx_password_reset_token_token", columnList = "token")
    ]
)
class PasswordResetTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false, unique = true)
    var token: String = TokenGenerator.generateSecureToken(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @Column(nullable = false)
    var expiresAt: Instant,
    @Column
    var usedAt: Instant? = null,
    @CreationTimestamp
    var createdAt: Instant = Instant.now()
) {

    val isUsed: Boolean
        get() = usedAt != null

    val isExpired: Boolean
        get() = expiresAt.isBefore(Instant.now())
}
