package lt.vitalijus.chirp.domain.events

interface EventPublisher {

    fun <T : ChirpEvent> publish(event: T)
}
