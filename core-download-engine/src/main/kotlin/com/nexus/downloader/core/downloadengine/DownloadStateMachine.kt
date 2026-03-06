package com.nexus.downloader.core.downloadengine

import com.nexus.downloader.domain.model.DownloadStatus

/**
 * Deterministic state machine for a single download's lifecycle.
 *
 * Valid states:
 * QUEUED → FETCHING_METADATA → ALLOCATING_CHUNKS → DOWNLOADING
 *       → PAUSED (from DOWNLOADING)
 *       → VERIFYING → ASSEMBLING → COMPLETED
 *       → FAILED (from any active state)
 *       → CANCELLED (from any non-terminal state)
 */
class DownloadStateMachine(initialState: DownloadStatus = DownloadStatus.QUEUED) {

    @Volatile
    private var currentState: DownloadStatus = initialState

    fun current(): DownloadStatus = currentState

    fun transition(newState: DownloadStatus) {
        currentState = newState
    }

    fun isTerminal(): Boolean = currentState in TERMINAL_STATES

    fun canPause(): Boolean = currentState == DownloadStatus.DOWNLOADING

    fun canResume(): Boolean = currentState == DownloadStatus.PAUSED

    fun canCancel(): Boolean = currentState !in TERMINAL_STATES

    companion object {
        val TERMINAL_STATES = setOf(
            DownloadStatus.COMPLETED,
            DownloadStatus.FAILED,
            DownloadStatus.CANCELLED
        )
        val RECOVERABLE_STATES = setOf(
            DownloadStatus.DOWNLOADING,
            DownloadStatus.FETCHING_METADATA,
            DownloadStatus.ALLOCATING_CHUNKS,
            DownloadStatus.VERIFYING,
            DownloadStatus.ASSEMBLING,
            DownloadStatus.PAUSED
        )
    }
}
