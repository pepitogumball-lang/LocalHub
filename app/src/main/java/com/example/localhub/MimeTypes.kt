package com.example.localhub

object MimeTypes {
    private val MAP = mapOf(
        "html" to "text/html",
        "htm" to "text/html",
        "css" to "text/css",
        "js" to "application/javascript",
        "json" to "application/json",
        "png" to "image/png",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "gif" to "image/gif",
        "svg" to "image/svg+xml",
        "webp" to "image/webp",
        "mp3" to "audio/mpeg",
        "mp4" to "video/mp4",
        "webm" to "video/webm",
        "wav" to "audio/wav",
        "wasm" to "application/wasm",
        "txt" to "text/plain",
        "md" to "text/markdown",
        "pdf" to "application/pdf",
        "ico" to "image/x-icon",
        "ttf" to "font/ttf",
        "otf" to "font/otf",
        "woff" to "font/woff",
        "woff2" to "font/woff2",
        "xml" to "application/xml",
        "zip" to "application/zip"
    )

    fun getMimeType(filename: String): String {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return MAP[ext] ?: "application/octet-stream"
    }
}
