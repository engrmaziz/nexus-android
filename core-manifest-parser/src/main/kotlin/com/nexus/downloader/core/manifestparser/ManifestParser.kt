package com.nexus.downloader.core.manifestparser

interface ManifestParser {
    suspend fun parseHls(url: String): ParsedManifest
    suspend fun parseDash(url: String): ParsedManifest
}

data class ParsedManifest(
    val url: String,
    val type: ManifestType,
    val streams: List<StreamEntry> = emptyList()
)

data class StreamEntry(
    val url: String,
    val bandwidth: Long = 0,
    val resolution: String = ""
)

enum class ManifestType {
    HLS, DASH, UNKNOWN
}
