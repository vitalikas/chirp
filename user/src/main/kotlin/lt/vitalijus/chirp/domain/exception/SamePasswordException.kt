package lt.vitalijus.chirp.domain.exception

class SamePasswordException : RuntimeException("New password must be different from the current password.")
