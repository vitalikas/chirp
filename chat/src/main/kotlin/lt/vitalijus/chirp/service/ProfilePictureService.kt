package lt.vitalijus.chirp.service

import lt.vitalijus.chirp.domain.events.ProfilePictureUpdatedEvent
import lt.vitalijus.chirp.domain.events.type.UserId
import lt.vitalijus.chirp.domain.exception.ChatParticipantNotFoundException
import lt.vitalijus.chirp.domain.exception.InvalidProfilePictureException
import lt.vitalijus.chirp.domain.models.ProfilePictureUploadCredentials
import lt.vitalijus.chirp.infra.database.repositories.ChatParticipantRepository
import lt.vitalijus.chirp.infra.storage.SupabaseStorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfilePictureService(
    private val supabaseStorageService: SupabaseStorageService,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @param:Value("\${supabase.url}") private val supabaseUrl: String
) {

    private val logger = LoggerFactory.getLogger(ProfilePictureService::class.java)

    fun generateUploadCredentials(
        userId: UserId,
        mimeType: String
    ): ProfilePictureUploadCredentials {
        return supabaseStorageService.generateSignedUploadUrl(
            userId = userId,
            mimeType = mimeType
        )
    }

    @Transactional
    fun deleteProfilePicture(
        userId: UserId
    ) {
        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(id = userId)

        participant.profilePictureUrl?.let { url ->
            chatParticipantRepository.save(
                participant.apply { profilePictureUrl = null }
            )

            supabaseStorageService.deleteFile(url = url)

            applicationEventPublisher.publishEvent(
                ProfilePictureUpdatedEvent(
                    userId = userId,
                    newUrl = null
                )
            )
        }
    }

    @Transactional
    fun confirmProfilePictureUpload(
        userId: UserId,
        publicUrl: String
    ) {
        if (!publicUrl.startsWith(supabaseUrl)) throw InvalidProfilePictureException("Invalid URL")

        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(id = userId)

        val oldUrl = participant.profilePictureUrl

        chatParticipantRepository.save(
            participant.apply { profilePictureUrl = publicUrl }
        )

        try {
            oldUrl?.let {
                supabaseStorageService.deleteFile(url = it)
            }
        } catch (e: Exception) {
            logger.warn("Deleting profile picture failed for user $userId", e)
        }

        applicationEventPublisher.publishEvent(
            ProfilePictureUpdatedEvent(
                userId = userId,
                newUrl = publicUrl
            )
        )
    }
}
