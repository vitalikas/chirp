package lt.vitalijus.chirp.api.mappers

import lt.vitalijus.chirp.api.dto.PictureUploadResponse
import lt.vitalijus.chirp.domain.models.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toResponse(): PictureUploadResponse {
    return PictureUploadResponse(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers,
        expiresAt = expiresAt
    )
}
