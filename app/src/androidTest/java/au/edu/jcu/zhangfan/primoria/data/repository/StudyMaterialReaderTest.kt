package au.edu.jcu.zhangfan.primoria.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 29)
class StudyMaterialReaderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun readsTextAndMarkdownLocallyAndRemovesContactDetails() = runTest {
        for ((extension, mime) in listOf("txt" to "text/plain", "md" to "text/markdown")) {
            val file = putFile(extension, mime, "# Light energy\nContact: student@example.org.".toByteArray())
            try {
                val result = readStudyMaterial(context, file)
                assertTrue(result.excerpt.contains("# Light energy"))
                assertTrue(result.excerpt.contains("[removed]"))
                assertFalse(result.excerpt.contains("student@example.org"))
                assertFalse(result.limited)
            } finally {
                context.contentResolver.delete(file, null, null)
            }
        }
    }

    @Test
    fun extractsTextFromPdfAndRejectsAnEmptyPage() = runTest {
        val readable = putFile("pdf", "application/pdf", pdf("Plants absorb light."))
        val empty = putFile("pdf", "application/pdf", pdf(null))
        try {
            assertEquals("Plants absorb light.", readStudyMaterial(context, readable).excerpt)
            val error = runCatching { readStudyMaterial(context, empty) }.exceptionOrNull()
            assertTrue(error is StudyMaterialReadException)
            assertTrue(error?.message.orEmpty().contains("No readable text"))
        } finally {
            context.contentResolver.delete(readable, null, null)
            context.contentResolver.delete(empty, null, null)
        }
    }

    private fun putFile(extension: String, mime: String, bytes: ByteArray): Uri {
        val resolver = context.contentResolver
        val uri = checkNotNull(
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "Primoria test ${UUID.randomUUID()}.$extension")
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
            }),
        )
        checkNotNull(resolver.openOutputStream(uri)).use { it.write(bytes) }
        return uri
    }

    private fun pdf(text: String?): ByteArray {
        val document = PdfDocument()
        try {
            val page = document.startPage(PdfDocument.PageInfo.Builder(320, 400, 1).create())
            text?.let { page.canvas.drawText(it, 25f, 40f, Paint().apply { textSize = 16f }) }
            document.finishPage(page)
            return ByteArrayOutputStream().use { output ->
                document.writeTo(output)
                output.toByteArray()
            }
        } finally {
            document.close()
        }
    }
}
