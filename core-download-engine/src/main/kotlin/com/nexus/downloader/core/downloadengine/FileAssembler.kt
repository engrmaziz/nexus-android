package com.nexus.downloader.core.downloadengine

import timber.log.Timber
import java.io.File

/**
 * Handles the final stage of a download: integrity verification and
 * renaming the temporary file to its permanent name.
 *
 * Temporary files use the [TEMP_EXTENSION] suffix while downloading.
 * On successful assembly the file is renamed to remove the suffix.
 */
object FileAssembler {

    const val TEMP_EXTENSION = ".nexus_tmp"
    private const val TAG = "FileAssembler"

    /** Returns the temporary [File] for an in-progress download. */
    fun getTempFile(destinationDir: String, fileName: String): File =
        File(destinationDir, "$fileName$TEMP_EXTENSION")

    /** Returns the final destination [File] for a completed download. */
    fun getFinalFile(destinationDir: String, fileName: String): File =
        File(destinationDir, fileName)

    /**
     * Verifies that [file] exists and its size matches [expectedSize].
     * Returns `true` on match, `false` otherwise.
     */
    fun verifyIntegrity(file: File, expectedSize: Long): Boolean {
        if (!file.exists()) {
            Timber.tag(TAG).w("File not found: ${file.absolutePath}")
            return false
        }
        val actualSize = file.length()
        val match = actualSize == expectedSize
        if (!match) {
            Timber.tag(TAG).w("Size mismatch: expected $expectedSize, got $actualSize")
        }
        return match
    }

    /**
     * Renames [tempFile] to [finalFile].
     * Returns `true` on success, `false` if the rename fails.
     */
    fun assemble(tempFile: File, finalFile: File): Boolean {
        if (!tempFile.exists()) {
            Timber.tag(TAG).e("Temp file missing: ${tempFile.absolutePath}")
            return false
        }
        // Remove any previous partial final file
        if (finalFile.exists()) finalFile.delete()

        val success = tempFile.renameTo(finalFile)
        if (!success) {
            Timber.tag(TAG).e("Failed to rename ${tempFile.name} → ${finalFile.name}")
        }
        return success
    }

    /** Deletes [file] if it exists; silently ignores errors. */
    fun deleteIfExists(file: File) {
        if (file.exists()) file.delete()
    }
}
