package lt.vitalijus.chirp.infra.database.entities

import jakarta.persistence.*
import lt.vitalijus.chirp.infra.security.EmailTokenGenerator
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "email_verification_tokens",
    schema = "user_service"
)
class EmailVerificationTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false, unique = true)
    var token: String = EmailTokenGenerator.generateSecureToken(),
    @Column(nullable = false)
    var expiresAt: Instant,
    @Column
    var usedAt: Instant? = null,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity
) {

    val isUsed: Boolean
        get() = usedAt != null

    val isExpired: Boolean
        get() = expiresAt.isBefore(Instant.now())
}