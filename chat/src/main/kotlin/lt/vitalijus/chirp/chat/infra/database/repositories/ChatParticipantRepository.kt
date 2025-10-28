package lt.vitalijus.chirp.chat.infra.database.repositories

import lt.vitalijus.chirp.chat.domain.models.ChatParticipant
import lt.vitalijus.chirp.chat.infra.database.entities.ChatParticipantEntity
import lt.vitalijus.chirp.domain.events.type.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatParticipantRepository : JpaRepository<ChatParticipantEntity, UserId> {

    fun findByUserIdIn(userIds: List<UserId>): Set<ChatParticipantEntity>

    @Query(
        """
        SELECT p
        FROM ChatParticipantEntity p
        WHERE LOWER(p.username) = :query OR LOWER(p.email) = :query
    """
    )
    fun findByEmailOrUsername(query: String): ChatParticipant?
}
