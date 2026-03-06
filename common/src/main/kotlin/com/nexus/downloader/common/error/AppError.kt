package com.nexus.downloader.common.error

sealed class AppError(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable(message, cause)

sealed class NetworkError(message: String, cause: Throwable? = null) : AppError(message, cause) {
    class NoConnection(message: String = "No internet connection") : NetworkError(message)
    class Timeout(message: String = "Request timed out") : NetworkError(message)
    class ServerError(val code: Int, message: String = "Server error: $code") : NetworkError(message)
    class Unknown(message: String = "Unknown network error", cause: Throwable? = null) : NetworkError(message, cause)
}

sealed class DatabaseError(message: String, cause: Throwable? = null) : AppError(message, cause) {
    class ReadError(message: String = "Failed to read from database", cause: Throwable? = null) : DatabaseError(message, cause)
    class WriteError(message: String = "Failed to write to database", cause: Throwable? = null) : DatabaseError(message, cause)
    class NotFound(message: String = "Record not found") : DatabaseError(message)
}

sealed class DownloadError(message: String, cause: Throwable? = null) : AppError(message, cause) {
    class InvalidUrl(message: String = "Invalid download URL") : DownloadError(message)
    class InsufficientStorage(message: String = "Insufficient storage space") : DownloadError(message)
    class DownloadFailed(message: String = "Download failed", cause: Throwable? = null) : DownloadError(message, cause)
    class AlreadyExists(message: String = "File already exists") : DownloadError(message)
}
