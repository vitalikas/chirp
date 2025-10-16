package lt.vitalijus.chirp.domain.events

import java.time.Instant

interface ChirpEvent {

    val eventId: String
    val eventKey: String
    val occurredAt: Instant
    val exchange: String
}