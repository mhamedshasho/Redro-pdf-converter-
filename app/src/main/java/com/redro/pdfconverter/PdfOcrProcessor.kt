package com.redro.pdfconverter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream

class PdfOcrProcessor(context: Context) {
    private val appContext = context.applicationContext

    fun recognizeScannedPages(uri: Uri, analysis: PdfAnalysisResult): PdfAnalysisResult {
        require(OcrLanguageModelManager.isReady(appContext)) { "OCR language data is not installed" }
        val source = File.createTempFile("redro-ocr-", ".pdf", appContext.cacheDir)
        try {
            val input = appContext.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Unable to read selected PDF")
            input.use { stream ->
                FileOutputStream(source).use { output -> stream.copyTo(output) }
            }
            val descriptor = ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY)
            descriptor.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val api = TessBaseAPI()
                    check(api.init(OcrLanguageModelManager.dataPath(appContext), "ara+eng")) {
                        "Unable to initialize Arabic and English OCR"
                    }
                    try {
                        val updatedPages = analysis.pages.mapIndexed { index, pageAnalysis ->
                            if (pageAnalysis.text.isNotBlank()) {
                                pageAnalysis
                            } else {
                                renderer.openPage(index).use { page ->
                                    val targetWidth = 1600
                                    val scale = targetWidth.toFloat() / page.width
                                    val width = targetWidth
                                    val height = (page.height * scale).toInt().coerceAtLeast(1)
                                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                    try {
                                        bitmap.eraseColor(Color.WHITE)
                                        page.render(
                                            bitmap,
                                            null,
                                            Matrix().apply { setScale(scale, scale) },
                                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                        )
                                        api.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO)
                                        api.setImage(bitmap)
                                        val recognized = api.getUTF8Text().orEmpty().trim()
                                        api.clear()
                                        if (recognized.isBlank()) pageAnalysis
                                        else pageAnalysis.copy(
                                            text = recognized,
                                            contentType = PdfPageContentType.TEXT_BEARING
                                        )
                                    } finally {
                                        bitmap.recycle()
                                    }
                                }
                            }
                        }
                        return analysis.copy(pages = updatedPages)
                    } finally {
                        api.recycle()
                    }
                }
            }
        } finally {
            source.delete()
        }
    }
}
