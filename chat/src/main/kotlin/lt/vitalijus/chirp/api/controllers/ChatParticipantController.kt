package lt.vitalijus.chirp.api.controllers

import jakarta.validation.Valid
import lt.vitalijus.chirp.api.dto.ChatParticipantDto
import lt.vitalijus.chirp.api.dto.ConfirmProfilePictureRequest
import lt.vitalijus.chirp.api.dto.PictureUploadResponse
import lt.vitalijus.chirp.api.mappers.toChatParticipantDto
import lt.vitalijus.chirp.api.mappers.toResponse
import lt.vitalijus.chirp.api.util.requestUserId
import lt.vitalijus.chirp.service.ChatParticipantService
import lt.vitalijus.chirp.service.ProfilePictureService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/participants")
class ChatParticipantController(
    private val chatParticipantService: ChatParticipantService,
    private val profilePictureService: ProfilePictureService
) {

    @GetMapping
    fun getChatParticipantByUsernameOrEmail(
        @RequestParam(required = false) query: String?,
    ): ChatParticipantDto {
        val participant = if (query == null) {
            chatParticipantService.findChatParticipantById(requestUserId)
        } else {
            chatParticipantService.findChatParticipantByEmailOrUsername(query = query)
        }
        return participant?.toChatParticipantDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/profile-picture-upload")
    fun getProfilePictureUploadUrl(
        @RequestParam mimeType: String
    ): PictureUploadResponse {
        return profilePictureService.generateUploadCredentials(
            userId = requestUserId,
            mimeType = mimeType
        ).toResponse()
    }

    @PostMapping("/confirm-profile-picture")
    fun confirmProfilePictureUpload(
        @Valid @RequestBody body: ConfirmProfilePictureRequest
    ) {
        return profilePictureService.confirmProfilePictureUpload(
            userId = requestUserId,
            publicUrl = body.publicUrl
        )
    }

    @DeleteMapping("/profile-picture")
    fun deleteProfilePicture() {
        profilePictureService.deleteProfilePicture(
            userId = requestUserId
        )
    }
}
