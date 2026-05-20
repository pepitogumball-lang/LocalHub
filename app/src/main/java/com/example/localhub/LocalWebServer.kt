package com.example.localhub

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream

class LocalWebServer(
    private val context: Context,
    private val rootUri: Uri,
    port: Int
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        var uri = session.uri
        if (uri == "/") {
            uri = "/index.html"
        }

        // Remove leading slash for path matching
        val relativePath = uri.substring(1)
        val file = findFile(relativePath)

        return if (file != null && file.isFile) {
            try {
                val mimeType = MimeTypes.getMimeType(file.name ?: "")
                val inputStream = context.contentResolver.openInputStream(file.uri)
                if (inputStream != null) {
                    newChunkedResponse(Response.Status.OK, mimeType, inputStream)
                } else {
                    newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Error")
                }
            } catch (e: Exception) {
                newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.message)
            }
        } else {
            // Check if it's a directory and search for index.html inside
            if (file != null && file.isDirectory) {
                val indexFile = file.findFile("index.html")
                if (indexFile != null) {
                    return serveFile(indexFile)
                }
            }
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "<h1>404 Not Found</h1><p>Path: $uri</p>")
        }
    }

    private fun serveFile(file: DocumentFile): Response {
        return try {
            val mimeType = MimeTypes.getMimeType(file.name ?: "")
            val inputStream = context.contentResolver.openInputStream(file.uri)
            newChunkedResponse(Response.Status.OK, mimeType, inputStream)
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.message)
        }
    }

    private fun findFile(path: String): DocumentFile? {
        val root = DocumentFile.fromTreeUri(context, rootUri) ?: return null
        if (path.isEmpty()) return root

        val parts = path.split("/")
        var current: DocumentFile? = root
        for (part in parts) {
            if (part.isEmpty()) continue
            current = current?.findFile(part)
            if (current == null) break
        }
        return current
    }
}
