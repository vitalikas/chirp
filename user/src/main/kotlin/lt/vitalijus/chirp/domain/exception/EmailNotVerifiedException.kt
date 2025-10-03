package lt.vitalijus.chirp.domain.exception

class EmailNotVerifiedException : RuntimeException(
    "Email is not verified. Please check your email for verification instructions."
)
