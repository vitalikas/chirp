package lt.vitalijus.chirp.api.controllers

import jakarta.validation.Valid
import lt.vitalijus.chirp.api.dto.DeviceTokenDto
import lt.vitalijus.chirp.api.dto.RegisterDeviceRequest
import lt.vitalijus.chirp.api.mappers.toDeviceTokenDto
import lt.vitalijus.chirp.api.mappers.toPlatform
import lt.vitalijus.chirp.api.util.requestUserId
import lt.vitalijus.chirp.infra.service.PushNotificationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notification")
class DeviceTokenController(
    private val pushNotificationService: PushNotificationService
) {

    @PostMapping("/register")
    fun registerDeviceToken(
        @Valid @RequestBody body: RegisterDeviceRequest
    ): DeviceTokenDto {
        return pushNotificationService.registerDevice(
            userId = requestUserId,
            token = body.token,
            platform = body.platformDto.toPlatform()
        ).toDeviceTokenDto()
    }

    @DeleteMapping("/{token}")
    fun unregisterDeviceToken(
        @PathVariable("token") token: String
    ) {
        pushNotificationService.unregisterDevice(token = token)
    }
}
