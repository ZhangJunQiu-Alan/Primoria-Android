package au.edu.jcu.zhangfan.primoria.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import au.edu.jcu.zhangfan.primoria.domain.logic.PersonalDataRedactor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.Writer
import java.nio.ByteBuffer

internal data class StudyMaterial(val title: String, val excerpt: String, val limited: Boolean)

internal class StudyMaterialReadException(message: String) : IOException(message)

internal suspend fun readStudyMaterial(context: Context, uri: Uri): StudyMaterial = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val fileName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
        if (it.moveToFirst()) it.getString(0) else null
    }
    val extension = fileName.orEmpty().substringAfterLast('.', "").lowercase()
    val mime = resolver.getType(uri)
    val pdf = extension == "pdf" || mime == "application/pdf"
    if (!pdf && extension !in setOf("txt", "md", "markdown") &&
        mime !in setOf("text/plain", "text/markdown", "application/markdown", "application/x-markdown")
    ) {
        throw StudyMaterialReadException("Choose a PDF, TXT or Markdown file.")
    }
    val bytes = resolver.openInputStream(uri)?.use { input ->
        val content = ByteArrayOutputStream()
        val buffer = ByteArray(8_192)
        while (true) {
            ensureActive()
            val length = input.read(buffer)
            if (length < 0) break
            if (content.size() + length > MAX_FILE_BYTES) {
                throw StudyMaterialReadException("This file is over 8 MB. Choose a smaller file.")
            }
            content.write(buffer, 0, length)
        }
        content.toByteArray()
    } ?: throw StudyMaterialReadException("That file couldn't be opened. Choose it again.")

    val (raw, pagesLimited) = if (pdf) readPdf(context, bytes) else {
        val text = try {
            val charset = if (
                bytes.size >= 2 && ((bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) ||
                    (bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()))
            ) Charsets.UTF_16 else Charsets.UTF_8
            charset.newDecoder().decode(ByteBuffer.wrap(bytes)).toString().trimStart('\uFEFF')
        } catch (_: IOException) {
            throw StudyMaterialReadException("Save the text file as UTF-8 and try again.")
        }
        text to false
    }
    val excerpt = PersonalDataRedactor.redact(raw.take(MAX_MATERIAL_CHARS + 1)).take(MAX_MATERIAL_CHARS).trim()
    if (excerpt.none { it.isLetterOrDigit() }) {
        throw StudyMaterialReadException(
            if (pdf) "No readable text was found. Choose a text-based PDF, TXT or Markdown file."
            else "This file has no readable study text.",
        )
    }
    val title = fileName?.substringBeforeLast('.')?.take(80)?.trim().orEmpty().ifBlank { "Study material" }
    StudyMaterial(title, excerpt, pagesLimited || raw.length > MAX_MATERIAL_CHARS)
}

private fun readPdf(context: Context, bytes: ByteArray): Pair<String, Boolean> {
    PDFBoxResourceLoader.init(context.applicationContext)
    try {
        PDDocument.load(bytes).use { document ->
            if (document.isEncrypted && !document.currentAccessPermission.canExtractContent()) {
                throw StudyMaterialReadException("This PDF restricts text extraction. Choose another file.")
            }
            val characters = StringBuilder()
            val writer = object : Writer() {
                override fun write(chars: CharArray, start: Int, length: Int) {
                    val remaining = MAX_MATERIAL_CHARS + 1 - characters.length
                    characters.append(chars, start, minOf(length, remaining))
                    if (length > remaining) throw ExcerptComplete()
                }

                override fun flush() = Unit
                override fun close() = Unit
            }
            val stripper = PDFTextStripper().apply { endPage = MAX_PDF_PAGES }
            try {
                stripper.writeText(document, writer)
            } catch (_: ExcerptComplete) {
                return characters.toString() to true
            }
            return characters.toString() to (document.numberOfPages > MAX_PDF_PAGES)
        }
    } catch (_: InvalidPasswordException) {
        throw StudyMaterialReadException("This PDF needs a password. Choose an unlocked file.")
    }
}

private class ExcerptComplete : IOException()

private const val MAX_FILE_BYTES = 8 * 1024 * 1024
private const val MAX_PDF_PAGES = 12
