package com.redro.pdfconverter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

data class VisualPageImage(
    val pageNumber: Int,
    val widthPixels: Int,
    val heightPixels: Int,
    val widthPoints: Float,
    val heightPoints: Float,
    val png: ByteArray
)

class VisualPageRenderer(context: Context) {
    private val appContext = context.applicationContext

    fun render(uri: Uri, targetWidth: Int = 1800): List<VisualPageImage> {
        require(targetWidth in 600..3000)
        val source = File.createTempFile("redro-render-", ".pdf", appContext.cacheDir)
        try {
            val input = appContext.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Unable to read selected PDF")
            input.use { stream ->
                FileOutputStream(source).use { output -> stream.copyTo(output) }
            }
            val descriptor = ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY)
            descriptor.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val pages = ArrayList<VisualPageImage>(renderer.pageCount)
                    for (index in 0 until renderer.pageCount) {
                        renderer.openPage(index).use { page ->
                            val scale = targetWidth.toFloat() / page.width
                            val width = targetWidth
                            val height = (page.height * scale).toInt().coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, Matrix().apply { setScale(scale, scale) }, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            val bytes = ByteArrayOutputStream().use { output ->
                                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                                output.toByteArray()
                            }
                            bitmap.recycle()
                            pages += VisualPageImage(
                                pageNumber = index + 1,
                                widthPixels = width,
                                heightPixels = height,
                                widthPoints = page.width.toFloat(),
                                heightPoints = page.height.toFloat(),
                                png = bytes
                            )
                        }
                    }
                    return pages
                }
            }
        } finally {
            source.delete()
        }
    }
}
